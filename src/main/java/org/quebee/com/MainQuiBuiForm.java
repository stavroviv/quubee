package org.quebee.com;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
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
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.SwingConstants.TOP;

public class MainQuiBuiForm {
    final JFrame frame = new JFrame("Qui Bui");
    final DialogWrapper dialog;


    public MainQuiBuiForm(Project project) {
        final JBTabsImpl tabsCte = new JBTabsImpl(null, null, ApplicationManager.getApplication());
        dialog = new DialogWrapper(project, false, DialogWrapper.IdeModalityType.PROJECT) {

            @Override
            protected @NotNull JComponent createCenterPanel() {

                for (int i = 0; i < 200; i++) {
                    if (i < 10) {
                        final JBTabsImpl tabs = new JBTabsImpl(null, null, ApplicationManager.getApplication());
                        addFromTables(tabs);
                        addLinksTable(tabs);
                        tabs.addTab(new TabInfo(new JTable())).setText("Grouping").setActions(new DefaultActionGroup(), null);
                        tabs.addTab(new TabInfo(new JTable())).setText("Conditions").setActions(new DefaultActionGroup(), null);
                        tabs.addTab(new TabInfo(new JTable())).setText("Union/Aliases").setActions(new DefaultActionGroup(), null);
                        addOrderTab(tabs);
                        tabsCte.addTab(new TabInfo(tabs.getComponent())).setText("Grouping " + i);
                    } else {
                        tabsCte.addTab(new TabInfo(new JPanel())).setText("Grouping " + i);
                    }
                }
//                tabsCte.getPresentation().sett
                tabsCte.getPresentation().setTabsPosition(JBTabsPosition.right);
                tabsCte.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
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
