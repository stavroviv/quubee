package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.qpart.FullQuery;

public interface SaveQueryDataNotifier {

    Topic<SaveQueryDataNotifier> SAVE_QUERY_DATA = Topic.create("save query data", SaveQueryDataNotifier.class);

    void onAction(FullQuery context, String cteName, int unionNumber);
}
