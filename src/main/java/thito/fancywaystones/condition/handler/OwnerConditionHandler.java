package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

import java.util.UUID;

public class OwnerConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && data != null) {
            UUID ownerUUID = data.getOwnerUUID();
            if (ownerUUID != null) return ownerUUID.equals(player.getUniqueId()) || (ownerUUID.getLeastSignificantBits() == 0 && ownerUUID.getMostSignificantBits() == 0) ? null :
                    placeholder.replace("{language.condition.owner}");
        }
        return placeholder.replace("{language.condition.owner}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        if (player != null && data != null) {
            UUID ownerUUID = data.getOwnerUUID();
            if (ownerUUID != null) return ownerUUID.equals(player.getUniqueId()) || (ownerUUID.getLeastSignificantBits() == 0 && ownerUUID.getMostSignificantBits() == 0) ?
                    placeholder.replace("{language.condition.not-owner}") : null;
        }
        return null;
    }
}
