package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.condition.ConditionHandler;

public class NaturalWaystoneConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        WaystoneData waystoneData = placeholder.get(Placeholder.WAYSTONE);
        if (waystoneData != null) {
            if (waystoneData.isNaturalWaystone()) {
                return placeholder.replace("{language.condition.natural-waystone}");
            }
        }
        return null;
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        WaystoneData waystoneData = placeholder.get(Placeholder.WAYSTONE);
        if (waystoneData != null) {
            if (!waystoneData.isNaturalWaystone()) {
                return placeholder.replace("{language.condition.not-natural-waystone}");
            }
        }
        return null;
    }
}
