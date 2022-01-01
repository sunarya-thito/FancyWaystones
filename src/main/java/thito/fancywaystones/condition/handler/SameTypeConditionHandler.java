package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;

public class SameTypeConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        return placeholder.get(Placeholder.SOURCE_WAYSTONE).getType() == placeholder.get(Placeholder.WAYSTONE).getType() ?
                null : placeholder.clone().put("{type}", ph -> ph.get(Placeholder.WAYSTONE).getType().name()).replace("{language.condition.same-type}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        return placeholder.get(Placeholder.SOURCE_WAYSTONE).getType() != placeholder.get(Placeholder.WAYSTONE).getType() ?
                null : placeholder.clone().put("{type}", ph -> ph.get(Placeholder.WAYSTONE).getType().name()).replace("{language.condition.not-same-type}");
    }
}
