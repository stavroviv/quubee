package org.quebee.com.util;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import java.util.function.Predicate;

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

    public static <T> void removeRowsByPredicate(Predicate<T> predicate, ListTableModel<T> model) {
        for (var i = model.getItems().size() - 1; i >= 0; i--) {
            var item = model.getItem(i);
            if (predicate.test(item)) {
                model.removeRow(i);
            }
        }
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

    public static QBTreeNode addNodeWithChildren(QBTreeNode node, TableElement newUserObject,
                                                 QBTreeNode root, ListTreeTableModel model) {
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        root.add(newTableNode);
        if (root.getChildCount() == 1) {
            model.reload();
        } else {
            model.nodesWereInserted(root, new int[]{root.getChildCount() - 1});
        }
        return newTableNode;
    }
}
