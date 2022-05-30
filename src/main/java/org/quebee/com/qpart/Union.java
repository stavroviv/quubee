package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;

    // from tables
    private QBTreeNode selectedTablesRoot = new QBTreeNode(new TableElement("empty"));
    private ListTableModel<TableElement> selectedFieldsModel = new ListTableModel<>();

    // joins
    private ListTableModel<LinkElement> joinTableModel;

    // grouping
    private QBTreeNode groupingRoot;
    private ListTableModel<TableElement> groupingTableModel;
    private ListTableModel<TableElement> aggregateTableModel;

    public Union(PlainSelect select, Integer order) {
        this.order = order;
        fillFromTablesModels(select);
        fillSelectedFieldsModels(select);
    }

    private void fillSelectedFieldsModels(PlainSelect select) {
        for (SelectItem selectItem : select.getSelectItems()) {
            selectedFieldsModel.addRow(new TableElement(selectItem.toString()));
        }
    }

    private void fillFromTablesModels(PlainSelect select) {
        String name = select.getFromItem(Table.class).getFullyQualifiedName();
        selectedTablesRoot.add(new QBTreeNode(new TableElement(name)));
        List<Join> joins = select.getJoins();
        if (Objects.isNull(joins)) {
            return;
        }
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                selectedTablesRoot.add(new QBTreeNode(new TableElement(rightItem.toString())));
            }
        }
    }
}
