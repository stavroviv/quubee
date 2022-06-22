package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.columns.EditableBooleanColumn;
import org.quebee.com.model.ConditionElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeComboTableElement;
import org.quebee.com.notifier.SelectedTableAfterAddNotifier;
import org.quebee.com.notifier.SelectedTableRemoveNotifier;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class ConditionsPanel extends AbstractQueryPanel {
    private final String header = "Conditions";
    private final JBSplitter component = new JBSplitter();

    public ConditionsPanel(MainPanel mainPanel) {
        super(mainPanel);
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTree());
        component.setSecondComponent(getConditionsTable());
    }

    private JComponent getConditionsTable() {
        var isCustomInfo = new EditableBooleanColumn<>("Custom", 50, ConditionElement::isCustom, ConditionElement::setCustom);

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

        var model = new ListTableModel<ConditionElement>(isCustomInfo, conditionInfo);
        var table = new TableView<>(model);

        var decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            model.addRow(new ConditionElement());
            //    model.reload();
        });
        decorator.addExtraAction(new AnActionButton("Copy", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("test");
            }
        });
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
        return decorator.createPanel();
    }

    @NotNull
    private JComponent getTreeComboWithValue(ConditionElement variable) {
        if (Objects.isNull(variable.getConditionLeft())) {
            return new ComboBox<String>();
        }
        var split = variable.getConditionLeft().split("\\.");
        var root = new TreeComboTableElement(split[1], split[0], DatabaseIcons.Col);
        var model = new ListTreeTableModel(root, new ColumnInfo[]{new TreeColumnInfo("Tables")});
        var treeComboBox = new TreeComboBox(model);
        treeComboBox.setSelectedItem(root);
        return treeComboBox;
    }

    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel allFieldsModel;

    private JComponent getFieldsTree() {
        allFieldsRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsModel = new ListTreeTableModel(allFieldsRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        var table = new TreeTable(allFieldsModel);
        table.setTreeCellRenderer(new TableElement.Renderer());
        table.setRootVisible(false);

        var decorator = ToolbarDecorator.createDecorator(table);
        return decorator.createPanel();
    }

    private class CustomConditionEditor extends AbstractTableCellEditor {


        private final ConditionElement variable;

        public CustomConditionEditor(ConditionElement variable) {
            this.variable = variable;
            this.conditionLeftCombo = getTreeComboBox();
        }

        @NotNull
        private TreeComboBox getTreeComboBox() {
            var root = new TreeComboTableElement("root", null, null);
            for (var table : tables) {
                String tableName = table.getUserObject().getDescription();
                var tableNode = new TreeComboTableElement(tableName, null, DatabaseIcons.Table);
                for (int i = 0; i < table.getChildCount(); i++) {
                    var column = table.getChildAt(i);
                    tableNode.add(new TreeComboTableElement(column.getUserObject().getName(), tableName, DatabaseIcons.Col));
                }
                root.add(tableNode);
            }
            var model = new ListTreeTableModel(root, new ColumnInfo[]{new TreeColumnInfo("Tables")});
            return new TreeComboBox(model, false);
        }

        private final TreeComboBox conditionLeftCombo;
        private final JBTextField conditionRight = new JBTextField();
        private final ComboBox<String> comparisonCombo = new ComboBox<>(new String[]{"=", "!=", ">", "<", ">=", "<=", "like"});
        private final JBTextField conditionCustom = new JBTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (variable.isCustom()) {
                conditionCustom.setText(variable.getCondition());
                return conditionCustom;
            }
            var hBox = Box.createHorizontalBox();
//            conditionLeftCombo.setPreferredSize(new Dimension(200, 15));
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
                    elem.setConditionLeft(selectedItem1.getParent().toString() + "." + selectedItem1.toString());
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
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        allFieldsRoot.nodeToList().stream()
                .filter(x -> x.getUserObject().getDescription().equals(removeTableName))
                .findFirst().ifPresent(x -> {
                    var index = allFieldsRoot.getIndex(x);
                    allFieldsRoot.remove(x);
                    allFieldsModel.nodesWereRemoved(allFieldsRoot, new int[]{index}, new Object[]{x});
                });
        tables.remove(node);
    }
}
