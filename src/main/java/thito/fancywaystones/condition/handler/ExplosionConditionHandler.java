package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class ExplosionConditionHandler implements ConditionHandler {
    public static final VariableContent<Boolean> EXPLOSION_CAUSE = new VariableContent<>(Boolean.class);
    @Override
    public String test(Placeholder placeholder) {
        return placeholder.hasContent(EXPLOSION_CAUSE) ? null : placeholder.replace("{language.condition.explosion}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return placeholder.hasContent(EXPLOSION_CAUSE) ? placeholder.replace("{language.condition.not-explosion}") : null;
    }
}
