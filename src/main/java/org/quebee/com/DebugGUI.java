package org.quebee.com;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBus;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.notifier.ReloadDbTablesNotifier;

import java.util.HashMap;
import java.util.List;

import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class DebugGUI implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            Statement parse = CCJSqlParserUtil.parse("select id from table where id = 1");
            System.out.println(parse);
        } catch (JSQLParserException ignored) {
        }
        MainQuiBuiForm form = new MainQuiBuiForm(project);
        DBTables dbStructure = new DBTables();
        HashMap<String, List<String>> dbElements = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            dbElements.put("test " + i, List.of("test 1", "test 2", "test 3"));
        }
        dbStructure.setDbElements(dbElements);
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        ReloadDbTablesNotifier publisher = messageBus.syncPublisher(RELOAD_TABLES_TOPIC);
        publisher.onAction(dbStructure);
        form.show();
    }
}
