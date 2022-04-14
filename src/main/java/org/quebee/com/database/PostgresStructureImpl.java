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
    private Map<String, List<String>> dbElements;

    @Override
    public DBTables getDBStructure(JdbcConsole console) {
        dbElements = new HashMap<>();

//        TableRow tablesRoot = new TableRow(DATABASE_TABLE_ROOT);
//        tablesRoot.setRoot(true);
//        TreeItem<TableRow> root = new TreeItem<>(tablesRoot);
//        root.setExpanded(true);

        var data = new DBTables();
        if (console == null) {
            data.setDbElements(dbElements);
//            data.setRoot(root);
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

        for (DasObject database : databases) {
            var path = ObjectPaths.of(database);
            var schemas = dataSource.getModel()
                    .traverser()
                    .expandAndSkip(x -> x.getKind() == ObjectKind.DATABASE && x.getName().equals(path.getName()))
                    .filter(DasUtil.byKind(ObjectKind.SCHEMA))
                    .traverse();

            for (DasObject schema : schemas) {
                if (!DataSourceSchemaMapping.isIntrospected(scope, schema)) {
                    continue;
                }
                var dasChildren1 = schema.getDasChildren(ObjectKind.TABLE);
                for (DasObject object : dasChildren1) {
                    addToStructure(object);
                }
                break;
            }
        }

        data.setDbElements(dbElements);
//        data.setRoot(root);
        return data;
    }

    private void addToStructure(DasObject table) {
//        TableRow parentNode = new TableRow(table.getName());
//        parentNode.setRoot(true);
//        TreeItem<TableRow> stringTreeItem = new TreeItem<>(parentNode);
//        root.getChildren().add(stringTreeItem);
        List<String> tableElements = new ArrayList<>();
        table.getDasChildren(ObjectKind.COLUMN).forEach(column -> {
            tableElements.add(column.getName());
//            stringTreeItem.getChildren().add(new TreeItem<>(new TableRow(column.getName())));
        });
        dbElements.put(table.getName(), tableElements);
    }
}
