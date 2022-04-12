package org.quebee.com.notifier;

import com.intellij.util.messages.Topic;
import org.quebee.com.database.DBTables;

public interface ReloadDbTablesNotifier {

    Topic<ReloadDbTablesNotifier> RELOAD_TABLES_TOPIC = Topic.create("reload db tables", ReloadDbTablesNotifier.class);

    void onAction(DBTables context);

}
