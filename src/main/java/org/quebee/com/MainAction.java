package org.quebee.com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import org.jetbrains.annotations.NotNull;
import org.quebee.com.jgridtablecomponent.DatabaseTablesModel;
import org.quebee.com.jgridtablecomponent.FileSystemModel;
import org.quebee.com.jgridtablecomponent.JTreeTable;

import javax.swing.*;
import java.awt.*;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        DialogPanel p = DemoTipsKt.demoTips(e.getProject());
//        JFrame f = new JFrame();
//        f.setLayout(new BorderLayout());
//        f.add(p, BorderLayout.CENTER);
//        f.pack();
//        f.setVisible(true);

        final JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
//        final int[] count = new int[1];
        final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
//
        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        frame.getContentPane().add(flow);
        flow.add(tabs.getComponent());
//
        frame.getContentPane().add(tabs.getComponent(), BorderLayout.CENTER);
//
//        NonOpaquePanel panel = new NonOpaquePanel();
        JPanel south = new JPanel(new FlowLayout());
        south.setOpaque(true);
        south.setBackground(JBColor.WHITE);
//
//        final JComboBox pos = new JComboBox(new Object[]{JBTabsPosition.top, JBTabsPosition.left, JBTabsPosition.right, JBTabsPosition.bottom});
//        pos.setSelectedIndex(0);
//        south.add(pos);
//        pos.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                final JBTabsPosition p = (JBTabsPosition)pos.getSelectedItem();
//                if (p != null) {
//                    tabs.getPresentation().setTabsPosition(p);
//                }
//            }
//        });
//
//        final JCheckBox bb = new JCheckBox("Buffered", true);
//        bb.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(final ItemEvent e) {
//                // tabs.setUseBufferedPaint(bb.isSelected());
//            }
//        });
//        south.add(bb);
//
//        final JCheckBox f = new JCheckBox("Focused");
//        f.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(final ItemEvent e) {
//                tabs.setFocused(f.isSelected());
//            }
//        });
//        south.add(f);
//
//
        final JButton v = new JButton("OK");
        v.addItemListener(e1 -> tabs.setSideComponentVertical(v.isSelected()));
        v.setDefaultCapable(true);
        south.add(v);
        final JButton v1 = new JButton("Cancel");
        v1.addItemListener(e1 -> tabs.setSideComponentVertical(v.isSelected()));
        south.add(v1);
//
//        final JCheckBox before = new JCheckBox("Before", true);
//        before.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(final ItemEvent e) {
//                tabs.setSideComponentBefore(before.isSelected());
//            }
//        });
//        south.add(before);
//
//        final JCheckBox row = new JCheckBox("Single row", true);
//        row.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(final ItemEvent e) {
//                tabs.setSingleRow(row.isSelected());
//            }
//        });
//        south.add(row);
//
//        final JCheckBox hide = new JCheckBox("Hide tabs", tabs.isHideTabs());
//        hide.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(final ItemEvent e) {
//                tabs.setHideTabs(hide.isSelected());
//            }
//        });
//        south.add(hide);
//
        frame.getContentPane().add(south, BorderLayout.SOUTH);
