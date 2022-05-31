package org.quebee.com.database;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.dataSource.DataSourceSchemaMapping;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.ObjectPaths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresStructureImpl implements DBStructure {
    private final Map<String, List<String>> dbElements = new HashMap<>();

    @Override
    public DBTables getDBStructure(JdbcConsole console) {
        var data = new DBTables();
        if (console == null) {
            data.setDbElements(dbElements);
            return data;
        }

        var dataSource = console.getDataSource();
        var databaseDriver = dataSource.getDatabaseDriver();
        if (databaseDriver == null) {
            throw new IllegalStateException("Database driver not set");
        }

        var sqlDialect = databaseDriver.getSqlDialect();
        if (!sqlDialect.equals("PostgreSQL")) {
            throw new IllegalStateException("Not supported yet");
        }

        var scope = console.getDataSource().getIntrospectionScope();
        var databases = dataSource.getModel().traverser().expand(DasUtil.byKind(ObjectKind.DATABASE));

        for (var database : databases) {
            var path = ObjectPaths.of(database);
            var schemas = dataSource.getModel()
                    .traverser()
                    .expandAndSkip(x -> x.getKind() == ObjectKind.DATABASE && x.getName().equals(path.getName()))
                    .filter(DasUtil.byKind(ObjectKind.SCHEMA))
                    .traverse();

            for (var schema : schemas) {
                if (!DataSourceSchemaMapping.isIntrospected(scope, schema)) {
                    continue;
                }
                var dasChildren = schema.getDasChildren(ObjectKind.TABLE);
                for (var object : dasChildren) {
                    addToStructure(object);
                }
                break;
            }
        }

        data.setDbElements(dbElements);
        return data;
    }

    private void addToStructure(DasObject table) {
        var tableElements = new ArrayList<String>();
        table.getDasChildren(ObjectKind.COLUMN).forEach(column -> tableElements.add(column.getName()));
        dbElements.put(table.getName(), tableElements);
    }
}
