package org.quebee.com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DialogPanel p = DemoTipsKt.demoTips(e.getProject());
        JFrame f = new JFrame();
        f.setLayout(new BorderLayout());
        f.add(p, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
