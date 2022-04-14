package org.quebee.com;

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
import org.quebee.com.panel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MainQuiBuiForm {

    private final DialogWrapper dialog;

    public MainQuiBuiForm(Project project) {
        var tabsCte = new JBTabsImpl(project, null, ApplicationManager.getApplication());
        var tabsUnion = new JBTabsImpl(project, null, ApplicationManager.getApplication());
        var unionPanel = new JPanel(new BorderLayout());
        var ctePanel = new JPanel(new BorderLayout());
        dialog = new DialogWrapper(project, false, DialogWrapper.IdeModalityType.PROJECT) {

            @Override
            protected @NotNull JComponent createCenterPanel() {

                for (int i = 0; i < 99; i++) {
                    tabsUnion.addTab(new TabInfo(new JPanel()))
                            .setText(String.valueOf(i))
                            .setIcon(DatabaseIcons.Table);
                    tabsCte.addTab(new TabInfo(new JPanel()))
                            .setText("Test table name" + i)
                            .setIcon(DatabaseIcons.Package);
                }

                tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);
                tabsCte.setPreferredSize(JBUI.size(55, 200));
                tabsUnion.getPresentation().setTabsPosition(JBTabsPosition.right);

                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BorderLayout());

                JPanel panelCurrent = new JPanel();
                panelCurrent.setLayout(new BorderLayout());
                final JBTabsImpl tabs = new JBTabsImpl(project, null, ApplicationManager.getApplication());
                addQueryTabs(tabs);
                panelCurrent.add(tabs, BorderLayout.CENTER);

                unionPanel.add(getEmptyPanel(), BorderLayout.NORTH);
                unionPanel.add(tabsUnion, BorderLayout.CENTER);
                unionPanel.setPreferredSize(JBUI.size(55, 200));
                panelCurrent.add(unionPanel, BorderLayout.EAST);

                mainPanel.add(panelCurrent, BorderLayout.CENTER);

                ctePanel.add(getEmptyPanel(), BorderLayout.NORTH);
                ctePanel.add(tabsCte, BorderLayout.CENTER);
//                ctePanel.setPreferredSize(JBUI.size(55, 200));

                mainPanel.add(ctePanel, BorderLayout.EAST);
                return mainPanel;
            }


            @NotNull
            private JPanel getEmptyPanel() {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBorder(JBUI.Borders.emptyTop(17));
                return emptyPanel;
            }

            @Override
            protected Action @NotNull [] createActions() {
                return ArrayUtil.append(super.createActions(), new AbstractAction("hide") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showHideEastPanels(ctePanel, unionPanel);
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
        showHideEastPanels(ctePanel, unionPanel);
    }

    private void addQueryTabs(JBTabsImpl tabs) {
        List.of(
                new FromTables(),
                new LinksPanel(),
                new GroupingPanel(),
                new ConditionsPanel(),
                new UnionAliasesPanel(),
                new OrderPanel()
        ).forEach(queryTab ->
                tabs.addTab(new TabInfo(queryTab.getComponent()).setText(queryTab.getHeader()))
        );
    }

    private void showHideEastPanels(JPanel ctePanel, JPanel unionPanel) {
        ctePanel.setVisible(!ctePanel.isVisible());
        unionPanel.setVisible(!unionPanel.isVisible());
    }

    public void show() {
        dialog.show();
    }
}
