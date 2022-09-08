package org.quebee.com.util;

import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.DnDSource;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.TreeNode;

import java.awt.*;

public abstract class AvailableFieldsTreeDnDSource implements DnDSource {

    private final TreeTable treeTable;

    public AvailableFieldsTreeDnDSource(TreeTable treeTable) {
        this.treeTable = treeTable;
    }

    protected String getTreeTableName() {
        return treeTable.getName();
    }

    public abstract boolean canStartDragging(DnDAction action, @NotNull Point dragOrigin);

    public abstract String getFieldDescription(TreeNode attachedObject);

    public @NotNull DnDDragStartBean startDragging(DnDAction action, @NotNull Point dragOrigin) {
        var value = (TreeNode) treeTable.getValueAt(treeTable.getSelectedRow(), 0);
        value.setSource(getTreeTableName());
        return new DnDDragStartBean(value, dragOrigin);
    }

    public @NotNull Pair<Image, Point> createDraggedImage(DnDAction action,
                                                          Point dragOrigin,
                                                          @NotNull DnDDragStartBean bean) {
        var c = new SimpleColoredComponent();
        c.setForeground(RenderingUtil.getForeground(treeTable));
        c.setBackground(RenderingUtil.getBackground(treeTable));
        var attachedObject = (TreeNode) bean.getAttachedObject();
        var userObject = attachedObject.getUserObject();
        c.setIcon(userObject.getIcon());

        var description = getFieldDescription(attachedObject);
        c.append(" +" + description, SimpleTextAttributes.REGULAR_ATTRIBUTES);

        var size = c.getPreferredSize();
        c.setSize(size);
        var image = UIUtil.createImage(c, size.width, size.height, 2);
        c.setOpaque(false);
        var g = image.createGraphics();
        c.paint(g);
        g.dispose();

        return Pair.create(image, new Point(-20, 5));
    }
}
