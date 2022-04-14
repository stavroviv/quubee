package org.quebee.com;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.jetbrains.annotations.NotNull;

public class DebugGUI implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            Statement parse = CCJSqlParserUtil.parse("select id from table where id = 1");
            System.out.println(parse);
        } catch (JSQLParserException exception) {

        }
        MainQuiBuiForm form = new MainQuiBuiForm(project);
        form.show();
    }
}
