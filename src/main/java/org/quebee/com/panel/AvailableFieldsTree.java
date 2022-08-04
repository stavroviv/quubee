package org.quebee.com.panel;

import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;

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
}
