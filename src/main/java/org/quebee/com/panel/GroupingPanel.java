package org.quebee.com.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;
import org.quebee.com.qpart.FullQuery;
import org.quebee.com.util.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static org.quebee.com.notifier.SaveQueryDataNotifier.SAVE_QUERY_DATA;
import static org.quebee.com.notifier.SelectedFieldAddNotifier.SELECTED_FIELD_ADD;
import static org.quebee.com.notifier.SelectedTableAfterAddNotifier.SELECTED_TABLE_AFTER_ADD;

@Getter
public class GroupingPanel implements QueryComponent {
    private final String header = "Grouping";
    private final JBSplitter component = new JBSplitter();

    public GroupingPanel() {
        component.setProportion(0.3f);
        component.setFirstComponent(getAvailableGroupingFieldsTree());
        component.setSecondComponent(getGroupingAggregatesPanel());
    }

    @Override
    public void initListeners(Disposable disposable) {
        var bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect(disposable).subscribe(SELECTED_TABLE_AFTER_ADD, this::addSelectedTable);
        bus.connect(disposable).subscribe(SELECTED_FIELD_ADD, this::addSelectedField);
        bus.connect(disposable).subscribe(SAVE_QUERY_DATA, this::saveQueryData);
    }

    private void saveQueryData(FullQuery fullQuery, String s, int id) {
        for (var i = groupingRoot.getChildCount() - 2; i >= 0; i--) {
            groupingRoot.remove(i);
        }
        ComponentUtils.clearTree(allFieldsRoot);
        groupingModel.reload();
    }

    private void addSelectedField(QBTreeNode node) {
        var parent =  node.getParent().getUserObject();
        var userObject = node.getUserObject();
        var tableElement = new TableElement(parent.getName() + "." + userObject.getName(), parent.getId());
        tableElement.setColumn(true);
        groupingRoot.insert(new QBTreeNode(tableElement), groupingRoot.getChildCount() - 1);
        groupingModel.nodesWereInserted(groupingRoot, new int[]{groupingRoot.getChildCount() - 2});
    }

    private void addSelectedTable(QBTreeNode node) {
        var userObject = node.getUserObject();
        var newUserObject = new TableElement(userObject.getName());
        newUserObject.setTable(true);
        newUserObject.setId(userObject.getId());
        var newTableNode = new QBTreeNode(newUserObject);
        node.children().asIterator()
                .forEachRemaining(x -> newTableNode.add(new QBTreeNode((TableElement) x.getUserObject())));
        allFieldsRoot.add(newTableNode);
        groupingModel.nodesWereInserted(allFieldsRoot, new int[]{allFieldsRoot.getChildCount() - 1});
    }

    private JComponent getGroupingAggregatesPanel() {
        var splitter = new JBSplitter();
        splitter.setFirstComponent(getGroupingTable());
        splitter.setSecondComponent(getAggregateTable());
        splitter.setOrientation(true);
        return splitter;
    }

    private ListTableModel<TableElement> aggregateTableModel;

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
        aggregateTableModel = new ListTableModel<>(
                columnInfoAggregate,
                columnInfoFunction
        );

        var aggregateTable = new TableView<>(aggregateTableModel);
        var decorator = ToolbarDecorator.createDecorator(aggregateTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    private ListTableModel<TableElement> groupingTableModel;

    @NotNull
    private JComponent getGroupingTable() {
        var columnInfo = new ColumnInfo<TableElement, String>("Grouping Field") {

            @Override
            public @Nullable String valueOf(TableElement o) {
                return Objects.isNull(o) ? "" : o.getName();
            }
        };

        groupingTableModel = new ListTableModel<>(
                columnInfo
        );
        var groupingTable = new JBTable(groupingTableModel);
        var decorator = ToolbarDecorator.createDecorator(groupingTable);
        var panel = decorator.createPanel();
        decorator.getActionsPanel().setVisible(false);
        return panel;
    }

    private QBTreeNode groupingRoot;
    private QBTreeNode allFieldsRoot;
    private ListTreeTableModel groupingModel;

    private JComponent getAvailableGroupingFieldsTree() {
        groupingRoot = new QBTreeNode(new TableElement("empty"));
        allFieldsRoot = new QBTreeNode(new TableElement("All fields"));
        groupingRoot.add(allFieldsRoot);
        groupingModel = new ListTreeTableModel(groupingRoot, new ColumnInfo[]{
                new TreeColumnInfo("Fields")
        });
        var table = new TreeTable(groupingModel);
        table.setTreeCellRenderer(new TableElement.Renderer());
        table.setRootVisible(false);

        var decorator = ToolbarDecorator.createDecorator(table);
        var panel = decorator.createPanel();

        var hBox = Box.createHorizontalBox();

        hBox.add(panel);
        hBox.add(Box.createHorizontalStrut(5));

        var comp = Box.createVerticalBox();
      //  comp.setBorder(new LineBorder(JBColor.RED, 1));
        comp.setPreferredSize(new Dimension(30, 300));
        comp.add(Box.createVerticalStrut(10));
        comp.add(smallButton(">"));
        comp.add(smallButton(">>"));
        comp.add(smallButton("<"));
        comp.add(smallButton("<<"));
        comp.add(Box.createVerticalStrut(30));
        comp.add(smallButton(">"));
        comp.add(smallButton(">>"));
        comp.add(smallButton("<"));
        comp.add(smallButton("<<"));
        comp.add(Box.createVerticalStrut(10));

        hBox.add(comp);

        return hBox;
    }
    
    private JButton smallButton(String text) {
        var button = new JButton(text);
        button.setMaximumSize(new Dimension(50, 30));
        return button;
    }
}
