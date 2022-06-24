package org.quebee.com.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.columns.EditableBooleanColumn;
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.qpart.OneCte;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.*;

@Getter
public class UnionAliasesPanel extends AbstractQueryPanel {
    private final String header = "Union/Aliases";
    private final JComponent component;

    public UnionAliasesPanel(MainPanel mainPanel) {
        super(mainPanel);
        this.component = createComponent();
    }

    private JComponent createComponent() {
        var component = new JBSplitter();
        component.setProportion(0.3f);
        component.setFirstComponent(getUnionTablePanel());
        component.setSecondComponent(getAliasTablePanel());
        return component;
    }

    private ListTableModel<AliasElement> aliasTableModel;

    private JComponent getAliasTablePanel() {
        var nameInfo = new EditableStringColumn<>("Field Name", AliasElement::getAliasName, AliasElement::setAliasName);
        aliasTableModel = new ListTableModel<>(nameInfo);
        var aliasTable = new TableView<>(aliasTableModel);
        aliasTable.getTableHeader().setReorderingAllowed(false);
        var decorator = ToolbarDecorator.createDecorator(aliasTable);
        decorator.disableAddAction();
        return decorator.createPanel();
    }

    private ListTableModel<TableElement> unionTableModel;
    private TableView<TableElement> unionTable;

    private JComponent getUnionTablePanel() {
        var nameInfo = new ColumnInfo<TableElement, String>("Name") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };
        var isDistinctInfo = new EditableBooleanColumn<>("Distinct", 50, TableElement::isDistinct, TableElement::setDistinct);

        unionTableModel = new ListTableModel<>(
                nameInfo,
                isDistinctInfo
        );

