package org.quebee.com.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.TableElement;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.quebee.com.notifier.SelectedTableAddNotifier.SELECTED_TABLE_ADD;
import static org.quebee.com.notifier.SelectedTableRemoveNotifier.SELECTED_TABLE_REMOVE;

@Getter
public class LinksPanel implements QueryComponent {

    private final String header = "Links";
    private final JComponent component;

//    private final ComboBox<String> tableComboBox = new ComboBox<>();

    private final Set<TreeTableNode> tables = new HashSet<>();

    public LinksPanel() {
        var table1Info = new ColumnInfo<LinkElement, String>("Table 1") {
            @Override
            public int getWidth(JTable table) {
                return 150;
            }

            @Override
            public void setValue(LinkElement linkElement, String value) {
                linkElement.setTable1(value);
            }

            @Override
            public @NotNull TableCellEditor getEditor(LinkElement linkElement) {
                return availableTablesEditor();
            }

            @Override
            public boolean isCellEditable(LinkElement linkElement) {
                return true;
            }

            @Override
            public @NotNull String valueOf(LinkElement o) {
                return o.getTable1();
            }

            @Override
            public Class<LinkElement> getColumnClass() {
                return LinkElement.class;
            }
        };

        var allTable1Info = new ColumnInfo<LinkElement, Boolean>("All") {
            @Override
            public int getWidth(JTable table) {
                return 25;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isAllTable1();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setAllTable1(value);
//                setModified();
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };

        var table2Info = new ColumnInfo<LinkElement, String>("Table 2") {
            @Override
            public int getWidth(JTable table) {
                return 150;
            }

            @Override
            public void setValue(LinkElement linkElement, String value) {
                linkElement.setTable2(value);
            }

            @Override
            public @NotNull TableCellEditor getEditor(LinkElement linkElement) {
                return availableTablesEditor();
            }

            @Override
            public boolean isCellEditable(LinkElement linkElement) {
                return true;
            }

            @Override
            public @NotNull String valueOf(LinkElement o) {
                return o.getTable2();
            }

            @Override
            public Class<LinkElement> getColumnClass() {
                return LinkElement.class;
            }
        };

        var allTable2Info = new ColumnInfo<LinkElement, Boolean>("All") {
            @Override
            public int getWidth(JTable table) {
                return 25;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isAllTable2();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setAllTable2(value);
//                setModified();
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };

        var customInfo = new ColumnInfo<LinkElement, Boolean>("Custom") {
            @Override
            public int getWidth(JTable table) {
                return 50;
            }

            @Override
            public @NotNull Boolean valueOf(LinkElement o) {
                return o.isCustom();
            }

            @Override
            public void setValue(LinkElement variable, Boolean value) {
                variable.setCustom(value);
//                setModified();
            }

            @Override
            public Class<Boolean> getColumnClass() {
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };
        var linkingConditionInfo = new ColumnInfo<LinkElement, String>("Linking Condition") {

//            @Override
//            public @NotNull TableCellEditor getEditor(LinkElement linkElement) {
//                return availableTablesEditor();
//            }

            @Override
            public @NotNull String valueOf(LinkElement o) {
                return "Test";
            }

            @Override
            public void setValue(LinkElement variable, String value) {
                //  variable.setCustom(value);
//                setModified();
            }

            @Override
            public Class<String> getColumnClass() {
                return String.class;
            }

            @Override
            public boolean isCellEditable(LinkElement variable) {
                return true;
            }
        };
        var model = new ListTableModel<>(
                table1Info, allTable1Info, table2Info, allTable2Info, customInfo, linkingConditionInfo
        );
        var table = new TableView<>(model);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(button -> {
            model.addRow(new LinkElement());
            //    model.reload();
        });
//        decorator.setRemoveAction(button -> {
//            System.out.println(button);
//            // myTableModel.addRow();
//        });
        this.component = decorator.createPanel();
        initListeners();
    }

    @NotNull
    private DefaultCellEditor availableTablesEditor() {
        return new DefaultCellEditor(
                new ComboBox<>(tables.stream()
                        .map(x -> ((TableElement) x.getUserObject()).getName())
                        .toArray(String[]::new))
        );
    }

    private void initListeners() {
        var bus = ApplicationManager.getApplication().getMessageBus();
        bus.connect().subscribe(SELECTED_TABLE_ADD, this::addSelectedTable);
        bus.connect().subscribe(SELECTED_TABLE_REMOVE, this::removeSelectedTable);
    }

    private void removeSelectedTable(MutableTreeTableNode node) {
        tables.remove(node);
    }

    private void addSelectedTable(MutableTreeTableNode node) {
        if (Objects.isNull(node.getParent())) {
            return;
        }
        if (Objects.isNull(node.getParent().getParent())) {
            tables.add(node);
            return;
        }
        tables.add(node.getParent());
    }
}
