package org.quebee.com.panel;

import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDTarget;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.EditableModel;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.OrderElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.AvailableFieldsTreeDnDSource;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MyRowsDnDSupport;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class OrderPanel extends QueryPanel {
    public final static String ORDER_PANEL_HEADER = "Order";
    public final static String ASC = "Ascending";
    public final static String DESC = "Descending";
    private final static String ALL_FIELDS = "All fields";
    private final String header = ORDER_PANEL_HEADER;
    private final JBSplitter component = new JBSplitter();

    public OrderPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTable());
        component.setSecondComponent(getOrderTable());
        enableDragAndDrop();
    }

    private QBTreeNode availableOrderRoot;
    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel availableOrderModel;
    private TreeTable availableOrderTree;

    private void enableDragAndDrop() {
        DnDManager.getInstance().registerSource(new MyDnDSource(availableOrderTree), availableOrderTree, mainPanel.getDisposable());
        DnDManager.getInstance().registerTarget(new MyDnDTarget(), availableOrderTree, mainPanel.getDisposable());
        MyRowsDnDSupport.install(orderTable, (EditableModel) orderTable.getModel(), availableOrderTree, (event) -> {
            if (event.getAttachedObject() instanceof QBTreeNode) {
                var p = event.getPoint();
                var i = orderTable.rowAtPoint(p);
                var item = (QBTreeNode) event.getAttachedObject();
                addOrderElement(item, i);
                removeFromAvailable(item);
            }
        });
    }

    private class MyDnDTarget implements DnDTarget {

        public boolean update(DnDEvent aEvent) {
            aEvent.setDropPossible(true);
            return true;
        }

        public void drop(DnDEvent aEvent) {
            if (aEvent.getAttachedObject() instanceof MyRowsDnDSupport.RowDragInfo) {
                moveFieldToAvailable(orderTable.getSelectedObject(), true);
            }
        }
    }

    private class MyDnDSource extends AvailableFieldsTreeDnDSource {

        public MyDnDSource(TreeTable treeTable) {
            super(treeTable);
        }

        @Override
        public boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin) {
            var value = (QBTreeNode) availableOrderTree.getValueAt(availableOrderTree.getSelectedRow(), 0);
            var parent = value.getParent();
            if (value.equals(allFieldsRoot)) {
                return false;
            }
            return availableOrderRoot.equals(parent) || !parent.equals(allFieldsRoot);
        }

        @Override
        public String getFieldDescription(QBTreeNode attachedObject) {
            // FIXME
            return OrderPanel.this.getFieldDescription(attachedObject);
        }
    }

    private JComponent getFieldsTable() {
        availableOrderRoot = new QBTreeNode(new TableElement("empty"));
        availableOrderModel = new ListTreeTableModel(availableOrderRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });

        availableOrderTree = new TreeTable(availableOrderModel);
        availableOrderTree.setTreeCellRenderer(new TableElement.Renderer());
        availableOrderTree.setRootVisible(false);
        availableOrderTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1) {
                    return;
                }
                moveFieldToSelected(selectedAvailableField());
            }
        });
//        availableOrderTree.getSelectionModel()
//                .addListSelectionListener(e -> buttonAdd.setEnabled(availableOrderTree.getSelectedRow() != -1));
        var decorator = ToolbarDecorator.createDecorator(availableOrderTree);
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
        comp.setPreferredSize(new Dimension(30, 400));

        buttonAdd = smallButton(">", e -> moveFieldToSelected(selectedAvailableField()), true);
        comp.add(buttonAdd);
        comp.add(smallButton(">>", e -> availableOrderRoot.nodeToList().forEach(this::moveFieldToSelected), true));
        buttonRemove = smallButton("<", e -> moveFieldToAvailable(orderTable.getSelectedObject(), true), true);
        comp.add(buttonRemove);
        comp.add(smallButton("<<", e -> {
            orderTable.getItems().forEach(x -> moveFieldToAvailable(x, false));
            ComponentUtils.clearTable(orderTableModel);
        }, true));

        hBox.add(comp);
        return hBox;
    }

    private JButton buttonAdd;
    private JButton buttonRemove;

    private void moveFieldToSelected(QBTreeNode item) {
        if (Objects.isNull(item)) {
            return;
        }
        if (Objects.nonNull(allFieldsRoot) && (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent()))) {
            return;
        }
        addOrderElement(item, -1);
        removeFromAvailable(item);
    }

    private QBTreeNode selectedAvailableField() {
        int selectedRow = availableOrderTree.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        return (QBTreeNode) availableOrderTree.getValueAt(selectedRow, 0);
    }

    private ListTableModel<OrderElement> orderTableModel;
    private TableView<OrderElement> orderTable;

    private JComponent getOrderTable() {
        var fieldInfo = new ColumnInfo<OrderElement, String>("Field") {
            @Override
            public String valueOf(OrderElement o) {
                return o.getField();
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
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() != 2) {
                    return;
                }
                var table = (TableView<?>) mouseEvent.getSource();
                var columnName = orderTable.getColumnName(table.getSelectedColumn());
                if (columnName.equals("Field")) {
                    moveFieldToAvailable(orderTable.getSelectedObject(), true);
                }
            }
        });
