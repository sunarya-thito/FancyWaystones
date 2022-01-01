package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;
import thito.fancywaystones.location.LocalLocation;

public class SameServerConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        return placeholder.get(Placeholder.WAYSTONE).getLocation() instanceof LocalLocation ?
                null : placeholder.replace("{language.condition.same-server}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return placeholder.get(Placeholder.WAYSTONE).getLocation() instanceof LocalLocation ?
                placeholder.replace("{language.condition.not-same-server}") : null;
    }
}
