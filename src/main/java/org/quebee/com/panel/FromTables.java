package org.quebee.com.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.Messages;

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
    public void initListeners(Disposable disposable) {
        subscribe(disposable, ReloadDbTablesNotifier.class, this::setDatabaseTables);
        subscribe(disposable, SelectedTableAddNotifier.class, this::addSelectedTable);
        subscribe(disposable, SelectedTableRemoveNotifier.class, this::removeSelectedTable);
        subscribe(disposable, SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(disposable, SaveQueryDataNotifier.class, this::saveQueryData);
        subscribe(disposable, LoadQueryDataNotifier.class, this::loadQueryData);
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
                    var userObject = (TableElement) x.getUserObject();
                    if (node.getUserObject().getName().equals(userObject.getName())) {
                        Messages.getPublisher(mainPanel.getId(), SelectedTableAddNotifier.class).onAction(node);
                    }
                })
        );
        union.getSelectedFieldsModel().getItems().forEach(x ->
                databaseRoot.nodeToList().forEach(tableNode -> {
                    if (tableNode.getUserObject().getName().equals(x.getPsTable().getName())) {
                        tableNode.nodeToList().forEach(fieldNode -> {
                            if (fieldNode.getUserObject().getName().equals(x.getColumnName())) {
                                Messages.getPublisher(mainPanel.getId(), SelectedFieldAddNotifier.class).onAction(fieldNode);
                            }
                        });
                    }
                })
        );
        selectedTablesModel.reload();
    }

    private void removeSelectedTable(MutableTreeTableNode node) {
        if (Objects.nonNull(node.getParent().getParent())) {
            return;
        }
        var index = selectedTablesRoot.getIndex(node);
        selectedTablesRoot.remove(node);
        selectedTablesModel.nodesWereRemoved(selectedTablesRoot, new int[]{index}, new Object[]{node});
        removeSelectedFieldsByTable(node, selectedFieldsModel);
    }

    private void removeSelectedFieldsByTable(TreeTableNode node, ListTableModel<TableElement> model) {
        for (var i = model.getItems().size() - 1; i >= 0; i--) {
            var userObject = (TableElement) node.getUserObject();
            if (model.getItem(i).getParentId().equals(userObject.getId())) {
                model.removeRow(i);
            }
        }
    }

    private void addSelectedTable(QBTreeNode node) {
        if (Objects.isNull(node.getParent())) {
            return;
        }
        if (Objects.isNull(node.getParent().getParent())) {
            addSelectedTableNode(node);
            return;
        }
        var parent = node.getParent();
        var parentUserObject = (TableElement) parent.getUserObject();
        var exists = false;
        var children = selectedTablesRoot.children();
        while (children.hasMoreElements()) {
            var mutableTreeTableNode = children.nextElement();
            var userObject = (TableElement) mutableTreeTableNode.getUserObject();
            if (userObject.getId() == parentUserObject.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            addSelectedTableNode(parent);
        }
        Messages.getPublisher(mainPanel.getId(), SelectedFieldAddNotifier.class).onAction(node);
    }

    private void addSelectedField(QBTreeNode node) {
        var parent = node.getParent().getUserObject();
        var userObject = node.getUserObject();
        selectedFieldsModel.addRow(new TableElement(parent.getName() + "." + userObject.getName(), parent.getId()));
    }

    private void addSelectedTableNode(QBTreeNode node) {
        var userObject = node.getUserObject();
        var newNodeId = userObject.getId();
        var exists = false;
        for (int i = 0; i < selectedTablesRoot.getChildCount(); i++) {
            TreeTableNode childAt = selectedTablesRoot.getChildAt(i);
            if (((TableElement) childAt.getUserObject()).getId().equals(newNodeId)) {
                exists = true;
                break;
            }
        }

        var newUserObject = new TableElement(userObject.getName());
        newUserObject.setTable(true);
        if (!exists) {
            newUserObject.setId(newNodeId);
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
        Messages.getPublisher(mainPanel.getId(), SelectedTableAfterAddNotifier.class).onAction(newTableNode);
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

        decorator.setRemoveAction(button -> Messages.getPublisher(mainPanel.getId(), SelectedTableRemoveNotifier.class)
                .onAction((QBTreeNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0)));

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
                Messages.getPublisher(mainPanel.getId(), SelectedFieldAddNotifier.class).onAction(value);
            }
        });
        return panel;
    }

    private ListTableModel<TableElement> selectedFieldsModel;

    public JComponent selectedFields() {
        var fieldInfo = new ColumnInfo<TableElement, String>("Fields") {

            @Override
            public @NotNull String valueOf(TableElement o) {
                return o.getName();
            }

            @Override
            public Class<TableElement> getColumnClass() {
                return TableElement.class;
            }
        };

        selectedFieldsModel = new ListTableModel<>(new ColumnInfo[]{
                fieldInfo
        });
        var table = new TableView<>(selectedFieldsModel);

        var decorator = ToolbarDecorator.createDecorator(table);
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
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
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
                Messages.getPublisher(mainPanel.getId(), SelectedTableAddNotifier.class)
                        .onAction((QBTreeNode) treeTable.getValueAt(table.getSelectedRow(), 0));
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
