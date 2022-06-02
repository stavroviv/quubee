package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

import java.util.Objects;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;

    // from tables
    private QBTreeNode selectedTablesRoot = new QBTreeNode(new TableElement("empty"));
    private ListTableModel<TableElement> selectedFieldsModel = new ListTableModel<>();

    // joins
    private ListTableModel<LinkElement> joinTableModel = new ListTableModel<>();

    // grouping
    private QBTreeNode groupingRoot;
    private ListTableModel<TableElement> groupingTableModel = new ListTableModel<>();
    private ListTableModel<TableElement> aggregateTableModel = new ListTableModel<>();

    public Union(PlainSelect select, Integer order) {
        this.order = order;
        fillFromTablesModels(select);
        fillSelectedFieldsModels(select);
    }

    private void fillSelectedFieldsModels(PlainSelect select) {
        for (var selectItem : select.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }
            var selectExpressionItem = (SelectExpressionItem) selectItem;
            var expression = selectExpressionItem.getExpression();
            if (expression instanceof Column) {
                var column = (Column) expression;
                var item = new TableElement(column.getTable(), column.getColumnName());
                if (Objects.nonNull(selectExpressionItem.getAlias())) {
                    item.setAlias(selectExpressionItem.getAlias().getName());
                }
                selectedFieldsModel.addRow(item);
            }
        }
    }

    private void fillFromTablesModels(PlainSelect select) {
        var name = select.getFromItem(Table.class).getFullyQualifiedName();
        selectedTablesRoot.add(new QBTreeNode(new TableElement(name)));
        var joins = select.getJoins();
        if (Objects.isNull(joins)) {
            return;
        }
        for (var join : joins) {
            var rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                selectedTablesRoot.add(new QBTreeNode(new TableElement(rightItem.toString())));
            }
        }
    }
}
