package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
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
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

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
                    Box hBox = Box.createHorizontalBox();
                    JTextField comp = new JTextField(variable.getConditionLeft());
                    comp.setPreferredSize(new Dimension(200, 15));
                    comp.setBorder(new LineBorder(JBColor.RED, 1));
                    hBox.add(comp);
                    hBox.add(new JTextField(variable.getConditionComparison()));
                    hBox.add(new JTextField(variable.getConditionRight()));
                    return hBox;
                };
            }

            @Override
            public @NotNull TableCellEditor getEditor(final ConditionElement variable) {
                return new CustomConditionEditor(variable);
            }
        };

        ListTableModel<ConditionElement> model = new ListTableModel<>(new ColumnInfo[]{
                isCustomInfo,
                conditionInfo,
        });
        TableView<ConditionElement> table = new TableView<>(model);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
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

        private final TreeComboBox conditionLeftCombo = getTreeComboBox();
        private final ConditionElement variable;

        public CustomConditionEditor(ConditionElement variable) {
            this.variable = variable;
        }

        @NotNull
        private TreeComboTableElement getTest(String test) {
            return new TreeComboTableElement(test, DatabaseIcons.Table);
        }

        @NotNull
        private TreeComboBox getTreeComboBox() {
            TreeComboTableElement root = getTest("test");

            final TreeComboTableElement child = getTest("test 1");
            for (int i = 0; i < 100; i++) {
                child.add(getTest("test 1" + i));
            }
            child.add(getTest("test 12"));
            root.add(child);
            root.add(getTest("test 2"));
            root.add(getTest("test 3"));
            root.add(getTest("test 4"));

            ListTreeTableModel model = new ListTreeTableModel(
                    root,
                    new ColumnInfo[]{
                            new TreeColumnInfo("Tables")
                    });

            return new TreeComboBox(model, false);
        }

        private final JTextField conditionRight = new JTextField();
        private final ComboBox<String> comparisonCombo =
                new ComboBox<>(new String[]{"=", "!=", ">", "<", ">=", "<=", "like"});
        private final JTextField conditionCustom = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (variable.isCustom()) {
                conditionCustom.setText(variable.getCondition());
                return conditionCustom;
            }
            Box hBox = Box.createHorizontalBox();
            conditionLeftCombo.setPreferredSize(new Dimension(200, 15));
            hBox.add(conditionLeftCombo);
            comparisonCombo.setItem(variable.getConditionComparison());
            hBox.add(comparisonCombo);
            conditionRight.setText(variable.getConditionRight());
            hBox.add(conditionRight);
            return hBox;
        }

        @Override
        public Object getCellEditorValue() {
            ConditionElement elem = new ConditionElement();
            if (conditionLeftCombo.getSelectedItem() != null) {
                elem.setConditionLeft(conditionLeftCombo.getSelectedItem().toString());
            }
            elem.setConditionRight(conditionRight.getText());
            elem.setConditionComparison(comparisonCombo.getItem());
            elem.setCondition(conditionCustom.getText());
            return elem;
        }
    };

    @Override
    public void initListeners() {
        subscribe(SelectedTableAfterAddNotifier.class, this::addSelectedTable);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

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
    }
}
