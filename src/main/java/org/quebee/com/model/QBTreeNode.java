package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.Objects;

public class QBTreeNode extends DefaultMutableTreeTableNode {

    public QBTreeNode(Object userObject) {
        super(userObject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QBTreeNode that = (QBTreeNode) o;

        Object userObject = getUserObject();
        if (userObject instanceof TableElement) {
            TableElement thisUserObject = (TableElement) userObject;
            TableElement thatUserObject = (TableElement) that.getUserObject();
            return thisUserObject.getId().equals(thatUserObject.getId());
        }
        return Objects.equals(this, that);
    }

    @Override
    public int hashCode() {
        return getUserObject().hashCode();
    }
}
