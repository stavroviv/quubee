package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;
import org.quebee.com.util.JetSelectMessages;

import javax.swing.*;

import static org.quebee.com.util.JetSelectMessages.getTopic;

public abstract class AbstractQueryPanel {

    protected MainPanel mainPanel;

    public AbstractQueryPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public abstract JComponent getComponent();

    public abstract String getHeader();

    public abstract void initListeners();

    protected <L> void subscribe(Class<L> listenerClass, L handler) {
        var bus = ApplicationManager.getApplication().getMessageBus();
        Topic<L> topic = getTopic(mainPanel.getId(), listenerClass);
        bus.connect(mainPanel.getDisposable()).subscribe(topic, handler);
    }

    protected <L> L getPublisher(Class<L> handler) {
        return JetSelectMessages.getPublisher(mainPanel.getId(), handler);
    }

    public String getTooltipText() {
        return "";
    }
}
