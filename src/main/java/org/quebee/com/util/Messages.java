package org.quebee.com.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Messages {

    public static <L> L getPublisher(Topic<L> topic) {
        var messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.syncPublisher(topic);
    }

    static Map<UUID, Map<String, Topic<?>>> topics = new HashMap<>();

    public static <L> L getPublisher(UUID id, String topic, Class<L> listenerClass) {
        var messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.syncPublisher(getTopic(id, topic, listenerClass));
    }

    public static <L> Topic<L> getTopic(UUID id, String topicName, Class<L> listenerClass) {
        Map<String, Topic<?>> stringTopicMap = topics.get(id);
        Topic<L> topic;
        if (stringTopicMap == null) {
            Map<String, Topic<?>> value = new HashMap<>();
            topic = Topic.create(topicName, listenerClass);
            value.put(topicName, topic);
            topics.put(id, value);
        } else {
            topic = (Topic<L>) stringTopicMap.get(topicName);
            if (topic == null) {
                topic = Topic.create(topicName, listenerClass);
                stringTopicMap.put(topicName, topic);
            }
        }
        return topic;
    }

    public static void removeTopics(UUID id) {
        topics.remove(id);
    }
}
