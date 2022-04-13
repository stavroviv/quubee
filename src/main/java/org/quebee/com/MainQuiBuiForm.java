package org.quebee.com;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.impl.StripeButtonUI;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.tabs.impl.SingleHeightTabs;
import com.intellij.ui.tabs.impl.TabLabel;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.DatabaseIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quebee.com.panel.FromTables;
import org.quebee.com.panel.LinksPanel;
import org.quebee.com.panel.OrderPanel;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN;
import static javax.swing.SwingConstants.TOP;

public class MainQuiBuiForm {
    final JFrame frame = new JFrame("Qui Bui");
    final DialogWrapper dialog;

    private Icon makeVerticalTabIcon(String title, Icon icon, boolean clockwise) {
        JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        Dimension d = label.getPreferredSize();
        int w = d.height;
        int h = d.width;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        UISettings.setupAntialiasing(g2);
        AffineTransform at = clockwise
                ? AffineTransform.getTranslateInstance(w, 0)
                : AffineTransform.getTranslateInstance(0, h);
        at.quadrantRotate(clockwise ? 1 : -1);
        g2.setTransform(at);
        SwingUtilities.paintComponent(g2, label, new JPanel(), 0, 0, d.width, d.height);
        g2.dispose();
        return new ImageIcon(bi);
    }

    public MainQuiBuiForm(Project project) {
        final JBTabsImpl tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        dialog = new DialogWrapper(project, false, DialogWrapper.IdeModalityType.PROJECT) {

            @Override
            protected @NotNull JComponent createCenterPanel() {

                for (int i = 0; i < 200; i++) {
                    if (i < 10) {
                        final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication()) {
//                            @NotNull
//                            @Override
//                            protected TabLabel createTabLabel(@NotNull TabInfo info) {
//                                TabLabel tabLabel = new TabLabel(this, info);
//                                tabLabel.getLabelComponent().add(new JButton("Test"));
////                                Graphics graphics = tabLabel.getLabelComponent().getGraphics();
////                                graphics.drawOval(1, 1, 1, 1);
////                                tabLabel.
//                                return tabLabel;
//                            }
                        };

                        addFromTables(tabs);
                        addLinksTable(tabs);
                        JPanel component = new JPanel();
                        Icon test = makeVerticalTabIcon("test" + i, DatabaseIcons.Table, true);
//                        component.add(test);
                        tabs.addTab(new TabInfo(component)).setText("Grouping").setActions(new DefaultActionGroup(), null);
                        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
                        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
                        addOrderTab(tabs);

//                        ComponentUI test = StripeButtonUI.createUI(new JButton("Test"));
//                        /ss.
                        TabInfo info = new TabInfo(tabs.getComponent());
                        info.append("",
                                new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, new Color(112, 112, 164)));
//                        info.setl

                        tabsCte.addTab(info).setIcon(test);
                    } else {
                        tabsCte.addTab(new TabInfo(new JPanel()));
                    }
                }
//                tabsCte.getPresentation().sett
                tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);

                return tabsCte;
            }

            @Override
            protected Action @NotNull [] createActions() {
                return ArrayUtil.append(super.createActions(), new AbstractAction("hide") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tabsCte.setHideTabs(!tabsCte.isHideTabs());
                    }
                });
            }

            @Override
            protected void createDefaultActions() {
                super.createDefaultActions();
                init();
            }
        };
        dialog.setResizable(true);
//        dialog.setUndecorated(true);
        dialog.setTitle("Qui Bui");
        dialog.setSize(900, 550);
    }

    private void addFromTables(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new FromTables().element).setText(FromTables.HEADER));
    }

    private void addLinksTable(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new LinksPanel().element)).setText(LinksPanel.HEADER);
    }

    private void addOrderTab(JBTabsImpl tabs) {
        tabs.addTab(new TabInfo(new OrderPanel())).setText(OrderPanel.HEADER);
    }

    public void show() {
        dialog.show();
    }
}
