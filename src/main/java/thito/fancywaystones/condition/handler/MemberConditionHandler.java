package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class MemberConditionHandler implements ConditionHandler {
    @Override
    public boolean test(Placeholder placeholder) {
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        Player player = placeholder.get(Placeholder.PLAYER);
        if (data != null && player != null) {
            return data.getMembers().contains(new WaystoneMember(player.getUniqueId()));
        }
        return false;
    }
}
