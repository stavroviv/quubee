package org.quebee.com.model;

import java.util.Objects;

public class ConditionElement {
    private boolean custom;
    private String condition;
    private String conditionLeft;
    private String conditionComparison;
    private String conditionRight;

    public String getConditionLeft() {
        return conditionLeft;
    }

    public void setConditionLeft(String conditionLeft) {
        this.conditionLeft = conditionLeft;
    }

    public String getConditionComparison() {
        return Objects.isNull(conditionComparison) ? "=" : conditionComparison;
    }

    public void setConditionComparison(String conditionComparison) {
        this.conditionComparison = conditionComparison;
    }

    public String getConditionRight() {
        return Objects.isNull(conditionRight) ? "" : conditionRight;
    }

    public void setConditionRight(String conditionRight) {
        this.conditionRight = conditionRight;
    }

    public String getCondition() {
        return Objects.isNull(condition) ? "" : condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
