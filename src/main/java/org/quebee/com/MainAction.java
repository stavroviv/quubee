package org.quebee.com;

import com.intellij.database.console.JdbcConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.PostgresStructureImpl;
import org.quebee.com.panel.MainPanel;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.Messages;

import static org.quebee.com.notifier.LoadQueryDataNotifier.LOAD_QUERY_DATA;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class MainAction extends AnAction {

    @Override
    @SneakyThrows
    public void actionPerformed(@NotNull AnActionEvent action) {
        var selectionText = getSelectionText(action);

        var fullQuery = new FullQuery(CCJSqlParserUtil.parse(selectionText));
        var form = new MainPanel(fullQuery);

        var messageBus = ApplicationManager.getApplication().getMessageBus();
        setDatabaseTables(action);
        messageBus.syncPublisher(LOAD_QUERY_DATA).onAction(fullQuery, fullQuery.getFirstCte(), 0);

        form.show();
    }

    private String getSelectionText(AnActionEvent action) {
        var editor = action.getRequiredData(CommonDataKeys.EDITOR);
        var caretModel = editor.getCaretModel();
        var currentCaret = caretModel.getCurrentCaret();
        return currentCaret.hasSelection()
                ? currentCaret.getSelectedText()
                : action.getData(CommonDataKeys.PSI_FILE).getText();
    }

    private void setDatabaseTables(@NotNull AnActionEvent action) {
        var console = JdbcConsole.findConsole(action);
        var structure = new PostgresStructureImpl();
        var dbStructure = structure.getDBStructure(console);
        Messages.getPublisher(RELOAD_TABLES_TOPIC).onAction(dbStructure);
    }
}
