package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.tabs.impl.TabLabel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.qpart.OneCte;
import org.quebee.com.util.DefaultDialogWrapper;
import org.quebee.com.util.JetSelectMessages;
import org.quebee.com.util.RenameDialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.quebee.com.panel.OrderPanel.ORDER_PANEL_HEADER;
import static org.quebee.com.util.Constants.BODY;

public class MainPanel extends DefaultDialogWrapper {

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
        super(null, false);
        setModal(false);
        setResizable(true);
        setTitle("Jet Select");
        setSize(900, 550);

        this.fullQuery = fullQuery;

        initListeners();
        loadCte();
        queryPanels.forEach(QueryPanel::initListeners);
    }

    @Override
    protected void dispose() {
        JetSelectMessages.removeTopics(id);
        super.dispose();
    }

    private int maxCte;

    @Override
    protected JComponent createCenterPanel() {
        tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);

        setCtePopupMenu();

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
        queryTabs.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                setPanelsVisible();
            }
        });

        var horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(unionPanel);
        horizontalBox.add(ctePanel);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(queryTabs, BorderLayout.CENTER);
        mainPanel.add(horizontalBox, BorderLayout.EAST);
        return mainPanel;
    }

    private void setCtePopupMenu() {
        var group = new DefaultActionGroup();
        var actionAdd = new AnAction("Add", "Add", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                //var name = "Table_expression_" + (maxUnion + 1);
                addCteInteractive(((TabLabel) e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)).getInfo());
               // maxUnion++;
            }
        };
        group.add(actionAdd);
        var actionRemove = new AnAction("Remove", "Remove", AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var data = (TabLabel) e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
                if (Objects.isNull(data)) {
                    return;
                }
                removeCte(data.getInfo());
            }
        };
        group.add(actionRemove);

        var actionRename = new AnAction("Rename...", "Rename...", null) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var data = (TabLabel) e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
                if (Objects.isNull(data)) {
                    return;
                }
                renameCte(data.getInfo());
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                disabledForLast(e);
            }
        };
        group.add(actionRename);

        var actionMoveUp = new AnAction("Up", "Up", AllIcons.Actions.MoveUp) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                var data = (TabLabel) e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
                if (Objects.isNull(data)) {
                    return;
                }
                e.getPresentation().setEnabled(tabsCte.getTabs().indexOf(data.getInfo()) != 0);
            }
        };
        group.add(actionMoveUp);

        var actionMoveDown = new AnAction("Down", "Down", AllIcons.Actions.MoveDown) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                disabledForLast(e);
            }
        };
        group.add(actionMoveDown);

        tabsCte.setPopupGroup(group, "JetSelectCtePopup", true);
    }

    private void disabledForLast(@NotNull AnActionEvent e) {
        var data = (TabLabel) e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
        if (Objects.isNull(data)) {
            return;
        }
        boolean enabled = tabsCte.getTabs().indexOf(data.getInfo()) != tabsCte.getTabs().size() - 1;
        e.getPresentation().setEnabled(enabled);
    }

    private void initListeners() {
        tabsCte.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (dataIsLoading) {
                    return;
                }
                if (Objects.nonNull(oldSelection)) {
                    var text = getText(oldSelection);
                    JetSelectMessages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery, text, 0);
                    JetSelectMessages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, text);
                }
                var text = getText(newSelection);
                loadUnions(text);
                loadCteTree(text);
                JetSelectMessages.getPublisher(id, LoadQueryDataNotifier.class).onAction(fullQuery, text, 0);
                JetSelectMessages.getPublisher(id, LoadQueryCteDataNotifier.class).onAction(fullQuery, text);
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

    @NotNull
    private String getText(TabInfo oldSelection) {
//        var tab_name = oldSelection.getComponent().getClientProperty("tab_name");
//        if (tab_name != null) {
//            return tab_name.toString();
//        }
        return oldSelection.getText();
    }

    private void loadCteTree(String text) {
        var commonExpressions = new DBTables();
        for (var cteName : fullQuery.getCteNames()) {
            if (text.equals(cteName)) {
                break;
            }
            var cte = fullQuery.getCte(cteName);
            var columns = new ArrayList<String>();
            for (var item : cte.getAliasTable().getItems()) {
                columns.add(item.getAliasName());
            }
            commonExpressions.getDbElements().put(cteName, columns);
        }
        JetSelectMessages.getPublisher(id, ReloadCteTablesNotifier.class).onAction(commonExpressions);
    }

    private void loadCte() {
        dataIsLoading = true;
        fullQuery.getCteNames().forEach(this::newTabInfo);
        renameLast();
        setPanelsVisible();
        loadUnions(fullQuery.getFirstCte());
        dataIsLoading = false;
    }

    private void renameLast() {
        tabsCte.getTabs().get(tabsCte.getTabs().size() - 1).setText(BODY);
    }

    @NotNull
    private TabInfo newTabInfo(String name) {
        var tabInfo = tabsCte.addTab(new TabInfo(Box.createVerticalBox()))
                .setText(name)
                .setIcon(DatabaseIcons.Package);
        tabInfo.getComponent().putClientProperty("tab_name", name);
        return tabInfo;
    }

    private void setPanelsVisible() {
        unionPanel.setVisible(tabsUnion.getTabCount() > 1);
        var selectedInfo = queryTabs.getSelectedInfo();
        if (Objects.nonNull(selectedInfo) && selectedInfo.getText().equals(ORDER_PANEL_HEADER)
                && getCurrentCte().getUnionMap().size() > 1) {
            unionPanel.setVisible(false);
        }
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

    private List<QueryPanel> queryPanels;

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
        queryPanels = List.of(
                new FromTables(this),
                new JoinsPanel(this),
                new GroupingPanel(this),
                new ConditionsPanel(this),
                new UnionAliasesPanel(this),
                new OrderPanel(this)
        );
        queryPanels.forEach(queryTab ->
                tabs.addTab(new TabInfo(queryTab.getComponent())
                        .setText(queryTab.getHeader())
                        .setTooltipText(queryTab.getTooltipText()))
        );
    }

    public String getCurrentUnion() {
        var tabInfo = tabsUnion.getSelectedInfo();
        return "Union " + (Objects.isNull(tabInfo) ? "0" : tabInfo.getText());
    }

    public String getCurrentCteName() {
        var tabInfo = tabsCte.getSelectedInfo();
        return Objects.isNull(tabInfo) ? "" : tabInfo.getText();
    }

    public OneCte getCurrentCte() {
        return fullQuery.getCte(getCurrentCteName());
    }

    public void addUnion(int index) {
        fullQuery.getCte(getCurrentCteName()).addUnion(index);
        tabsUnion.addTab(new TabInfo(Box.createVerticalBox()).setText("" + index).setIcon(DatabaseIcons.Table));
        setPanelsVisible();
    }

    public void removeUnion(int index) {
        fullQuery.getCte(getCurrentCteName()).removeUnion(index);
        tabsUnion.getTabs().stream()
                .filter(x -> x.getText().equals(String.valueOf(index)))
                .findAny().ifPresent(x -> {
                    dataIsLoading = true;
                    tabsUnion.removeTab(x);
                    dataIsLoading = false;
                });
        setPanelsVisible();
    }

    public void addCteInteractive(TabInfo currentTab) {
        var name = "Table_expression_" + (maxCte + 1);
        fullQuery.addCte(name);
        var info = newTabInfo(name);
        renamePrevLast(name);
        renameLast();
        tabsCte.select(info, true);
        queryTabs.select(queryTabs.getTabAt(0), true);
        maxCte++;
    }

    private void renamePrevLast(String name) {
        tabsCte.getTabs().get(tabsCte.getTabs().size() - 2).setText(name);
    }

    private void removeCte(TabInfo tabInfo) {
        fullQuery.removeCte(tabInfo.getText());
        tabsCte.removeTab(tabInfo);
    }

    private void renameCte(TabInfo info) {
        var input = info.getText();
        var dialog = new RenameDialogWrapper(input, "Rename common table expression and its usages to:") {
            @Override
            protected void doOKAction() {
                info.setText(getResult().getText());
                super.doOKAction();
            }

            @Override
            protected String validateSource() {
                var tabNames = tabsCte.getTabs().stream()
                        .map(TabInfo::getText)
                        .filter(text -> !text.equals(input))
                        .collect(Collectors.toList());
                var text = getResult().getText();
                if (tabNames.contains(text)) {
                    return "Common table expression with name <b>" + text + "</b> already exists";
                }
                return null;
            }
        };
        dialog.show();
    }

    public void activateNewUnion(String newUnion) {
        queryTabs.select(queryTabs.getTabAt(0), true);
        tabsUnion.getTabs().stream()
                .filter(x -> x.getText().equals(newUnion.replace("Union ", "")))
                .findAny()
                .ifPresent(tabInfo -> tabsUnion.select(tabInfo, true));
    }
}
