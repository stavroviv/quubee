package org.quebee.com;

import com.intellij.database.console.JdbcConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBStructure;
import org.quebee.com.database.DBStructureImpl;
import org.quebee.com.database.DBTables;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent action) {
        MainQuiBuiForm form = new MainQuiBuiForm();
        setDatabaseTables(action, form);
        form.show();
    }

    private void setDatabaseTables(@NotNull AnActionEvent action, MainQuiBuiForm form) {
        JdbcConsole console = JdbcConsole.findConsole(action);
        DBStructure structure = new DBStructureImpl();
        DBTables dbStructure = structure.getDBStructure(console);
        form.setDatabaseTables(dbStructure);
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
