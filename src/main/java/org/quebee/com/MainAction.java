package org.quebee.com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.codeStyle.extractor.ui.ExtractedSettingsDialog;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        frame.getContentPane().add(flow);
        flow.add(tabs.getComponent());
        frame.getContentPane().add(tabs.getComponent(), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout());
        south.setOpaque(true);
        south.setBackground(JBColor.WHITE);

        final JButton v = new JButton("OK");
        v.addItemListener(e1 -> tabs.setSideComponentVertical(v.isSelected()));
        v.setDefaultCapable(true);
        south.add(v);
        final JButton v1 = new JButton("Cancel");
        v1.addItemListener(e1 -> tabs.setSideComponentVertical(v.isSelected()));
        south.add(v1);

        frame.getContentPane().add(south, BorderLayout.SOUTH);

        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(databaseTableExample0());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(databaseTableExample0());
        splitter2.setSecondComponent(databaseTableExample0());
        splitter.setSecondComponent(splitter2);

        TabInfo info = new TabInfo(splitter);
        tabs.addTab(info).setText("Tables and fields").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(getLinksTable())).setText("Links").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Grouping").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Order").setActions(new DefaultActionGroup(), null);

        frame.setPreferredSize(new Dimension(1200, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JTable getLinksTable() {
        Object[][] array = new String[][]{
                {"Test", "dd", "1.5"},
                {"Kkkk", "dd", "4.0"},
                {"Mmmm", "f", "2.2"}
        };
        Object[] columnsHeader = new String[]{"Name", "UOM", "Quantity"};
        return new JTable(array, columnsHeader);
    }

    public JScrollPane databaseTableExample0() {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");

        DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode("test 1");
        child.add(new DefaultMutableTreeTableNode("test 11"));
        child.add(new DefaultMutableTreeTableNode("test 12"));
        root.add(child);
        root.add(new DefaultMutableTreeTableNode("test 2"));
        root.add(new DefaultMutableTreeTableNode("test 3"));
        root.add(new DefaultMutableTreeTableNode("test 4"));
        DefaultTreeTableModel defaultTreeTableModel = new DefaultTreeTableModel(root, List.of("Database"));
//        defaultTreeTableModel.get

        final ColumnInfo[] COLUMNS = new ColumnInfo[]{getTitleColumnInfo()};
        ListTreeTableModel dd = new ListTreeTableModel(root, COLUMNS);
        TreeTable treeTable = new TreeTable(dd);
//        treeTable.getro
        return new JScrollPane(treeTable);
    }

    protected static ColumnInfo getTitleColumnInfo() {
        return new ColumnInfo("TITLE") {
            @Nullable
            @Override
            public Object valueOf(Object o) {
                if (o instanceof ExtractedSettingsDialog.SettingsTreeNode) {
                    return ((ExtractedSettingsDialog.SettingsTreeNode) o).getTitle();
                } else {
                    return o.toString();
                }
            }

            @Override
            public Class getColumnClass() {
                return TreeTableModel.class;
            }
        };
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
