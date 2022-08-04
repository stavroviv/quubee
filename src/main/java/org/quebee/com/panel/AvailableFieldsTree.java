package org.quebee.com.panel;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MouseAdapterDoubleClick;

import javax.swing.*;
import java.awt.event.MouseEvent;

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
                moveFieldToSelected(ComponentUtils.selectedAvailableField(availableTree));
            }
        });
        return availableTree;
    }

    protected abstract void moveFieldToSelected(QBTreeNode node);
}
