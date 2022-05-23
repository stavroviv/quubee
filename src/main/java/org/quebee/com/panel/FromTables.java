package org.quebee.com.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.TableElement;
import org.quebee.com.util.Messages;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;
import static org.quebee.com.notifier.SelectedTableAddNotifier.SELECTED_TABLE_ADD;
import static org.quebee.com.notifier.SelectedTableRemoveNotifier.SELECTED_TABLE_REMOVE;

@Getter
public class FromTables implements QueryComponent {
    private final String header = "Tables and Fields";
    private final JComponent component;

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

        this.component = splitter;
        initListeners();
    }

    private void initListeners() {
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect().subscribe(RELOAD_TABLES_TOPIC, this::setDatabaseTables);
        bus.connect().subscribe(SELECTED_TABLE_ADD, this::addSelectedTable);
        bus.connect().subscribe(SELECTED_TABLE_REMOVE, this::removeSelectedTable);
    }

    private void removeSelectedTable(MutableTreeTableNode node) {
        if (Objects.nonNull(node.getParent().getParent())) {
            return;
        }
        int index = selectedTablesRoot.getIndex(node);
        selectedTablesRoot.remove(node);
        selectedTablesModel.nodesWereRemoved(selectedTablesRoot, new int[]{index}, new Object[]{node});
    }

    private void addSelectedTable(MutableTreeTableNode node) {
        if (Objects.isNull(node.getParent())) {
            return;
        }
        if (Objects.isNull(node.getParent().getParent())) {
            addSelectedTableNode(node);
            return;
        }
        var parent = node.getParent();
        var parentUserObject = (TableElement) parent.getUserObject();
        boolean exists = false;
        var children = selectedTablesRoot.children();
        while (children.hasMoreElements()) {
            MutableTreeTableNode mutableTreeTableNode = children.nextElement();
            TableElement userObject = (TableElement) mutableTreeTableNode.getUserObject();
            if (userObject.getId() == parentUserObject.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            addSelectedTableNode(parent);
        }
    }

    private void addSelectedTableNode(TreeTableNode node) {
        var child = new DefaultMutableTreeTableNode(node.getUserObject());
        node.children().asIterator()
                .forEachRemaining(x -> child.add(new DefaultMutableTreeTableNode(x.getUserObject())));
        selectedTablesRoot.add(child);
        if (selectedTablesRoot.getChildCount() == 1) {
            selectedTablesModel.reload();
        } else {
            selectedTablesModel.nodesWereInserted(selectedTablesRoot, new int[]{selectedTablesRoot.getChildCount() - 1});
        }
    }

    DefaultMutableTreeTableNode selectedTablesRoot;
    ListTreeTableModel selectedTablesModel;
    TreeTable selectedTablesTree;

    public JComponent selectedTables() {
        selectedTablesRoot = new DefaultMutableTreeTableNode(new TableElement("empty"));
        selectedTablesModel = new ListTreeTableModel(selectedTablesRoot, new ColumnInfo[]{
                new TreeColumnInfo("Tables")
        });

        selectedTablesTree = new TreeTable(selectedTablesModel);
//        treeTable.getTree().setToggleClickCount(0);
        selectedTablesTree.setRootVisible(false);
        selectedTablesTree.setTreeCellRenderer(new TableElement.Renderer());
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        var decorator = ToolbarDecorator.createDecorator(selectedTablesTree);
        decorator.addExtraAction(new AnActionButton("Inner Query", DatabaseIcons.Database) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });
//        decorator.setAddAction(button -> {
////            selectedTablesRoot.add(getTest("test" + new Random(1000).nextInt()));
//            selectedTablesModel.reload();
//        });

        decorator.setRemoveAction(button -> Messages.getPublisher(SELECTED_TABLE_REMOVE)
                .onSelectedTableRemoved((MutableTreeTableNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0)));

        JPanel panel = decorator.createPanel();
        decorator.getActionsPanel().getAnActionButton(CommonActionsPanel.Buttons.REMOVE).addCustomUpdater(
                e -> {
                    if (selectedTablesTree.getSelectedRow() == -1) {
                        return false;
                    }
                    var value = (TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
                    return Objects.isNull(value.getParent().getParent());
                }
        );
        return panel;
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
            //extracted(SELECTED_TABLE_REMOVE, null);
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
        // make own component?
//        treeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
//            @Override
//            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
//                throw new ExpandVetoException(event);
//            }
//
//            @Override
//            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
//
//            }
//        });
        treeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1 || mouseEvent.getX() < 40) {
                    return;
                }
                Messages.getPublisher(SELECTED_TABLE_ADD)
                        .onSelectedTableAdded((MutableTreeTableNode) treeTable.getValueAt(table.getSelectedRow(), 0));
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
