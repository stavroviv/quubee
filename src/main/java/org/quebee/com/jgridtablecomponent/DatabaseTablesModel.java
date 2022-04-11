package org.quebee.com.jgridtablecomponent;

import java.io.File;
import java.util.Date;

public class DatabaseTablesModel extends AbstractTreeTableModel implements TreeTableModel {

    static protected String[] cNames = {"Database"};

    static protected Class<?>[] cTypes = {TreeTableModel.class};

    public DatabaseTablesModel() {
        super(new DatabaseTableNode(new File(File.separator)));
    }

    protected File getFile(Object node) {
        DatabaseTableNode fileNode = ((DatabaseTableNode) node);
        return fileNode.getFile();
    }

    protected Object[] getChildren(Object node) {
        DatabaseTableNode fileNode = ((DatabaseTableNode) node);
        return fileNode.getChildren();
    }

    public int getChildCount(Object node) {
        Object[] children = getChildren(node);
        return (children == null) ? 0 : children.length;
    }

    public Object getChild(Object node, int i) {
        return getChildren(node)[i];
    }

    public boolean isLeaf(Object node) {
        return getFile(node).isFile();
    }

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class<?> getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object node, int column) {
        File file = getFile(node);
        try {
            if (column == 0) {
                return file.getName();
            }
        } catch (SecurityException se) {
        }

        return null;
    }
}

class DatabaseTableNode {
    File file;
    Object[] children;

    public DatabaseTableNode(File file) {
        this.file = file;
    }

    public String toString() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    protected Object[] getChildren() {
        if (children != null) {
            return children;
        }
        try {
            String[] files = file.list();
            if (files != null) {
                children = new DatabaseTableNode[files.length];
                String path = file.getPath();
                for (int i = 0; i < files.length; i++) {
                    File childFile = new File(path, files[i]);
                    children[i] = new DatabaseTableNode(childFile);
                }
            }
        } catch (SecurityException se) {
        }
        return children;
    }
}

