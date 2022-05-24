package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.Objects;

public class QBTreeNode  {
    private final DefaultMutableTreeTableNode node;

    public QBTreeNode(DefaultMutableTreeTableNode node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QBTreeNode that = (QBTreeNode) o;

        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }
}
