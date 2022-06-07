package org.quebee.com.panel;

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
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

@Getter
public class FromTables extends AbstractQueryPanel {
    private final String header = "Tables and Fields";
    private final JComponent component;

    private QBTreeNode databaseRoot;
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
    }

    @Override
    public void initListeners() {
        subscribe(ReloadDbTablesNotifier.class, this::setDatabaseTables);
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
        union.getSelectedTablesRoot().nodeToList().forEach(x ->
                databaseRoot.nodeToList().forEach(node -> {
                    var userObject = x.getUserObject();
                    if (node.getUserObject().getName().equals(userObject.getName())) {
                        getPublisher(SelectedTableAddNotifier.class).onAction(node, x.getUserObject().getAlias());
                    }
                })
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
        if (Objects.isNull(node.getParent())) {
            return;
        }
        if (Objects.isNull(node.getParent().getParent())) {
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

        var newUserObject = new TableElement(userObject.getName());
        newUserObject.setTable(true);
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
        decorator.addExtraAction(new AnActionButton("Inner Query", DatabaseIcons.Database) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });
//        decorator.setAddAction(button -> {
////            selectedTablesRoot.add(getTest("test" + new Random(1000).nextInt()));
//            selectedTablesModel.reload();
//        });

        decorator.setRemoveAction(button ->
                getPublisher(SelectedTableRemoveNotifier.class)
                        .onAction((QBTreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0))
        );

        var panel = decorator.createPanel();
        decorator.getActionsPanel().getAnActionButton(CommonActionsPanel.Buttons.REMOVE).addCustomUpdater(
                e -> {
                    if (selectedTablesTree.getSelectedRow() == -1) {
                        return false;
                    }
                    var value = (TreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0);
                    return Objects.isNull(value.getParent().getParent());
                }
        );
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
        return panel;
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
            TableUtil.doRemoveSelectedItems(selectedFieldsTable, selectedFieldsModel, null);
        });
        return decorator.createPanel();
    }

    public JComponent databaseTables() {
        databaseRoot = new QBTreeNode(new TableElement("tables"));
        databaseModel = new ListTreeTableModel(databaseRoot, new ColumnInfo[]{
                new TreeColumnInfo("Database")
        });
        final var treeTable = new TreeTable(databaseModel);
        // make own component?
//        treeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
//            @Override
//            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
//                throw new ExpandVetoException(event);
//            }
//
//            @Override
//            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
//
//            }
//        });
        treeTable.addMouseListener(fieldsMouseListener(treeTable));
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setTreeCellRenderer(new TableElement.Renderer());

        var decorator = ToolbarDecorator.createDecorator(treeTable);
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
        for (var stringListEntry : dbStructure.getDbElements().entrySet()) {
            var table = new TableElement(stringListEntry.getKey());
            table.setTable(true);
            var child = new QBTreeNode(table);
            databaseRoot.add(child);
            for (var columnName : stringListEntry.getValue()) {
                var column = new TableElement(columnName);
                column.setColumn(true);
                child.add(new QBTreeNode(column));
            }
        }
        databaseModel.reload();
    }
}
