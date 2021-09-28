package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;
import thito.fancywaystones.location.*;

public class LandAccessConditionHandler implements ConditionHandler {
    @Override
    public boolean test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData waystoneData = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && waystoneData != null) {
            WaystoneLocation waystoneLocation = waystoneData.getLocation();
            if (waystoneLocation instanceof LocalLocation) {
                return Util.checkDirectAccess(player, ((LocalLocation) waystoneLocation).getLocation().getBlock());
            }
        }
        return false;
    }
}
