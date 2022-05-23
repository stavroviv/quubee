package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public interface SelectedTableRemoveNotifier {

    Topic<SelectedTableRemoveNotifier> SELECTED_TABLE_REMOVE =
            Topic.create("selected table remove topic", SelectedTableRemoveNotifier.class);

    void onSelectedTableRemoved(MutableTreeTableNode element);
}
