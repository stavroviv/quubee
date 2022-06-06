package org.quebee.com.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Messages {

    private final static Map<UUID, Map<Class<?>, Topic<?>>> topics = new HashMap<>();

    public static <L> L getPublisher(UUID id, Class<L> handler) {
        var messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.syncPublisher(getTopic(id, handler));
    }

    public static <L> Topic<L> getTopic(UUID id, Class<L> handler) {
        Map<Class<?>, Topic<?>> stringTopicMap = topics.get(id);
        Topic<L> topic;
        if (stringTopicMap == null) {
            Map<Class<?>, Topic<?>> value = new HashMap<>();
            topic = Topic.create("jet select topic " + handler.getName(), handler);
            value.put(handler, topic);
            topics.put(id, value);
        } else {
            topic = (Topic<L>) stringTopicMap.get(handler);
            if (topic == null) {
                topic = Topic.create("jet select topic " + handler.getName(), handler);
                stringTopicMap.put(handler, topic);
            }
        }
        return topic;
    }

    public static void removeTopics(UUID id) {
        topics.remove(id);
    }
}
