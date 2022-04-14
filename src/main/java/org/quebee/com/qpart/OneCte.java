package org.quebee.com.qpart;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.quebee.com.util.Constants.UNION_0;

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

    public OneCte(String cteName, Integer order) {
        this.cteName = cteName;
        this.order = order;
        unionMap.put(UNION_0, new Union(0));
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
