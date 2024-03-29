package org.quebee.com.panel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.OrderElement;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeNode;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

@Getter
public class OrderPanel extends AvailableFieldsTree {
    public static final String ORDER_PANEL_HEADER = "Order";
    public static final String ASC = "Ascending";
    public static final String DESC = "Descending";

    private static final String ALL_FIELDS = "All fields";
    private static final String ORDER_TABLE = "orderTable";

    private final String header = ORDER_PANEL_HEADER;
    private final JBSplitter component = new JBSplitter();

    public OrderPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTable());
        component.setSecondComponent(getOrderTable());
        enableDragAndDrop();
    }

    @Override
    protected void enableDragAndDrop() {
        super.enableDragAndDrop();
        installDnDSupportToTable(orderTable);
    }

    private JComponent getFieldsTable() {
        var decorator = ToolbarDecorator.createDecorator(getAvailableTree(false));
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
        comp.setPreferredSize(new Dimension(30, 400));

        comp.add(smallButton(">", e -> moveFieldToSelected(ComponentUtils.selectedAvailableField(availableTree), -1)));
        comp.add(smallButton(">>", e -> availableTreeRoot.nodeToList().forEach(x -> moveFieldToSelected(x, -1))));
        comp.add(smallButton("<", e -> moveFieldToAvailable(orderTable.getSelectedObject(), true)));
        comp.add(smallButton("<<", e -> {
            orderTable.getItems().forEach(x -> moveFieldToAvailable(x, false));
            ComponentUtils.clearTable(orderTableModel);
        }));

        hBox.add(comp);
        return hBox;
    }

    @Override
    protected void moveFieldToSelected(TreeNode value, int index) {
        moveFieldToSelected(value, index, ORDER_TABLE);
    }

    @Override
    protected void moveFieldToSelected(TreeNode value, int index, String componentName) {
        var newItem = new OrderElement();
        if (value != null) {
            newItem.setField(getDescription(value));
            newItem.setIcon(value.getUserObject().getIcon());
        }
        newItem.setSorting(ASC);
        moveFieldToTable(index, value, newItem, orderTableModel, orderTable);
    }

    private ListTableModel<OrderElement> orderTableModel;
    private TableView<OrderElement> orderTable;

    private JComponent getOrderTable() {
        var fieldInfo = new ColumnInfo<OrderElement, String>("Field") {
            @Override
            public String valueOf(OrderElement o) {
                return o.getField();
            }
            @Override
            public TableCellRenderer getRenderer(OrderElement tableElement) {
                return new TableElement.TableRenderer(tableElement);
            }
        };
        var sortingInfo = new EditableStringColumn<>("Sorting", OrderElement::getSorting, OrderElement::setSorting) {
            @Override
            public TableCellEditor getEditor(OrderElement item) {
                return new DefaultCellEditor(new ComboBox<>(new String[]{ASC, DESC}));
            }
        };
        orderTableModel = new ListTableModel<>(
                fieldInfo,
                sortingInfo
        );
        orderTable = new TableView<>(orderTableModel);
        orderTable.addMouseListener(new MouseAdapterDoubleClick() {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                var columnName = orderTable.getColumnName(table.getSelectedColumn());
                if (columnName.equals("Field")) {
                    moveFieldToAvailable(orderTable.getSelectedObject(), true);
                }
            }
        });
        orderTable.setName(ORDER_TABLE);
        var decorator = ToolbarDecorator.createDecorator(orderTable);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        return decorator.createPanel();
    }

    @Override
    protected void dndMoveFieldToAvailable(String sourceName) {
        moveFieldToAvailable(orderTable.getSelectedObject(), true);
    }

    private void moveFieldToAvailable(OrderElement selectedObject, boolean removeSource) {
        var index = orderTableModel.indexOf(selectedObject);
        var item = new TableElement(selectedObject.getField());
        if (!selectedObject.getField().contains(".")) {
            addSelectedField(item);
        }
        removeFromTable(index, removeSource, orderTableModel, orderTable);
    }

    @Override
    public void initListeners() {
        subscribe(LoadQueryCteDataNotifier.class, this::loadQueryData);
        subscribe(SaveQueryCteDataNotifier.class, this::saveQueryData);
        subscribe(AliasAddNotifier.class, this::addSelectedField);
        subscribe(AliasRemoveNotifier.class, this::removeSelectedField);
        subscribe(AliasRenameNotifier.class, this::renameFields);
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
        subscribe(UnionAddRemoveNotifier.class, this::addRemoveUnion);
        subscribe(RefreshAvailableTables.class, this::refreshAvailableTables);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void refreshAvailableTables(List<TreeNode> tables) {
        addAllFieldsRoot();
        tables.forEach(this::addSelectedTable);
    }

    private void addRemoveUnion(int i, boolean interactive) {
        if (!interactive) {
            return;
        }
        if (i > 1) {
            removeAllFieldsRoot();
        } else {
            getPublisher(NotifyRefreshAvailableTables.class).onAction();
        }
    }

    private void loadQueryData(FullQuery fullQuery, String cteName) {
        var cte = fullQuery.getCte(cteName);
        if (Objects.isNull(cte)) {
            return;
        }
        cte.getAliasTable().getItems().forEach(x -> {
            var item = new TableElement(x.getAliasName());
            addSelectedField(item);
        });
        ComponentUtils.loadTableToTable(cte.getOrderTable(), orderTableModel);
        removeSelectedFieldsFromAvailable();
    }

    private void renameFields(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }
        orderTableModel.getItems().stream()
                .filter(x -> oldName.equals(x.getField()))
                .forEach(x -> x.setField(newName));
        availableTreeRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(oldName))
                .forEach(x -> x.getUserObject().setName(newName));
    }

    private void removeSelectedFieldsFromAvailable() {
        orderTableModel.getItems().forEach(element -> availableTreeRoot.nodeToList().stream()
                .filter(x -> {
                    var name = x.getUserObject().getName();
                    return Objects.nonNull(name) && name.equals(element.getField());
                })
                .forEach(x -> {
                    var index = availableTreeRoot.getIndex(x);
                    availableTreeRoot.remove(x);
                    availableModel.nodesWereRemoved(availableTreeRoot, new int[]{index}, new Object[]{x});
                })
        );
    }

    private void removeSelectedField(String name) {
        availableTreeRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(name))
                .forEach(x -> {
                    var index = availableTreeRoot.getIndex(x);
                    availableTreeRoot.remove(x);
                    availableModel.nodesWereRemoved(availableTreeRoot, new int[]{index}, new Object[]{x});
                });
        ComponentUtils.removeRowsByPredicate(x -> x.getField().equals(name), orderTableModel);
    }

    private void removeSelectedTable(TreeNode node) {
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, availableModel);
        var removeTableName = node.getUserObject().getDescription();
        ComponentUtils.removeRowsByPredicate(x -> x.getField().contains(removeTableName + "."), orderTableModel);
    }

    private void saveQueryData(FullQuery query, String cteName) {
        var cte = query.getCte(cteName);
        ComponentUtils.loadTableToTable(orderTableModel, cte.getOrderTable());
        ComponentUtils.clearTree(availableTreeRoot);
        availableModel.reload();
        ComponentUtils.clearTable(orderTableModel);
    }

    private void addSelectedField(TableElement element) {
        var tableElement = new TableElement(element);
        tableElement.setIcon(DatabaseIcons.Col);
        if (hasAllFields()) {
            availableTreeRoot.insert(new TreeNode(tableElement), availableTreeRoot.getChildCount() - 1);
            availableModel.nodesWereInserted(availableTreeRoot, new int[]{availableTreeRoot.getChildCount() - 2});
        } else {
            availableTreeRoot.add(new TreeNode(tableElement));
            availableModel.reload();
        }
    }

    private void addSelectedTable(TreeNode node) {
        var currentCte = mainPanel.getCurrentCte();
        if (currentCte.getUnionMap().size() > 1) {
            return;
        }
        addAllFieldsRoot();
        addSelectedTableToAvailable(node);
    }

    private void addAllFieldsRoot() {
        if (hasAllFields()) {
            return;
        }
        allFieldsRoot = new TreeNode(new TableElement(ALL_FIELDS));
        availableTreeRoot.add(allFieldsRoot);
        availableModel.reload();
    }

    private void removeAllFieldsRoot() {
        if (!hasAllFields()) {
            return;
        }
        availableTreeRoot.remove(allFieldsRoot);
        availableModel.reload();
    }

    private boolean hasAllFields() {
        return availableTreeRoot.nodeToList().stream().anyMatch(x -> {
            var name = x.getUserObject().getName();
            return Objects.nonNull(name) && name.equals(ALL_FIELDS);
        });
    }
}
