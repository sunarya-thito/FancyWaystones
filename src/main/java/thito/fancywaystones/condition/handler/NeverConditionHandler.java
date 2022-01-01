package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class NeverConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        return placeholder.replace("{language.condition.never}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return null;
    }
}
