package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;

public interface SelectedTableRemoveNotifier extends NodeNotifier {

    Topic<SelectedTableRemoveNotifier> SELECTED_TABLE_REMOVE =
            Topic.create("selected table remove topic", SelectedTableRemoveNotifier.class);
}
