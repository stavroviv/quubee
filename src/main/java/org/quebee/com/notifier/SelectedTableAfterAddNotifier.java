package org.quebee.com.notifier;

import org.quebee.com.model.QBTreeNode;

public interface SelectedTableAfterAddNotifier {
    void onAction(QBTreeNode node, boolean interactive);
}
