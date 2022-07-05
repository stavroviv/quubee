package org.quebee.com.notifier;

import org.quebee.com.database.DBTables;

public interface ReloadCteTablesNotifier {

    void onAction(DBTables context);
}
