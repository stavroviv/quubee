package org.quebee.com.util;

import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.QBTreeNode;

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
}
