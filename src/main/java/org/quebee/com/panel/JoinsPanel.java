package org.quebee.com.panel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.LoadQueryDataNotifier;
import org.quebee.com.notifier.SaveQueryDataNotifier;
import org.quebee.com.notifier.SelectedTableAfterAddNotifier;
import org.quebee.com.notifier.SelectedTableRemoveNotifier;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Getter
public class JoinsPanel extends AbstractQueryPanel {

    private final String header = "Joins";
    private final JComponent component;
    private final Set<QBTreeNode> tables = new HashSet<>();
    private final ListTableModel<LinkElement> joinTableModel;
    private final TableView<LinkElement> joinTable;

    public JoinsPanel(MainPanel mainPanel) {
        super(mainPanel);

        var table1Info = new ColumnInfo<LinkElement, String>("Table 1") {
            @Override
            public int getWidth(JTable table) {
                return 150;
            }

            @Override
            public void setValue(LinkElement linkElement, String value) {
                linkElement.setTable1(value);
            }

            @Override
            public @NotNull TableCellEditor getEditor(LinkElement linkElement) {
                return availableTablesEditor();
            }

            @Override
            public boolean isCellEditable(LinkElement linkElement) {
                return true;
            }

            @Override
            public @NotNull String valueOf(LinkElement o) {
                return Objects.isNull(o) ? "" : o.getTable1();
            }
        };

        var allTable1Info = new ColumnInfo<LinkElement, Boolean>("All") {
            @Override
            public int getWidth(JTable table) {
                return 25;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isAllTable1();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setAllTable1(value);
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };

        var table2Info = new ColumnInfo<LinkElement, String>("Table 2") {
            @Override
            public int getWidth(JTable table) {
                return 150;
            }

            @Override
            public void setValue(LinkElement linkElement, String value) {
                linkElement.setTable2(value);
            }

            @Override
            public @NotNull TableCellEditor getEditor(LinkElement linkElement) {
                return availableTablesEditor();
            }

            @Override
            public boolean isCellEditable(LinkElement linkElement) {
                return true;
            }

            @Override
            public @NotNull String valueOf(LinkElement o) {
                return Objects.isNull(o) ? "" : o.getTable2();
            }
        };

        var allTable2Info = new ColumnInfo<LinkElement, Boolean>("All") {
            @Override
            public int getWidth(JTable table) {
                return 25;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isAllTable2();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setAllTable2(value);
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };

        var customInfo = new ColumnInfo<LinkElement, Boolean>("Custom") {
            @Override
            public int getWidth(JTable table) {
                return 50;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isCustom();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setCustom(value);
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };

        var linkingConditionInfo = new ColumnInfo<LinkElement, LinkElement>("Linking Condition") {

            @Override
            public @NotNull LinkElement valueOf(LinkElement o) {
                return o;
            }

            @Override
            public void setValue(LinkElement variable, LinkElement value) {
                variable.setCondition(value.getCondition());
                variable.setField1(value.getField1());
                variable.setComparison(value.getComparison());
                variable.setField2(value.getField2());
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }

            @Override
            public TableCellRenderer getRenderer(LinkElement variable) {
                if (variable.isCustom()) {
                    return (table, value, isSelected, hasFocus, row, column) -> new JBTextField(variable.getCondition());
                }
                return (table, value, isSelected, hasFocus, row, column) -> {
                    Box hBox = Box.createHorizontalBox();
                    hBox.add(new ComboBox<>(new String[]{variable.getField1()}));
                    hBox.add(new ComboBox<>(new String[]{variable.getComparison()}));
                    hBox.add(new ComboBox<>(new String[]{variable.getField2()}));
                    return hBox;
                };
            }

            @Override
            public @NotNull TableCellEditor getEditor(final LinkElement variable) {
                return new AbstractTableCellEditor() {

                    @Override
                    public LinkElement getCellEditorValue() {
                        LinkElement elem = new LinkElement();
                        if (conditionField1.getSelectedItem() != null) {
                            elem.setField1(conditionField1.getSelectedItem().toString());
                        }
                        if (conditionField2.getSelectedItem() != null) {
                            elem.setField2(conditionField2.getSelectedItem().toString());
                        }
                        elem.setComparison(conditionComparison.getItem());
                        elem.setCondition(conditionCustom.getText());
                        return elem;
                    }

                    private final ComboBox<String> conditionField1 = new ComboBox<>();
                    private final ComboBox<String> conditionField2 = new ComboBox<>();
                    private final ComboBox<String> conditionComparison = new ComboBox<>(
                            new String[]{"=", "!=", ">", "<", ">=", "<="}
                    );
                    private final JBTextField conditionCustom = new JBTextField();

                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                                 int row, int column) {
                        if (variable.isCustom()) {
                            conditionCustom.setText(variable.getCondition());
                            return conditionCustom;
                        }
                        var hBox = Box.createHorizontalBox();
                        setFieldCombo(conditionField1, LinkElement::getTable1);
                        conditionField1.setItem(variable.getField1());
                        hBox.add(conditionField1);

                        conditionComparison.setItem(variable.getComparison());
                        hBox.add(conditionComparison);

                        setFieldCombo(conditionField2, LinkElement::getTable2);
                        conditionField2.setItem(variable.getField2());
                        hBox.add(conditionField2);

                        return hBox;
                    }

                    private void setFieldCombo(ComboBox<String> comboBox, Function<LinkElement, String> getter) {
                        comboBox.removeAllItems();
                        var selectedObject = joinTable.getSelectedObject();
                        if (Objects.isNull(selectedObject)) {
                            return;
                        }
                        var table = getter.apply(selectedObject);
                        tables.stream()
                                .filter(x -> {
                                    var userObject = x.getUserObject();
                                    return userObject.getName().equals(table)
                                            || (Objects.nonNull(userObject.getAlias()) && userObject.getAlias().equals(table));
                                })
                                .forEach(x -> x.children().asIterator().forEachRemaining(y -> {
                                    var userObject = (TableElement) y.getUserObject();
                                    comboBox.addItem(userObject.getName());
                                }));
                    }
                };
            }
        };

        joinTableModel = new ListTableModel<>(
                table1Info, allTable1Info, table2Info, allTable2Info, customInfo, linkingConditionInfo
        );

        joinTable = new TableView<>(joinTableModel);
        var decorator = ToolbarDecorator.createDecorator(joinTable);
        decorator.setAddAction(button -> {
            var item = new LinkElement();
            item.setComparison("=");
            joinTableModel.addRow(item);
        });

        this.component = decorator.createPanel();
    }

    @NotNull
    private DefaultCellEditor availableTablesEditor() {
        return new DefaultCellEditor(
                new ComboBox<>(tables.stream()
                        .map(x -> {
                            TableElement userObject = x.getUserObject();
                            return Objects.nonNull(userObject.getAlias()) ? userObject.getAlias() : userObject.getName();
                        })
                        .toArray(String[]::new))
        );
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
        ComponentUtils.loadTableToTable(union.getJoinTableModel(), joinTableModel);
    }

    private void saveQueryData(FullQuery fullQuery, String cteName, int id) {
        var union = fullQuery.getCte(cteName).getUnion("" + id);
        ComponentUtils.loadTableToTable(joinTableModel, union.getJoinTableModel());
        ComponentUtils.clearTable(joinTableModel);
        tables.clear();
    }

    private void removeSelectedTable(QBTreeNode node) {
        var tableElement = node.getUserObject();
        for (var i = joinTableModel.getItems().size() - 1; i >= 0; i--) {
            if (joinTableModel.getItem(i).getTable1().equals(tableElement.getName())
                    || joinTableModel.getItem(i).getTable2().equals(tableElement.getName())) {
                joinTableModel.removeRow(i);
            }
        }
        tables.remove(node);
    }

    private void addSelectedTable(QBTreeNode node) {
        if (Objects.isNull(node.getParent())) {
            return;
        }
        if (Objects.isNull(node.getParent().getParent())) {
            tables.add(node);
            return;
        }
        tables.add(node.getParent());
    }
}
