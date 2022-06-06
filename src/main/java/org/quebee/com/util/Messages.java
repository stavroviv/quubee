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

    @SuppressWarnings("unchecked")
    public static <L> Topic<L> getTopic(UUID id, Class<L> handler) {
        var topicsById = topics.get(id);
        Topic<L> topic;
        if (topicsById == null) {
            var topicsByHandler = new HashMap<Class<?>, Topic<?>>();
            topic = Topic.create("jet select topic " + handler.getName(), handler);
            topicsByHandler.put(handler, topic);
            topics.put(id, topicsByHandler);
        } else {
            topic = (Topic<L>) topicsById.get(handler);
            if (topic == null) {
                topic = Topic.create("jet select topic " + handler.getName(), handler);
                topicsById.put(handler, topic);
            }
        }
        return topic;
    }

    public static void removeTopics(UUID id) {
        topics.remove(id);
    }
}
