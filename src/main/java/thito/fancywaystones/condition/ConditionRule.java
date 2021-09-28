package thito.fancywaystones.condition;

import thito.fancywaystones.*;

public class ConditionRule {
    private Condition subCondition;
    private ConditionHandler handler;

    public ConditionRule(ConditionHandler handler) {
        this.handler = handler;
    }

    public ConditionHandler getHandler() {
        return handler;
    }

    public Condition getSubCondition() {
        return subCondition;
    }

    public void setSubCondition(Condition subCondition) {
        this.subCondition = subCondition;
    }

    public boolean test(Placeholder placeholder) {
        return handler.test(placeholder) && (subCondition == null || subCondition.test(placeholder));
    }
}
