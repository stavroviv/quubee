package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.qpart.FullQuery;

public interface LoadQueryCteDataNotifier {

    Topic<LoadQueryCteDataNotifier> LOAD_QUERY_CTE_DATA = Topic.create("load query cte data", LoadQueryCteDataNotifier.class);

    void onAction(FullQuery query, String cteName);
}
