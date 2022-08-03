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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
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
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SelectedFieldRemoveNotifier.class, this::removeSelectedField);
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void removeSelectedField(TableElement tableElement) {
        groupingRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(tableElement.getDescription()))
                .forEach(x -> {
                    var index = groupingRoot.getIndex(x);
                    groupingRoot.remove(x);
                    groupingModel.nodesWereRemoved(groupingRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void removeSelectedTable(QBTreeNode node) {
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, groupingModel);
        groupingRoot.nodeToList().stream()
                .filter(x -> {
                    var tableName = x.getUserObject().getTableName();
                    return Objects.nonNull(tableName) && tableName.equals(removeTableName);
                })
                .forEach(x -> {
                    var index = groupingRoot.getIndex(x);
                    groupingRoot.remove(x);
                    groupingModel.nodesWereRemoved(groupingRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void saveQueryData(FullQuery fullQuery, String s, int id) {
        for (var i = groupingRoot.getChildCount() - 2; i >= 0; i--) {
            groupingRoot.remove(i);
        }
        ComponentUtils.clearTree(allFieldsRoot);
        groupingModel.reload();
    }

    private void addSelectedField(TableElement element, boolean interactive) {
        var tableElement = new TableElement(element);
        tableElement.setIcon(DatabaseIcons.Col);
        groupingRoot.insert(new QBTreeNode(tableElement), groupingRoot.getChildCount() - 1);
        groupingModel.nodesWereInserted(groupingRoot, new int[]{groupingRoot.getChildCount() - 2});
    }

    private void addSelectedTable(QBTreeNode node) {
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        groupingModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }

    private JComponent getGroupingAggregatesPanel() {
        var splitter = new JBSplitter();
        splitter.setFirstComponent(getGroupingTable());
        splitter.setSecondComponent(getAggregateTable());
        splitter.setOrientation(true);
        return splitter;
    }

    private ListTableModel<TableElement> aggregateTableModel;

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

        var aggregateTable = new TableView<>(aggregateTableModel);
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
        var decorator = ToolbarDecorator.createDecorator(groupingTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    private QBTreeNode groupingRoot;
    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel groupingModel;
    private TreeTable availableGroupTree;

    private JComponent getAvailableGroupingFieldsTree() {
        groupingRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsRoot = new QBTreeNode(new TableElement("All fields"));
        groupingRoot.add(allFieldsRoot);
        groupingModel = new ListTreeTableModel(groupingRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        availableGroupTree = new TreeTable(groupingModel);
        availableGroupTree.setTreeCellRenderer(new TableElement.Renderer());
        availableGroupTree.setRootVisible(false);
        availableGroupTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1) {
                    return;
                }
                moveFieldToSelected(ComponentUtils.selectedAvailableField(table));
            }
        });

        var decorator = ToolbarDecorator.createDecorator(availableGroupTree);
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
        //  comp.setBorder(new LineBorder(JBColor.RED, 1));
        comp.setPreferredSize(new Dimension(30, 300));
        comp.add(Box.createVerticalStrut(10));
        comp.add(smallButton(">"));
        comp.add(smallButton(">>"));
        comp.add(smallButton("<"));
        comp.add(smallButton("<<"));
        comp.add(Box.createVerticalStrut(30));
        comp.add(smallButton(">"));
        comp.add(smallButton(">>"));
        comp.add(smallButton("<"));
        comp.add(smallButton("<<"));
        comp.add(Box.createVerticalStrut(10));

        hBox.add(comp);

        return hBox;
    }

    private void moveFieldToSelected(QBTreeNode item) {
        if (Objects.isNull(item)) {
            return;
        }
        if (Objects.nonNull(allFieldsRoot) && (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent()))) {
            return;
        }
        addGroupElement(item, -1);
        ComponentUtils.removeFromAvailable(item, groupingRoot, groupingModel, availableGroupTree);
    }

    private void addGroupElement(QBTreeNode value, int newIndex) {
        var item = new TableElement(value.getUserObject());
        if (newIndex == -1) {
            groupingTableModel.addRow(item);
        } else {
            groupingTableModel.insertRow(newIndex, item);
        }
        groupingTable.setSelection(Collections.singleton(item));
    }

    private JButton smallButton(String text) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        return button;
    }
}
