package org.quebee.com.panel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.AggregateElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;

@Getter
public class GroupingPanel extends AvailableFieldsTree {
    private static final  String SUM = "sum";
    private static final  String[] FUNCTIONS = {SUM, "count", "max", "min"};
    private static final String AGGREGATE_TABLE = "aggregateTable";
    private static final String GROUPING_TABLE = "groupingTable";

    private final String header = "Grouping";
    private final JBSplitter component = new JBSplitter();

    public GroupingPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getAvailableGroupingFieldsTree());
        component.setSecondComponent(getGroupingAggregatesPanel());
        enableDragAndDrop();
    }

    @Override
    protected void enableDragAndDrop() {
        super.enableDragAndDrop();
        installDnDSupportToTable(groupingTable);
        installDnDSupportToTable(aggregateTable);
    }

    @Override
    public void initListeners() {
        subscribe(SaveQueryDataNotifier.class, this::saveQueryData);
        subscribe(LoadQueryDataNotifier.class, this::loadQueryData);
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SelectedFieldRemoveNotifier.class, this::removeSelectedField);
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTableToAvailable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void removeSelectedField(TableElement tableElement) {
        availableTreeRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(tableElement.getDescription()))
                .forEach(x -> {
                    var index = availableTreeRoot.getIndex(x);
                    availableTreeRoot.remove(x);
                    availableModel.nodesWereRemoved(availableTreeRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void removeSelectedTable(QBTreeNode node) {
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, availableModel);
        availableTreeRoot.nodeToList().stream()
                .filter(x -> {
                    var tableName = x.getUserObject().getTableName();
                    return Objects.nonNull(tableName) && tableName.equals(removeTableName);
                })
                .forEach(x -> {
                    var index = availableTreeRoot.getIndex(x);
                    availableTreeRoot.remove(x);
                    availableModel.nodesWereRemoved(availableTreeRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(groupingTableModel, union.getGroupingTableModel());
        ComponentUtils.loadTableToTable(aggregateTableModel, union.getAggregateTableModel());
        for (var i = availableTreeRoot.getChildCount() - 2; i >= 0; i--) {
            availableTreeRoot.remove(i);
        }
        ComponentUtils.clearTree(allFieldsRoot);
        availableModel.reload();
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
        availableTreeRoot.insert(new QBTreeNode(tableElement), availableTreeRoot.getChildCount() - 1);
        availableModel.nodesWereInserted(availableTreeRoot, new int[]{availableTreeRoot.getChildCount() - 2});
    }

    private JComponent getGroupingAggregatesPanel() {
        var splitter = new JBSplitter();
        splitter.setFirstComponent(getGroupingTable());
        splitter.setSecondComponent(getAggregateTable());
        splitter.setOrientation(true);
        return splitter;
    }

    private ListTableModel<AggregateElement> aggregateTableModel;
    private TableView<AggregateElement> aggregateTable;

    private JComponent getAggregateTable() {
        var columnInfoAggregate = new ColumnInfo<AggregateElement, String>("Aggregate Field") {

            @Override
            public @Nullable String valueOf(AggregateElement o) {
                return Objects.isNull(o) ? "" : o.getField();
            }
        };
        var columnInfoFunction = new EditableStringColumn<>("Function",
                AggregateElement::getFunction, AggregateElement::setFunction) {

            @Override
            public TableCellEditor getEditor(AggregateElement item) {
                return new DefaultCellEditor(new ComboBox<>(FUNCTIONS));
            }
        };
        aggregateTableModel = new ListTableModel<>(
                columnInfoAggregate,
                columnInfoFunction
        );

        aggregateTable = new TableView<>(aggregateTableModel);
        aggregateTable.setName(AGGREGATE_TABLE);
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
                moveFieldToAvailable(groupingTable.getSelectedObject(), true, groupingTableModel, groupingTable);
            }
        });
        groupingTable.setName(GROUPING_TABLE);
        var decorator = ToolbarDecorator.createDecorator(groupingTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    @Override
    protected void dndMoveFieldToAvailable(String sourceName) {
        if (GROUPING_TABLE.equals(sourceName)) {
            moveFieldToAvailable(groupingTable.getSelectedObject(), true, groupingTableModel, groupingTable);
        } else if (AGGREGATE_TABLE.equals(sourceName)) {
            moveFieldToAvailable(aggregateTable.getSelectedObject(), true, aggregateTableModel, aggregateTable);
        }
    }

    protected void transferFromTableToTable(String sourceName, int index) {
        if (GROUPING_TABLE.equals(sourceName)) {
            var selectedObject = groupingTable.getSelectedObject();
            if (Objects.isNull(selectedObject)) {
                return;
            }
            var newItem = new AggregateElement();
            newItem.setField(selectedObject.getName());
            newItem.setFunction(SUM);
            addElementToTable(newItem, index, aggregateTableModel, aggregateTable);
            groupingTableModel.removeRow(groupingTableModel.indexOf(selectedObject));
        } else if (AGGREGATE_TABLE.equals(sourceName)) {
            var selectedObject = aggregateTable.getSelectedObject();
            if (Objects.isNull(selectedObject)) {
                return;
            }
            var newItem = new TableElement(selectedObject.getField());
            addElementToTable(newItem, index, groupingTableModel, groupingTable);
            aggregateTableModel.removeRow(aggregateTableModel.indexOf(selectedObject));
        }
    }

    private <T> void moveFieldToAvailable(T selected, boolean removeSource, ListTableModel<T> model, TableView<T> table) {
        var index = model.indexOf(selected);
        TableElement item = null;
        if (selected instanceof TableElement) {
            item = new TableElement((TableElement) selected);
        } else if (selected instanceof AggregateElement) {
            item = new TableElement(((AggregateElement) selected).getField());
        }
        addSelectedField(item, true);
        removeFromTable(index, removeSource, model, table);
    }

    private JComponent getAvailableGroupingFieldsTree() {
        var decorator = ToolbarDecorator.createDecorator(getAvailableTree(true));
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
        //  comp.setBorder(new LineBorder(JBColor.RED, 1));
        comp.setPreferredSize(new Dimension(30, 300));
        comp.add(Box.createVerticalStrut(10));
        comp.add(smallButton(">", e -> moveFieldToSelected(ComponentUtils.selectedAvailableField(availableTree), -1)));
        comp.add(smallButton(">>", e -> availableTreeRoot.nodeToList().forEach(x -> moveFieldToSelected(x, -1))));
        comp.add(smallButton("<", e -> moveFieldToAvailable(groupingTable.getSelectedObject(), true, groupingTableModel, groupingTable)));
        comp.add(smallButton("<<", e -> {
            groupingTable.getItems().forEach(x -> moveFieldToAvailable(x, false, groupingTableModel, groupingTable));
            ComponentUtils.clearTable(groupingTableModel);
        }));

        comp.add(Box.createVerticalStrut(30));

        comp.add(smallButton(">", e -> moveFieldToAggregate(ComponentUtils.selectedAvailableField(availableTree), -1)));
        comp.add(smallButton(">>", e -> availableTreeRoot.nodeToList().forEach(x -> moveFieldToAggregate(x, -1))));
        comp.add(smallButton("<", e -> moveFieldToAvailable(aggregateTable.getSelectedObject(), true, aggregateTableModel, aggregateTable)));
        comp.add(smallButton("<<", e -> {
            aggregateTable.getItems().forEach(x -> moveFieldToAvailable(x, false, aggregateTableModel, aggregateTable));
            ComponentUtils.clearTable(aggregateTableModel);
        }));
        comp.add(Box.createVerticalStrut(10));

        hBox.add(comp);

        return hBox;
    }

    @Override
    protected void moveFieldToSelected(QBTreeNode item, int index) {
        moveFieldToSelected(item, index, GROUPING_TABLE);
    }

    @Override
    protected void moveFieldToSelected(QBTreeNode item, int index, String componentName) {
        if (componentName.equals(GROUPING_TABLE)) {
            var newItem = new TableElement(getFieldDescription(item));
            moveFieldToTable(index, item, newItem, groupingTableModel, groupingTable);
        } else if (componentName.equals(AGGREGATE_TABLE)) {
            moveFieldToAggregate(item, index);
        }
    }

    private void moveFieldToAggregate(QBTreeNode item, int index) {
        var newItem = new AggregateElement();
        newItem.setField(getFieldDescription(item));
        newItem.setFunction(SUM);
        moveFieldToTable(index, item, newItem, aggregateTableModel, aggregateTable);
    }

    private String getFieldDescription(QBTreeNode value) {
        if (availableTreeRoot.equals(value.getParent())) {
            return value.getUserObject().getDescription();
        }
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        return tableObject.getName() + "." + columnObject.getName();
    }
}
