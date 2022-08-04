package org.quebee.com.panel;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Objects;

@Getter
public class GroupingPanel extends QueryPanel {
    private final String header = "Grouping";
    private final JBSplitter component = new JBSplitter();

    public GroupingPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getAvailableGroupingFieldsTree());
        component.setSecondComponent(getGroupingAggregatesPanel());
    }

    @Override
    public void initListeners() {
        subscribe(SaveQueryDataNotifier.class, this::saveQueryData);
        subscribe(LoadQueryDataNotifier.class, this::loadQueryData);
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SelectedFieldRemoveNotifier.class, this::removeSelectedField);
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void removeSelectedField(TableElement tableElement) {
        availableGroupingRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(tableElement.getDescription()))
                .forEach(x -> {
                    var index = availableGroupingRoot.getIndex(x);
                    availableGroupingRoot.remove(x);
                    availableGroupingModel.nodesWereRemoved(availableGroupingRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void removeSelectedTable(QBTreeNode node) {
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, availableGroupingModel);
        availableGroupingRoot.nodeToList().stream()
                .filter(x -> {
                    var tableName = x.getUserObject().getTableName();
                    return Objects.nonNull(tableName) && tableName.equals(removeTableName);
                })
                .forEach(x -> {
                    var index = availableGroupingRoot.getIndex(x);
                    availableGroupingRoot.remove(x);
                    availableGroupingModel.nodesWereRemoved(availableGroupingRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(groupingTableModel, union.getGroupingTableModel());
        ComponentUtils.loadTableToTable(aggregateTableModel, union.getAggregateTableModel());
        for (var i = availableGroupingRoot.getChildCount() - 2; i >= 0; i--) {
            availableGroupingRoot.remove(i);
        }
        ComponentUtils.clearTree(allFieldsRoot);
        availableGroupingModel.reload();
        ComponentUtils.clearTable(groupingTableModel);
        ComponentUtils.clearTable(aggregateTableModel);
    }

    private void loadQueryData(FullQuery fullQuery, String cteName, int i) {
        var union = fullQuery.getCte(cteName).getUnion("" + i);
        ComponentUtils.loadTableToTable(union.getGroupingTableModel(), groupingTableModel);
        ComponentUtils.loadTableToTable(union.getAggregateTableModel(), aggregateTableModel);
    }

    private void addSelectedField(TableElement element, boolean interactive) {
        var tableElement = new TableElement(element);
        tableElement.setIcon(DatabaseIcons.Col);
        availableGroupingRoot.insert(new QBTreeNode(tableElement), availableGroupingRoot.getChildCount() - 1);
        availableGroupingModel.nodesWereInserted(availableGroupingRoot, new int[]{availableGroupingRoot.getChildCount() - 2});
    }

    private void addSelectedTable(QBTreeNode node) {
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        availableGroupingModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }

    private JComponent getGroupingAggregatesPanel() {
        var splitter = new JBSplitter();
        splitter.setFirstComponent(getGroupingTable());
        splitter.setSecondComponent(getAggregateTable());
        splitter.setOrientation(true);
        return splitter;
    }

    private ListTableModel<TableElement> aggregateTableModel;
    private TableView<TableElement> aggregateTable;

    private JComponent getAggregateTable() {
        var columnInfoAggregate = new ColumnInfo<TableElement, String>("Aggregate Field") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };
        var columnInfoFunction = new ColumnInfo<TableElement, String>("Function") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };
        aggregateTableModel = new ListTableModel<>(
                columnInfoAggregate,
                columnInfoFunction
        );

        aggregateTable = new TableView<>(aggregateTableModel);
        var decorator = ToolbarDecorator.createDecorator(aggregateTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    private ListTableModel<TableElement> groupingTableModel;
    private TableView<TableElement> groupingTable;

    private JComponent getGroupingTable() {
        var columnInfo = new ColumnInfo<TableElement, String>("Grouping Field") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return o.getDescription();
            }
        };

        groupingTableModel = new ListTableModel<>(
                columnInfo
        );
        groupingTable = new TableView<>(groupingTableModel);
        groupingTable.addMouseListener(new MouseAdapterDoubleClick() {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                moveFieldToAvailable(groupingTable.getSelectedObject(), true);
            }
        });
        var decorator = ToolbarDecorator.createDecorator(groupingTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    private void moveFieldToAvailable(TableElement selectedObject, boolean removeFromOrder) {
        var index = groupingTableModel.indexOf(selectedObject);
        var item = new TableElement(selectedObject);
        addSelectedField(item, true);
        if (removeFromOrder) {
            groupingTableModel.removeRow(index);
            if (groupingTableModel.getRowCount() > 0) {
                ComponentUtils.setSelectedRow(groupingTable, index == groupingTableModel.getRowCount() ? index - 1 : index);
            }
        }
    }

    private QBTreeNode availableGroupingRoot;
    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel availableGroupingModel;
    private TreeTable availableGroupingTree;

    private JComponent getAvailableGroupingFieldsTree() {
        availableGroupingRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsRoot = new QBTreeNode(new TableElement("All fields"));
        availableGroupingRoot.add(allFieldsRoot);
        availableGroupingModel = new ListTreeTableModel(availableGroupingRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        availableGroupingTree = new TreeTable(availableGroupingModel);
        availableGroupingTree.setTreeCellRenderer(new TableElement.Renderer());
        availableGroupingTree.setRootVisible(false);
        availableGroupingTree.addMouseListener(new MouseAdapterDoubleClick() {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                moveFieldToSelected(ComponentUtils.selectedAvailableField(availableGroupingTree));
            }
        });

        var decorator = ToolbarDecorator.createDecorator(availableGroupingTree);
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
        //  comp.setBorder(new LineBorder(JBColor.RED, 1));
        comp.setPreferredSize(new Dimension(30, 300));
        comp.add(Box.createVerticalStrut(10));
        comp.add(smallButton(">", e -> moveFieldToSelected(ComponentUtils.selectedAvailableField(availableGroupingTree))));
        comp.add(smallButton(">>", e -> availableGroupingRoot.nodeToList().forEach(this::moveFieldToSelected)));
        comp.add(smallButton("<", e -> moveFieldToAvailable(groupingTable.getSelectedObject(), true)));
        comp.add(smallButton("<<", e -> {
            groupingTable.getItems().forEach(x -> moveFieldToAvailable(x, false));
            ComponentUtils.clearTable(groupingTableModel);
        }));
        comp.add(Box.createVerticalStrut(30));
        comp.add(smallButton(">", e -> moveFieldToAggregate(ComponentUtils.selectedAvailableField(availableGroupingTree))));
        comp.add(smallButton(">>", null));
        comp.add(smallButton("<", null));
        comp.add(smallButton("<<", null));
        comp.add(Box.createVerticalStrut(10));

        hBox.add(comp);

        return hBox;
    }

    private void moveFieldToSelected(QBTreeNode item) {
        moveFieldToTable(item, groupingTableModel, groupingTable);
    }
    private void moveFieldToAggregate(QBTreeNode item){
        moveFieldToTable(item, aggregateTableModel, aggregateTable);
    }

    private void moveFieldToTable(QBTreeNode item, ListTableModel<TableElement> model, TableView<TableElement> table) {
        if (Objects.isNull(item)) {
            return;
        }
        if (Objects.nonNull(allFieldsRoot) && (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent()))) {
            return;
        }
        addGroupElement(item, -1, model, table);
        ComponentUtils.removeFromAvailable(item, availableGroupingRoot, availableGroupingModel, availableGroupingTree);
    }

    private void addGroupElement(QBTreeNode value, int newIndex,
                                 ListTableModel<TableElement> model,
                                 TableView<TableElement> table) {
        var item = new TableElement(getFieldDescription(value));
        if (newIndex == -1) {
            model.addRow(item);
        } else {
            model.insertRow(newIndex, item);
        }
        table.setSelection(Collections.singleton(item));
    }

    private String getFieldDescription(QBTreeNode value) {
        if (availableGroupingRoot.equals(value.getParent())) {
            return value.getUserObject().getDescription();
        }
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        return tableObject.getName() + "." + columnObject.getName();
    }

    private JButton smallButton(String text, ActionListener l) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        button.addActionListener(l);
        return button;
    }
}
