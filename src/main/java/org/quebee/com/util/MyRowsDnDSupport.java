package org.quebee.com.util;

import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDSupport;
import com.intellij.ui.awt.RelativeRectangle;
import com.intellij.util.ui.EditableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.TreeNode;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class MyRowsDnDSupport {

    public static void install(JTable table, EditableModel model, Consumer<DnDEvent> handler) {
        table.setDragEnabled(true);
        table.setTransferHandler(new TransferHandler(null));
        DnDSupport.createBuilder(table)
                .setBeanProvider(info -> {
                    final var p = info.getPoint();
                    return new DnDDragStartBean(new RowDragInfo(table, getRow(table, p)));
                })
                .setTargetChecker(event -> targetChecker(table, model, event))
                .setDropHandler(event -> dropHandler(table, model, handler, event))
                .install();
    }

    private static void dropHandler(JComponent component, EditableModel model, Consumer<DnDEvent> handler, DnDEvent event) {
        handler.accept(event);
        final var attachedObject = event.getAttachedObject();
        final var eventPoint = event.getPoint();
        if (attachedObject instanceof RowDragInfo && ((RowDragInfo) attachedObject).component == component) {
            var oldIndex = ((RowDragInfo) attachedObject).row;
            if (oldIndex == -1) return;
            var newIndex = getRow(component, eventPoint);
            if (newIndex == -1) {
                newIndex = getRowCount(component) - 1;
            }

            if (oldIndex != newIndex) {
                if (model instanceof RefinedDropSupport) {
                    var cellBounds = getCellBounds(component, newIndex);
                    var position = ((RefinedDropSupport) model).isDropInto(component, oldIndex, newIndex)
                            ? RefinedDropSupport.Position.INTO : (event.getPoint().y < cellBounds.y + cellBounds.height / 2)
                            ? RefinedDropSupport.Position.ABOVE : RefinedDropSupport.Position.BELOW;
                    if (((RefinedDropSupport) model).canDrop(oldIndex, newIndex, position)) {
                        ((RefinedDropSupport) model).drop(oldIndex, newIndex, position);
                    }
                } else {
                    if (model.canExchangeRows(oldIndex, newIndex)) {
                        model.exchangeRows(oldIndex, newIndex);
                        ComponentUtils.setSelectedRow(component, newIndex);
                    }
                }
            }
        }
        event.hideHighlighter();
    }

    private static boolean targetChecker(JComponent component, EditableModel model, DnDEvent event) {
        var attachedObject = event.getAttachedObject();
        if (attachedObject instanceof TreeNode) {
            return dndAnotherSourceInfo(component, event);
        } else if (attachedObject instanceof RowDragInfo) {
            if (((RowDragInfo) attachedObject).component == component) {
                return dndRowDragInfo(component, model, event);
            } else {
                return dndAnotherSourceInfo(component, event);
            }
        }
        event.setDropPossible(false, "");
        return false;
    }

    public static boolean dndAnotherSourceInfo(JComponent component, DnDEvent event) {
        var newIndex = getRow(component, event.getPoint());
        var cellBounds = getCellBounds(component, newIndex);
        if (newIndex == -1) {
            cellBounds.y = ((JTable) component).getRowHeight() * getRowCount(component);
        }

        var rectangle = new RelativeRectangle(component, cellBounds);
        rectangle.getDimension().height = 2;
        event.setDropPossible(true);
        event.setHighlighting(rectangle, DnDEvent.DropTargetHighlightingType.FILLED_RECTANGLE);

        return true;
    }

    private static boolean dndRowDragInfo(JComponent component, EditableModel model, DnDEvent event) {
        var attachedObject = event.getAttachedObject();
        var oldIndex = ((RowDragInfo) attachedObject).row;
        var newIndex = getRow(component, event.getPoint());
        if (newIndex == -1) {
            event.setDropPossible(false, "");
            return true;
        }

        event.setDropPossible(true);

        var cellBounds = getCellBounds(component, newIndex);
        if (oldIndex != newIndex) {
            // Drag&Drop always starts with new==old and we shouldn't display 'rejecting' cursor if they are equal
            var canExchange = model.canExchangeRows(oldIndex, newIndex);
            if (canExchange) {
                if (oldIndex < newIndex) {
                    cellBounds.y += cellBounds.height - 2;
                }
                var rectangle = new RelativeRectangle(component, cellBounds);
                rectangle.getDimension().height = 2;
                event.setDropPossible(true);
                event.setHighlighting(rectangle, DnDEvent.DropTargetHighlightingType.FILLED_RECTANGLE);
            } else {
                event.setDropPossible(false);
            }
        }

        return true;
    }

    private static int getRow(JComponent component, Point point) {
        if (component instanceof JTable) {
            return ((JTable) component).rowAtPoint(point);
        } else if (component instanceof JList) {
            return ((JList<?>) component).locationToIndex(point);
        } else if (component instanceof JTree) {
            return ((JTree) component).getClosestRowForLocation(point.x, point.y);
        } else {
            throw new IllegalArgumentException("Unsupported component: " + component);
        }
    }

    private static int getRowCount(JComponent component) {
        if (component instanceof JTable) {
            return ((JTable) component).getRowCount();
        } else if (component instanceof JList) {
            return ((JList) component).getModel().getSize();
        } else if (component instanceof JTree) {
            return ((JTree) component).getRowCount();
        } else {
            throw new IllegalArgumentException("Unsupported component: " + component);
        }
    }

    private static Rectangle getCellBounds(JComponent component, int row) {
        if (component instanceof JTable) {
            var rectangle = ((JTable) component).getCellRect(row, 0, true);
            rectangle.width = component.getWidth();
            return rectangle;
        } else if (component instanceof JList) {
            return ((JList<?>) component).getCellBounds(row, row);
        } else if (component instanceof JTree) {
            return ((JTree) component).getRowBounds(row);
        } else {
            throw new IllegalArgumentException("Unsupported component: " + component);
        }
    }

    public static class RowDragInfo {
        @Getter
        public final JComponent component;
        public final int row;

        RowDragInfo(JComponent component, int row) {
            this.component = component;
            this.row = row;
        }
    }

    public interface RefinedDropSupport {
        enum Position {ABOVE, INTO, BELOW}

        boolean isDropInto(JComponent component, int oldIndex, int newIndex);

        //oldIndex may be equal to newIndex
        boolean canDrop(int oldIndex, int newIndex, @NotNull Position position);

        //This method is also responsible for selection changing
        void drop(int oldIndex, int newIndex, @NotNull Position position);
    }
}