//        orderTable.getSelectionModel()
//                .addListSelectionListener(e -> buttonRemove.setEnabled(orderTable.getSelectedRow() != -1));
        var decorator = ToolbarDecorator.createDecorator(orderTable);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        return decorator.createPanel();
    }

    private void moveFieldToAvailable(OrderElement selectedObject, boolean removeFromOrder) {
        var index = orderTableModel.indexOf(selectedObject);
        var item = new TableElement(selectedObject.getField());
        if (!selectedObject.getField().contains(".")) {
            addSelectedField(item);
        }
        if (removeFromOrder) {
            orderTableModel.removeRow(index);
            if (orderTableModel.getRowCount() > 0) {
                ComponentUtils.setSelectedRow(orderTable, index == orderTableModel.getRowCount() ? index - 1 : index);
            }
        }
    }

    private void addOrderElement(QBTreeNode value, int newIndex) {
        var item = new OrderElement();
        if (value != null) {
            item.setField(getFieldDescription(value));
        }
        item.setSorting(ASC);
        if (newIndex == -1) {
            orderTableModel.addRow(item);
        } else {
            orderTableModel.insertRow(newIndex, item);
        }
        orderTable.setSelection(Collections.singleton(item));
    }

    private void removeFromAvailable(QBTreeNode item) {
        if (!availableOrderRoot.equals(item.getParent())) {
            return;
        }
        availableOrderRoot.nodeToList().stream()
                .filter(x -> x.equals(item))
                .forEach(x -> {
                    var index = availableOrderRoot.getIndex(x);
                    availableOrderRoot.remove(x);
                    availableOrderModel.nodesWereRemoved(availableOrderRoot, new int[]{index}, new Object[]{x});
                    SwingUtilities.invokeLater(() -> ComponentUtils.setSelectedRow(availableOrderTree, index));
                });
    }

    private String getFieldDescription(QBTreeNode value) {
        if (availableOrderRoot.equals(value.getParent())) {
            return value.getUserObject().getName();
        }
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        return tableObject.getName() + "." + columnObject.getName();
    }

    private JButton smallButton(String text, ActionListener l, boolean b) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        button.addActionListener(l);
        button.updateUI();
        button.setEnabled(b);
        return button;
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

    private void refreshAvailableTables(List<QBTreeNode> tables) {
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
        availableOrderRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(oldName))
                .forEach(x -> x.getUserObject().setName(newName));
    }

    private void removeSelectedFieldsFromAvailable() {
        orderTableModel.getItems().forEach(element -> availableOrderRoot.nodeToList().stream()
                .filter(x -> {
                    var name = x.getUserObject().getName();
                    return Objects.nonNull(name) && name.equals(element.getField());
                })
                .forEach(x -> {
                    var index = availableOrderRoot.getIndex(x);
                    availableOrderRoot.remove(x);
                    availableOrderModel.nodesWereRemoved(availableOrderRoot, new int[]{index}, new Object[]{x});
                })
        );
    }

    private void removeSelectedField(String name) {
        availableOrderRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(name))
                .forEach(x -> {
                    var index = availableOrderRoot.getIndex(x);
                    availableOrderRoot.remove(x);
                    availableOrderModel.nodesWereRemoved(availableOrderRoot, new int[]{index}, new Object[]{x});
                });
        ComponentUtils.removeRowsByPredicate(x -> x.getField().equals(name), orderTableModel);
    }

    private void removeSelectedTable(QBTreeNode node) {
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, availableOrderModel);
        var removeTableName = node.getUserObject().getDescription();
        ComponentUtils.removeRowsByPredicate(x -> x.getField().contains(removeTableName + "."), orderTableModel);
    }

    private void saveQueryData(FullQuery query, String cteName) {
        var cte = query.getCte(cteName);
        ComponentUtils.loadTableToTable(orderTableModel, cte.getOrderTable());
        ComponentUtils.clearTree(availableOrderRoot);
        availableOrderModel.reload();
        ComponentUtils.clearTable(orderTableModel);
    }

    private void addSelectedField(TableElement element) {
        var tableElement = new TableElement(element);
        tableElement.setIcon(DatabaseIcons.Col);
        if (hasAllFields()) {
            availableOrderRoot.insert(new QBTreeNode(tableElement), availableOrderRoot.getChildCount() - 1);
            availableOrderModel.nodesWereInserted(availableOrderRoot, new int[]{availableOrderRoot.getChildCount() - 2});
        } else {
            availableOrderRoot.add(new QBTreeNode(tableElement));
            availableOrderModel.reload();
        }
    }

    private void addSelectedTable(QBTreeNode node) {
        var currentCte = mainPanel.getCurrentCte();
        if (currentCte.getUnionMap().size() > 1) {
            return;
        }
        addAllFieldsRoot();
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        availableOrderModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }

    private void addAllFieldsRoot() {
        if (hasAllFields()) {
            return;
        }
        allFieldsRoot = new QBTreeNode(new TableElement(ALL_FIELDS));
        availableOrderRoot.add(allFieldsRoot);
        availableOrderModel.reload();
    }

    private void removeAllFieldsRoot() {
        if (!hasAllFields()) {
            return;
        }
        availableOrderRoot.remove(allFieldsRoot);
        availableOrderModel.reload();
    }

    private boolean hasAllFields() {
        return availableOrderRoot.nodeToList().stream().anyMatch(x -> {
            var name = x.getUserObject().getName();
            return Objects.nonNull(name) && name.equals(ALL_FIELDS);
        });
    }
}
