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
    private String name;
    private String alias;

    private boolean root;
    private boolean notSelectable;
    private boolean nested;

//    private boolean cteRoot;

    // FIXME
    private boolean table;
    private boolean cte;
    private boolean column;

    private boolean distinct;

    private String description;

    public TableElement(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    private String tableName;
    private String columnName;

    public TableElement(FromItem fromItem) {
        if (fromItem instanceof Table) {
            Table fromItem1 = (Table) fromItem;
            this.name = fromItem1.getName();
        }
        this.alias = Objects.nonNull(fromItem.getAlias()) ? fromItem.getAlias().getName() : null;
        this.id = UUID.randomUUID();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public TableElement(TableElement tableElement) {
        this.name = tableElement.getName();
        this.alias = tableElement.getAlias();
        this.columnName = tableElement.getColumnName();
        this.tableName = tableElement.getTableName();
        this.table = tableElement.isTable();
        this.column = tableElement.isColumn();
        this.id = UUID.randomUUID();
    }

    public TableElement(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getDescription() {
        if (Objects.nonNull(tableName) && Objects.nonNull(columnName)) {
            return tableName + "." + columnName;
        }
        return getNameWithAlias();
    }

    public String getNameWithAlias() {
        return Objects.nonNull(alias) ? alias : name;
    }

    public static class Renderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            var userObject = ((MutableTreeTableNode) value).getUserObject();
            if (!(userObject instanceof TableElement)) {
                return;
            }
            var element = (TableElement) userObject;
            if (element.isTable()) {
                setIcon(DatabaseIcons.Table);
            } else if (element.isCte()) {
                setIcon(DatabaseIcons.Tablespace);
            } else if (element.isColumn()) {
                setIcon(DatabaseIcons.Col);
            } else {
                setIcon(DatabaseIcons.ObjectGroup);
            }
            append(element.getDescription());
        }
    }
}
