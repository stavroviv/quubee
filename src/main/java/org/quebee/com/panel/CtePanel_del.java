package org.quebee.com.panel;

import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.quebee.com.columns.EditableStringColumn;
import org.quebee.com.model.CteElement_del;
import org.quebee.com.notifier.LoadAllCteDataNotifier_del;
import org.quebee.com.qpart.FullQuery;

import javax.swing.*;

@Getter
public class CtePanel_del extends AbstractQueryPanel {
    private final String header = header();
    private final JComponent component;

    public CtePanel_del(MainPanel mainPanel) {
        super(mainPanel);
        this.component = getCteTable();
    }

    @Override
    public String getTooltipText() {
        return "Common table expressions";
    }

    public static String header() {
        return "CTE";
    }

    private ListTableModel<CteElement_del> cteTablemodel;
    private int maxUnion;

    private JComponent getCteTable() {
        cteTablemodel = new ListTableModel<>(
                new EditableStringColumn<>("Name", CteElement_del::getName, CteElement_del::setName)
        );
        var table = new TableView<>(cteTablemodel);

        var decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            var name = "Table_expression_" + (maxUnion + 1);
            cteTablemodel.addRow(new CteElement_del(name));
            table.editCellAt(cteTablemodel.getRowCount() - 1, 0);
            mainPanel.addCte(name);
            maxUnion++;
        });
        decorator.setMoveDownAction(button -> {
            TableUtil.moveSelectedItemsDown(table);
        });
        decorator.setMoveUpAction(button -> {
            TableUtil.moveSelectedItemsUp(table);
        });
        decorator.setRemoveAction(button -> {
            TableUtil.removeSelectedItems(table);
        });
        decorator.setRemoveActionUpdater(e -> {
            if (table.getSelectedRow() == -1) {
                return false;
            }
            return cteTablemodel.getRowCount() > 1;
        });
        return decorator.createPanel();
    }

    @Override
    public void initListeners() {
        subscribe(LoadAllCteDataNotifier_del.class, this::loadQueryData);
    }

    private void loadQueryData(FullQuery fullQuery) {
        fullQuery.getCteNames().forEach(x -> cteTablemodel.addRow(new CteElement_del(x)));
        maxUnion = fullQuery.getCteNames().size() + 1;
    }
}
