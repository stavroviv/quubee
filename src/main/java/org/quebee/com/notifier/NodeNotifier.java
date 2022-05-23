package org.quebee.com.notifier;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public interface NodeNotifier {
    void onAction(MutableTreeTableNode element);
}
