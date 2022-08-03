package org.quebee.com.util;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class MouseAdapterDoubleClick extends MouseAdapter {
    private boolean checkMousePosition;

    public MouseAdapterDoubleClick() {
    }

    public MouseAdapterDoubleClick(boolean checkMousePosition) {
        this.checkMousePosition = checkMousePosition;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        var table = (JTable) mouseEvent.getSource();
        if (mouseEvent.getClickCount() != 2 || table.getSelectedRow() == -1) {
            return;
        }
        if (checkMousePosition && mouseEvent.getX() < 40) {
            return;
        }
        mouseDoubleClicked(mouseEvent, table);
    }

    abstract protected void mouseDoubleClicked(MouseEvent mouseEvent, JTable table);
}
