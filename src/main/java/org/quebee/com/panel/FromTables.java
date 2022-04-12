package org.quebee.com.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.QuiBuiNotifier;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.quebee.com.notifier.QuiBuiNotifier.QUI_BUI_TOPIC;
import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;

public class FromTables {
    public static final String HEADER = "Tables and Fields";
    public JComponent element;

    private DefaultMutableTreeTableNode databaseRoot;
    private ListTreeTableModel databaseModel;

    public FromTables() {
        JBSplitter splitter = new JBSplitter();
        splitter.setProportion(0.3f);

        splitter.setFirstComponent(databaseTables());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(selectedTables());
        splitter2.setSecondComponent(selectedFields());
        splitter.setSecondComponent(splitter2);
        this.element = splitter;
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
        bus.connect().subscribe(RELOAD_TABLES_TOPIC, this::setDatabaseTables);
        bus.connect().subscribe(QUI_BUI_TOPIC, this::addSelectedTable);
    }

    private void addSelectedTable(TableElement o) {
        selectedTablesRoot.add(new DefaultMutableTreeTableNode(o));
        selectedTablesModel.reload();
    }

    DefaultMutableTreeTableNode selectedTablesRoot;
    ListTreeTableModel selectedTablesModel;

    public JComponent selectedTables() {
        selectedTablesRoot = new DefaultMutableTreeTableNode(new TableElement("empty"));
        selectedTablesModel = new ListTreeTableModel(selectedTablesRoot, new ColumnInfo[]{
                new TreeColumnInfo("Tables")
        });
        var treeTable = new TreeTable(selectedTablesModel);
        treeTable.setRootVisible(false);
        treeTable.setTreeCellRenderer(new TableElement.Renderer());
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        var decorator = ToolbarDecorator.createDecorator(treeTable);

        decorator.setAddAction(button -> {
//            root.insert(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()), i.get());
//            model.nodesWereInserted(root, new int[]{i.get()});
            selectedTablesRoot.add(getTest("test" + new Random(1000).nextInt()));
            selectedTablesModel.reload();
        });
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        return decorator.createPanel();
    }

    public JComponent selectedFields() {

        DefaultMutableTreeTableNode root = getTest("test");

        final DefaultMutableTreeTableNode child = getTest("test 1");
        for (int i = 0; i < 100; i++) {
            child.add(getTest("test 1" + i));
        }
        child.add(getTest("test 12"));
        root.add(child);
        root.add(getTest("test 2"));
        root.add(getTest("test 3"));
        root.add(getTest("test 4"));

        ListTreeTableModel model = new ListTreeTableModel(root, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        TreeTable treeTable = new TreeTable(model);
        treeTable.setTreeCellRenderer(new TableElement.Renderer());
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(treeTable);

        decorator.setAddAction(button -> {
//            root.insert(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()), i.get());
//            model.nodesWereInserted(root, new int[]{i.get()});
            root.add(getTest("test" + new Random(1000).nextInt()));
            model.reload();
        });
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        return decorator.createPanel();
    }

    @NotNull
    private DefaultMutableTreeTableNode getTest(String test) {
        return new DefaultMutableTreeTableNode(new TableElement(test));
    }

    public JComponent databaseTables() {
        databaseRoot = new DefaultMutableTreeTableNode(new TableElement("tables"));
        databaseModel = new ListTreeTableModel(databaseRoot, new ColumnInfo[]{
                new TreeColumnInfo("Database")
        });
        final TreeTable treeTable = new TreeTable(databaseModel);

        treeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                TreeTable table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
                    QuiBuiNotifier publisher = messageBus.syncPublisher(QUI_BUI_TOPIC);
                    Object userObject = ((MutableTreeTableNode) treeTable.getValueAt(table.getSelectedRow(), 0)).getUserObject();
                    TableElement value1 = (TableElement) userObject;
                   publisher.onAction(value1);
                }
            }
        });
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setTreeCellRenderer(new TableElement.Renderer());

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(treeTable);
        decorator.addExtraAction(new AnActionButton("empty", IconUtil.getEmptyIcon(false)) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });

        return decorator.createPanel();
    }

    public void setDatabaseTables(DBTables dbStructure) {
        for (Map.Entry<String, List<String>> stringListEntry : dbStructure.getDbElements().entrySet()) {
            TableElement table = new TableElement(stringListEntry.getKey());
            table.setTable(true);
            var child = new DefaultMutableTreeTableNode(table);
            databaseRoot.add(child);
            for (String columnName : stringListEntry.getValue()) {
                TableElement column = new TableElement(columnName);
                column.setColumn(true);
                child.add(new DefaultMutableTreeTableNode(column));
            }
        }
        databaseModel.reload();
    }
}
