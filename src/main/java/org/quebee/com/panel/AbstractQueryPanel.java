package org.quebee.com.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;

import javax.swing.*;

import static org.quebee.com.util.Messages.getTopic;

public abstract class AbstractQueryPanel {

    protected MainPanel mainPanel;

    public AbstractQueryPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public abstract JComponent getComponent();

    public abstract String getHeader();

    public abstract void initListeners(Disposable disposable);

    protected  <L> void subscribe(Disposable disposable, Class<L> listenerClass, L handler) {
        var bus = ApplicationManager.getApplication().getMessageBus();
        Topic<L> topic = getTopic(mainPanel.getId(), listenerClass);
        bus.connect(disposable).subscribe(topic, handler);
    }
}
