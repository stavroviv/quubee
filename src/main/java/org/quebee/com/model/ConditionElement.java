package org.quebee.com.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConditionElement {
    private boolean custom;
    private String condition;
    private String conditionLeft;
    private String conditionComparison;
    private String conditionRight;

    public ConditionElement(ConditionElement source) {
        this.custom = source.custom;
        this.condition = source.condition;
        this.conditionLeft = source.conditionLeft;
        this.conditionComparison = source.conditionComparison;
        this.conditionRight = source.conditionRight;
    }
}
