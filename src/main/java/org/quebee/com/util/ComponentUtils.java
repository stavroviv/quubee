package org.quebee.com.util;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.QBTreeNode;

import javax.swing.*;

public class ComponentUtils {

    public static void clearTable(ListTableModel<?> tableModel) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }

    public static void clearTree(QBTreeNode treeRoot) {
        for (int i = treeRoot.getChildCount() - 1; i >= 0; i--) {
            treeRoot.remove(i);
        }
    }

    public static <T> void loadTableToTable(ListTableModel<T> source, ListTableModel<T> destination) {
        clearTable(destination);
        destination.addRows(source.getItems());
    }

    public static void loadTreeToTree(QBTreeNode source, QBTreeNode destination) {
        clearTree(destination);
        source.nodeToList().forEach(destination::add);
    }

    public static void removeNodeByTable(QBTreeNode node, QBTreeNode root, ListTreeTableModel treeTableModel) {
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        root.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(removeTableName))
                .findFirst().ifPresent(x -> {
                    var index = root.getIndex(x);
                    root.remove(x);
                    treeTableModel.nodesWereRemoved(root, new int[]{index}, new Object[]{x});
                });
    }

    public static void setSelectedRow(JComponent component, int row) {
        if (component instanceof JTable) {
            ((JTable) component).getSelectionModel().setSelectionInterval(row, row);
        } else if (component instanceof JList) {
            ((JList<?>) component).setSelectedIndex(row);
        } else if (component instanceof JTree) {
            ((JTree) component).setSelectionRow(row);
        } else {
            throw new IllegalArgumentException("Unsupported component: " + component);
        }
    }
}
