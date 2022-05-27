package org.quebee.com;

import net.sf.jsqlparser.statement.Statement;
import org.quebee.com.qpart.FullQuery;

public class StatementProcessor {

    public static FullQuery statementToModel(Statement statement) {
        FullQuery fullQuery = new FullQuery();
        return fullQuery;
    }
}
