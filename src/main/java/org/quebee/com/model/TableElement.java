package org.quebee.com.model;

import com.intellij.ui.ColoredTreeCellRenderer;
import icons.DatabaseIcons;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.UUID;

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

    public TableElement(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
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

    public boolean isTable() {
        return table;
    }

    public void setTable(boolean table) {
        this.table = table;
    }

    public boolean isColumn() {
        return column;
    }

    public void setColumn(boolean column) {
        this.column = column;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isNotSelectable() {
        return notSelectable;
    }

    public void setNotSelectable(boolean notSelectable) {
        this.notSelectable = notSelectable;
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    public boolean isCte() {
        return cte;
    }

    public void setCte(boolean cte) {
        this.cte = cte;
    }

    public boolean isCteRoot() {
        return cteRoot;
    }

    public void setCteRoot(boolean cteRoot) {
        this.cteRoot = cteRoot;
    }
}
