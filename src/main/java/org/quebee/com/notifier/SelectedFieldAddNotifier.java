package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public interface SelectedFieldAddNotifier {

    Topic<SelectedFieldAddNotifier> SELECTED_FIELD_ADD =
            Topic.create("selected field add topic", SelectedFieldAddNotifier.class);

    void onSelectedFieldAdded(MutableTreeTableNode element);
}
