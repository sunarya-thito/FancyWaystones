package thito.fancywaystones.condition;

import thito.fancywaystones.Placeholder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConditionRule {
    private Condition subCondition;
    private boolean negate;
    private ConditionHandler handler;

    public ConditionRule(ConditionHandler handler) {
        this.handler = handler;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public boolean isNegate() {
        return negate;
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

    public List<String> test(Placeholder placeholder) {
        String tested = negate ? handler.testNegate(placeholder) : handler.test(placeholder);
        if (tested != null) return Collections.singletonList(tested);
        if (subCondition != null) {
            return subCondition.test(placeholder);
        }
        return null;
    }
}
