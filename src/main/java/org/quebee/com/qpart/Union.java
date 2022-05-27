package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;

    // from tables
    private QBTreeNode selectedTablesRoot;
    private ListTableModel<TableElement> selectedFieldsModel;

    // joins
    private ListTableModel<LinkElement> joinTableModel;

    // grouping
    private QBTreeNode groupingRoot;
    private ListTableModel<TableElement> groupingTableModel;
    private ListTableModel<TableElement> aggregateTableModel;

    public Union(Integer order) {
        this.order = order;
    }
}
