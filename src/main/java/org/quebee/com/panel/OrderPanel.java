package org.quebee.com.panel;

import com.intellij.openapi.ui.ComboBox;
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
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.OrderElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.qpart.OneCte;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

@Getter
public class OrderPanel extends QueryPanel {
    private final static String ASC = "Ascending";
    private final static String DESC = "Descending";
    private final String header = "Order";
    private final JBSplitter component = new JBSplitter();

    public OrderPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTable());
        component.setSecondComponent(getOrderTable());
    }

    private QBTreeNode availableOrderRoot;
    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel availableOrderModel;
    private TreeTable availableOrderTree;

    private JComponent getFieldsTable() {
        availableOrderRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsRoot = new QBTreeNode(new TableElement("All fields"));
        availableOrderRoot.add(allFieldsRoot);
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
                var item = (QBTreeNode) table.getValueAt(table.getSelectedRow(), 0);
                if (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent())) {
                    return;
                }
                addOrderElement(item, -1);
                removeFromAvailable(item);
            }
        });

        var decorator = ToolbarDecorator.createDecorator(availableOrderTree);
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
//        comp.set
//        comp.setBorder(new LineBorder(JBColor.RED, 1));
        comp.setPreferredSize(new Dimension(30, 400));
//        comp.add(Box.createVerticalStrut(10));
        comp.add(smallButton(">"));
        comp.add(smallButton(">>"));
        comp.add(smallButton("<"));
        comp.add(smallButton("<<"));

        hBox.add(comp);

        return hBox;
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
        var decorator = ToolbarDecorator.createDecorator(orderTable);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        return decorator.createPanel();
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
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        return tableObject.getName() + "." + columnObject.getName();
    }

    private JButton smallButton(String text) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        return button;
    }

    @Override
    public void initListeners() {
        subscribe(LoadQueryCteDataNotifier.class, this::loadQueryData);
        subscribe(SaveQueryCteDataNotifier.class, this::saveQueryData);
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SelectedFieldRemoveNotifier.class, this::removeSelectedField);
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
//        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void loadQueryData(FullQuery query, String s) {
        // TODO load available fields tree
        OneCte currentCte = mainPanel.getCurrentCte();
//        currentCte.getAliasTable().getItems().forEach(x->addSelectedField(x, true));
    }

    private void removeSelectedField(TableElement tableElement) {
        availableOrderRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(tableElement.getDescription()))
                .forEach(x -> {
                    var index = availableOrderRoot.getIndex(x);
                    availableOrderRoot.remove(x);
                    availableOrderModel.nodesWereRemoved(availableOrderRoot, new int[]{index}, new Object[]{x});
                });
    }

    private void saveQueryData(FullQuery query, String s) {
        for (var i = availableOrderRoot.getChildCount() - 2; i >= 0; i--) {
            availableOrderRoot.remove(i);
        }
        ComponentUtils.clearTree(allFieldsRoot);
        availableOrderModel.reload();
        ComponentUtils.clearTable(orderTableModel);
    }

    private void addSelectedField(TableElement element, boolean interactive) {
        if (!interactive) {
            return;
        }
        var tableElement = new TableElement(element);
        tableElement.setIcon(DatabaseIcons.Col);
        availableOrderRoot.insert(new QBTreeNode(tableElement), availableOrderRoot.getChildCount() - 1);
        availableOrderModel.nodesWereInserted(availableOrderRoot, new int[]{availableOrderRoot.getChildCount() - 2});
    }

    private void addSelectedTable(QBTreeNode node) {
        var currentCte = mainPanel.getCurrentCte();
        if (currentCte.getUnionMap().size() > 1) {
            return;
        }
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        availableOrderModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }
}
