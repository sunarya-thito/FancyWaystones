package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class OwnerConditionHandler implements ConditionHandler {
    @Override
    public boolean test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && data != null) return data.getOwnerUUID().equals(player.getUniqueId());
        return false;
    }
}
