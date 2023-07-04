package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDTarget;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.*;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeNode;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

@Getter
public class FromTables extends QueryPanel {
    private static final String SELECTED_TABLES_TREE = "selectedTablesTree";
    private static final String TABLES_TREE_TABLE = "tablesTreeTable";

    private final String header = "Tables and Fields";
    private final JComponent component;

    private final TreeNode sourceRoot = new TreeNode(new TableElement("database_root"));
    private final TreeNode tablesRoot = new TreeNode(new TableElement("tables"));
    private final TreeNode cteRoot = new TreeNode(new TableElement("common table expressions"));

    private ListTreeTableModel databaseModel;

    public FromTables(MainPanel mainPanel) {
        super(mainPanel);
        var splitterLeft = new JBSplitter();
        splitterLeft.setProportion(0.3f);
        splitterLeft.setFirstComponent(databaseTables());

        var splitterRight = new JBSplitter();
        splitterRight.setFirstComponent(selectedTables());
        splitterRight.setSecondComponent(selectedFields());
        splitterLeft.setSecondComponent(splitterRight);

        this.component = splitterLeft;
        enableDragAndDrop();
    }

    private void enableDragAndDrop() {
        DnDManager.getInstance().registerSource(new MyDnDSource(tablesTreeTable), tablesTreeTable, mainPanel.getDisposable());
        DnDManager.getInstance().registerSource(new MyDnDSource(selectedTablesTree), selectedTablesTree, mainPanel.getDisposable());
        DnDManager.getInstance().registerTarget(new MyDnDTargetST(), selectedTablesTree, mainPanel.getDisposable());
        MyRowsDnDSupport.install(selectedFieldsTable, selectedFieldsModel, (event) -> {
            if (event.getAttachedObject() instanceof TreeNode) {
//                var p = event.getPoint();
//                var i = conditionTable.rowAtPoint(p);
//                addCondition((QBTreeNode) event.getAttachedObject(), i);
            }
        });
    }

    private class MyDnDTargetST implements DnDTarget {

        @Override
        public void drop(DnDEvent event) {
            if (event.getAttachedObject() instanceof TreeNode) {
                var node = (TreeNode) event.getAttachedObject();
                if (TABLES_TREE_TABLE.equals(node.getSource())) {
                    getPublisher(SelectedTableAddNotifier.class).onAction(node, null);
                }
            }
        }

        @Override
        public boolean update(DnDEvent event) {
            if (event.getAttachedObject() instanceof TreeNode) {
                var node = (TreeNode) event.getAttachedObject();
                if (SELECTED_TABLES_TREE.equals(node.getSource())) {
                    event.setDropPossible(false);
                    return false;
               }
            } else if (event.getAttachedObject() instanceof MyRowsDnDSupport.RowDragInfo) {
                event.setDropPossible(false);
                return false;
            }
            event.setDropPossible(true);
            return true;
        }
    }

    private class MyDnDSource extends AvailableFieldsTreeDnDSource {

        public MyDnDSource(TreeTable treeTable) {
            super(treeTable);
        }

        public boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin) {
            if (SELECTED_TABLES_TREE.equals(getTreeTableName())) {
                return true;
            }
            var value = (TreeNode) tablesTreeTable.getValueAt(tablesTreeTable.getSelectedRow(), 0);
            return !(value.equals(tablesRoot) || value.equals(cteRoot));
        }

