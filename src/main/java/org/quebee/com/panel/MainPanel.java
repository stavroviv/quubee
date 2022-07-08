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
        abstractQueryPanels.forEach(AbstractQueryPanel::initListeners);
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
                var name = "Table_expression_" + (maxUnion + 1);
                addCte(name);
                maxUnion++;
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
                    JetSelectMessages.getPublisher(id, SaveQueryDataNotifier.class).onAction(fullQuery, oldSelection.getText(), 0);
                    JetSelectMessages.getPublisher(id, SaveQueryCteDataNotifier.class).onAction(fullQuery, oldSelection.getText());
                }
                loadUnions(newSelection.getText());
                loadCteTree(newSelection.getText());
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
}
