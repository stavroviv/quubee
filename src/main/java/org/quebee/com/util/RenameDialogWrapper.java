package org.quebee.com.util;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.xml.util.XmlStringUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class RenameDialogWrapper extends DefaultDialogWrapper {
    private final String input;
    private final String header;
    @Getter
    private JBTextField result;
    private JLabel label;

    public RenameDialogWrapper(String input, String header) {
        super(null, false);
        this.input = input;
        this.header = header;
        setTitle("Rename");
        setSize(400, 50);
        setValues();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return result;
    }

    private void setValues() {
        result.setText(input);
        label.setText(XmlStringUtil.wrapInHtml(header));
    }

    protected ValidationInfo doValidate() {
        if (result.getText().isBlank()) {
            return new ValidationInfo("Field must be set", result);
        }
        var validateSource = validateSource();
        if (Objects.nonNull(validateSource)) {
            return new ValidationInfo(validateSource, result);
        }
        return null;
    }

    protected String validateSource() {
        return null;
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

        label = new JLabel();
        panel.add(label, gbConstraints);

        result = new JBTextField();
        panel.add(result, gbConstraints);

        return panel;
    }
}
