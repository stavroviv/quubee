package org.quebee.com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageDialogBuilder;
import org.jetbrains.annotations.NotNull;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MessageDialogBuilder.yesNo("Test", "Test").ask(e.getProject());
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
