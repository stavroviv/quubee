package org.quebee.com.qpart;

import com.intellij.ui.dualView.TreeTableView;
import com.intellij.ui.table.TableView;
import lombok.Getter;
import lombok.Setter;
import org.quebee.com.model.ConditionElement;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.TableRow;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;
//    private TreeTableView<TableRow> tablesView = new TreeTableView<>();
    private TableView<TableRow> fieldTable = new TableView<>();

    private TableView<LinkElement> linkTable = new TableView<>();

//    private TreeTableView<TableRow> groupFieldsTree = new TreeTableView<>();
    private TableView<TableRow> groupTableResults = new TableView<>();
    private TableView<TableRow> groupTableAggregates = new TableView<>();

//    private TreeTableView<TableRow> conditionsTreeTable = new TreeTableView<>();
    private TableView<ConditionElement> conditionTableResults = new TableView<>();

    public Union(Integer order) {
        this.order = order;
//        tablesView.setRoot(new TreeItem<>());
    }
}
