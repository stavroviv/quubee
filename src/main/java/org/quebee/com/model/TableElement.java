package org.quebee.com.model;

import com.intellij.ui.ColoredTreeCellRenderer;
import icons.DatabaseIcons;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
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

    private boolean distinct;

    public TableElement(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    private Table psTable;
    private String columnName;

    public TableElement(FromItem fromItem) {
        if (fromItem instanceof Table) {
            Table fromItem1 = (Table) fromItem;
            this.name = fromItem1.getName();
        }
        this.alias = Objects.nonNull(fromItem.getAlias()) ? fromItem.getAlias().getName() : null;
        this.id = UUID.randomUUID();
    }

    public TableElement(QBTreeNode node) {
        this(node.getNameWithAlias());
    }

    public String getNameOrAlias() {
        return Objects.isNull(alias) ? columnName : alias;
    }

    public TableElement(Table table, String columnName) {
        this.psTable = table;
        this.columnName = columnName;
    }

    public String getName() {
        return Objects.nonNull(name) ? name : psTable.getName() + "." + columnName;
    }

    public static class Renderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object userObject = ((MutableTreeTableNode) value).getUserObject();
            if (!(userObject instanceof TableElement)) {
                return;
            }
            TableElement element = (TableElement) userObject;
            if (element.isTable()) {
                setIcon(DatabaseIcons.Table);
            } else if (element.isColumn()) {
                setIcon(DatabaseIcons.Col);
            } else {
                setIcon(DatabaseIcons.ObjectGroup);
            }
            append(Objects.nonNull(element.getAlias()) ? element.getAlias() : element.getName());
        }
    }
}
