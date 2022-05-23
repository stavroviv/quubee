package org.quebee.com.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;

public class Messages {

    public static  <L> L getPublisher(Topic<L> topic) {
        var messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.syncPublisher(topic);
    }
}
