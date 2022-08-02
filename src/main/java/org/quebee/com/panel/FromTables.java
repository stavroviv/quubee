package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.ide.dnd.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.*;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MyRowsDnDSupport;
import org.quebee.com.util.RenameDialogWrapper;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

@Getter
public class FromTables extends QueryPanel {
    private final String header = "Tables and Fields";
    private final JComponent component;

    private final QBTreeNode sourceRoot = new QBTreeNode(new TableElement("database_root"));
    private final QBTreeNode tablesRoot = new QBTreeNode(new TableElement("tables"));
    private final QBTreeNode cteRoot = new QBTreeNode(new TableElement("common table expressions"));

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
        DnDManager.getInstance().registerSource(new MyDnDSource(), tablesTreeTable, mainPanel.getDisposable());
        DnDManager.getInstance().registerTarget(new MyDnDTargetST(), selectedTablesTree, mainPanel.getDisposable());
//        DnDManager.getInstance().registerTarget(new MyDnDTargetSF(), selectedFieldsTable, mainPanel.getDisposable());
        MyRowsDnDSupport.install(selectedFieldsTable, selectedFieldsModel, null, (event) -> {
            if (event.getAttachedObject() instanceof QBTreeNode) {
//                var p = event.getPoint();
//                var i = conditionTable.rowAtPoint(p);
//                addCondition((QBTreeNode) event.getAttachedObject(), i);
            }
        });
    }

    private class MyDnDTargetST implements DnDTarget {

        @Override
        public void drop(DnDEvent event) {

        }

        @Override
        public boolean update(DnDEvent event) {
            event.setDropPossible(true);
            return true;
        }
    }

    private class MyDnDSource implements DnDSource {

        public boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin) {
            return true;
        }

        public @NotNull DnDDragStartBean startDragging(DnDAction action, @NotNull Point dragOrigin) {
            var value = (QBTreeNode) tablesTreeTable.getValueAt(tablesTreeTable.getSelectedRow(), 0);
            return new DnDDragStartBean(value, dragOrigin);
        }

        public @NotNull Pair<Image, Point> createDraggedImage(DnDAction action, Point dragOrigin, @NotNull DnDDragStartBean bean) {
            var c = new SimpleColoredComponent();
            c.setForeground(RenderingUtil.getForeground(tablesTreeTable));
            c.setBackground(RenderingUtil.getBackground(tablesTreeTable));
            c.setIcon(DatabaseIcons.Col);

            var attachedObject = (QBTreeNode) bean.getAttachedObject();
            c.append(" +" + attachedObject.getUserObject().getDescription(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            var size = c.getPreferredSize();
            c.setSize(size);
            var image = UIUtil.createImage(c, size.width, size.height, 2);
            c.setOpaque(false);
            var g = image.createGraphics();
            c.paint(g);
            g.dispose();

            return Pair.create(image, new Point(-20, 5));
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
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(selectedFieldsModel, union.getSelectedFieldsModel());
        ComponentUtils.loadTreeToTree(selectedTablesRoot, union.getSelectedTablesRoot());
        ComponentUtils.clearTree(selectedTablesRoot);
        ComponentUtils.clearTable(selectedFieldsModel);
    }

    private void loadQueryData(FullQuery fullQuery, String cteName, int i1) {
        var cte = fullQuery.getCte(cteName);
        if (Objects.isNull(cte)) {
            return;
        }

        var union = cte.getUnion("" + i1);
        // FIXME  add getting all model from CTE model
        union.getSelectedTablesRoot().nodeToList().forEach(x ->
                {
                    tablesRoot.nodeToList().forEach(node -> {
                        var userObject = x.getUserObject();
                        if (node.getUserObject().getName().equals(userObject.getName())) {
                            getPublisher(SelectedTableAddNotifier.class).onAction(node, x.getUserObject().getAlias());
                        }
                    });
                    // FIXME
                    cteRoot.nodeToList().forEach(node -> {
                        var userObject = x.getUserObject();
                        if (node.getUserObject().getName().equals(userObject.getName())) {
                            getPublisher(SelectedTableAddNotifier.class).onAction(node, x.getUserObject().getAlias());
                        }
                    });
                }
        );
        union.getSelectedFieldsModel().getItems().forEach(x ->
                getPublisher(SelectedFieldAddNotifier.class).onAction(x, false)
        );
        selectedTablesModel.reload();
    }

    private void removeSelectedTable(QBTreeNode node) {
        if (Objects.nonNull(node.getParent().getParent())) {
            return;
        }
        var index = selectedTablesRoot.getIndex(node);
        selectedTablesRoot.remove(node);
        selectedTablesModel.nodesWereRemoved(selectedTablesRoot, new int[]{index}, new Object[]{node});
        removeSelectedFieldsByTable(node);
    }

    private void removeSelectedFieldsByTable(QBTreeNode node) {
        var removeTableName = node.getUserObject().getDescription();
        for (var i = selectedFieldsModel.getItems().size() - 1; i >= 0; i--) {
            var name = selectedFieldsModel.getItem(i).getTableName();
            if (name.equals(removeTableName)) {
                selectedFieldsModel.removeRow(i);
            }
        }
    }

    private void addSelectedTable(QBTreeNode node, String alias) {
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

    private void addSelectedTableNode(QBTreeNode node, String alias) {
        var userObject = node.getUserObject();
        var existNumber = 0;
        for (var i = 0; i < selectedTablesRoot.getChildCount(); i++) {
            var childAt = (QBTreeNode) selectedTablesRoot.getChildAt(i);
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
        var newTableNode = new QBTreeNode(newUserObject);
        node.children().asIterator()
                .forEachRemaining(x -> newTableNode.add(new QBTreeNode((TableElement) x.getUserObject())));
        selectedTablesRoot.add(newTableNode);
        if (selectedTablesRoot.getChildCount() == 1) {
            selectedTablesModel.reload();
        } else {
            selectedTablesModel.nodesWereInserted(selectedTablesRoot, new int[]{selectedTablesRoot.getChildCount() - 1});
        }
        getPublisher(SelectedTableAfterAddNotifier.class).onAction(newTableNode);
    }

    private QBTreeNode selectedTablesRoot;
    private ListTreeTableModel selectedTablesModel;
    private TreeTable selectedTablesTree;

    public JComponent selectedTables() {
        selectedTablesRoot = new QBTreeNode(new TableElement("empty"));
        selectedTablesModel = new ListTreeTableModel(selectedTablesRoot, new ColumnInfo[]{
                new TreeColumnInfo("Tables")
        });

        selectedTablesTree = new TreeTable(selectedTablesModel);
//        treeTable.getTree().setToggleClickCount(0);
        selectedTablesTree.setRootVisible(false);
        selectedTablesTree.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedTablesTree.setTreeCellRenderer(new TableElement.Renderer());
        //  JBScrollPane jbScrollPane = new JBScrollPane(treeTable);

        var decorator = ToolbarDecorator.createDecorator(selectedTablesTree);

        var actionRename = new AnActionButton("Rename Table", AllIcons.Actions.Edit) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var value = (QBTreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
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
                        .onAction((QBTreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0))
        );
        decorator.setRemoveActionUpdater(activeTableUpdater());

        selectedTablesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1 || mouseEvent.getX() < 40) {
                    return;
                }
                var value = (QBTreeNode) selectedTablesTree.getValueAt(table.getSelectedRow(), 0);
                if (Objects.isNull(value.getParent().getParent())) {
                    return;
                }
                var columnObject = value.getUserObject();
                var tableObject = value.getParent().getUserObject();
                getPublisher(SelectedFieldAddNotifier.class).onAction(
                        new TableElement(tableObject.getNameWithAlias(), columnObject.getName()), true
                );
            }
        });
        return decorator.createPanel();
    }

    private AnActionButtonUpdater activeTableUpdater() {
        return e -> {
            if (selectedTablesTree.getSelectedRow() == -1) {
                return false;
            }
            var value = (TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
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
            public Class<TableElement> getColumnClass() {
                return TableElement.class;
            }
        };

        selectedFieldsModel = new ListTableModel<>(new ColumnInfo[]{
                fieldInfo
        });
        selectedFieldsTable = new TableView<>(selectedFieldsModel);

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
        tablesTreeTable.addMouseListener(fieldsMouseListener(tablesTreeTable));
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

    @NotNull
    private MouseAdapter fieldsMouseListener(TreeTable treeTable) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1 || mouseEvent.getX() < 40) {
                    return;
                }
                getPublisher(SelectedTableAddNotifier.class)
                        .onAction((QBTreeNode) treeTable.getValueAt(table.getSelectedRow(), 0), null);
            }
        };
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

    private void loadStructureToTree(DBTables dbStructure, QBTreeNode root, Icon icon) {
        for (var entry : dbStructure.getDbElements().entrySet()) {
            var table = new TableElement(entry.getKey(), icon);
            var child = new QBTreeNode(table);
            root.add(child);
            for (var columnName : entry.getValue()) {
                var column = new TableElement(columnName, DatabaseIcons.Col);
                child.add(new QBTreeNode(column));
            }
        }
    }
}
