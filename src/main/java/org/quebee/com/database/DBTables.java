package org.quebee.com.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBTables {

    private Map<String, List<String>> dbElements = new HashMap<>();

    public Map<String, List<String>> getDbElements() {
        return dbElements;
    }

    public void setDbElements(Map<String, List<String>> dbElements) {
        this.dbElements = dbElements;
    }
    //private TreeItem<TableRow> root = new TreeItem<>();
}
