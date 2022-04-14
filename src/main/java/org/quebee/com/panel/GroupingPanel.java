package org.quebee.com.panel;

import lombok.Getter;

import javax.swing.*;

@Getter
public class GroupingPanel  implements QueryComponent {
    private final String header = "Grouping";
    private final JComponent component;

    public GroupingPanel() {
        this.component = new JPanel();
    }
}
