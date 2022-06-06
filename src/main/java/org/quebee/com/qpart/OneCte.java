package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperation;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.UnionOp;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.TableElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class OneCte implements Orderable {
    private String cteName;
    private int order;
    private Map<String, Union> unionMap = new LinkedHashMap<>();
//    private Map<String, TableColumn<AliasRow, String>> unionColumns = new HashMap<>();

    private ListTableModel<AliasElement> aliasTable = new ListTableModel<>();
    private ListTableModel<TableElement> unionTable = new ListTableModel<>();
    //
//    private TreeTableView<TableRow> orderFieldsTree = new TreeTableView<>();
//    private TableView<TableRow> orderTableResults = new TableView<>();
    private int curMaxUnion;

    public Union getUnion(String unionNumber) {
        return unionMap.get(unionNumber);
    }

    public OneCte(String cteName, SelectBody selectBody, Integer order) {
        this.cteName = cteName;
        this.order = order;

        unionTable.addRow(new TableElement("Union " + 0));
        if (selectBody instanceof SetOperationList) {
            var body = (SetOperationList) selectBody;
            var i = 0;
            for (var select : body.getSelects()) {
                unionMap.put("" + i, new Union(select, i));
                i++;
            }
            loadUnionTable(body.getOperations());
        } else {
            unionMap.put("0", new Union(selectBody, 0));
        }
        loadAliasTable();
    }

    private void loadUnionTable(List<SetOperation> operations) {
        var i = 1;
        for (var operation : operations) {
            var item = new TableElement("Union " + i);
            item.setDistinct(!((UnionOp) operation).isAll());
            unionTable.addRow(item);
            i++;
        }
    }

    private void loadAliasTable() {
        var firstUnion = getUnion("0");
        for (var i = 0; i < firstUnion.getSelectedFieldsModel().getRowCount(); i++) {
            var aliasElement = new AliasElement();
            var j = 0;
            for (var s : getUnionMap().keySet()) {
                var value = getUnion(s);
                var item = value.getSelectedFieldsModel().getItem(i);
                if (j == 0) {
                    aliasElement.setAliasName(Objects.isNull(item.getAlias()) ? item.getColumnName() : item.getAlias());
                }
                aliasElement.putAlias("Union " + j, item.getTableName() + "." + item.getColumnName());
                j++;
            }
            aliasTable.addRow(aliasElement);
        }
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    @Override
    public void setOrder(Integer order) {
        this.order = order;
    }
}
