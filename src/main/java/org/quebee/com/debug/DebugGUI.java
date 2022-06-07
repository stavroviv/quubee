package org.quebee.com.debug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.notifier.LoadQueryCteDataNotifier;
import org.quebee.com.notifier.LoadQueryDataNotifier;
import org.quebee.com.notifier.ReloadDbTablesNotifier;
import org.quebee.com.panel.MainPanel;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.JetSelectMessages;

import java.util.HashMap;
import java.util.List;


public class DebugGUI implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
//        testActivity1();
        testActivity2();
//        testActivityJoinTable();
    }

    private void testActivity1() {
        FullQuery fullQuery;
        try {
            var parse = CCJSqlParserUtil.parse("select id from table where id = 1");
            fullQuery = new FullQuery(parse);
        } catch (JSQLParserException e) {
            Messages.showMessageDialog(e.getMessage(), "Warning", Messages.getErrorIcon());
            return;
        }
        var form = new MainPanel(fullQuery);
        var dbStructure = new DBTables();
        var dbElements = new HashMap<String, List<String>>();
        for (var i = 0; i < 700; i++) {
            if (i % 2 == 0) {
                dbElements.put("test_" + i, List.of("test_1", "test_2", "test_3", "test_4"));
            } else {
                dbElements.put("test_" + i, List.of("test_5", "test_6"));
            }
        }
        dbStructure.setDbElements(dbElements);

        JetSelectMessages.getPublisher(form.getId(), ReloadDbTablesNotifier.class).onAction(dbStructure);
        form.show();
    }

    private void testActivity2() {
        FullQuery fullQuery;
        try {
            Statement statement = CCJSqlParserUtil.parse("with test_table_1 as (" +
                    "select table.id, table.test_2, table.test_3, table1.test_4 " +
                    "from table " +
                    "join table1 on table.id=table1.id " +
                    "where table.id = 1" +
                    "), " +
                    "ss2 as (" +
                    "select table.id from table where table.id = 1 union " +
                    "select table1.id from table1 where table1.id = 1 " +
                    "), " +
                    "test_table_255 as (" +
                    "select table.id from table where table.id = 1 union " +
                    "select table.id from table where table.id = 1 union " +
                    "select table.id from table where table.id = 1 union " +
                    "select table1.id from table1 where table1.id = 1 " +
                    ") " +
                    "select test_table_1.id,ss2.id " +
                    "from test_table_1 " +
                    "join ss2 on test_table_1.id=ss2.id and test_table_1.id=ss2.id " +
                    "join ss2 as ss3 on test_table_1.id=ss3.id and test_table_1.id=ss3.id " +
                    "where test_table_1.id = 1"
            );
            fullQuery = new FullQuery(statement);
        } catch (JSQLParserException e) {
            Messages.showMessageDialog(e.getMessage(), "Warning", Messages.getErrorIcon());
            return;
        }

        var form = new MainPanel(fullQuery) {
            @Override
            protected void doOKAction() {
                System.out.println(getFullQuery().getFullSelectText());
                super.doOKAction();
            }
        };
        var dbStructure = new DBTables();
        var dbElements = new HashMap<String, List<String>>();
        dbElements.put("table", List.of("id", "test_2", "test_3", "test_4"));
        dbElements.put("table1", List.of("id", "test_2", "test_3", "test_4"));
        dbStructure.setDbElements(dbElements);

        JetSelectMessages.getPublisher(form.getId(), ReloadDbTablesNotifier.class).onAction(dbStructure);
        JetSelectMessages.getPublisher(form.getId(), LoadQueryDataNotifier.class).onAction(fullQuery, fullQuery.getFirstCte(), 0);
        JetSelectMessages.getPublisher(form.getId(), LoadQueryCteDataNotifier.class).onAction(fullQuery, fullQuery.getFirstCte());

        form.show();
    }

    private void testActivityJoinTable() {
        FullQuery fullQuery;
        try {
            Statement statement = CCJSqlParserUtil.parse(
                    "select test_table_1.id, ss2.id " +
                            "from test_table_1 " +
                            "left join test_table_2 on test_table_1.id=test_table_2.id " +
                            "and test_table_1.test_2=test_table_2.test_2 " +
                            "right join test_table_3 as ss3 on test_table_1.id=test_table_3.id " +
                            "and test_table_1.test_2=test_table_3.test_2 "
            );
            fullQuery = new FullQuery(statement);
        } catch (JSQLParserException e) {
            Messages.showMessageDialog(e.getMessage(), "Warning", Messages.getErrorIcon());
            return;
        }

        var form = new MainPanel(fullQuery) {
            @Override
            protected void doOKAction() {
                System.out.println(getFullQuery().getFullSelectText());
                super.doOKAction();
            }
        };
        var dbStructure = new DBTables();
        var dbElements = new HashMap<String, List<String>>();
        dbElements.put("test_table_1", List.of("id", "test_2", "test_3", "test_4"));
        dbElements.put("test_table_2", List.of("id", "test_2", "test_3", "test_4"));
        dbElements.put("test_table_3", List.of("id", "test_2", "test_3", "test_4"));
        dbStructure.setDbElements(dbElements);

        JetSelectMessages.getPublisher(form.getId(), ReloadDbTablesNotifier.class).onAction(dbStructure);
        JetSelectMessages.getPublisher(form.getId(), LoadQueryDataNotifier.class).onAction(fullQuery, fullQuery.getFirstCte(), 0);
        JetSelectMessages.getPublisher(form.getId(), LoadQueryCteDataNotifier.class).onAction(fullQuery, fullQuery.getFirstCte());

        form.show();
    }
}
