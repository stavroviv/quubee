package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.qpart.FullQuery;

public interface LoadQueryDataNotifier {

    Topic<LoadQueryDataNotifier> LOAD_QUERY_DATA = Topic.create("load query data", LoadQueryDataNotifier.class);

    void onAction(FullQuery context, String cteName, int unionNumber);
}
