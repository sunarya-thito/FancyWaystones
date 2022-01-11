package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;

public class NegatedConditionHandler implements ConditionHandler {
    private ConditionHandler conditionHandler;

    public NegatedConditionHandler(ConditionHandler conditionHandler) {
        this.conditionHandler = conditionHandler;
    }

    @Override
    public String test(Placeholder placeholder) {
        return conditionHandler.testNegate(placeholder);
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return conditionHandler.test(placeholder);
    }
}
