package org.quebee.com;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;
import icons.DatabaseIcons;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.panel.FromTables;
import org.quebee.com.panel.LinksPanel;
import org.quebee.com.panel.OrderPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainQuiBuiForm {

    final DialogWrapper dialog;

    public MainQuiBuiForm(Project project) {
        final JBTabsImpl tabsCte = new JBTabsImpl(project, null, ApplicationManager.getApplication());
        final JBTabsImpl tabsUnion = new JBTabsImpl(project, null, ApplicationManager.getApplication());
        dialog = new DialogWrapper(project, false, DialogWrapper.IdeModalityType.PROJECT) {

            @Override
            protected @NotNull JComponent createCenterPanel() {

                for (int i = 0; i < 200; i++) {
                    tabsUnion.addTab(new TabInfo(new JPanel()))
                            .setText(String.valueOf(i))
                            .setIcon(DatabaseIcons.Table);
                    tabsCte.addTab(new TabInfo(new JPanel()))
                            .setText("Test table name" + i)
                            .setIcon(DatabaseIcons.Package);
                }

                tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);

                tabsUnion.getPresentation().setTabsPosition(JBTabsPosition.right);
                tabsUnion.setBorder(JBUI.Borders.emptyLeft(0));
//                tabsUnion.setBorder(JBUI.Borders.emptyTop(90));

                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());

                JPanel panelCurrent = new JPanel();
                panelCurrent.setLayout(new BorderLayout());
                final JBTabsImpl tabs = new JBTabsImpl(project, null, ApplicationManager.getApplication());
                addFromTables(tabs);
                addLinksTable(tabs);
                tabs.addTab(new TabInfo(new JPanel())).setText("Grouping").setActions(new DefaultActionGroup(), null);
                tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
                tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
                addOrderTab(tabs);

                panelCurrent.add(tabs, BorderLayout.CENTER);
                JPanel unionPanel = new JPanel(new BorderLayout());
                unionPanel.setBorder(JBUI.Borders.emptyLeft(0));
                JPanel comp = new JPanel();
                comp.setBorder(JBUI.Borders.emptyTop(17));
                unionPanel.add(comp, BorderLayout.NORTH);
                unionPanel.add(tabsUnion, BorderLayout.CENTER);
                panelCurrent.add(unionPanel, BorderLayout.EAST);
                panel.add(panelCurrent, BorderLayout.CENTER);
                panel.add(tabsCte, BorderLayout.EAST);
                return panel;
            }

            @Override
            protected Action @NotNull [] createActions() {
                return ArrayUtil.append(super.createActions(), new AbstractAction("hide") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tabsCte.setVisible(!tabsCte.isVisible());
                    }
                });
            }

            @Override
            protected void createDefaultActions() {
                super.createDefaultActions();
                init();
            }
        };
        dialog.setResizable(true);
        dialog.setTitle("Qui Bui");
        dialog.setSize(900, 550);
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

    public void show() {
        dialog.show();
    }
}
