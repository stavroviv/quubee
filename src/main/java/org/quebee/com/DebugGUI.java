package org.quebee.com;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class DebugGUI implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        MainQuiBuiForm form = new MainQuiBuiForm(project);
        form.show();
    }
}
