package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.quebee.com.model.AliasElement;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class OneCte implements Orderable {
    private String cteName;
    private int order;
    private Map<String, Union> unionMap = new LinkedHashMap<>();
//    private Map<String, TableColumn<AliasRow, String>> unionColumns = new HashMap<>();

    private ListTableModel<AliasElement> aliasTable = new ListTableModel<>();
    //    private TableView<TableRow> unionTable = new TableView<>();
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
        if (selectBody instanceof SetOperationList) {
            var body = (SetOperationList) selectBody;
            var i = 0;
            for (var select : body.getSelects()) {
                unionMap.put("" + i, new Union((PlainSelect) select, i));
                i++;
            }
        } else {
            unionMap.put("0", new Union((PlainSelect) selectBody, 0));
        }
        loadAliasTable();
    }

    private void loadAliasTable() {
        var firstUnion = getUnion("0");
        for (var i = 0; i < firstUnion.getSelectedFieldsModel().getRowCount(); i++) {
            var aliasElement = new AliasElement();
            int j = 0;
            for (var s : getUnionMap().keySet()) {
                var value = getUnion(s);
                var item = value.getSelectedFieldsModel().getItem(i);
                if (j == 0) {
                    aliasElement.setAliasName(item.getNameOrAlias());
                }
                aliasElement.putAlias("Union " + j, item.getPsTable().getName() + "." + item.getColumnName());
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
