package org.quebee.com;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.quebee.com.qpart.FullQuery;

import java.util.List;
import java.util.Objects;

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
            withItemsList.forEach(x -> {
                System.out.println(x);
//                fullQuery.getCteMap().put(x.getName(), new OneCte(x.getSubSelect()));
            });
        } else {
            fullQuery = new FullQuery(selectStatement.getSelectBody());
        }

        return fullQuery;
    }
}
