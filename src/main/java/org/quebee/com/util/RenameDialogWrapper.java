package org.quebee.com.util;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.xml.util.XmlStringUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class RenameDialogWrapper extends DefaultDialogWrapper {
    private final String input;
    @Getter
    private JBTextField result;

    public RenameDialogWrapper(String input) {
        super(null, false);
        this.input = input;
        setTitle("Rename");
        setSize(400, 50);
        setValues();
    }

    private void setValues() {
        result.setText(input);
    }

    @Override
    protected JComponent createCenterPanel() {
        var panel = new JPanel(new GridBagLayout());
        var gbConstraints = new GridBagConstraints();
//        gbConstraints.insets = JBUI.insets(0, 0, 4, StringUtil.isEmpty(myNewNamePrefix.getText()) ? 0 : 1);
        gbConstraints.insets = JBUI.insetsBottom(4);
        gbConstraints.weighty = 0;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gbConstraints.fill = GridBagConstraints.BOTH;

        var label = new JLabel();
        label.setText(XmlStringUtil.wrapInHtml("Rename common table expression and its usages to:"));
        panel.add(label, gbConstraints);

        result = new JBTextField();
        panel.add(result, gbConstraints);

        return panel;
    }

    @Override
    protected void doOKAction() {
        if (result.getText().isBlank()) {
            return;
        }
        super.doOKAction();
    }
}
