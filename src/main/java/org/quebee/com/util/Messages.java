package org.quebee.com.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Messages {

//    public static <L> L getPublisher(Topic<L> topic) {
//        var messageBus = ApplicationManager.getApplication().getMessageBus();
//        return messageBus.syncPublisher(topic);
//    }

    static Map<UUID, Map<Class<?>, Topic<?>>> topics = new HashMap<>();

    public static <L> L getPublisher(UUID id, Class<L> listenerClass) {
        var messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.syncPublisher(getTopic(id, listenerClass));
    }

    public static <L> Topic<L> getTopic(UUID id, Class<L> listenerClass) {
        Map<Class<?>, Topic<?>> stringTopicMap = topics.get(id);
        Topic<L> topic;
        if (stringTopicMap == null) {
            Map<Class<?>, Topic<?>> value = new HashMap<>();
            topic = Topic.create("jet select topic " + listenerClass.getName(), listenerClass);
            value.put(listenerClass, topic);
            topics.put(id, value);
        } else {
            topic = (Topic<L>) stringTopicMap.get(listenerClass);
            if (topic == null) {
                topic = Topic.create("jet select topic " + listenerClass.getName(), listenerClass);
                stringTopicMap.put(listenerClass, topic);
            }
        }
        return topic;
    }

    public static void removeTopics(UUID id) {
        topics.remove(id);
    }
}
