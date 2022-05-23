package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;

public interface SelectedFieldAddNotifier extends NodeNotifier {

    Topic<SelectedFieldAddNotifier> SELECTED_FIELD_ADD =
            Topic.create("selected field add topic", SelectedFieldAddNotifier.class);
}
