package org.quebee.com.database;

import com.intellij.database.console.JdbcConsole;


public interface DBStructure {

    DBTables getDBStructure(JdbcConsole console);

}
