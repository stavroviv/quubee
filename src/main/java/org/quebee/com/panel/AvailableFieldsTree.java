package org.quebee.com.panel;

import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDTarget;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.EditableModel;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.util.AvailableFieldsTreeDnDSource;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;
import org.quebee.com.util.MyRowsDnDSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Objects;

abstract class AvailableFieldsTree extends QueryPanel {
    protected QBTreeNode availableTreeRoot;
    protected QBTreeNode allFieldsRoot;
    protected ListTreeTableModel availableModel;
    protected TreeTable availableTree;

    public AvailableFieldsTree(MainPanel mainPanel) {
        super(mainPanel);
    }

    protected void enableDragAndDrop() {
        DnDManager.getInstance().registerSource(new MyDnDSource(availableTree), availableTree, mainPanel.getDisposable());
        DnDManager.getInstance().registerTarget(new MyDnDTarget(), availableTree, mainPanel.getDisposable());
    }

    protected <T> void installDnDSupportToTable(TableView<T> table) {
        MyRowsDnDSupport.install(table, (EditableModel) table.getModel(), availableTree, (event) -> {
            if (event.getAttachedObject() instanceof QBTreeNode) {
                var p = event.getPoint();
                var i = table.rowAtPoint(p);
                var item = (QBTreeNode) event.getAttachedObject();
                moveFieldToSelected(item, i, event.getCurrentOverComponent().getName());
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
                var attachedObject = (MyRowsDnDSupport.RowDragInfo)aEvent.getAttachedObject();
                dndMoveFieldToAvailable(attachedObject.getComponent().getName());
            }
        }
    }

    protected abstract void dndMoveFieldToAvailable(String sourceName);

    private class MyDnDSource extends AvailableFieldsTreeDnDSource {

        public MyDnDSource(TreeTable treeTable) {
            super(treeTable);
        }

        @Override
        public boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin) {
            var value = (QBTreeNode) availableTree.getValueAt(availableTree.getSelectedRow(), 0);
            var parent = value.getParent();
            if (value.equals(allFieldsRoot)) {
                return false;
            }
            return availableTreeRoot.equals(parent) || !parent.equals(allFieldsRoot);
        }

        @Override
        public String getFieldDescription(QBTreeNode attachedObject) {
            return getDescription(attachedObject);
        }
    }

    protected String getDescription(QBTreeNode value) {
        if (availableTreeRoot.equals(value.getParent())) {
            return value.getUserObject().getName();
        }
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        return tableObject.getName() + "." + columnObject.getName();
    }

    protected TreeTable getAvailableTree(boolean addAllFields) {
        availableTreeRoot = new QBTreeNode(new TableElement("empty"));
        if (addAllFields) {
            allFieldsRoot = new QBTreeNode(new TableElement("All fields"));
            availableTreeRoot.add(allFieldsRoot);
        }
        availableModel = new ListTreeTableModel(availableTreeRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        availableTree = new TreeTable(availableModel);
        availableTree.setTreeCellRenderer(new TableElement.Renderer());
        availableTree.setRootVisible(false);
        availableTree.addMouseListener(new MouseAdapterDoubleClick() {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                moveFieldToSelected(ComponentUtils.selectedAvailableField(availableTree), -1);
            }
        });
        return availableTree;
    }

    protected abstract void moveFieldToSelected(QBTreeNode node, int index);

    protected abstract void moveFieldToSelected(QBTreeNode node, int index, String componentName);

    protected <T> void moveFieldToTable(int index, QBTreeNode item, T newItem, ListTableModel<T> model, TableView<T> table) {
        if (Objects.isNull(item)) {
            return;
        }
        if (Objects.nonNull(allFieldsRoot) && (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent()))) {
            return;
        }
        addElementToTable(newItem, index, model, table);
        ComponentUtils.removeFromAvailable(item, availableTreeRoot, availableModel, availableTree);
    }

    protected <T> void addElementToTable(T item, int newIndex, ListTableModel<T> model, TableView<T> table) {
        if (newIndex == -1) {
            model.addRow(item);
        } else {
            model.insertRow(newIndex, item);
        }
        table.setSelection(Collections.singleton(item));
    }

    protected JButton smallButton(String text, ActionListener l) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        button.addActionListener(l);
        return button;
    }

    protected <T> void removeFromTable(int index, boolean removeFromOrder, ListTableModel<T> model, TableView<T> table) {
        if (!removeFromOrder) {
            return;
        }
        model.removeRow(index);
        if (model.getRowCount() > 0) {
            ComponentUtils.setSelectedRow(table, index == model.getRowCount() ? index - 1 : index);
        }
    }

    protected void addSelectedTableToAvailable(QBTreeNode node) {
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        availableModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }
}
