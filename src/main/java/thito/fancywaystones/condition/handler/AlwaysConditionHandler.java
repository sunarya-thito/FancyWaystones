package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class AlwaysConditionHandler implements ConditionHandler {
    @Override
    public boolean test(Placeholder placeholder) {
        return true;
    }
}