        unionTable = new TableView<>(unionTableModel);
        var decorator = ToolbarDecorator.createDecorator(unionTable);
        decorator.setAddAction(button -> addNewUnion(true));
        decorator.setRemoveAction(button -> removeUnion());
        decorator.setRemoveActionUpdater(e -> {
            if (unionTable.getSelectedRow() == -1) {
                return false;
            }
            return unionTableModel.getRowCount() > 1;
        });
        return decorator.createPanel();
    }

    private void removeUnion() {
        var unionRow = unionTable.getSelectedObject();
        if (Objects.isNull(unionRow) || unionTableModel.getRowCount() <= 1) {
            return;
        }
        var columnInfos = aliasTableModel.getColumnInfos();

        var columnIndex = unionTable.getSelectedRow() + 1;
        var result = new ColumnInfo[columnInfos.length - 1];
        System.arraycopy(columnInfos, 0, result, 0, columnIndex);
        if (columnInfos.length != columnIndex) {
            System.arraycopy(columnInfos, columnIndex + 1, result, columnIndex, columnInfos.length - columnIndex - 1);
        }
        aliasTableModel.setColumnInfos(result);

        mainPanel.removeUnion(Integer.parseInt(unionRow.getName().replace("Union ", "")));
        TableUtil.doRemoveSelectedItems(unionTable, unionTableModel, null);
    }

    private int curMaxUnion;

    private void addNewUnion(boolean interactive) {
        var newUnion = "Union " + curMaxUnion;
        if (interactive) {
            var item = new TableElement(newUnion);
            unionTableModel.addRow(item);
            mainPanel.addUnion(curMaxUnion);
        }
        var columnInfos = aliasTableModel.getColumnInfos();
        var newColumnInfo = Arrays.copyOf(columnInfos, columnInfos.length + 1);
        var aliasNameInfo = new ColumnInfo<AliasElement, String>(newUnion) {
            @Override
            public String valueOf(AliasElement o) {
                String value = o.getAlias().get(getName());
                return Objects.isNull(value) ? "" : value;
            }

            @Override
            public void setValue(AliasElement o, String value) {
                o.putAlias(getName(), value);
            }

            @Override
            public TableCellEditor getEditor(AliasElement linkElement) {
                return new AvailableFieldsEditor(getName());
            }

            @Override
            public boolean isCellEditable(AliasElement variable) {
                return true;
            }

            @Override
            public TableCellRenderer getRenderer(AliasElement aliasElement) {
                return (table, value, isSelected, hasFocus, row, column) ->
                        new JBTextField(aliasElement.getAlias().get(getName()));
            }
        };

        newColumnInfo[columnInfos.length] = aliasNameInfo;
        aliasTableModel.setColumnInfos(newColumnInfo);
        curMaxUnion++;
    }

    private class AvailableFieldsEditor extends AbstractTableCellEditor {
        private final String union;
        private ComboBox<String> stringComboBox;

        public AvailableFieldsEditor(String union) {
            this.union = union;
        }

        @Override
        public Object getCellEditorValue() {
            return stringComboBox.getItem();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            var fields = unionValues.get(union);
            if (Objects.isNull(fields)) {
                stringComboBox = new ComboBox<>();
                return stringComboBox;
            }

            stringComboBox = new ComboBox<>(fields.toArray(String[]::new));
            stringComboBox.setEditable(true);
            var clearExtension = ExtendableTextComponent.Extension.create(
                    AllIcons.Actions.Close, AllIcons.Actions.CloseHovered,
                    "Clear", () -> clearAliasValue()
            );
            stringComboBox.setEditor(new BasicComboBoxEditor() {
                @Override
                protected JTextField createEditorComponent() {
                    var ecbEditor = new ExtendableTextField();
                    ecbEditor.addExtension(clearExtension);
                    ecbEditor.setBorder(null);
                    return ecbEditor;
                }
            });
            return stringComboBox;
        }
    }

    private void clearAliasValue() {

    }

    @Override
    public void initListeners() {
        subscribe(LoadQueryCteDataNotifier.class, this::loadQueryData);
        subscribe(SaveQueryCteDataNotifier.class, this::saveQueryData);
        subscribe(SelectedFieldAddNotifier.class, this::addSelectedField);
        subscribe(SelectedFieldRemoveNotifier.class, this::removeSelectedField);
        subscribe(SelectedTableRemoveNotifier.class, this::removeSelectedTable);
    }

    private void removeSelectedTable(QBTreeNode node) {
        var userObject = node.getUserObject();
        var removeTableName = userObject.getDescription();
        for (var i = aliasTableModel.getItems().size() - 1; i >= 0; i--) {
            var name = aliasTableModel.getItem(i).getTableName();
            if (name.equals(removeTableName)) {
                aliasTableModel.removeRow(i);
            }
        }
    }

    private void removeSelectedField(TableElement tableElement) {
        var currentUnion = mainPanel.getCurrentUnion();
        var removeElement = tableElement.getDescription();
        for (var i = aliasTableModel.getItems().size() - 1; i >= 0; i--) {
            var name = aliasTableModel.getItem(i).getAlias().get(currentUnion);
            if (name.equals(removeElement)) {
                aliasTableModel.removeRow(i);
                break;
            }
        }
    }

    Map<String, List<String>> unionValues = new HashMap<>();

    private void addSelectedField(TableElement tableElement, boolean interactive) {
        if (!interactive) {
            return;
        }
        var item = new AliasElement();
        var columnName = tableElement.getColumnName();
        item.setAliasName(columnName);
        item.setTableName(tableElement.getTableName());
        var description = tableElement.getDescription();
        item.putAlias(mainPanel.getCurrentUnion(), description);
        aliasTableModel.addRow(item);

        addAliasToComboboxValues(description, mainPanel.getCurrentUnion());
    }

    private void addAliasToComboboxValues(String description, String union) {
        if (unionValues.get(union) == null) {
            unionValues.put(union, new ArrayList<>(List.of(description)));
        } else {
            unionValues.get(union).add(description);
        }
    }

    private void saveQueryData(FullQuery query, String cteName) {
        var cte = query.getCte(cteName);
        ComponentUtils.loadTableToTable(aliasTableModel, cte.getAliasTable());
        ComponentUtils.clearTable(unionTableModel);
        ComponentUtils.clearTable(aliasTableModel);
        curMaxUnion = 0;
    }

    private void loadQueryData(FullQuery fullQuery, String cteName) {
        var cte = fullQuery.getCte(cteName);
        if (Objects.isNull(cte)) {
            return;
        }
        clearAndAddUnionColumns(cte);
        loadAliasComboValues(cte);
        ComponentUtils.loadTableToTable(cte.getAliasTable(), aliasTableModel);
        ComponentUtils.loadTableToTable(cte.getUnionTable(), unionTableModel);
    }

    private void loadAliasComboValues(OneCte cte) {
        for (var entry : cte.getUnionMap().entrySet()) {
            for (var item : entry.getValue().getSelectedFieldsModel().getItems()) {
                addAliasToComboboxValues(item.getDescription(), "Union " + entry.getKey());
            }
        }
    }

    private void clearAndAddUnionColumns(OneCte cte) {
        var columnInfos = aliasTableModel.getColumnInfos();
        var newColumnInfo = Arrays.copyOf(columnInfos, 1);
        aliasTableModel.setColumnInfos(newColumnInfo);
        for (var i = 0; i < cte.getUnionMap().keySet().size(); i++) {
            addNewUnion(false);
        }
    }
}