        @Override
        public String getFieldDescription(TreeNode attachedObject) {
            return attachedObject.getUserObject().getDescription();
        }
    }

    @Override
    public void initListeners() {
        subscribe(ReloadDbTablesNotifier.class, this::setDatabaseTables);
        subscribe(ReloadCteTablesNotifier.class, this::setCteTables);
        subscribe(SelectedTableAddNotifier.class, this::addSelectedTable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SaveQueryDataNotifier.class, this::saveQueryData);
        subscribe(LoadQueryDataNotifier.class, this::loadQueryData);
        subscribe(NotifyRefreshAvailableTables.class, this::refreshAvailableTables);
        subscribe(AliasFullRemoveNotifier.class, this::onAliasRemove);
    }

    private void onAliasRemove(AliasElement aliasElement) {
        var row = ComponentUtils.getFirstRowByPredicate(x -> {
            var anObject = aliasElement.getAlias().get(mainPanel.getCurrentUnion());
            return x.getDescription().equals(anObject);
        }, selectedFieldsModel);
        getPublisher(SelectedFieldRemoveNotifier.class).onAction(row);
        selectedFieldsModel.removeRow(selectedFieldsModel.indexOf(row));
    }

    private void refreshAvailableTables() {
        getPublisher(RefreshAvailableTables.class).onAction(selectedTablesRoot.nodeToList());
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(selectedFieldsModel, union.getSelectedFieldsModel());
        ComponentUtils.loadTreeToTree(selectedTablesRoot, union.getSelectedTablesRoot());
        ComponentUtils.clearTree(selectedTablesRoot);
        ComponentUtils.clearTable(selectedFieldsModel);
    }

    private void loadQueryData(FullQuery fullQuery, String cteName, int unionNumber) {
        var cte = fullQuery.getCte(cteName);
        if (Objects.isNull(cte)) {
            return;
        }

        var union = cte.getUnion("" + unionNumber);
        union.getSelectedTablesRoot().nodeToList().forEach(x ->
                List.of(tablesRoot, cteRoot).forEach(y -> y.nodeToList().forEach(node -> {
                    var xName = x.getUserObject().getName();
                    var nodeName = node.getUserObject().getName();
                    if (nodeName.equals(xName)) {
                        getPublisher(SelectedTableAddNotifier.class).onAction(node, x.getUserObject().getAlias());
                    }
                }))
        );
        union.getSelectedFieldsModel().getItems().forEach(x ->
                getPublisher(SelectedFieldAddNotifier.class).onAction(x, false)
        );
        selectedTablesModel.reload();
    }

    private void removeSelectedTable(TreeNode node) {
        if (Objects.nonNull(node.getParent().getParent())) {
            return;
        }
        var index = selectedTablesRoot.getIndex(node);
        selectedTablesRoot.remove(node);
        selectedTablesModel.nodesWereRemoved(selectedTablesRoot, new int[]{index}, new Object[]{node});
        removeSelectedFieldsByTable(node);
    }

    private void removeSelectedFieldsByTable(TreeNode node) {
        var removeTableName = node.getUserObject().getDescription();
        for (var i = selectedFieldsModel.getItems().size() - 1; i >= 0; i--) {
            var name = selectedFieldsModel.getItem(i).getTableName();
            if (name.equals(removeTableName)) {
                selectedFieldsModel.removeRow(i);
            }
        }
    }

    private void addSelectedTable(TreeNode node, String alias) {
        var parent1 = node.getParent();
        if (Objects.nonNull(parent1) && parent1.equals(sourceRoot)) {
            return;
        }
        var parent2 = parent1.getParent();
        if (Objects.nonNull(parent2) && parent2.equals(sourceRoot)) {
            addSelectedTableNode(node, alias);
            return;
        }
        var parent = node.getParent();
        var parentUserObject = parent.getUserObject();
        if (selectedTablesRoot.nodeToList().stream()
                .noneMatch(x -> x.getUserObject().getName().equals(parentUserObject.getName()))) {
            addSelectedTableNode(parent, alias);
        }
        getPublisher(SelectedFieldAddNotifier.class).onAction(
                new TableElement(parentUserObject.getName(), node.getUserObject().getName()), true
        );
    }

    private void addSelectedField(TableElement node, boolean interactive) {
        selectedFieldsModel.addRow(node);
    }

    private void addSelectedTableNode(TreeNode node, String alias) {
        var userObject = node.getUserObject();
        var existNumber = 0;
        for (var i = 0; i < selectedTablesRoot.getChildCount(); i++) {
            var childAt = (TreeNode) selectedTablesRoot.getChildAt(i);
            if (childAt.getUserObject().getName().equals(node.getUserObject().getName())) {
                existNumber++;
            }
        }

        var newUserObject = new TableElement(userObject.getName(), userObject.getIcon());
        if (existNumber > 0 && Objects.isNull(alias)) {
            newUserObject.setAlias(newUserObject.getName() + existNumber);
        } else {
            newUserObject.setAlias(alias);
        }
        var newTableNode = ComponentUtils.addNodeWithChildren(node, newUserObject, selectedTablesRoot, selectedTablesModel);
        getPublisher(SelectedTableAfterAddNotifier.class).onAction(newTableNode);
    }

    private TreeNode selectedTablesRoot;
    private ListTreeTableModel selectedTablesModel;
    private TreeTable selectedTablesTree;

    public JComponent selectedTables() {
        selectedTablesRoot = new TreeNode(new TableElement("empty"));
        selectedTablesModel = new ListTreeTableModel(selectedTablesRoot, new ColumnInfo[]{
                new TreeColumnInfo("Tables")
        });

        selectedTablesTree = new TreeTable(selectedTablesModel);
//            selectedTablesTree = new TreeTable(selectedTablesModel); {
//            @Override
//            protected @NotNull ExpandableItemsHandler<TableCell> createExpandableItemsHandler() {
//                ExpandableItemsHandler<TableCell> expandableItemsHandler = super.createExpandableItemsHandler();
//                expandableItemsHandler.getExpandedItems();
//                return expandableItemsHandler;
//            }
//        };
        selectedTablesTree.setName(SELECTED_TABLES_TREE);
//        treeTable.getTree().setToggleClickCount(0);
        selectedTablesTree.setRootVisible(false);
        selectedTablesTree.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedTablesTree.setTreeCellRenderer(new TableElement.Renderer());
//        selectedFieldsModel.getListeners()
//        selectedTablesTree.addTreeWillExpandListener(new Tree.ExpandListener() {
//
//            public void nodeExpand(ExpandEvent event) {
//                // No children for the first node
//                if (!hasChildren(event.getItemId())) {
//                    tree.setChildrenAllowed(event.getItemId(), false);
//
//                } else {
//                    // Add a few new child nodes to the expanded node
//
//                    tree.addItem(childId);
//                    tree.setParent(childId, event.getItemId());
//
//
//                }
//            }
//        });
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        var decorator = ToolbarDecorator.createDecorator(selectedTablesTree);

        var actionRename = new AnActionButton("Rename Table", AllIcons.Actions.Edit) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var value = (TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
                var userObject = value.getUserObject();
                var input = userObject.getName();
                var dialog = new RenameDialogWrapper(input, "Rename table and its usages to:") {
                    @Override
                    protected void doOKAction() {
//                        info.setText(getResult().getText());
                        super.doOKAction();
                    }

                    @Override
                    protected String validateSource() {
                        var result = getResult().getText();
                        if (selectedTablesRoot.nodeToList().stream()
                                .map(x -> x.getUserObject().getName())
                                .anyMatch(x -> x.equals(result) && !x.equals(input))) {
                            return "Table with name <b>" + result + "</b> already exists";
                        }
                        return null;
                    }
                };
                dialog.show();
            }
        };
        actionRename.addCustomUpdater(activeTableUpdater());
        decorator.addExtraAction(actionRename);

        decorator.addExtraAction(new AnActionButton("Inner Query", DatabaseIcons.Database) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });

        decorator.setRemoveAction(button ->
                getPublisher(SelectedTableRemoveNotifier.class)
                        .onAction((TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0))
        );
        decorator.setRemoveActionUpdater(activeTableUpdater());

        selectedTablesTree.addMouseListener(new MouseAdapterDoubleClick(true) {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                var value = (TreeNode) selectedTablesTree.getValueAt(table.getSelectedRow(), 0);
                addSelectedFieldInteractive(value);
            }
        });
        return decorator.createPanel();
    }

    private void addSelectedFieldInteractive(TreeNode value) {
        if (Objects.isNull(value.getParent().getParent())) {
            value.nodeToList().forEach(this::addSelectedColumn);
            return;
        }
        addSelectedColumn(value);
    }

    private void addSelectedColumn(TreeNode child) {
        var columnObject = child.getUserObject();
        var tableObject = child.getParent().getUserObject();
        getPublisher(SelectedFieldAddNotifier.class).onAction(
                new TableElement(tableObject.getNameWithAlias(), columnObject.getName()), true
        );
    }

    private AnActionButtonUpdater activeTableUpdater() {
        return e -> {
            if (selectedTablesTree.getSelectedRow() == -1) {
                return false;
            }
            var value = (javax.swing.tree.TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
            return Objects.isNull(value.getParent().getParent());
        };
    }

    private ListTableModel<TableElement> selectedFieldsModel;
    private TableView<TableElement> selectedFieldsTable;

    public JComponent selectedFields() {
        var fieldInfo = new ColumnInfo<TableElement, String>("Fields") {

            @Override
            public @NotNull String valueOf(TableElement o) {
                return o.getDescription();
            }

            @Override
            public TableCellRenderer getRenderer(TableElement tableElement) {
                return new TableElement.TableRenderer(tableElement);
            }
        };

        selectedFieldsModel = new ListTableModel<>(new ColumnInfo[]{
                fieldInfo
        });
        selectedFieldsTable = new TableView<>(selectedFieldsModel);
//        selectedFieldsTable.getColumn().setDefaultRenderer(TableElement.class, new TableElement.TableRenderer());
        var decorator = ToolbarDecorator.createDecorator(selectedFieldsTable);
        decorator.setAddAction(button -> {
//            model.addRow(new TableElement());
            //    model.reload();
        });
//        decorator.addExtraAction(new AnActionButton("Copy", AllIcons.Actions.Copy) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e) {
//                System.out.println("test");
//            }
//        });
        decorator.setRemoveAction(button -> {
            getPublisher(SelectedFieldRemoveNotifier.class).onAction(selectedFieldsTable.getSelectedObject());
            TableUtil.removeSelectedItems(selectedFieldsTable);
        });
        return decorator.createPanel();
    }

    private TreeTable tablesTreeTable;

    public JComponent databaseTables() {
        sourceRoot.add(tablesRoot);
        databaseModel = new ListTreeTableModel(sourceRoot, new ColumnInfo[]{
                new TreeColumnInfo("Database")
        });
        tablesTreeTable = new TreeTable(databaseModel);
        tablesTreeTable.setName(TABLES_TREE_TABLE);
        tablesTreeTable.addMouseListener(new MouseAdapterDoubleClick(true) {
            @Override
            protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table) {
                getPublisher(SelectedTableAddNotifier.class)
                        .onAction((TreeNode) tablesTreeTable.getValueAt(table.getSelectedRow(), 0), null);
            }
        });
        tablesTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablesTreeTable.setTreeCellRenderer(new TableElement.Renderer());
        tablesTreeTable.setRootVisible(false);

        var decorator = ToolbarDecorator.createDecorator(tablesTreeTable);
        decorator.addExtraAction(new AnActionButton("Empty", IconUtil.getEmptyIcon(false)) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });

        return decorator.createPanel();
    }

    public void setDatabaseTables(DBTables dbStructure) {
        loadStructureToTree(dbStructure, tablesRoot, DatabaseIcons.Table);
        databaseModel.reload();
        tablesTreeTable.getTree().expandRow(sourceRoot.getIndex(tablesRoot));
    }

    private void setCteTables(DBTables dbStructure) {
        ComponentUtils.clearTree(cteRoot);
        if (dbStructure.getDbElements().isEmpty()) {
            var index = sourceRoot.getIndex(cteRoot);
            sourceRoot.remove(cteRoot);
            databaseModel.nodesWereRemoved(sourceRoot, new int[]{index}, new Object[]{cteRoot});
        } else {
            boolean expand;
            if (!sourceRoot.nodeToList().contains(cteRoot)) {
                sourceRoot.add(cteRoot);
                databaseModel.nodesWereInserted(sourceRoot, new int[]{sourceRoot.getChildCount() - 1});
                expand = true;
            } else {
                expand = tablesTreeTable.getTree().isExpanded(databaseModel.getChildCount(tablesRoot) + 1);
            }
            loadStructureToTree(dbStructure, cteRoot, DatabaseIcons.Tablespace);
            databaseModel.reload(cteRoot);
            if (expand) {
                tablesTreeTable.getTree().expandRow(databaseModel.getChildCount(tablesRoot) + 1);
            }
        }
    }

    private void loadStructureToTree(DBTables dbStructure, TreeNode root, Icon icon) {
        for (var entry : dbStructure.getDbElements().entrySet()) {
            var table = new TableElement(entry.getKey(), icon);
            var child = new TreeNode(table);
            root.add(child);
            for (var columnName : entry.getValue()) {
                var column = new TableElement(columnName, DatabaseIcons.Col);
                child.add(new TreeNode(column));
            }
        }
    }
}
