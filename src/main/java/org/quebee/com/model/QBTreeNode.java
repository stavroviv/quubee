package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

public class QBTreeNode extends DefaultMutableTreeTableNode {

    private final TableElement usObject;

    public QBTreeNode(TableElement userObject) {
        super(userObject);
        this.usObject = userObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QBTreeNode that = (QBTreeNode) o;
        return usObject.getId().equals(that.getUserObject().getId());
    }

    @Override
    public TableElement getUserObject() {
        return usObject;
    }

    @Override
    public int hashCode() {
        return getUserObject().hashCode();
    }
}
