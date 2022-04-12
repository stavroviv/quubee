package org.quebee.com;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.database.DBTables;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainQuiBuiForm {
    final JFrame frame = new JFrame("Qui Bui");

    public MainQuiBuiForm() {
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        frame.getContentPane().add(flow);
        flow.add(tabs.getComponent());
        frame.getContentPane().add(tabs.getComponent(), BorderLayout.CENTER);

        addButtons(tabs);

        tabs.addTab(getTablesFields()).setText("Tables and Fields").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(getLinksTable())).setText("Links").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Grouping").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
        addOrderTab(tabs);

        frame.setPreferredSize(new Dimension(1200, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void addOrderTab(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new OrderPanel())).setText(OrderPanel.HEADER);
    }

    private void addButtons(JBTabsImpl tabs) {
        JPanel south = new JPanel(new FlowLayout());
        south.setOpaque(true);
//        south.setBackground(JBColor.WHITE);

        final JButton okButton = new JButton(Messages.getOkButton());
        okButton.setActionCommand(Messages.getOkButton());
        okButton.addActionListener(event -> hide());
//        okButton.setDefaultCapable(true);
        south.add(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> hide());
        south.add(cancelButton);

        frame.getContentPane().add(south, BorderLayout.SOUTH);
    }

    private TabInfo getTablesFields() {
        JBSplitter splitter = new JBSplitter();
        splitter.setProportion(0.3f);

        splitter.setFirstComponent(databaseTables());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(treeTable("Tables"));
        splitter2.setSecondComponent(treeTable("Fields"));
        splitter.setSecondComponent(splitter2);
        return new TabInfo(splitter);
    }

    private JComponent getLinksTable() {
        ListTableModel model = new ListTableModel(new ColumnInfo[]{getTitleColumnInfo("Test")});
        TableView table = new TableView(model);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            model.addRow(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
            //    model.reload();
        });
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        return decorator.createPanel();
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
        treeTable.setTreeCellRenderer(new TableRenderer());
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
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setTreeCellRenderer(new TableRenderer());

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

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.setVisible(false);
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

    public static class TableRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            setIcon(DatabaseIcons.Table);
            append(value.toString());
        }
    }

    static ColumnInfo<Object, String> getTitleColumnInfo(String name) {
        return new ColumnInfo<>(name) {

            @Nullable
            @Override
            public String valueOf(Object o) {
                return o.toString();
            }

            @Override
            public Class<?> getColumnClass() {
                return TreeTableModel.class;
            }
        };
    }

}
