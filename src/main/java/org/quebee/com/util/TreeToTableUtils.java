package org.quebee.com.util;

import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ListTableModel;
import org.quebee.com.model.QBTreeNode;

import java.util.Collections;
import java.util.Objects;

public class TreeToTableUtils {

    public static <T> void moveFieldToTable(QBTreeNode item, T newItem, ListTableModel<T> model, TableView<T> table, QBTreeNode allFieldsRoot,
                                            QBTreeNode availableGroupingRoot,
                                            ListTreeTableModel availableGroupingModel,
                                            TreeTable availableGroupingTree) {
        if (Objects.isNull(item)) {
            return;
        }
        if (Objects.nonNull(allFieldsRoot) && (allFieldsRoot.equals(item) || allFieldsRoot.equals(item.getParent()))) {
            return;
        }
        addElementToTable(newItem, -1, model, table);
        ComponentUtils.removeFromAvailable(item, availableGroupingRoot, availableGroupingModel, availableGroupingTree);
    }

    public static <T> void addElementToTable(T item, int newIndex, ListTableModel<T> model, TableView<T> table) {
        if (newIndex == -1) {
            model.addRow(item);
        } else {
            model.insertRow(newIndex, item);
        }
        table.setSelection(Collections.singleton(item));
    }
}
