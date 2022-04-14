package org.quebee.com.panel;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import java.util.Random;

import static org.quebee.com.panel.OrderPanel.getTitleColumnInfo;

@Getter
public class LinksPanel implements QueryComponent {

    private final String header = "Links";
    private final JComponent component;

    public LinksPanel() {
        ListTableModel model = new ListTableModel(new ColumnInfo[]{getTitleColumnInfo("Test")});
        TableView table = new TableView(model);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            model.addRow(new DefaultMutableTreeTableNode("test" + new Random(1000).nextInt()));
            //    model.reload();
        });
        decorator.setRemoveAction(button -> {
            System.out.println(button);
            // myTableModel.addRow();
        });
        this.component = decorator.createPanel();
    }

}
