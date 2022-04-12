package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.model.TableElement;

public interface QuiBuiNotifier {

    Topic<QuiBuiNotifier> QUI_BUI_TOPIC = Topic.create("qui bui topic", QuiBuiNotifier.class);

    void onAction(TableElement context);

}
