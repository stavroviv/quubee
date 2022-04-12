package org.quebee.com;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import icons.DatabaseIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.panel.FromTables;
import org.quebee.com.panel.LinksPanel;
import org.quebee.com.panel.OrderPanel;

import javax.swing.*;
import java.awt.*;

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

        addFromTables(tabs);
        addLinksTable(tabs);
        tabs.addTab(new TabInfo(new JTable())).setText("Grouping").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
        addOrderTab(tabs);

        frame.setPreferredSize(new Dimension(900, 550));
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void addFromTables(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new FromTables().element).setText(FromTables.HEADER));
    }

    private void addLinksTable(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new LinksPanel().element)).setText(LinksPanel.HEADER);
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

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.setVisible(false);
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
