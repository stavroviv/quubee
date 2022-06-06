package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import icons.DatabaseIcons;
import lombok.Getter;
import org.quebee.com.notifier.LoadQueryCteDataNotifier;
import org.quebee.com.notifier.LoadQueryDataNotifier;
import org.quebee.com.notifier.SaveQueryCteDataNotifier;
import org.quebee.com.notifier.SaveQueryDataNotifier;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainPanel extends DialogWrapper {

    private JBTabsImpl tabsCte;
    private JBTabsImpl tabsUnion;
    private Box unionPanel;
    private Box ctePanel;
    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final FullQuery fullQuery;

    public MainPanel(FullQuery fullQuery) {
        super(null, false, DialogWrapper.IdeModalityType.PROJECT);
        setModal(false);
        setResizable(true);
        setTitle("Jet Select");
        setSize(900, 550);

        this.fullQuery = fullQuery;

        initListeners();
        loadCte();
        abstractQueryPanels.forEach(queryComponent -> queryComponent.initListeners(getDisposable()));
    }

    @Override
    protected void dispose() {
        Messages.removeTopics(id);
        super.dispose();
    }

    @Override
    protected JComponent createCenterPanel() {
        tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);

        ctePanel = Box.createVerticalBox();
        ctePanel.add(Box.createVerticalStrut(25));
        ctePanel.add(tabsCte);

        tabsUnion = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        tabsUnion.getPresentation().setTabsPosition(JBTabsPosition.right);
        unionPanel = Box.createVerticalBox();
        unionPanel.add(Box.createVerticalStrut(25));
        unionPanel.add(tabsUnion);

        var tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        addQueryTabs(tabs);

        var horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(unionPanel);
        horizontalBox.add(ctePanel);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabs, BorderLayout.CENTER);
        mainPanel.add(horizontalBox, BorderLayout.EAST);
        return mainPanel;
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
        init();
    }

    private void initListeners() {
        tabsCte.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (dataIsLoading) {
                    return;
                }
                if (Objects.nonNull(oldSelection)) {
                    Messages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery, oldSelection.getText(), 0);
                    Messages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, oldSelection.getText());
                }
                loadUnions(newSelection.getText());
                Messages.getPublisher(id, LoadQueryDataNotifier.class).onAction(fullQuery, newSelection.getText(), 0);
                Messages.getPublisher(id, LoadQueryCteDataNotifier.class).onAction(fullQuery, newSelection.getText());
            }
        });
        tabsUnion.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (dataIsLoading) {
                    return;
                }
                var selectedCte = tabsCte.getSelectedInfo();
                if (Objects.isNull(selectedCte)) {
                    return;
                }
                if (Objects.nonNull(oldSelection)) {
                    Messages.getPublisher(id, SaveQueryDataNotifier.class).onAction(
                            fullQuery, selectedCte.getText(), tabsUnion.getTabs().indexOf(oldSelection)
                    );
                }
                Messages.getPublisher(id, LoadQueryDataNotifier.class).onAction(
                        fullQuery, selectedCte.getText(), tabsUnion.getTabs().indexOf(newSelection)
                );
            }
        });
    }

    private void loadCte() {
        dataIsLoading = true;
        fullQuery.getCteNames().forEach(x ->
                tabsCte.addTab(new TabInfo(Box.createVerticalBox())).setText(x).setIcon(DatabaseIcons.Package)
        );
        ctePanel.setVisible(tabsCte.getTabCount() > 1);
        loadUnions(fullQuery.getFirstCte());
        dataIsLoading = false;
    }

    private boolean dataIsLoading;

    private void loadUnions(String cteName) {
        dataIsLoading = true;
        tabsUnion.removeAllTabs();
        var cte = fullQuery.getCte(cteName);
        cte.getUnionMap().keySet().forEach(y ->
                tabsUnion.addTab(new TabInfo(Box.createVerticalBox())).setText(y).setIcon(DatabaseIcons.Table)
        );
        unionPanel.setVisible(tabsUnion.getTabCount() > 1);
        dataIsLoading = false;
    }

    private List<AbstractQueryPanel> abstractQueryPanels;

    public void saveQueryPart() {
        if (Objects.isNull(tabsCte.getSelectedInfo()) || Objects.isNull(tabsUnion.getSelectedInfo())) {
            return;
        }
        Messages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery,
                tabsCte.getSelectedInfo().getText(), Integer.parseInt(tabsUnion.getSelectedInfo().getText())
        );
        Messages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, tabsCte.getSelectedInfo().getText());
    }

    private void addQueryTabs(JBTabsImpl tabs) {
        abstractQueryPanels = List.of(
                new FromTables(this),
                new JoinsPanel(this),
                new GroupingPanel(this),
                new ConditionsPanel(this),
                new UnionAliasesPanel(this),
                new OrderPanel(this)
        );
        abstractQueryPanels.forEach(queryTab ->
                tabs.addTab(new TabInfo(queryTab.getComponent()).setText(queryTab.getHeader()))
        );
    }

    public String currentUnion() {
        TabInfo tabInfo = tabsUnion.getSelectedInfo();
        return "Union " + (Objects.isNull(tabInfo) ? "0" : tabInfo.getText());
    }
}
