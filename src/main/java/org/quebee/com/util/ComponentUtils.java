package org.quebee.com.util;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeNode;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Predicate;

public class ComponentUtils {

    public static void clearTable(ListTableModel<?> tableModel) {
        for (var i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }

    public static void clearTree(TreeNode treeRoot) {
        for (var i = treeRoot.getChildCount() - 1; i >= 0; i--) {
            treeRoot.remove(i);
        }
    }

    public static <T> void loadTableToTable(ListTableModel<T> source, ListTableModel<T> destination) {
        clearTable(destination);
        destination.addRows(source.getItems());
    }

    public static void loadTreeToTree(TreeNode source, TreeNode destination) {
        clearTree(destination);
        source.nodeToList().forEach(destination::add);
    }

    public static void removeNodeByTable(TreeNode node, TreeNode root, ListTreeTableModel treeTableModel) {
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

    public static <T> void removeFirstRowByPredicate(Predicate<T> predicate, ListTableModel<T> model) {
        var row = getFirstRowByPredicate(predicate, model);
        if (Objects.nonNull(row)) {
            model.removeRow(model.indexOf(row));
        }
    }

    public static <T> T getFirstRowByPredicate(Predicate<T> predicate, ListTableModel<T> model) {
        for (var i = 0; i < model.getItems().size(); i++) {
            var item = model.getItem(i);
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
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

    public static TreeNode selectedAvailableField(TreeTable treeTable) {
        var selectedRow = treeTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        return (TreeNode) treeTable.getValueAt(selectedRow, 0);
    }

    public static TreeNode addNodeWithChildren(TreeNode node, TableElement newUserObject,
                                               TreeNode root, ListTreeTableModel model) {
        var newTableNode = new TreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new TreeNode(x.getUserObject())));
        root.add(newTableNode);
        if (root.getChildCount() == 1) {
            model.reload();
        } else {
            model.nodesWereInserted(root, new int[]{root.getChildCount() - 1});
        }
        return newTableNode;
    }
}
