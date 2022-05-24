package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.quebee.com.notifier.SelectedTableAfterAddNotifier.SELECTED_TABLE_AFTER_ADD;
import static org.quebee.com.notifier.SelectedTableRemoveNotifier.SELECTED_TABLE_REMOVE;

@Getter
public class LinksPanel implements QueryComponent {

    private final String header = "Links";
    private final JComponent component;
    private final Set<TreeTableNode> tables = new HashSet<>();
    private final ListTableModel<LinkElement> linkTableModel;
    private final TableView<LinkElement> linkTable;

    public LinksPanel() {
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
                return o.getTable1();
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
//                setModified();
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
                return o.getTable2();
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
//                setModified();
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
//                setModified();
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
                    private final ComboBox<String> conditionField2 = new ComboBox<>(
                            new String[]{"test_1", "test_2", "test_3"}
                    );
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
                        Box hBox = Box.createHorizontalBox();
//                        conditionLeftCombo.setPreferredSize(new Dimension(200, 15));
                        setFieldCombo();
                        conditionField1.setItem(variable.getField1());
                        hBox.add(conditionField1);

                        conditionComparison.setItem(variable.getComparison());
                        hBox.add(conditionComparison);

                        conditionField2.setItem(variable.getField2());
                        hBox.add(conditionField2);
                        return hBox;
                    }

                    private void setFieldCombo() {
                        conditionField1.removeAllItems();
                        var selectedObject = linkTable.getSelectedObject();
                        if (selectedObject == null) {
                            return;
                        }
                        tables.stream()
                                .filter(x -> ((TableElement) x.getUserObject()).getName().equals(selectedObject.getTable1()))
                                .forEach(x -> x.children().asIterator().forEachRemaining(y -> {
                                    var userObject = (TableElement) y.getUserObject();
                                    conditionField1.addItem(userObject.getName());
                                }));
                    }
                };
            }
        };

        linkTableModel = new ListTableModel<>(
                table1Info, allTable1Info, table2Info, allTable2Info, customInfo, linkingConditionInfo
        );

        linkTable = new TableView<>(linkTableModel);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(linkTable);
        decorator.setAddAction(button -> {
            LinkElement item = new LinkElement();
            item.setComparison("=");
            linkTableModel.addRow(item);
        });

        this.component = decorator.createPanel();
        initListeners();
    }

    @NotNull
    private DefaultCellEditor availableTablesEditor() {
        var comboBox = new ComboBox<>(tables.stream()
                .map(x -> ((TableElement) x.getUserObject()).getName())
                .toArray(String[]::new));
//        comboBox.setRenderer(new DefaultListCellRenderer() {
//            @Override
//            public Component getListCellRendererComponent(JList list, Object value, int index,
//                                                          boolean isSelected, boolean cellHasFocus) {
//                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                TableElement elementValue = (TableElement) value;
//                setText(elementValue.getName());
//                return this;
//            }
//        });
//        comboBox.addActionListener(e -> {
//            @SuppressWarnings("unchecked")
//            var comboBox1 = (ComboBox<TableElement>) e.getSource();
//            var item = (TableElement) comboBox1.getSelectedItem();
//            if (Objects.isNull(linkTable.getSelectedObject()) || Objects.isNull(item)) {
//                return;
//            }
//            linkTable.getSelectedObject().setTable1Id(item.getId());
//        });

        return new DefaultCellEditor(comboBox);
    }

    private void initListeners() {
        var bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect().subscribe(SELECTED_TABLE_AFTER_ADD, this::addSelectedTable);
        bus.connect().subscribe(SELECTED_TABLE_REMOVE, this::removeSelectedTable);
    }

    private void removeSelectedTable(QBTreeNode node) {
        TableElement tableElement = (TableElement) node.getUserObject();
        for (int i = linkTableModel.getItems().size() - 1; i >= 0; i--) {
            if (linkTableModel.getItem(i).getTable1().equals(tableElement.getName())
                    || linkTableModel.getItem(i).getTable2().equals(tableElement.getName())) {
                linkTableModel.removeRow(i);
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
