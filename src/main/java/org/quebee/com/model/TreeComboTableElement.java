package org.quebee.com.model;

import com.intellij.openapi.ui.TreeComboBox;
import com.intellij.ui.SimpleColoredComponent;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;

@Getter
public class TreeComboTableElement extends DefaultMutableTreeNode implements TreeComboBox.CustomPresentation {
    private final String text;
    private final Icon icon;
    private final String table;

    @Override
    public String toString() {
        return text;
    }

    public TreeComboTableElement(String text, String table, Icon icon) {
        this.text = text;
        this.table = table;
        this.icon = icon;
    }

    @Override
    public void append(SimpleColoredComponent component, int index) {
        if (index < 0) {
            component.append(table + "." + text);
            return;
        }
        component.append(Objects.nonNull(text) ? text : "");
    }

    @Override
    public Icon getIcon(int index, int flags) {
        return icon;
    }
}
