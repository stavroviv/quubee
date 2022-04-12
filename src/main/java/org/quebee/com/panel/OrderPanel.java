package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import java.util.Random;

import static org.quebee.com.notifier.QuiBuiNotifier.QUI_BUI_TOPIC;

public class OrderPanel extends JBSplitter {
    public static final String HEADER = "Order";

    public OrderPanel() {
        this.setProportion(0.3f);
        this.setFirstComponent(getFieldsTable());
        this.setSecondComponent(getOrderTable());
        init(ApplicationManager.getApplication().getMessageBus());
    }

    public void init(MessageBus bus) {
        bus.connect().subscribe(QUI_BUI_TOPIC, context -> {
            System.out.println(context);
            System.out.println(context);
        });
    }

    private JComponent getOrderTable() {
        ListTableModel model = new ListTableModel(new ColumnInfo[]{
                getTitleColumnInfo("Field"),
                getTitleColumnInfo("Sorting")
        });
        TableView table = new TableView(model);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            model.addRow(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
            //    model.reload();
        });
        decorator.setMoveDownAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        return decorator.createPanel();
    }

    private JComponent getFieldsTable() {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("test");
        ListTreeTableModel model = new ListTreeTableModel(root, new ColumnInfo[]{getTitleColumnInfo("Fields")});
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

    static ColumnInfo<Object, String> getTitleColumnInfo(String name) {
        return new ColumnInfo<>(name) {

            @Nullable
            @Override
            public String valueOf(Object o) {
                return o.toString();
            }

            @Override
            public Class<?> getColumnClass() {
                return TreeTableModel.class;
            }
        };
    }
}
