package org.quebee.com.model;

import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeComboTableElement extends DefaultMutableTreeNode implements TreeComboBox.CustomPresentation {
    private final String text;
    private final Icon icon;

    @Override
    public String toString() {
        return text;
    }

    public TreeComboTableElement(String text, Icon icon) {
        this.text = text;
        this.icon = icon;
    }

    @Override
    public void append(SimpleColoredComponent component, int index) {
        component.append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    public Icon getIcon(int index, int flags) {
        return icon;
    }
}
