package org.quebee.com.notifier;

import org.quebee.com.model.QBTreeNode;

import java.util.List;

public interface RefreshAvailableTables {
    void onAction(List<QBTreeNode> tables);
}
