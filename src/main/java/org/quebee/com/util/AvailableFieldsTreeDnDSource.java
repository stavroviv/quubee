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
        var component = new SimpleColoredComponent();
        component.setForeground(RenderingUtil.getForeground(treeTable));
        component.setBackground(RenderingUtil.getBackground(treeTable));
        var attachedObject = (TreeNode) bean.getAttachedObject();
        var userObject = attachedObject.getUserObject();
        component.setIcon(userObject.getIcon());

        var description = getFieldDescription(attachedObject);
        component.append(" +" + description, SimpleTextAttributes.REGULAR_ATTRIBUTES);

        var size = component.getPreferredSize();
        component.setSize(size);
        var image = UIUtil.createImage(component, size.width, size.height, 2);
        component.setOpaque(false);

        var graphics = image.createGraphics();
        component.paint(graphics);
        graphics.dispose();

        return Pair.create(image, new Point(-20, 5));
    }
}