//
//        tabs.addListener(new TabsListener() {
//            @Override
//            public void selectionChanged(final TabInfo oldSelection, final TabInfo newSelection) {
//                System.out.println("TabsWithActions.selectionChanged old=" + oldSelection + " new=" + newSelection);
//            }
//        });
//
//        final JTree someTree = new Tree() {
//            @Override
//            public void addNotify() {
//                super.addNotify();
//                System.out.println("JBTabs.addNotify");
//            }
//
//            @Override
//            public void removeNotify() {
//                System.out.println("JBTabs.removeNotify");
//                super.removeNotify();
//            }
//        };
//        //someTree.setBorder(new LineBorder(Color.cyan));
//        tabs.addTab(new TabInfo(someTree)).setText("Tree1").setActions(new DefaultActionGroup(), null)
//                .setIcon(AllIcons.Debugger.Frame);
//
//        final JTree component = new Tree();
//        final TabInfo toAnimate1 = new TabInfo(component);
//        //toAnimate1.setIcon(IconLoader.getIcon("/debugger/console.png"));
//        final JCheckBox attract1 = new JCheckBox("Attract 1");
//        attract1.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                //toAnimate1.setText("Should be animated");
//
//                if (attract1.isSelected()) {
//                    toAnimate1.fireAlert();
//                }
//                else {
//                    toAnimate1.stopAlerting();
//                }
//            }
//        });
//        south.add(attract1);
//
//        final JCheckBox hide1 = new JCheckBox("Hide 1", toAnimate1.isHidden());
//        hide1.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                toAnimate1.setHidden(!toAnimate1.isHidden());
//            }
//        });
//        south.add(hide1);
//
//
//        final JCheckBox block = new JCheckBox("Block", false);
//        block.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                tabs.setPaintBlocked(!block.isSelected(), true);
//            }
//        });
//        south.add(block);
//
//        final JCheckBox fill = new JCheckBox("Tab fill in", true);
//        fill.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                tabs.getPresentation().setActiveTabFillIn(fill.isSelected() ? Color.white : null);
//            }
//        });
//        south.add(fill);
//
//
//        final JButton refire = new JButton("Re-fire attraction");
//        refire.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                toAnimate1.fireAlert();
//            }
//        });
//
//        south.add(refire);
//
//
//
//
//        final JEditorPane text = new JEditorPane();
//        text.setEditorKit(new HTMLEditorKit());
//        StringBuilder buffer = new StringBuilder();
//        for (int i = 0; i < 50; i ++) {
//            buffer.append("1234567890abcdefghijklmnopqrstv1234567890abcdefghijklmnopqrstv1234567890abcdefghijklmnopqrstv<br>");
//        }
//        text.setText(buffer.toString());
//
//        final JLabel tb = new JLabel("Side comp");
//        tb.setBorder(new LineBorder(Color.red));
//        tabs.addTab(new TabInfo(ScrollPaneFactory.createScrollPane(text)).setSideComponent(tb)).setText("Text text text");
//        tabs.addTab(toAnimate1).append("Tree2", new SimpleTextAttributes(SimpleTextAttributes.STYLE_WAVED, Color.black, Color.red));
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 1").setActions(new DefaultActionGroup(), null);
        Object[][] array = new String[][] {{ "Test" , "dd", "1.5" },
                { "Kkkk"  , "dd", "4.0" },
                { "Mmmm", "f" , "2.2" }};

        Object[] columnsHeader = new String[] {"Name", "UOM",
                "Quantity"};
        JTable component = new JTable(array, columnsHeader);

        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(databaseTableExample0());
        JBSplitter splitter2 = new JBSplitter();
        splitter2.setFirstComponent(databaseTableExample0());
        splitter2.setSecondComponent(TreeTableExample0());
        splitter.setSecondComponent(splitter2);
        TabInfo info = new TabInfo(splitter);
        tabs.addTab(info).setText("Tables and fields").setActions(new DefaultActionGroup(), null);


//        component.add()

        tabs.addTab(new TabInfo(component)).setText("Links").setActions(new DefaultActionGroup(), null);

        tabs.addTab(new TabInfo(new JTable())).setText("Grouping").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
        tabs.addTab(new TabInfo(new JTable())).setText("Order").setActions(new DefaultActionGroup(), null);
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 5").setActions(new DefaultActionGroup(), null);
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 6").setActions(new DefaultActionGroup(), null);
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 7").setActions(new DefaultActionGroup(), null);
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 8").setActions(new DefaultActionGroup(), null);
//        tabs.addTab(new TabInfo(new JTable())).setText("Table 9").setActions(new DefaultActionGroup(), null);
//
//        //tabs.getComponent().setBorder(new EmptyBorder(5, 5, 5, 5));
//        tabs.setTabSidePaintBorder(5);
//        tabs.setPaintBorder(1, 1, 1, 1);
//
//        tabs.getPresentation().setActiveTabFillIn(Color.white);
//
//        //tabs.setBorder(new LineBorder(Color.blue, 5));
//        tabs.setBorder(new EmptyBorder(30, 30, 30, 30));
//
//        tabs.setUiDecorator(new UiDecorator() {
//            @Override
//            public UiDecoration getDecoration() {
//                return new UiDecoration(null, new Insets(0, -1, 0, -1));
//            }
//        });
//
//        frame.setBounds(1400, 200, 1000, 800);
        frame.setPreferredSize(new Dimension(1200, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

//        JFrame f = new JFrame();
//        f.setLayout(new BorderLayout());
//        QueryBuilder queryBuilder = new QueryBuilder();
//       // f.add(queryBuilder, BorderLayout.CENTER);
//        f.pack();
//        f.setVisible(true);

//        JFrame frame = new JFrame("QueryBuilder");
//
//        frame.setContentPane(new QueryBuilder().panel1);
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);

//        messagebus()
    }

    public JScrollPane TreeTableExample0() {
        JTreeTable treeTable = new JTreeTable(new FileSystemModel());
        return new JScrollPane(treeTable);
    }

    public JScrollPane databaseTableExample0() {
        JTreeTable treeTable = new JTreeTable(new DatabaseTablesModel());
        return new JScrollPane(treeTable);
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
