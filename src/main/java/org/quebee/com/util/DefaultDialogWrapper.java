package org.quebee.com.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

public abstract class DefaultDialogWrapper extends DialogWrapper {

    protected DefaultDialogWrapper(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent, IdeModalityType.PROJECT);
        init();
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
    }
}
