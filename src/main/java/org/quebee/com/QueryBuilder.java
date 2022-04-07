package org.quebee.com;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;

public class QueryBuilder {
    public JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTree tree2;
    private JTable table1;
    private JButton button1;
    private JButton button2;
    private JTree tree1;
    private JTree tree3;
    private JButton button9;
    private JButton button10;
    private JSplitPane splitPanelOne;


    public static void main(String[] args) {
        JFrame frame = new JFrame("QueryBuilder");
        frame.setPreferredSize(new Dimension(600, 700));
        QueryBuilder queryBuilder = new QueryBuilder();
//        queryBuilder.splitPanelOne.getco(Color.BLACK);
        BasicSplitPaneDivider divider = (BasicSplitPaneDivider) queryBuilder.splitPanelOne.getComponent(0);
        divider.setBackground(Color.black);
        divider.setBorder(null);
//        queryBuilder.splitPanelOne.getComponents()[0].setBackground(Color.BLACK);
//        queryBuilder.splitPanelOne.getComponents()[0].set(Color.BLACK);
        frame.setContentPane(queryBuilder.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        queryBuilder.panel1.add(new Tree());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
