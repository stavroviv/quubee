package org.quebee.com.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
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
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.database.DBTables;
import org.quebee.com.model.TableElement;
import org.quebee.com.util.Messages;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static org.quebee.com.notifier.ReloadDbTablesNotifier.RELOAD_TABLES_TOPIC;
import static org.quebee.com.notifier.SelectedFieldAddNotifier.SELECTED_FIELD_ADD;
import static org.quebee.com.notifier.SelectedTableAddNotifier.SELECTED_TABLE_ADD;
import static org.quebee.com.notifier.SelectedTableRemoveNotifier.SELECTED_TABLE_REMOVE;

@Getter
public class FromTables implements QueryComponent {
    private final String header = "Tables and Fields";
    private final JComponent component;

    private DefaultMutableTreeTableNode databaseRoot;
    private ListTreeTableModel databaseModel;

    public FromTables() {
        var splitterLeft = new JBSplitter();
        splitterLeft.setProportion(0.3f);

        splitterLeft.setFirstComponent(databaseTables());

        var splitterRight = new JBSplitter();
        splitterRight.setFirstComponent(selectedTables());
        splitterRight.setSecondComponent(selectedFields());
        splitterLeft.setSecondComponent(splitterRight);

        this.component = splitterLeft;
        initListeners();
    }

    private void initListeners() {
        var bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect().subscribe(RELOAD_TABLES_TOPIC, this::setDatabaseTables);
        bus.connect().subscribe(SELECTED_TABLE_ADD, this::addSelectedTable);
        bus.connect().subscribe(SELECTED_TABLE_REMOVE, this::removeSelectedTable);
        bus.connect().subscribe(SELECTED_FIELD_ADD, this::addSelectedField);
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
        for (int i = model.getItems().size() - 1; i >= 0; i--) {
            var userObject = (TableElement) node.getUserObject();
            if (model.getItem(i).getParentId().equals(userObject.getId())) {
                model.removeRow(i);
            }
        }
    }

    private void addSelectedTable(MutableTreeTableNode node) {
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
            MutableTreeTableNode mutableTreeTableNode = children.nextElement();
            TableElement userObject = (TableElement) mutableTreeTableNode.getUserObject();
            if (userObject.getId() == parentUserObject.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            addSelectedTableNode(parent);
        }
        Messages.getPublisher(SELECTED_FIELD_ADD).onAction(node);
    }

    private void addSelectedField(MutableTreeTableNode node) {
        var parent = (TableElement) node.getParent().getUserObject();
        var userObject = (TableElement) node.getUserObject();
        selectedFieldsModel.addRow(new TableElement(parent.getName() + "." + userObject.getName(), parent.getId()));
    }

    private void addSelectedTableNode(TreeTableNode node) {
        var child = new DefaultMutableTreeTableNode(node.getUserObject());
        node.children().asIterator()
                .forEachRemaining(x -> child.add(new DefaultMutableTreeTableNode(x.getUserObject())));
        selectedTablesRoot.add(child);
        if (selectedTablesRoot.getChildCount() == 1) {
            selectedTablesModel.reload();
        } else {
            selectedTablesModel.nodesWereInserted(selectedTablesRoot, new int[]{selectedTablesRoot.getChildCount() - 1});
        }
    }

    DefaultMutableTreeTableNode selectedTablesRoot;
    ListTreeTableModel selectedTablesModel;
    TreeTable selectedTablesTree;

    public JComponent selectedTables() {
        selectedTablesRoot = new DefaultMutableTreeTableNode(new TableElement("empty"));
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

        decorator.setRemoveAction(button -> Messages.getPublisher(SELECTED_TABLE_REMOVE)
                .onAction((MutableTreeTableNode) selectedTablesTree.getValueAt(selectedTablesTree.getSelectedRow(), 0)));

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
                MutableTreeTableNode value = (MutableTreeTableNode) selectedTablesTree.getValueAt(table.getSelectedRow(), 0);
                if (Objects.isNull(value.getParent().getParent())) {
                    return;
                }
                Messages.getPublisher(SELECTED_FIELD_ADD).onAction(value);
            }
        });
        return panel;
    }

    ListTableModel<TableElement> selectedFieldsModel;

    public JComponent selectedFields() {
        var fieldInfo = new ColumnInfo<TableElement, String>("Fields") {

            @Override
            public @NotNull String valueOf(TableElement o) {
                return o.getName();
            }
//
//            @Override
//            public @NotNull Icon getIcon() {
//                return DatabaseIcons.Col;
//            }

            @Override
            public Class<TableElement> getColumnClass() {
                return TableElement.class;
            }
        };

//        typeColumn.setCellRenderer(new IconTableCellRenderer<Icon>() {
//            @NotNull
//            @Override
//            protected Icon getIcon(@NotNull Icon value, JTable table, int row) {
//                return value;
//            }
//
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
//                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
//                setText("");
//                return this;
//            }
//
//            @Override
//            protected boolean isCenterAlignment() {
//                return true;
//            }
//        });

        selectedFieldsModel = new ListTableModel<>(new ColumnInfo[]{
                fieldInfo,
        });
        var table = new TableView<>(selectedFieldsModel);
//        TableColumn severitiesColumn = table.getColumnModel().getColumn(0);
//        severitiesColumn.setCellRenderer(new IconTableCellRenderer<Icon>() {
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
//                Component component = super.getTableCellRendererComponent(table, value, false, focus, row, column);
//                this.setBorder(JBUI.Borders.empty(1, 2));
//                Color bg = RenderingUtil.getBackground(table, selected);
//                component.setBackground(bg);
//                ((JLabel)component).setText("");
//                return component;
//            }
//
//            protected Icon getIcon(@NotNull Icon value, JTable table, int row) {
////                if (value == null) {
////                    $$$reportNull$$$0(0);
////                }
//
//                return DatabaseIcons.Col;
//            }
//        });

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
        databaseRoot = new DefaultMutableTreeTableNode(new TableElement("tables"));
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
                Messages.getPublisher(SELECTED_TABLE_ADD)
                        .onAction((MutableTreeTableNode) treeTable.getValueAt(table.getSelectedRow(), 0));
            }
        };
    }

    public void setDatabaseTables(DBTables dbStructure) {
        for (var stringListEntry : dbStructure.getDbElements().entrySet()) {
            var table = new TableElement(stringListEntry.getKey());
            table.setTable(true);
            var child = new DefaultMutableTreeTableNode(table);
            databaseRoot.add(child);
            for (String columnName : stringListEntry.getValue()) {
                var column = new TableElement(columnName);
                column.setColumn(true);
                child.add(new DefaultMutableTreeTableNode(column));
            }
        }
        databaseModel.reload();
    }
}
