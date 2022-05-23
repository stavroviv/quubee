package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public interface SelectedTableAddNotifier {

    Topic<SelectedTableAddNotifier> SELECTED_TABLE_ADD =
            Topic.create("selected table add topic", SelectedTableAddNotifier.class);

    void onSelectedTableAdded(MutableTreeTableNode element);
}
