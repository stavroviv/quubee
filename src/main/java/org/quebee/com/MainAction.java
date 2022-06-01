package org.quebee.com;

import com.intellij.database.console.JdbcConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.PostgresStructureImpl;
import org.quebee.com.panel.MainPanel;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.Messages;

import static org.quebee.com.notifier.LoadQueryDataNotifier.LOAD_QUERY_DATA;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;
import static org.quebee.com.notifier.SaveAllQueryNotifier.SAVE_ALL_QUERY;

public class MainAction extends AnAction {

    private AnActionEvent action;

    @Override
    @SneakyThrows
    public void actionPerformed(@NotNull AnActionEvent action) {
        this.action = action;
        var selectionText = getSelectionText(action);

        var fullQuery = new FullQuery(CCJSqlParserUtil.parse(selectionText));
        var form = new MainPanel(fullQuery);

        var messageBus = ApplicationManager.getApplication().getMessageBus();
        setDatabaseTables(action);
        messageBus.syncPublisher(LOAD_QUERY_DATA).onAction(fullQuery, fullQuery.getFirstCte(), 0);

        form.show();
        var bus = ApplicationManager.getApplication().getMessageBus();
//        bus.connect(form.getDialog().getDisposable()).subscribe(SAVE_ALL_QUERY, this::getQueryText); ????
        bus.connect().subscribe(SAVE_ALL_QUERY, this::getQueryText);
    }

    private void getQueryText(FullQuery fullQuery) {
        var resultQuery = fullQuery.getQuery();
        ApplicationManager.getApplication().invokeLater(() -> {
            var editor = action.getRequiredData(CommonDataKeys.EDITOR);
            var document = editor.getDocument();
            // Work off of the primary caret to get the selection info
            var primaryCaret = editor.getCaretModel().getPrimaryCaret();
            int start = primaryCaret.getSelectionStart();
            int end = primaryCaret.getSelectionEnd();
            // Replace the selection with a fixed string.
            // Must do this document change in a write action context.
            var project = action.getProject();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                var data = action.getData(LangDataKeys.PSI_FILE);
                if (start != end) {
                    document.replaceString(start, end, resultQuery);
                } else {
                    document.replaceString(0, document.getTextLength(), resultQuery);
                }
                CodeStyleManager.getInstance(project).reformatText((PsiFile) data, 0, document.getTextLength());
            });
        });
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
