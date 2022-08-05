package org.quebee.com.notifier;

import org.quebee.com.model.TreeNode;

import java.util.List;

public interface RefreshAvailableTables {
    void onAction(List<TreeNode> tables);
}
