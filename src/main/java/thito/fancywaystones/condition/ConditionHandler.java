package thito.fancywaystones.condition;

import thito.fancywaystones.*;

public interface ConditionHandler {
    String test(Placeholder placeholder);
    String testNegate(Placeholder placeholder);
}
