package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.statement.select.*;
import org.quebee.com.model.AliasElement;
import org.quebee.com.model.OrderElement;
import org.quebee.com.model.TableElement;

import java.util.*;

import static org.quebee.com.panel.OrderPanel.ASC;
import static org.quebee.com.panel.OrderPanel.DESC;

@Getter
@Setter
public class OneCte implements Orderable {
    private String cteName;
    private int order;
    private int curMaxUnion;

    private Map<String, Union> unionMap = new LinkedHashMap<>();

    private ListTableModel<AliasElement> aliasTable = new ListTableModel<>();
    private ListTableModel<TableElement> unionTable = new ListTableModel<>();
    private ListTableModel<OrderElement> orderTable = new ListTableModel<>();

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
        loadOrderTable(selectBody);
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
        var firstUnion = getFirstUnion();
        for (var i = 0; i < firstUnion.getSelectedFieldsModel().getRowCount(); i++) {
            var aliasElement = new AliasElement();
            var j = 0;
            for (var s : getUnionMap().keySet()) {
                var value = getUnion(s);
                var item = value.getSelectedFieldsModel().getItem(i);
                if (j == 0) {
                    aliasElement.setAliasName(Objects.isNull(item.getAlias()) ? item.getColumnName() : item.getAlias());
                }
                aliasElement.setTableName(item.getTableName());
                aliasElement.putAlias("Union " + j, item.getTableName() + "." + item.getColumnName());
                j++;
            }
            aliasTable.addRow(aliasElement);
        }
    }

    private void loadOrderTable(SelectBody selectBody) {
        if (selectBody instanceof SetOperationList) {
            addOrderElements(((SetOperationList) selectBody).getOrderByElements());
        } else if (selectBody instanceof PlainSelect) {
            addOrderElements(((PlainSelect) selectBody).getOrderByElements());
        }
    }

    private void addOrderElements(List<OrderByElement> orderByElements) {
        Optional.ofNullable(orderByElements)
                .orElseGet(ArrayList::new)
                .forEach(this::addOrderElement);
    }

    private void addOrderElement(OrderByElement orderByElement) {
        var item = new OrderElement();
        item.setField(orderByElement.getExpression().toString());
        item.setSorting(orderByElement.isAsc() ? ASC : DESC);
        orderTable.addRow(item);
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    @Override
    public void setOrder(Integer order) {
        this.order = order;
    }

    public void addUnion(int order) {
        unionMap.put("" + order, new Union(null, order));
    }

    public void removeUnion(int index) {
        unionMap.remove("" + index);
    }

    public Union getUnion(String unionNumber) {
        return unionMap.get(unionNumber);
    }

    public Union getFirstUnion() {
        var entry = unionMap.entrySet().iterator().next();
        var key = entry.getKey();
        return unionMap.get(key);
    }
}
