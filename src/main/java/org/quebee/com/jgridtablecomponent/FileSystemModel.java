package org.quebee.com.jgridtablecomponent;

import java.io.File;
import java.util.Date;

public class FileSystemModel extends AbstractTreeTableModel implements TreeTableModel {

    static protected String[] cNames = {"Name", "Size", "Type", "Modified"};

    static protected Class[] cTypes = {TreeTableModel.class, Integer.class, String.class, Date.class};

    public static final Integer ZERO = new Integer(0);

    public FileSystemModel() {
        super(new FileNode(new File(File.separator)));
    }

    protected File getFile(Object node) {
        FileNode fileNode = ((FileNode) node);
        return fileNode.getFile();
    }

    protected Object[] getChildren(Object node) {
        FileNode fileNode = ((FileNode) node);
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

    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object node, int column) {
        File file = getFile(node);
        try {
            switch (column) {
                case 0:
                    return file.getName();
                case 1:
                    return file.isFile() ? new Integer((int) file.length()) : ZERO;
                case 2:
                    return file.isFile() ? "File" : "Directory";
                case 3:
                    return new Date(file.lastModified());
            }
        } catch (SecurityException se) {
        }

        return null;
    }
}

class FileNode {
    File file;
    Object[] children;

    public FileNode(File file) {
        this.file = file;
    }

    static private MergeSort fileMS = new MergeSort() {
        public int compareElementsAt(int a, int b) {
            return ((String) toSort[a]).compareTo((String) toSort[b]);
        }
    };

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
                fileMS.sort(files);
                children = new FileNode[files.length];
                String path = file.getPath();
                for (int i = 0; i < files.length; i++) {
                    File childFile = new File(path, files[i]);
                    children[i] = new FileNode(childFile);
                }
            }
        } catch (SecurityException se) {
        }
        return children;
    }
}

