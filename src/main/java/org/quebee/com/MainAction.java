package org.quebee.com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MainQuiBuiForm form = new MainQuiBuiForm();
        form.show();
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
