package org.quebee.com.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.TableElement;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.qpart.OneCte;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

import static org.quebee.com.notifier.LoadQueryCteDataNotifier.LOAD_QUERY_CTE_DATA;
import static org.quebee.com.notifier.SaveQueryCteDataNotifier.SAVE_QUERY_CTE_DATA;

@Getter
public class UnionAliasesPanel implements QueryComponent {
    private final String header = "Union/Aliases";
    private final JComponent component;

    public UnionAliasesPanel() {
        this.component = createComponent();
    }

    private JComponent createComponent() {
        JBSplitter component = new JBSplitter();
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

        var unionTable = new TableView<>(unionTableModel);
        var decorator = ToolbarDecorator.createDecorator(unionTable);
        decorator.setAddAction(button -> addNewUnion());
        return decorator.createPanel();
    }

    private int curMaxUnion;

    private void addNewUnion() {
        var item = new TableElement("Union " + curMaxUnion);
        unionTableModel.addRow(item);
        var columnInfos = aliasTableModel.getColumnInfos();
        var newColumnInfo = Arrays.copyOf(columnInfos, columnInfos.length + 1);
        var aliasNameInfo = new ColumnInfo<AliasElement, String>("Union " + curMaxUnion) {
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
    public void initListeners(Disposable disposable) {
        var bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect(disposable).subscribe(LOAD_QUERY_CTE_DATA, this::loadQueryData);
        bus.connect(disposable).subscribe(SAVE_QUERY_CTE_DATA, this::saveQueryData);
    }

    private void saveQueryData(FullQuery query, String s) {
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
        var firstUnion = cte.getUnion("0");
        for (var i = 0; i < firstUnion.getSelectedFieldsModel().getRowCount(); i++) {
            var aliasElement = new AliasElement();
            int j = 0;
            for (var s : cte.getUnionMap().keySet()) {
                var value = cte.getUnion(s);
                var item = value.getSelectedFieldsModel().getItem(i);
                if (j == 0) {
                    aliasElement.setAliasName(item.getNameOrAlias());
                }
                aliasElement.putAlias("Union " + j, item.getPsTable().getName() + "." + item.getColumnName());
                j++;
            }
            aliasTableModel.addRow(aliasElement);
        }
    }

    private void clearAndAddUnionColumns(OneCte cte) {
        var columnInfos = aliasTableModel.getColumnInfos();
        var newColumnInfo = Arrays.copyOf(columnInfos, 1);
        aliasTableModel.setColumnInfos(newColumnInfo);
        for (var i = 0; i < cte.getUnionMap().keySet().size(); i++) {
            addNewUnion();
        }
    }
}
