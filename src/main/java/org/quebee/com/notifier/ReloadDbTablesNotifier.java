package org.quebee.com.notifier;

import org.quebee.com.database.DBTables;

public interface ReloadDbTablesNotifier {

    void onAction(DBTables context);
}
