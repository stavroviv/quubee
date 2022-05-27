package org.quebee.com;

import com.intellij.database.console.JdbcConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBStructure;
import org.quebee.com.database.DBTables;
import org.quebee.com.database.PostgresStructureImpl;
import org.quebee.com.notifier.ReloadDbTablesNotifier;

import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent action) {
        MainQuiBuiForm form = new MainQuiBuiForm(action.getProject());
        setDatabaseTables(action);
        String selectionText = getSelectionText(action);
        System.out.println(selectionText);
        form.show();
    }

    private String getSelectionText(AnActionEvent action) {
        Editor editor = action.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        Caret currentCaret = caretModel.getCurrentCaret();
        return currentCaret.hasSelection()
                ? currentCaret.getSelectedText()
                : action.getData(CommonDataKeys.PSI_FILE).getText();
    }

    private void setDatabaseTables(@NotNull AnActionEvent action) {
        JdbcConsole console = JdbcConsole.findConsole(action);
        DBStructure structure = new PostgresStructureImpl();
        DBTables dbStructure = structure.getDBStructure(console);
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        ReloadDbTablesNotifier publisher = messageBus.syncPublisher(RELOAD_TABLES_TOPIC);
        publisher.onAction(dbStructure);
    }
}
