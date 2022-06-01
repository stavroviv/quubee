package org.quebee.com.panel;

import com.intellij.openapi.Disposable;

import javax.swing.*;

public interface QueryComponent {

    JComponent getComponent();

    String getHeader();

    void initListeners(Disposable disposable);
}
