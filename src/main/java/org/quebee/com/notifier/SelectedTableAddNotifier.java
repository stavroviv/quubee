package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;

public interface SelectedTableAddNotifier  extends NodeNotifier {

    Topic<SelectedTableAddNotifier> SELECTED_TABLE_ADD =
            Topic.create("selected table add topic", SelectedTableAddNotifier.class);
}
