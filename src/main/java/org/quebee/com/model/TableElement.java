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
    private boolean cte;
    private boolean cteRoot;

    private boolean table;
    private boolean column;

    private boolean distinct;

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

    public TableElement(QBTreeNode node) {
        var parent = node.getParent().getUserObject();
        var userObject = node.getUserObject();
        var tableName = Objects.nonNull(parent.getAlias()) ? parent.getAlias() : parent.getName();
        this.name = tableName + "." + userObject.getName();
        this.setTableName(tableName);
        this.id = UUID.randomUUID();
    }

//    public String getNameOrAlias() {
//        return Objects.isNull(alias) ? columnName : alias;
//    }

    public TableElement(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getName() {
        return Objects.nonNull(name) ? name : tableName + "." + columnName;
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
