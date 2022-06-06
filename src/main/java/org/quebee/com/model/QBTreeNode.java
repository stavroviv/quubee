package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QBTreeNode extends DefaultMutableTreeTableNode {

    private final TableElement usObject;

    public QBTreeNode(TableElement userObject) {
        super(userObject);
        this.usObject = userObject;
    }

//    public QBTreeNode(Object userObject) {
//        super(userObject);
//        this.usObject = (TableElement) userObject;
//    }
//
//    public QBTreeNode(QBTreeNode node) {
//        super(node.getUserObject());
//        this.usObject = node.getUserObject();
//    }

    public List<QBTreeNode> nodeToList() {
        var actualList = new ArrayList<QBTreeNode>();
        children().asIterator().forEachRemaining(x -> actualList.add((QBTreeNode) x));
        return actualList;
    }

    @Override
    public QBTreeNode getParent() {
        return (QBTreeNode) this.parent;
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

    public String getNameWithAlias() {
        var parent = getParent().getUserObject();
        var userObject = getUserObject();
        return (Objects.nonNull(parent.getAlias()) ? parent.getAlias() : parent.getName())
                + "." + userObject.getName();
    }
}
