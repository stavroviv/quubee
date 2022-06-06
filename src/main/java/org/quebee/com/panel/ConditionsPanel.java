package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.ConditionElement;
import org.quebee.com.model.TableElement;
import org.quebee.com.model.TreeComboTableElement;

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
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
//        bus.connect().subscribe(QUI_BUI_TOPIC, context -> {
//            System.out.println(context);
//            System.out.println(context);
//        });
    }

    private JComponent getConditionsTable() {
        var isCustomInfo = new ColumnInfo<ConditionElement, Boolean>("Custom") {
            @Override
            public int getWidth(JTable table) {
                return 50;
            }

            @Override
            public @NotNull Boolean valueOf(ConditionElement o) {
                return o.isCustom();
            }

            @Override
            public void setValue(ConditionElement variable, Boolean value) {
                variable.setCustom(value);
//                setModified();
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(ConditionElement variable) {
                return true;
            }

            @Override
            public @NotNull String getTooltipText() {
                return "Custom condition";
            }
        };

        var conditionInfo = new ColumnInfo<ConditionElement, ConditionElement>("Condition") {
            @Override
            public @NotNull ConditionElement valueOf(ConditionElement templateVariable) {
                return templateVariable;
            }

            @Override
            public void setValue(ConditionElement conditionElement, ConditionElement value) {
                conditionElement.setCondition(value.getCondition());
                conditionElement.setConditionComparison(value.getConditionComparison());
                conditionElement.setConditionRight(value.getConditionRight());
                conditionElement.setConditionLeft(value.getConditionLeft());
            }

            @Override
            public boolean isCellEditable(ConditionElement templateVariable) {
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
                return new AbstractTableCellEditor() {
                    private final TreeComboBox conditionLeftCombo = getTreeComboBox();

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

    private JComponent getFieldsTree() {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");
        ListTreeTableModel model = new ListTreeTableModel(root, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        TreeTable table = new TreeTable(model);
        table.setRootVisible(false);
        table.setTreeCellRenderer(new TableElement.Renderer());
        JBScrollPane jbScrollPane = new JBScrollPane(table);
//
//        ListTableModel model = new ListTableModel(new ColumnInfo[]{getTitleColumnInfo("Fields")});
//        TableView table = new TableView(model);

//        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
//        decorator.setAddAction(button -> {
//            model.addRow(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
//            //    model.reload();
//        });
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
        return jbScrollPane;
    }

    @Override
    public void initListeners(Disposable disposable) {

    }
}
