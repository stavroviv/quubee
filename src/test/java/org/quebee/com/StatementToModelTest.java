package org.quebee.com;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;
import org.quebee.com.qpart.FullQuery;

class StatementToModelTest {

    @Test
    void dummyTest() throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse("select table.id from table where table.id = 1");
        System.out.println(parse);
        FullQuery fullQuery = new FullQuery(parse);
    }

    @Test
    void dummyTest1() throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(
                "select table.id " +
                        "from table where table.id = 1 " +
                        "union all " +
                        "select table.id " +
                        "from table where table.id = 1"
        );
        System.out.println(parse);
        FullQuery fullQuery = new FullQuery(parse);
    }

    @Test
    void dummyTest3() throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(
                "with ss as (select table.id, table.id from table where table.id = 1), " +
                        "ss2 as (" +
                        "select table.id from table where table.id = 1 union " +
                        "select table1.id from table1 where table1.id = 1 " +
                        ") " +
                        "select ss.id " +
                        "from ss where ss.id = 1 " +
                        "union all " +
                        "select table.id " +
                        "from table where table.id = 1"
        );
        System.out.println(parse);
        FullQuery fullQuery = new FullQuery(parse);
    }

    @Test
    void dummyTest4() throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(
                "with ss as (select table.id from table where table.id = 1), " +
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
        System.out.println(parse);
        FullQuery fullQuery = new FullQuery(parse);
    }
}