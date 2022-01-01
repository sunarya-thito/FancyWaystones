package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class AlwaysConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        return null;
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return placeholder.replace("{language.condition.never}");
    }
}
