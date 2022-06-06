package org.quebee.com.notifier;

import org.quebee.com.model.QBTreeNode;

public interface SelectedTableAddNotifier {
    void onAction(QBTreeNode element, String alias);
}
