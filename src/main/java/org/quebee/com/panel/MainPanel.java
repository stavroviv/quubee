package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.JBUI;
import icons.DatabaseIcons;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

import static org.quebee.com.notifier.LoadQueryDataNotifier.LOAD_QUERY_DATA;
import static org.quebee.com.notifier.SaveQueryDataNotifier.SAVE_QUERY_DATA;

public class MainPanel {

    private final DialogWrapper dialog;
    private final JBTabsImpl tabsCte;
    private final JBTabsImpl tabsUnion;
    private final JPanel unionPanel;
    private final JPanel ctePanel;
    private final FullQuery fullQuery;

    public MainPanel(FullQuery fullQuery) {
        this.fullQuery = fullQuery;
        this.tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        this.tabsUnion = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        this.unionPanel = new JPanel(new BorderLayout());
        this.ctePanel = new JPanel(new BorderLayout());
        this.dialog = new DialogWrapper(null, false, DialogWrapper.IdeModalityType.PROJECT) {

            @Override
            protected @NotNull JComponent createCenterPanel() {

                tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);
                tabsCte.setPreferredSize(JBUI.size(55, 200));
                tabsUnion.getPresentation().setTabsPosition(JBTabsPosition.right);

                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BorderLayout());

                JPanel panelCurrent = new JPanel();
                panelCurrent.setLayout(new BorderLayout());
                final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
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
            protected void createDefaultActions() {
                super.createDefaultActions();
                init();
            }
        };
        dialog.setResizable(true);
        dialog.setTitle("Jet Select");
        dialog.setSize(900, 550);
        initListeners();
        loadCte();
    }

    private void initListeners() {
        tabsCte.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                Messages.getPublisher(SAVE_QUERY_DATA).onAction(fullQuery, oldSelection.getText(), 0);
                Messages.getPublisher(LOAD_QUERY_DATA).onAction(fullQuery, newSelection.getText(), 0);
                loadUnions(newSelection.getText());
            }
        });
        tabsUnion.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                var selectedCte = tabsCte.getSelectedInfo();
                if (Objects.isNull(selectedCte)) {
                    return;
                }
                Messages.getPublisher(SAVE_QUERY_DATA).onAction(
                        fullQuery, selectedCte.getText(), tabsUnion.getTabs().indexOf(oldSelection)
                );
                Messages.getPublisher(LOAD_QUERY_DATA).onAction(
                        fullQuery, selectedCte.getText(), tabsUnion.getTabs().indexOf(newSelection)
                );
            }
        });
    }

    private void loadCte() {
        var cte = fullQuery.getCteNames();
        cte.forEach(x -> tabsCte.addTab(new TabInfo(new JPanel()))
                .setText(x)
                .setIcon(DatabaseIcons.Package));
        tabsCte.setVisible(tabsCte.getTabCount() > 1);
    }

    private void loadUnions(String x) {
        var cte = fullQuery.getCte(x);
        unionPanel.setVisible(false);
        if (Objects.isNull(cte)) {
            return;
        }
        var unionMap = cte.getUnionMap().keySet();
        tabsUnion.removeAllTabs();
        unionMap.forEach(y -> tabsUnion.addTab(new TabInfo(new JPanel()))
                .setText(y)
                .setIcon(DatabaseIcons.Table));
        unionPanel.setVisible(tabsUnion.getTabCount() > 1);
    }

    private void addQueryTabs(JBTabsImpl tabs) {
        List.of(
                new FromTables(),
                new JoinsPanel(),
                new GroupingPanel(),
                new ConditionsPanel(),
                new UnionAliasesPanel(),
                new OrderPanel()
        ).forEach(queryTab ->
                tabs.addTab(new TabInfo(queryTab.getComponent()).setText(queryTab.getHeader()))
        );
    }

    public void show() {
        dialog.show();
    }
}
