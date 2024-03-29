package org.quebee.com.debug.experimental;/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class FillViewportHeightDemo extends JFrame implements ActionListener {

    private DefaultListModel model = new DefaultListModel();
    private int count = 0;
    private JTable table;
    private JCheckBoxMenuItem fillBox;
    private DefaultTableModel tableModel;

    private static String getNextString(int count) {
        var buf = new StringBuffer();
        for (var i = 0; i < 5; i++) {
            buf.append(String.valueOf(count));
            buf.append(",");
        }

        // remove last newline
        buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }

    private static DefaultTableModel getDefaultTableModel() {
        var cols = new String[]{"Foo", "Toto", "Kala", "Pippo", "Boing"};
        return new DefaultTableModel(null, cols);
    }

    public FillViewportHeightDemo() {
        super("Empty Table DnD Demo");

        tableModel = getDefaultTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setDropMode(DropMode.INSERT_ROWS);

        table.setTransferHandler(new TransferHandler() {

            public boolean canImport(TransferSupport support) {
                // for the demo, we'll only support drops (not clipboard paste)
                if (!support.isDrop()) {
                    return false;
                }

                // we only import Strings
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }

                return true;
            }

            public boolean importData(TransferSupport support) {
                // if we can't handle the import, say so
                if (!canImport(support)) {
                    return false;
                }

                // fetch the drop location
                var dropLocation = (JTable.DropLocation)support.getDropLocation();

                var row = dropLocation.getRow();

                // fetch the data and bail if this fails
                String data;
                try {
                    data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }

                var rowData = data.split(",");
                tableModel.insertRow(row, rowData);

                var rect = table.getCellRect(row, 0, false);
                if (rect != null) {
                    table.scrollRectToVisible(rect);
                }

                // demo stuff - remove for blog
                model.removeAllElements();
                model.insertElementAt(getNextString(count++), 0);
                // end demo stuff

                return true;
            }
        });

        var dragFrom = new JList(model);
        dragFrom.setFocusable(false);
        dragFrom.setPrototypeCellValue(getNextString(100));
        model.insertElementAt(getNextString(count++), 0);
        dragFrom.setDragEnabled(true);
        dragFrom.setBorder(BorderFactory.createLoweredBevelBorder());

        dragFrom.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() % 2 == 0) {
                    var text = (String)model.getElementAt(0);
                    var rowData = text.split(",");
                    tableModel.insertRow(table.getRowCount(), rowData);
                    model.removeAllElements();
                    model.insertElementAt(getNextString(count++), 0);
                }
            }
        });

        var p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        var wrap = new JPanel();
        wrap.add(new JLabel("Drag from here:"));
        wrap.add(dragFrom);
        p.add(Box.createHorizontalStrut(4));
        p.add(Box.createGlue());
        p.add(wrap);
        p.add(Box.createGlue());
        p.add(Box.createHorizontalStrut(4));
        getContentPane().add(p, BorderLayout.NORTH);

        var scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        fillBox = new JCheckBoxMenuItem("Fill Viewport Height");
        fillBox.addActionListener(this);

        var menuBar = new JMenuBar();
        var options = new JMenu("Options");
        menuBar.add(options);
        setJMenuBar(menuBar);

        var clear = new JMenuItem("Reset");
        clear.addActionListener(this);
        options.add(clear);
        options.add(fillBox);

        getContentPane().setPreferredSize(new Dimension(260, 180));
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == fillBox) {
            table.setFillsViewportHeight(fillBox.isSelected());
        } else {
            tableModel.setRowCount(0);
            count = 0;
            model.removeAllElements();
            model.insertElementAt(getNextString(count++), 0);
        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        var test = new FillViewportHeightDemo();
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        test.pack();
        test.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            createAndShowGUI();
        });
    }
}