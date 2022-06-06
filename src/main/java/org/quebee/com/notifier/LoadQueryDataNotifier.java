package org.quebee.com.notifier;

import org.quebee.com.qpart.FullQuery;

public interface LoadQueryDataNotifier {

    void onAction(FullQuery context, String cteName, int unionNumber);
}
