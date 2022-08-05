package org.quebee.com.notifier;

import org.quebee.com.model.TreeNode;

public interface SelectedTableAddNotifier {
    void onAction(TreeNode element, String alias);
}
