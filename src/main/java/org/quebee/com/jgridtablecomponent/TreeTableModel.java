package org.quebee.com.jgridtablecomponent;

import javax.swing.tree.TreeModel;

public interface TreeTableModel extends TreeModel
{
    int getColumnCount();

    String getColumnName(int column);

    Class getColumnClass(int column);

    Object getValueAt(Object node, int column);

    boolean isCellEditable(Object node, int column);

    void setValueAt(Object aValue, Object node, int column);
}
