package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;

public class SameDimensionConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        return placeholder.get(Placeholder.WAYSTONE).getLocation().getEnvironment() == placeholder.get(Placeholder.PLAYER).getWorld().getEnvironment() ?
                null : placeholder.clone().put("dimension", ph -> ph.get(Placeholder.WAYSTONE).getEnvironment().name())
                .replace("{language.condition.same-dimension}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return placeholder.get(Placeholder.WAYSTONE).getLocation().getEnvironment() != placeholder.get(Placeholder.PLAYER).getWorld().getEnvironment() ?
                null : placeholder.clone().put("dimension", ph -> ph.get(Placeholder.WAYSTONE).getEnvironment().name())
                .replace("{language.condition.not-same-dimension}");
    }
}
