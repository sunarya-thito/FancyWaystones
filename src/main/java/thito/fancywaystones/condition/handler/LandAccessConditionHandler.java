package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;
import thito.fancywaystones.location.*;

public class LandAccessConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData waystoneData = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && waystoneData != null) {
            WaystoneLocation waystoneLocation = waystoneData.getLocation();
            if (waystoneLocation instanceof LocalLocation) {
                return Util.checkDirectAccess(player, ((LocalLocation) waystoneLocation).getLocation().getBlock()) ? null :
                        placeholder.replace("{language.condition.land-access}");
            }
        }
        return placeholder.replace("{language.condition.land-access}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData waystoneData = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && waystoneData != null) {
            WaystoneLocation waystoneLocation = waystoneData.getLocation();
            if (waystoneLocation instanceof LocalLocation) {
                return Util.checkDirectAccess(player, ((LocalLocation) waystoneLocation).getLocation().getBlock()) ? placeholder.replace("{language.condition.not-land-access}") :
                        null;
            }
        }
        return null;
    }
}
