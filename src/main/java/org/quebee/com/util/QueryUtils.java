package org.quebee.com.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class QueryUtils {

    public static Expression ff(String where) {
        Expression whereExpression = null;
        try {
            var stmt = CCJSqlParserUtil.parse("SELECT * FROM TABLES WHERE " + where);
            var select = (Select) stmt;
            whereExpression = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return whereExpression;
    }
}
