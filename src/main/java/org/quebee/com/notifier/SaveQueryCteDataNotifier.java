package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.qpart.FullQuery;

public interface SaveQueryCteDataNotifier {

    Topic<SaveQueryCteDataNotifier> SAVE_QUERY_CTE_DATA = Topic.create("save query cte data", SaveQueryCteDataNotifier.class);

    void onAction(FullQuery query, String cteName);
}
