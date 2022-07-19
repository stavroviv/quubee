// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.quebee.com.util;

import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDSupport;
import com.intellij.ui.awt.RelativeRectangle;
import com.intellij.util.ui.EditableModel;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.model.QBTreeNode;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class MyRowsDnDSupport {

    public static void install(JTable table, EditableModel model, Consumer<DnDEvent> handler) {
        table.setDragEnabled(true);
        installImpl(table, model, handler);
    }

    private static void installImpl(JComponent component, EditableModel model, Consumer<DnDEvent> handler) {
        component.setTransferHandler(new TransferHandler(null));
        DnDSupport.createBuilder(component)
                .setBeanProvider(info -> {
                    final Point p = info.getPoint();
                    return new DnDDragStartBean(new RowDragInfo(component, getRow(component, p)));
                })
                .setTargetChecker(event -> {
                    final Object o = event.getAttachedObject();
                    int oldIndex = -10;
                    int newIndex = -10;
                    if (event.getAttachedObject() instanceof QBTreeNode) {
                        event.setDropPossible(true, "");
                        oldIndex = -1;
                        newIndex = getRow(component, event.getPoint());
                    } else if (!(o instanceof RowDragInfo) || ((RowDragInfo) o).component != component) {
                        event.setDropPossible(false, "");
                        return true;
                    } else {
                        oldIndex = ((RowDragInfo) o).row;
                        newIndex = getRow(component, event.getPoint());
                        if (newIndex == -1) {
                            event.setDropPossible(false, "");
                            return true;
                        }
                    }

                    event.setDropPossible(true);

                    Rectangle cellBounds = getCellBounds(component, newIndex);
                    if (oldIndex != newIndex) {
                        // Drag&Drop always starts with new==old and we shouldn't display 'rejecting' cursor if they are equal
                        boolean canExchange = model.canExchangeRows(oldIndex, newIndex);
                        if (canExchange) {
                            if (oldIndex < newIndex) {
                                cellBounds.y += cellBounds.height - 2;
                            }
                            RelativeRectangle rectangle = new RelativeRectangle(component, cellBounds);
                            rectangle.getDimension().height = 2;
                            event.setDropPossible(true);
                            event.setHighlighting(rectangle, DnDEvent.DropTargetHighlightingType.FILLED_RECTANGLE);
                        } else {
                            event.setDropPossible(false);
                        }
                    }
                    System.out.println("drop " + event.isDropPossible());
                    return true;
                })
                .setDropHandler(event -> {
                    handler.accept(event);
                    final var o = event.getAttachedObject();
                    final var p = event.getPoint();
                    if (o instanceof RowDragInfo && ((RowDragInfo) o).component == component) {
                        var oldIndex = ((RowDragInfo) o).row;
                        if (oldIndex == -1) return;
                        var newIndex = getRow(component, p);
                        if (newIndex == -1) {
                            newIndex = getRowCount(component) - 1;
                        }

                        if (oldIndex != newIndex) {
                            if (model instanceof RefinedDropSupport) {
                                var cellBounds = getCellBounds(component, newIndex);
                                var position = ((RefinedDropSupport) model).isDropInto(component, oldIndex, newIndex)
                                        ? RefinedDropSupport.Position.INTO
                                        : (event.getPoint().y < cellBounds.y + cellBounds.height / 2)
                                        ? RefinedDropSupport.Position.ABOVE
                                        : RefinedDropSupport.Position.BELOW;
                                if (((RefinedDropSupport) model).canDrop(oldIndex, newIndex, position)) {
                                    ((RefinedDropSupport) model).drop(oldIndex, newIndex, position);
                                }
                            } else {
                                if (model.canExchangeRows(oldIndex, newIndex)) {
                                    model.exchangeRows(oldIndex, newIndex);
                                    setSelectedRow(component, newIndex);
                                }
                            }
                        }
                    }
                    event.hideHighlighter();
                })
                .install();
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
            Rectangle rectangle = ((JTable) component).getCellRect(row, 0, true);
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

    private static void setSelectedRow(JComponent component, int row) {
        if (component instanceof JTable) {
            ((JTable) component).getSelectionModel().setSelectionInterval(row, row);
        } else if (component instanceof JList) {
            ((JList<?>) component).setSelectedIndex(row);
        } else if (component instanceof JTree) {
            ((JTree) component).setSelectionRow(row);
        } else {
            throw new IllegalArgumentException("Unsupported component: " + component);
        }
    }

    private static class RowDragInfo {
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
