package org.quebee.com;

import com.intellij.application.options.codeStyle.OptionTableWithPreviewPanel;
import com.intellij.application.options.codeStyle.SpeedSearchHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import icons.DatabaseIcons;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showQueBui();
    }

    private void showQueBui() {
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

        tabs.addTab(getTablesFields()).setText("Tables and fields").setActions(new DefaultActionGroup(), null);
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

    private TabInfo getTablesFields() {
        JBSplitter splitter = new JBSplitter();
        splitter.setProportion(0.3f);
        splitter.setFirstComponent(databaseTableExample0());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(databaseTableExample0());
        splitter2.setSecondComponent(databaseTableExample0());
        splitter.setSecondComponent(splitter2);
        return new TabInfo(splitter);
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

        ListTreeTableModel dd = new ListTreeTableModel(root, new ColumnInfo[]{getTitleColumnInfo()});
//        dd.set
        TreeTable treeTable = new TreeTable(dd);
        treeTable.setTreeCellRenderer(new MyTitleRenderer());
        return new JBScrollPane(treeTable);
    }

    private static class MyTitleRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            setIcon(DatabaseIcons.Table);
            append(value.toString());
        }
    }

    static ColumnInfo<Object, String> getTitleColumnInfo() {
        return new ColumnInfo<>("Database") {
//            @Override
//            public @Nullable Icon getIcon() {
//                return AllIcons.Actions.MenuSaveall;
//            }

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

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
