package org.quebee.com;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import org.quebee.com.qpart.FullQuery;

import java.util.List;
import java.util.Objects;

import static org.quebee.com.util.Constants.BODY;

public class StatementProcessor {

    public static FullQuery statementToModel(Statement statement) {
        if (!(statement instanceof Select)) {
            return null;
        }

        Select selectStatement = (Select) statement;
        List<WithItem> withItemsList = selectStatement.getWithItemsList();
        FullQuery fullQuery;
        if (Objects.nonNull(withItemsList)) {
            fullQuery = new FullQuery();
            int i = 0;
            for (WithItem x : withItemsList) {
                SubSelect subSelect = x.getSubSelect();
                fullQuery.addCte(x.getName(), subSelect.getSelectBody(), i++);
            }
            fullQuery.addCte(BODY, selectStatement.getSelectBody(), i);
        } else {
            fullQuery = new FullQuery(selectStatement.getSelectBody());
        }

        return fullQuery;
    }
}
