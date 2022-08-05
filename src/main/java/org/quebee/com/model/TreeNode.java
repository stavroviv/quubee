package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.ArrayList;
import java.util.List;

public class TreeNode extends DefaultMutableTreeTableNode  {

    private final TableElement usObject;

    public TreeNode(TableElement userObject) {
        super(userObject);
        this.usObject = userObject;
    }

    public List<TreeNode> nodeToList() {
        var actualList = new ArrayList<TreeNode>();
        children().asIterator().forEachRemaining(x -> actualList.add((TreeNode) x));
        return actualList;
    }

    @Override
    public TreeNode getParent() {
        return (TreeNode) this.parent;
    }

    public TreeNode getChildAt(int childIndex) {
        return (TreeNode) this.children.get(childIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode that = (TreeNode) o;
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
