package org.quebee.com;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBus;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.notifier.ReloadDbTablesNotifier;
import org.quebee.com.qpart.FullQuery;

import java.util.HashMap;
import java.util.List;

import static org.quebee.com.notifier.LoadQueryDataNotifier.LOAD_QUERY_DATA;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class DebugGUI implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        //testActivity1(project);
        testActivity2(project);
    }

    private void testActivity2(Project project) {
        FullQuery fullQuery;
        try {
            Statement parse = CCJSqlParserUtil.parse("with ss as (" +
                    "select table.id, table.test_2, table.test_3 from table where table.id = 1" +
                    "), " +
                    "ss2 as (" +
                    "select table.id from table where table.id = 1 union " +
                    "select table1.id from table1 where table1.id = 1 " +
                    ") " +
                    "select ss.id,ss2.id " +
                    "from ss " +
                    "join ss2 on ss.id=ss2.id and ss.id=ss2.id " +
                    "join ss2 as ss3 on ss.id=ss3.id and ss.id=ss3.id " +
                    "where ss.id = 1"
            );
            fullQuery = StatementProcessor.statementToModel(parse);
        } catch (JSQLParserException e) {
            Messages.showMessageDialog(e.getMessage(), "Warning", Messages.getErrorIcon());
            return;
        }

        MainQuiBuiForm form = new MainQuiBuiForm(project);
        DBTables dbStructure = new DBTables();
        HashMap<String, List<String>> dbElements = new HashMap<>();
        dbElements.put("table", List.of("id", "test_2", "test_3", "test_4"));
        dbElements.put("table1", List.of("id", "test_2", "test_3", "test_4"));
        dbStructure.setDbElements(dbElements);

        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.syncPublisher(RELOAD_TABLES_TOPIC).onAction(dbStructure);
        messageBus.syncPublisher(LOAD_QUERY_DATA).onAction(fullQuery, fullQuery.getFirstCte(), 0);

        form.show();
    }

    private void testActivity1(@NotNull Project project) {
        try {
            Statement parse = CCJSqlParserUtil.parse("select id from table where id = 1");
            System.out.println(parse);
        } catch (JSQLParserException ignored) {
        }
        MainQuiBuiForm form = new MainQuiBuiForm(project);
        DBTables dbStructure = new DBTables();
        HashMap<String, List<String>> dbElements = new HashMap<>();
        for (int i = 0; i < 700; i++) {
            if (i % 2 == 0) {
                dbElements.put("test_" + i, List.of("test_1", "test_2", "test_3", "test_4"));
            } else {
                dbElements.put("test_" + i, List.of("test_5", "test_6"));
            }
        }
        dbStructure.setDbElements(dbElements);
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        ReloadDbTablesNotifier publisher = messageBus.syncPublisher(RELOAD_TABLES_TOPIC);
        publisher.onAction(dbStructure);
        form.show();
    }
}
