package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.qpart.FullQuery;

public interface SaveAllQueryNotifier {

    Topic<SaveAllQueryNotifier> SAVE_ALL_QUERY = Topic.create("save all query", SaveAllQueryNotifier.class);

    void onAction(FullQuery fullQuery);
}
