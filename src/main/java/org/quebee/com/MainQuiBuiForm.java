package org.quebee.com;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.actionSystem.impl.ActionToolbarSpacer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
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

import static com.intellij.openapi.actionSystem.ActionPlaces.TOOLBAR;

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
        tabs.addTab(new TabInfo(new JTable())).setText("Order").setActions(new DefaultActionGroup(), null);

        frame.setPreferredSize(new Dimension(1200, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
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

        splitter.setFirstComponent(treeTable("Database"));
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(treeTable("Tables"));
        splitter2.setSecondComponent(treeTable("Fields"));
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

    public JComponent treeTable(String tableName) {

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");

        DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode("test 1");
        for (int i = 0; i < 100; i++) {
            child.add(new DefaultMutableTreeTableNode("test 1" + i));
        }
        child.add(new DefaultMutableTreeTableNode("test 12"));
        root.add(child);
        root.add(new DefaultMutableTreeTableNode("test 2"));
        root.add(new DefaultMutableTreeTableNode("test 3"));
        root.add(new DefaultMutableTreeTableNode("test 4"));

        ListTreeTableModel dd = new ListTreeTableModel(root, new ColumnInfo[]{getTitleColumnInfo(tableName)});
        TreeTable treeTable = new TreeTable(dd);
        treeTable.setTreeCellRenderer(new TableRenderer());
        JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        ActionToolbarImpl toolbar = new ActionToolbarImpl(TOOLBAR, new DefaultActionGroup(), true);
//        toolbar.setTargetComponent(treeTable);
//        Action act  = new ActionButton();
//        ActionButton add = new ActionButtonWithText(
//                this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
//        );
        ActionToolbarSpacer actionToolbarSpacer = new ActionToolbarSpacer(true);
//        add.setActionCommand("Test");
        toolbar.add(actionToolbarSpacer);
        toolbar.add(new Button("Delete"));
//        jbScrollPane.add(toolbar);
//        jbScrollPane.add(treeTable);

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true);
        panel.setContent(jbScrollPane);
        panel.setToolbar(toolbar);
        return panel;
    }

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.setVisible(false);
    }

    private static class TableRenderer extends ColoredTreeCellRenderer {

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
