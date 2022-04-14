package org.quebee.com.model;

import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeComboTableElement extends DefaultMutableTreeNode implements TreeComboBox.CustomPresentation {
    private String text;
    private Icon icon;

    @Override
    public String toString() {
        return text;
    }

    public TreeComboTableElement(String test, Icon icon) {
        this.text = test;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
