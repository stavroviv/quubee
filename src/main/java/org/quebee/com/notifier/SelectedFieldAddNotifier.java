package org.quebee.com.notifier;

import org.quebee.com.model.TableElement;

public interface SelectedFieldAddNotifier {
    void onAction(TableElement element, boolean interactive);
}
