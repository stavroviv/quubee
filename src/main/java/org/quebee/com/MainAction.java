package org.quebee.com;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.run.actions.AlignedIconWithTextAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.PostgresStructureImpl;
import org.quebee.com.panel.MainPanel;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.Messages;

import java.util.Objects;

import static org.quebee.com.notifier.LoadQueryCteDataNotifier.LOAD_QUERY_CTE_DATA;
import static org.quebee.com.notifier.LoadQueryDataNotifier.LOAD_QUERY_DATA;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class MainAction extends AlignedIconWithTextAction {

    private AnActionEvent action;

    @Override
    @SneakyThrows
    public void actionPerformed(@NotNull AnActionEvent action) {
        this.action = action;
        var selectionText = getSelectionText(action);

        var fullQuery = new FullQuery(CCJSqlParserUtil.parse(selectionText));
        var form = new MainPanel(fullQuery) {
            @Override
            protected void doOKAction() {
                saveQueryPart();
                setQueryTextToEditor(getFullQuery());
                super.doOKAction();
            }
        };

        var messageBus = ApplicationManager.getApplication().getMessageBus();
        setDatabaseTables(action);
        messageBus.syncPublisher(LOAD_QUERY_DATA).onAction(fullQuery, fullQuery.getFirstCte(), 0);
        messageBus.syncPublisher(LOAD_QUERY_CTE_DATA).onAction(fullQuery, fullQuery.getFirstCte());

        form.show();
    }

    private void setQueryTextToEditor(FullQuery fullQuery) {
        var resultQuery = fullQuery.getFullSelectText();
        ApplicationManager.getApplication().invokeLater(() -> {
            var editor = action.getRequiredData(CommonDataKeys.EDITOR);
            var document = editor.getDocument();
            var primaryCaret = editor.getCaretModel().getPrimaryCaret();
            var start = primaryCaret.getSelectionStart();
            var end = primaryCaret.getSelectionEnd();
            var project = action.getProject();

            WriteCommandAction.runWriteCommandAction(project, () -> {
                if (start != end) {
                    document.replaceString(start, end, resultQuery);
                } else {
                    document.replaceString(0, document.getTextLength(), resultQuery);
                }
                var data = action.getData(LangDataKeys.PSI_FILE);
                if (Objects.nonNull(data) && Objects.nonNull(project)) {
                    CodeStyleManager.getInstance(project).reformatText(data, 0, document.getTextLength());
                }
            });
        });
    }

    private String getSelectionText(AnActionEvent action) {
        var editor = action.getRequiredData(CommonDataKeys.EDITOR);
        var caretModel = editor.getCaretModel();
        var currentCaret = caretModel.getCurrentCaret();
        return currentCaret.hasSelection() ? currentCaret.getSelectedText() : getAllText(action);
    }

    private String getAllText(AnActionEvent action) {
        var data = action.getData(CommonDataKeys.PSI_FILE);
        return Objects.nonNull(data) ? data.getText() : "";
    }

    private void setDatabaseTables(@NotNull AnActionEvent action) {
        var console = JdbcConsole.findConsole(action);
        var structure = new PostgresStructureImpl();
        var dbStructure = structure.getDBStructure(console);
        Messages.getPublisher(RELOAD_TABLES_TOPIC).onAction(dbStructure);
    }
}
