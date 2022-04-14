package org.quebee.com.panel;

import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnActionHolder;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseTreePopupStep;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.ui.AnActionButton;
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
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.ConditionElement;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static org.quebee.com.notifier.QuiBuiNotifier.QUI_BUI_TOPIC;

public class ConditionsPanel implements QueryComponent {
    public static final String HEADER = "Conditions";
    private final JBSplitter splitter = new JBSplitter();

    @Override
    public JComponent getComponent() {
        return splitter;
    }

    public ConditionsPanel() {
        splitter.setProportion(0.3f);
        splitter.setFirstComponent(getFieldsTree());
        splitter.setSecondComponent(getConditionsTable());
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
        bus.connect().subscribe(QUI_BUI_TOPIC, context -> {
            System.out.println(context);
            System.out.println(context);
        });
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
                return "Custom super condition";
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
                    hBox.add(new JButton("First") {
                        {
                            setSize(250, 75);
                            setMaximumSize(getSize());
                        }
                    });
//                    hBox.add(Box.createHorizontalGlue());
                    hBox.add(new JTextField(variable.getConditionComparison()));
                    hBox.add(new JTextField(variable.getConditionRight()));
                    return hBox; //NON-NLS
                };
            }

            @Override
            public @NotNull TableCellEditor getEditor(final ConditionElement variable) {
                return new AbstractTableCellEditor() {
                    private final TreeComboBox conditionLeftCombo = getTreeComboBox();
                    @NotNull
                    private DefaultMutableTreeTableNode getTest(String test) {
                        return new DefaultMutableTreeTableNode(new TableElement(test));
                    }
                    @NotNull
                    private TreeComboBox getTreeComboBox() {
                        DefaultMutableTreeTableNode root = getTest("test");

                        final DefaultMutableTreeTableNode child = getTest("test 1");
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
                        TreeComboBox treeComboBox = new TreeComboBox(
                                model);
//                        treeComboBox.setRenderer(new TableElement.Renderer());
                        return treeComboBox;
                    }

                    private final JTextField conditionRight = new JTextField();
                    private final ComboBox<String> comparisonCombo =
                            new ComboBox<>(new String[]{"=", "!=", ">", "<", ">=", "<=", "like"});
                    private final JTextField conditionCustom = new JTextField();

                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        // var val = (ConditionElement) value;
                        if (variable.isCustom()) {
                            conditionCustom.setText(variable.getCondition());
                            return conditionCustom;
                        }
                        Box hBox = Box.createHorizontalBox();
                        conditionLeftCombo.setPreferredSize(new Dimension(200, 75));
//                        conditionLeftCombo.addActionListener(eggg -> {
////                            System.out.println(e);
////                            JBPopupFactory.getInstance().createActionGroupPopup(
////                                    "Test"
////                            ).show(conditionLeftCombo.getComponent(0));
//                            JBPopupFactory.getInstance().createTree(
//                                    new BaseTreePopupStep(ApplicationManager.getApplication(),
//                                            "Test",
//                                            new TreeGrid())
//                            );
//                        });
                     //   conditionLeftCombo.setItem(variable.getConditionLeft());
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
//                        elem.setConditionLeft(conditionLeftCombo.getItem());
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
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
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
}
