package org.quebee.com.panel;

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDSource;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.*;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.*;
import icons.DatabaseIcons;
import lombok.Getter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.columns.EditableBooleanColumn;
import org.quebee.com.model.ConditionElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeComboTableElement;
import org.quebee.com.notifier.LoadQueryDataNotifier;
import org.quebee.com.notifier.SaveQueryDataNotifier;
import org.quebee.com.notifier.SelectedTableAfterAddNotifier;
import org.quebee.com.notifier.SelectedTableRemoveNotifier;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;
import org.quebee.com.util.MyRowsDnDSupport;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class ConditionsPanel extends AbstractQueryPanel {
    private static final int COMBO_DEFAULT_WIDTH = 170;
    private static final String COLUMN_CUSTOM = "Custom";

    private final String header = "Conditions";
    private final JBSplitter component = new JBSplitter();

    public ConditionsPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTree());
        component.setSecondComponent(getConditionsTable());
        enableDragAndDrop();
    }

    private ListTableModel<ConditionElement> conditionTableModel;
    private TableView<ConditionElement> conditionTable;

    private JComponent getConditionsTable() {
        var isCustomInfo = new EditableBooleanColumn<>(COLUMN_CUSTOM, 50, ConditionElement::isCustom, ConditionElement::setCustom);

        var conditionInfo = new ColumnInfo<ConditionElement, ConditionElement>("Condition") {
            @Override
            public @NotNull ConditionElement valueOf(ConditionElement element) {
                return element;
            }

            @Override
            public void setValue(ConditionElement element, ConditionElement value) {
                element.setCondition(value.getCondition());
                element.setConditionComparison(value.getConditionComparison());
                element.setConditionRight(value.getConditionRight());
                element.setConditionLeft(value.getConditionLeft());
            }

            @Override
            public boolean isCellEditable(ConditionElement element) {
                return true;
            }

            @Override
            public TableCellRenderer getRenderer(ConditionElement variable) {
                if (variable.isCustom()) {
                    return (table, value, isSelected, hasFocus, row, column) -> new JTextField(variable.getCondition());
                }
                return (table, value, isSelected, hasFocus, row, column) -> {
                    var hBox = Box.createHorizontalBox();
                    hBox.add(getTreeComboWithValue(variable));
                    hBox.add(new ComboBox<>(new String[]{variable.getConditionComparison()}));
                    hBox.add(new JBTextField(variable.getConditionRight()));
                    return hBox;
                };
            }

            @Override
            public @NotNull TableCellEditor getEditor(final ConditionElement variable) {
                return new CustomConditionEditor(variable);
            }
        };

        conditionTableModel = new ListTableModel<>(isCustomInfo, conditionInfo);
        conditionTableModel.addTableModelListener(this::conditionTableListener);

        conditionTable = new TableView<>(conditionTableModel);
        enableDragAndDrop();
        var decorator = ToolbarDecorator.createDecorator(conditionTable);
        decorator.setAddAction(button -> {
            var item = new ConditionElement();
            item.setConditionComparison("=");
            conditionTableModel.addRow(item);
            conditionTable.setSelection(Collections.singleton(item));
        });
        decorator.addExtraAction(new AnActionButton("Copy", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                TableUtil.stopEditing(conditionTable);
                var selectedObject = conditionTable.getSelectedObject();
                if (Objects.isNull(selectedObject)) {
                    return;
                }
                var item = new ConditionElement(selectedObject);
                conditionTableModel.addRow(item);
                conditionTable.setSelection(Collections.singleton(item));
            }

            @Override
            public void updateButton(@NotNull AnActionEvent e) {
                super.updateButton(e);
                if (conditionTableModel.getRowCount() == 0 || conditionTable.getSelectedRow() == -1) {
                    e.getPresentation().setEnabled(false);
                }
            }
        });
        return decorator.createPanel();
    }

    private void conditionTableListener(TableModelEvent e) {
        var column = e.getColumn();
        if (column < 0) {
            return;
        }
        var model = (TableModel) e.getSource();
        var columnName = model.getColumnName(column);
        int firstRow = e.getFirstRow();
        customColumnChanged(firstRow, column, model, columnName);
    }

    private void customColumnChanged(int firstRow, int column, TableModel model, String columnName) {
        if (!COLUMN_CUSTOM.equals(columnName)) {
            return;
        }
        var value = (boolean) model.getValueAt(firstRow, column);
        var row = conditionTable.getSelectedObject();
        if (Objects.isNull(row)) {
            return;
        }

        if (value) {
            var left = Strings.nullToEmpty(row.getConditionLeft());
            var right = Strings.nullToEmpty(row.getConditionRight());
            row.setCondition(left + " " + row.getConditionComparison() + " " + right);
            return;
        }

        var whereExpression = getExpression(row.getCondition());
        if (canBeParsed(whereExpression)) {
            var where1 = (BinaryExpression) whereExpression;
            row.setConditionLeft(where1.getLeftExpression().toString());
            row.setConditionComparison(where1.getStringExpression());
            row.setConditionRight(where1.getRightExpression().toString());
        } else {
            row.setCustom(true);
            int i = Messages.showOkCancelDialog(
                    "Cannot convert to simple condition. Continue?", "Warning", "Ok", "Cancel",
                    Messages.getWarningIcon()
            );
            if (i == 0) {
                row.setCustom(false);
                row.setConditionLeft(null);
                row.setConditionComparison("=");
                row.setConditionRight(null);
                conditionTableModel.fireTableDataChanged();
            }
        }
    }

    private boolean canBeParsed(Expression expression) {
        if (!(expression instanceof ComparisonOperator || expression instanceof LikeExpression)) {
            return false;
        }
        var binaryExpression = (BinaryExpression) expression;
        var expr = binaryExpression.getLeftExpression().toString();
        var split = expr.split("\\.");
        if (split.length != 2) {
            return false;
        }
        for (var table : tables) {
            if (!table.getUserObject().getName().equals(split[0])) {
                continue;
            }
            for (var i = 0; i < table.getChildCount(); i++) {
                var childAt = table.getChildAt(i);
                if (childAt.getUserObject().getName().equals(split[1])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Expression getExpression(String where) {
        try {
            var stmt = CCJSqlParserUtil.parse("SELECT * FROM TABLES WHERE " + where);
            var select = (Select) stmt;
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException ignored) {
        }
        return null;
    }

    @NotNull
    private JComponent getTreeComboWithValue(ConditionElement variable) {
        if (Objects.isNull(variable.getConditionLeft())) {
            var stringComboBox = new ComboBox<>();
            stringComboBox.setPreferredSize(new Dimension(COMBO_DEFAULT_WIDTH, 15));
            return stringComboBox;
        }
        var split = variable.getConditionLeft().split("\\.");
        var root = new TreeComboTableElement(split[1], split[0], DatabaseIcons.Col);
        var model = new ListTreeTableModel(root, new ColumnInfo[]{new TreeColumnInfo("Tables")});
        var treeComboBox = new TreeComboBox(model);
        treeComboBox.setSelectedItem(root);
        treeComboBox.setPreferredSize(new Dimension(COMBO_DEFAULT_WIDTH, 15));
        return treeComboBox;
    }

    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel allFieldsModel;
    private TreeTable table;
    private JComponent getFieldsTree() {
        allFieldsRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsModel = new ListTreeTableModel(allFieldsRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        table = new TreeTable(allFieldsModel);
        table.setTreeCellRenderer(new TableElement.Renderer());
        table.setRootVisible(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                var table = (TreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1) {
                    return;
                }
                var value = (QBTreeNode) table.getValueAt(table.getSelectedRow(), 0);
                if (Objects.isNull(value.getParent().getParent())) {
                    return;
                }
                addCondition(value, -1);
            }
        });
        var decorator = ToolbarDecorator.createDecorator(table);
        return decorator.createPanel();
    }

    private void addCondition(QBTreeNode value, int newIndex) {
        var columnObject = value.getUserObject();
        var tableObject = value.getParent().getUserObject();
        var item = new ConditionElement();
        item.setConditionLeft(tableObject.getName() + "." + columnObject.getName());
        item.setConditionComparison("=");
        if (newIndex == -1) {
            conditionTableModel.addRow(item);
        } else {
            conditionTableModel.insertRow(newIndex, item);
        }
        conditionTable.setSelection(Collections.singleton(item));
    }

    private void enableDragAndDrop() {
        DnDManager.getInstance().registerSource(new MyDnDSource(), table, mainPanel.getDisposable());
        MyRowsDnDSupport.install(conditionTable, (EditableModel) conditionTable.getModel(), (event) -> {
            if (event.getAttachedObject() instanceof QBTreeNode) {
                final Point p = event.getPoint();
                int i = conditionTable.rowAtPoint(p);
                addCondition((QBTreeNode) event.getAttachedObject(), i + 1);
            }
        });
    }

    private class MyDnDSource implements DnDSource {

        public boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin) {
            return true;
        }

        public @NotNull DnDDragStartBean startDragging(DnDAction action, @NotNull Point dragOrigin) {
            var value = (QBTreeNode) table.getValueAt(table.getSelectedRow(), 0);
            return new DnDDragStartBean(value, dragOrigin);
        }

        public @NotNull Pair<Image, Point> createDraggedImage(DnDAction action, Point dragOrigin, @NotNull DnDDragStartBean bean) {
            var c = new SimpleColoredComponent();
            c.setForeground(RenderingUtil.getForeground(table));
            c.setBackground(RenderingUtil.getBackground(table));
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

    private class CustomConditionEditor extends AbstractTableCellEditor {

        private final ConditionElement variable;
        private final TreeComboBox conditionLeftCombo;
        private final JBTextField conditionRight;
        private final ComboBox<String> comparisonCombo;
        private final JBTextField conditionCustom;

        public CustomConditionEditor(ConditionElement variable) {
            this.variable = variable;
            this.conditionLeftCombo = getTreeComboBox();
            this.conditionRight = new ExpandableTextField();
            this.comparisonCombo = new ComboBox<>(new String[]{"=", "!=", ">", "<", ">=", "<=", "LIKE"});
            this.conditionCustom = new ExpandableTextField();
        }

        @NotNull
        private TreeComboBox getTreeComboBox() {
            var root = new TreeComboTableElement("root", null, null);
            for (var table : tables) {
                var userObject = table.getUserObject();
                var tableName = userObject.getDescription();
                var tableNode = new TreeComboTableElement(tableName, null, userObject.getIcon());
                for (int i = 0; i < table.getChildCount(); i++) {
                    var column = table.getChildAt(i);
                    tableNode.add(new TreeComboTableElement(column.getUserObject().getName(), tableName, DatabaseIcons.Col));
                }
                root.add(tableNode);
            }
            var model = new ListTreeTableModel(root, new ColumnInfo[]{new TreeColumnInfo("Tables")});
            return new TreeComboBox(model, false) {
                @Override
                public void setSelectedItem(Object object) {
                    if (Objects.isNull(object)) {
                        return;
                    }
                    var item = (TreeComboTableElement) object;
                    if (Objects.nonNull(item.getParent()) && item.getParent().equals(root)) {
                        return;
                    }
                    super.setSelectedItem(object);
                }
            };
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (variable.isCustom()) {
                conditionCustom.setText(variable.getCondition());
                return conditionCustom;
            }
            var hBox = Box.createHorizontalBox();
            conditionLeftCombo.setPreferredSize(new Dimension(COMBO_DEFAULT_WIDTH, 15));
            if (Objects.nonNull(variable.getConditionLeft())) {
                var split = variable.getConditionLeft().split("\\.");
                var treeModel = conditionLeftCombo.getTreeModel();
                var root = treeModel.getRoot();
                TreeComboTableElement find = null;
                for (int i = 0; i < treeModel.getChildCount(root); i++) {
                    var tableEl = (TreeComboTableElement) treeModel.getChild(root, i);
                    if (tableEl.getText().equals(split[0])) {
                        for (int j = 0; j < treeModel.getChildCount(tableEl); j++) {
                            var columnEl = (TreeComboTableElement) treeModel.getChild(tableEl, j);
                            if (columnEl.getText().equals(split[1])) {
                                find = columnEl;
                                break;
                            }
                        }
                    }
                }
                if (find != null) {
                    conditionLeftCombo.setSelectedItem(find);
                }
            }

            hBox.add(conditionLeftCombo);
            comparisonCombo.setItem(variable.getConditionComparison());
            hBox.add(comparisonCombo);
            conditionRight.setText(variable.getConditionRight());
            hBox.add(conditionRight);
            return hBox;
        }

        @Override
        public Object getCellEditorValue() {
            var elem = new ConditionElement();
            var selectedItem = conditionLeftCombo.getSelectedItem();
            if (selectedItem != null) {
                var selectedItem1 = (TreeComboTableElement) selectedItem;
                if (selectedItem1.getParent() != null) {
                    elem.setConditionLeft(selectedItem1.getParent().toString() + "." + selectedItem1);
                }
            }
            elem.setConditionRight(conditionRight.getText());
            elem.setConditionComparison(comparisonCombo.getItem());
            elem.setCondition(conditionCustom.getText());
            return elem;
        }
    }

    @Override
    public void initListeners() {
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
        subscribe(SaveQueryDataNotifier.class, this::saveQueryData);
        subscribe(LoadQueryDataNotifier.class, this::loadQueryData);
    }

    private void loadQueryData(FullQuery fullQuery, String cteName, int i1) {
        var union = fullQuery.getCte(cteName).getUnion("" + i1);
        ComponentUtils.loadTableToTable(union.getConditionTableModel(), conditionTableModel);
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(conditionTableModel, union.getConditionTableModel());
        ComponentUtils.clearTable(conditionTableModel);
        ComponentUtils.clearTree(allFieldsRoot);
        tables.clear();
    }

    private final Set<QBTreeNode> tables = new HashSet<>();

    private void addSelectedTable(QBTreeNode node) {
        var newUserObject = new TableElement(node.getUserObject());
        var newTableNode = new QBTreeNode(newUserObject);
        node.nodeToList().forEach(x -> newTableNode.add(new QBTreeNode(x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        if (allFieldsRoot.getChildCount() == 1) {
            allFieldsModel.reload();
        } else {
            allFieldsModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
        }
        if (Objects.isNull(node.getParent().getParent())) {
            tables.add(node);
        } else {
            tables.add(node.getParent());
        }
    }

    private void removeSelectedTable(QBTreeNode node) {
        ComponentUtils.removeNodeByTable(node, allFieldsRoot, allFieldsModel);
        tables.remove(node);
        removeConditionsByTable(node);
    }

    private void removeConditionsByTable(QBTreeNode node) {
        var removeTableName = node.getUserObject().getDescription();
        for (var i = conditionTableModel.getItems().size() - 1; i >= 0; i--) {
            var item = conditionTableModel.getItem(i);
            if (item.isCustom() && item.getCondition().contains(removeTableName + ".")) {
                conditionTableModel.removeRow(i);
                continue;
            }
            var conditionLeft = item.getConditionLeft();
            if (Objects.isNull(conditionLeft)) {
                continue;
            }
            var split = conditionLeft.split("\\.");
            if (split.length == 2 && removeTableName.equals(split[0])) {
                conditionTableModel.removeRow(i);
            }
        }
    }
}
