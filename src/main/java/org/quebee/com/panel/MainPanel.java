package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.notifier.LoadQueryCteDataNotifier;
import org.quebee.com.notifier.LoadQueryDataNotifier;
import org.quebee.com.notifier.SaveQueryCteDataNotifier;
import org.quebee.com.notifier.SaveQueryDataNotifier;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.JetSelectMessages;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainPanel extends DialogWrapper {

    private JBTabsImpl queryTabs;
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
        abstractQueryPanels.forEach(AbstractQueryPanel::initListeners);
//        JetSelectMessages.getPublisher(id, LoadAllCteDataNotifier.class).onAction(fullQuery);
    }

    @Override
    protected void dispose() {
        JetSelectMessages.removeTopics(id);
        super.dispose();
    }

    private int maxUnion;

    @Override
    protected JComponent createCenterPanel() {
        tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);

        var group = new DefaultActionGroup();
        var actionAdd = new AnAction("Add", "Add", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var name = "Table_expression_" + (maxUnion + 1);
                addCte(name);
                maxUnion++;
            }
        };
        group.add(actionAdd);
        var actionRemove = new AnAction("Remove", "Remove", AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println();
            }
        };
        group.add(actionRemove);
        var actionMoveUp = new AnAction("Up", "Up", AllIcons.Actions.MoveUp) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println();
            }
        };
        group.add(actionMoveUp);
        var actionMoveDown = new AnAction("Down", "Down", AllIcons.Actions.MoveDown) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println();
            }
        };
        group.add(actionMoveDown);

        tabsCte.setPopupGroup(group, "Test", true);


        ctePanel = Box.createVerticalBox();
        ctePanel.add(Box.createVerticalStrut(25));
        ctePanel.add(tabsCte);

        tabsUnion = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        tabsUnion.getPresentation().setTabsPosition(JBTabsPosition.right);
        unionPanel = Box.createVerticalBox();
        unionPanel.add(Box.createVerticalStrut(25));
        unionPanel.add(tabsUnion);

        queryTabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        addQueryTabs(queryTabs);

        var horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(unionPanel);
        horizontalBox.add(ctePanel);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(queryTabs, BorderLayout.CENTER);
        mainPanel.add(horizontalBox, BorderLayout.EAST);
        return mainPanel;
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
        init();
    }

    private void initListeners() {
//        queryTabs.addListener(new TabsListener() {
//            @Override
//            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
//                if (dataIsLoading) {
//                    return;
//                }
//                if (Objects.isNull(newSelection)) {
//                    return;
//                }
//                if (newSelection.getText().equals(CtePanel.header())) {
//                    ctePanel.setVisible(false);
//                    unionPanel.setVisible(false);
//                } else {
//                    setPanelsVisible();
//                }
//            }
//        });
        tabsCte.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (dataIsLoading) {
                    return;
                }
                if (Objects.nonNull(oldSelection)) {
                    JetSelectMessages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery, oldSelection.getText(), 0);
                    JetSelectMessages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, oldSelection.getText());
                }
                loadUnions(newSelection.getText());
                JetSelectMessages.getPublisher(id, LoadQueryDataNotifier.class).onAction(fullQuery, newSelection.getText(), 0);
                JetSelectMessages.getPublisher(id, LoadQueryCteDataNotifier.class).onAction(fullQuery, newSelection.getText());
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
                    JetSelectMessages.getPublisher(id, SaveQueryDataNotifier.class).onAction(
                            fullQuery, selectedCte.getText(), tabsUnion.getTabs().indexOf(oldSelection)
                    );
                }
                JetSelectMessages.getPublisher(id, LoadQueryDataNotifier.class).onAction(
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
        setPanelsVisible();
        loadUnions(fullQuery.getFirstCte());
        dataIsLoading = false;
    }

    private void setPanelsVisible() {
//        ctePanel.setVisible(tabsCte.getTabCount() > 1);
        unionPanel.setVisible(tabsUnion.getTabCount() > 1);
    }

    private boolean dataIsLoading;

    private void loadUnions(String cteName) {
        dataIsLoading = true;
        tabsUnion.removeAllTabs();
        var cte = fullQuery.getCte(cteName);
        cte.getUnionMap().keySet().forEach(y ->
                tabsUnion.addTab(new TabInfo(Box.createVerticalBox())).setText(y).setIcon(DatabaseIcons.Table)
        );
        setPanelsVisible();
        dataIsLoading = false;
    }

    private List<AbstractQueryPanel> abstractQueryPanels;

    public void saveQueryPart() {
        if (Objects.isNull(tabsCte.getSelectedInfo()) || Objects.isNull(tabsUnion.getSelectedInfo())) {
            return;
        }
        JetSelectMessages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery,
                tabsCte.getSelectedInfo().getText(), Integer.parseInt(tabsUnion.getSelectedInfo().getText())
        );
        JetSelectMessages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, tabsCte.getSelectedInfo().getText());
    }

    private void addQueryTabs(JBTabsImpl tabs) {
        abstractQueryPanels = List.of(
                new FromTables(this),
                new JoinsPanel(this),
                new GroupingPanel(this),
                new ConditionsPanel(this),
                new UnionAliasesPanel(this),
                new OrderPanel(this)
//                new CtePanel(this)
        );
        abstractQueryPanels.forEach(queryTab ->
                tabs.addTab(new TabInfo(queryTab.getComponent())
                        .setText(queryTab.getHeader())
                        .setTooltipText(queryTab.getTooltipText()))
        );
    }

    public String getCurrentUnion() {
        var tabInfo = tabsUnion.getSelectedInfo();
        return "Union " + (Objects.isNull(tabInfo) ? "0" : tabInfo.getText());
    }

    public String getCurrentCte() {
        var tabInfo = tabsCte.getSelectedInfo();
        return Objects.isNull(tabInfo) ? "" : tabInfo.getText();
    }

    public void addUnion(int index) {
        fullQuery.getCte(getCurrentCte()).addUnion(index);
        tabsUnion.addTab(new TabInfo(Box.createVerticalBox()).setText("" + index).setIcon(DatabaseIcons.Table));
        setPanelsVisible();
    }

    public void removeUnion(int index) {
        fullQuery.getCte(getCurrentCte()).removeUnion(index);
        tabsUnion.getTabs().stream()
                .filter(x -> x.getText().equals(String.valueOf(index)))
                .findAny().ifPresent(x -> {
                    dataIsLoading = true;
                    tabsUnion.removeTab(x);
                    dataIsLoading = false;
                });
        setPanelsVisible();
    }

    public void addCte(String name) {
        fullQuery.addCte(name);
        var info = new TabInfo(Box.createVerticalBox());
        tabsCte.addTab(info).setText(name).setIcon(DatabaseIcons.Package);
        tabsCte.select(info, true);
        queryTabs.select(queryTabs.getTabAt(0), true);
    }
}
