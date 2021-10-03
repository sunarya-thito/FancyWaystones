package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class ExplosionConditionHandler implements ConditionHandler {
    public static final VariableContent<Boolean> EXPLOSION_CAUSE = new VariableContent<>(Boolean.class);
    @Override
    public boolean test(Placeholder placeholder) {
        return placeholder.hasContent(EXPLOSION_CAUSE);
    }
}
