package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.WaystoneLocation;
import thito.fancywaystones.condition.ConditionHandler;
import thito.fancywaystones.location.LocalLocation;

public class SameWorldConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        WaystoneLocation location = placeholder.get(Placeholder.WAYSTONE).getLocation();
        return location instanceof LocalLocation && ((LocalLocation) location).getLocation().getWorld() == placeholder.get(Placeholder.PLAYER).getWorld() ?
                null : placeholder.clone().put("world", ph -> location.getWorldUUID()).replace("{language.condition.same-world}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        WaystoneLocation location = placeholder.get(Placeholder.WAYSTONE).getLocation();
        return location instanceof LocalLocation && ((LocalLocation) location).getLocation().getWorld() == placeholder.get(Placeholder.PLAYER).getWorld() ?
                placeholder.clone().put("world", ph -> location.getWorldUUID()).replace("{language.condition.not-same-world}") : null;
    }
}
