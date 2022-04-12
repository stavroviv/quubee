package org.quebee.com;

import com.intellij.util.messages.Topic;

public interface QuiBuiNotifier {

    Topic<QuiBuiNotifier> QUI_BUI_TOPIC = Topic.create("qui bui topic", QuiBuiNotifier.class);

    void onAction(Object context);

}
