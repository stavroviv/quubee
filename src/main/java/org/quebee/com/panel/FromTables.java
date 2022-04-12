package org.quebee.com.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.MainQuiBuiForm;
import org.quebee.com.notifier.QuiBuiNotifier;
import org.quebee.com.database.DBTables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.quebee.com.notifier.QuiBuiNotifier.QUI_BUI_TOPIC;
import static org.quebee.com.panel.OrderPanel.getTitleColumnInfo;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class FromTables {
    public static final String HEADER = "Tables and Fields";
    public JComponent element;

    public FromTables() {
        JBSplitter splitter = new JBSplitter();
        splitter.setProportion(0.3f);

        splitter.setFirstComponent(databaseTables());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(treeTable("Tables"));
        splitter2.setSecondComponent(treeTable("Fields"));
        splitter.setSecondComponent(splitter2);
        this.element = splitter;
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
        bus.connect().subscribe(RELOAD_TABLES_TOPIC, this::setDatabaseTables);
    }

    //    private DefaultMutableTreeTableNode root;
    public JComponent treeTable(String tableName) {

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");

        final DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode("test 1");
        for (int i = 0; i < 100; i++) {
            child.add(new DefaultMutableTreeTableNode("test 1" + i));
        }
        child.add(new DefaultMutableTreeTableNode("test 12"));
        root.add(child);
        root.add(new DefaultMutableTreeTableNode("test 2"));
        root.add(new DefaultMutableTreeTableNode("test 3"));
        root.add(new DefaultMutableTreeTableNode("test 4"));

        ListTreeTableModel model = new ListTreeTableModel(root, new ColumnInfo[]{getTitleColumnInfo(tableName)});
        TreeTable treeTable = new TreeTable(model);
        treeTable.setTreeCellRenderer(new MainQuiBuiForm.TableRenderer());
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(treeTable);

        decorator.setAddAction(button -> {
//            root.insert(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()), i.get());
//            model.nodesWereInserted(root, new int[]{i.get()});
            root.add(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
            model.reload();
        });
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        return decorator.createPanel();
    }

    private DefaultMutableTreeTableNode databaseRoot;
    private ListTreeTableModel databaseModel;

    public JComponent databaseTables() {

        databaseRoot = new DefaultMutableTreeTableNode("tables");

        databaseModel = new ListTreeTableModel(databaseRoot, new ColumnInfo[]{getTitleColumnInfo("Database")});
        TreeTable treeTable = new TreeTable(databaseModel);
        treeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                TreeTable table =(TreeTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
                    QuiBuiNotifier publisher = messageBus.syncPublisher(QUI_BUI_TOPIC);
                    publisher.onAction(table.getSelectedRow());
                }
            }
        });
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setTreeCellRenderer(new MainQuiBuiForm.TableRenderer());

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(treeTable);
        decorator.addExtraAction(new AnActionButton("Find", IconUtil.getRemoveIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });
//        decorator.setAddAction(button -> {
//            databaseRoot.add(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
//            databaseModel.reload();
//        });
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
        return decorator.createPanel();
    }

    public void setDatabaseTables(DBTables dbStructure) {
        for (Map.Entry<String, List<String>> stringListEntry : dbStructure.getDbElements().entrySet()) {
            DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode(stringListEntry.getKey());
            databaseRoot.add(child);
            for (String s : stringListEntry.getValue()) {
                child.add(new DefaultMutableTreeTableNode(s));
            }
        }
        databaseModel.reload();
    }
}
