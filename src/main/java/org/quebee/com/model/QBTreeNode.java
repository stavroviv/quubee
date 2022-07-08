package org.quebee.com.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QBTreeNode extends DefaultMutableTreeTableNode implements Transferable {

    private final TableElement usObject;

    public QBTreeNode(TableElement userObject) {
        super(userObject);
        this.usObject = userObject;
    }

    public List<QBTreeNode> nodeToList() {
        var actualList = new ArrayList<QBTreeNode>();
        children().asIterator().forEachRemaining(x -> actualList.add((QBTreeNode) x));
        return actualList;
    }

    @Override
    public QBTreeNode getParent() {
        return (QBTreeNode) this.parent;
    }

    public QBTreeNode getChildAt(int childIndex) {
        return (QBTreeNode) this.children.get(childIndex);
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

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[0];
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return null;
    }
}
