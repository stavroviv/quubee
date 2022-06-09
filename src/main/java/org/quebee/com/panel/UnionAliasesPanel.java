package org.quebee.com.panel;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.notifier.*;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.qpart.OneCte;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

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
        initAliasTableModel();
        var aliasTable = new TableView<>(aliasTableModel);
        var decorator = ToolbarDecorator.createDecorator(aliasTable);
        decorator.disableAddAction();
        return decorator.createPanel();
    }

    private void initAliasTableModel() {
        var nameInfo = new ColumnInfo<AliasElement, String>("Field Name") {

            @Override
            public String valueOf(AliasElement o) {
                return Objects.isNull(o) ? "" : o.getAliasName();
            }

            @Override
            public boolean isCellEditable(AliasElement aliasElement) {
                return true;
            }

            @Override
            public void setValue(AliasElement variable, String value) {
                variable.setAliasName(value);
            }
        };

        aliasTableModel = new ListTableModel<>(nameInfo);
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
        var isDistinctInfo = new ColumnInfo<TableElement, Boolean>("Distinct") {
            @Override
            public int getWidth(JTable table) {
                return 50;
            }

            @Override
            public @NotNull Boolean valueOf(TableElement o) {
                return o.isDistinct();
            }

            @Override
            public void setValue(TableElement variable, Boolean value) {
                variable.setDistinct(value);
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(TableElement variable) {
                return true;
            }
        };
        unionTableModel = new ListTableModel<>(
                nameInfo,
                isDistinctInfo
        );

        unionTable = new TableView<>(unionTableModel);
        var decorator = ToolbarDecorator.createDecorator(unionTable);
        decorator.setAddAction(button -> addNewUnion(true));
        decorator.setRemoveAction(button -> removeUnion());
        decorator.setRemoveActionUpdater(
                e -> {
                    if (unionTable.getSelectedRow() == -1) {
                        return false;
                    }
                    return unionTableModel.getRowCount() > 1;
                }
        );
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
                String s = o.getAlias().get(getName());
                return Objects.isNull(s) ? "" : s;
            }
        };
        newColumnInfo[columnInfos.length] = aliasNameInfo;
        aliasTableModel.setColumnInfos(newColumnInfo);
        curMaxUnion++;
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

    private void addSelectedField(TableElement tableElement, boolean interactive) {
        if (!interactive) {
            return;
        }
        var item = new AliasElement();
        item.setAliasName(tableElement.getColumnName());
        item.setTableName(tableElement.getTableName());
        item.putAlias(mainPanel.getCurrentUnion(), tableElement.getDescription());
        aliasTableModel.addRow(item);
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
        ComponentUtils.loadTableToTable(cte.getAliasTable(), aliasTableModel);
        ComponentUtils.loadTableToTable(cte.getUnionTable(), unionTableModel);
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
