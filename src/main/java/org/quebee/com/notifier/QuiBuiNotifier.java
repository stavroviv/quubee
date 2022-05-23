package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public interface QuiBuiNotifier {

    Topic<QuiBuiNotifier> QUI_BUI_TOPIC = Topic.create("qui bui topic", QuiBuiNotifier.class);

    void onSelectedTableAdded(MutableTreeTableNode element);
//
//    void onSelectedTableRemoved(MutableTreeTableNode element);
}
