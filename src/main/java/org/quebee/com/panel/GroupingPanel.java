package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import java.util.Objects;

@Getter
public class GroupingPanel  implements QueryComponent {
    private final String header = "Grouping";
    private final JBSplitter component = new JBSplitter();

    public GroupingPanel() {
        component.setProportion(0.3f);
        component.setFirstComponent(getFieldsTable());
        component.setSecondComponent(getGroupingAggregatesPanel());
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
//        bus.connect().subscribe(QUI_BUI_TOPIC, context -> {
//            System.out.println(context);
//            System.out.println(context);
//        });
    }

    private JComponent getGroupingAggregatesPanel() {
        var splitter = new JBSplitter();
        splitter.setFirstComponent(getGroupingTable());
        splitter.setSecondComponent(getAggregateTable());
        splitter.setOrientation(true);
        return splitter;
    }

    @NotNull
    private JComponent getAggregateTable() {
        var columnInfoAggregate = new ColumnInfo<TableElement, String>("Aggregate Field") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };
        var columnInfoFunction = new ColumnInfo<TableElement, String>("Function") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };
        var modelAggregate = new ListTableModel<TableElement>(
                columnInfoAggregate,
                columnInfoFunction
        );
        modelAggregate.addRow(new TableElement("test"));
        modelAggregate.addRow(new TableElement("test2"));
        modelAggregate.addRow(new TableElement("test3"));
        var aggregateTable = new TableView<>(modelAggregate);
//        JTableHeader tableHeader = new JTableHeader();
//        tableHeader.setColumnModel(new DefaultTableColumnModel());
//        aggregateTable.setTableHeader(tableHeader);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(aggregateTable);
        return decorator.createPanel();
    }

    @NotNull
    private JComponent getGroupingTable() {
        var columnInfo = new ColumnInfo<TableElement, String>("Grouping Field") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };

        var model = new ListTableModel<TableElement>(
                columnInfo
        );
        var groupingTable = new JBTable(model);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(groupingTable);
//        decorator.getActionsPanel().

        JPanel panel = decorator.createPanel();
        decorator.getActionsPanel().getToolbar().getActions().clear();
        return panel;
    }

    private JComponent getFieldsTable() {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");
        ListTreeTableModel model = new ListTreeTableModel(root, new ColumnInfo[]{new ColumnInfo<Object, String>("Fields") {

            @Nullable
            @Override
            public String valueOf(Object o) {
                return o.toString();
            }

            @Override
            public Class<?> getColumnClass() {
                return TreeTableModel.class;
            }
        }});
        TreeTable table = new TreeTable(model);
        table.setTreeCellRenderer(new TableElement.Renderer());
        JBScrollPane jbScrollPane = new JBScrollPane(table);
//
//        ListTableModel model = new ListTableModel(new ColumnInfo[]{getTitleColumnInfo("Fields")});
//        TableView table = new TableView(model);

//        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
//        decorator.setAddAction(button -> {
//            model.addRow(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
//            //    model.reload();
//        });
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
        return jbScrollPane;
    }

}
