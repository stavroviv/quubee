package org.quebee.com.notifier;

import org.quebee.com.qpart.FullQuery;

public interface SaveQueryCteDataNotifier {

    void onAction(FullQuery query, String cteName);
}
