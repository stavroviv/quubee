package org.quebee.com.model;

import com.intellij.ui.ColoredTreeCellRenderer;
import icons.DatabaseIcons;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.UUID;

@Getter
@Setter
public class TableElement {

    private UUID id;
    private UUID parentId;
    private String name;
    private String alias;

    private boolean root;
    private boolean notSelectable;
    private boolean nested;
    private boolean cte;
    private boolean cteRoot;

    private boolean table;
    private boolean column;

    public TableElement(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

//    @SuppressWarnings("unused")
//    public TableElement(TableElement element) {
//        this.name = element.getName();
//        this.id = element.getId();
//    }

    public TableElement(String name, UUID parentId) {
        this(name);
        this.parentId = parentId;
    }

    private Table psTable;
    private String columnName;

    public TableElement(Table table, String columnName) {
        this.psTable = table;
        this.columnName = columnName;
    }

    public static class Renderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object userObject = ((MutableTreeTableNode) value).getUserObject();
            if (!(userObject instanceof TableElement)) {
                return;
            }
            TableElement value1 = (TableElement) userObject;
            if (value1.isTable()) {
                setIcon(DatabaseIcons.Table);
            } else if (value1.isColumn()) {
                setIcon(DatabaseIcons.Col);
            } else {
                setIcon(DatabaseIcons.ObjectGroup);
            }
            append(value1.getName());
        }
    }
}
