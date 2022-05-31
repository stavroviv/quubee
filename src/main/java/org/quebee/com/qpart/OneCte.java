package org.quebee.com.qpart;

import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class OneCte implements Orderable {
    private String cteName;
    private int order;
    private Map<String, Union> unionMap = new LinkedHashMap<>();
//    private Map<String, TableColumn<AliasRow, String>> unionColumns = new HashMap<>();

    //    private TableView<AliasRow> aliasTable = new TableView<>();
//    private TableView<TableRow> unionTable = new TableView<>();
//
//    private TreeTableView<TableRow> orderFieldsTree = new TreeTableView<>();
//    private TableView<TableRow> orderTableResults = new TableView<>();
    private int curMaxUnion;

    public Union getUnion(String unionNumber) {
        return unionMap.get(unionNumber);
    }

    public OneCte(String cteName, SelectBody body, Integer order) {
        this.cteName = cteName;
        this.order = order;
        if (body instanceof SetOperationList) {
            SetOperationList body1 = (SetOperationList) body;
            int i = 0;
            for (SelectBody select : body1.getSelects()) {
                unionMap.put("" + i, new Union((PlainSelect) select, i));
                i++;
            }
        } else {
            unionMap.put("0", new Union((PlainSelect) body, 0));
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
