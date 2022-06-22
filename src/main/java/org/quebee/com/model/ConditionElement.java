package org.quebee.com.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionElement {
    private boolean custom;
    private String condition;
    private String conditionLeft;
    private String conditionComparison;
    private String conditionRight;

    public ConditionElement() {
    }

    public ConditionElement(String condition) {
        this.condition = condition;
        this.custom = true;
    }
}
