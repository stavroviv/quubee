package org.quebee.com.panel;

import com.intellij.openapi.Disposable;
import lombok.Getter;

import javax.swing.*;

@Getter
public class UnionAliasesPanel implements QueryComponent {
    private final String header = "Union/Aliases";
    private final JComponent component;

    public UnionAliasesPanel() {
        this.component = new JPanel();
    }

    @Override
    public void initListeners(Disposable disposable) {

    }
}
