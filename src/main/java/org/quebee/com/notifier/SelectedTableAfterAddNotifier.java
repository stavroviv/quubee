package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;

public interface SelectedTableAfterAddNotifier extends NodeNotifier {

    Topic<SelectedTableAfterAddNotifier> SELECTED_TABLE_AFTER_ADD =
            Topic.create("selected table after add topic", SelectedTableAfterAddNotifier.class);
}
