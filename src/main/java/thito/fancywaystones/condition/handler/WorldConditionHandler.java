package thito.fancywaystones.condition.handler;

import org.bukkit.entity.Player;
import thito.fancywaystones.Placeholder;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.WaystoneLocation;
import thito.fancywaystones.condition.ConditionHandler;

import java.util.List;
import java.util.UUID;

public class WorldConditionHandler implements ConditionHandler {
    private List<String> worldList;

    public WorldConditionHandler(List<String> worldList) {
        this.worldList = worldList;
    }

    @Override
    public String test(Placeholder placeholder) {
        if (worldList != null && !worldList.isEmpty()) {
            WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
            if (data != null) {
                WaystoneLocation waystoneLocation = data.getLocation();
                UUID worldUUID = waystoneLocation.getWorldUUID();
                return worldList.contains(worldUUID == null ? "" : worldUUID.toString()) ? null :
                        placeholder.clone().put("worlds", ph -> String.join(", ", worldList)).replace("{language.condition.worlds}");
            }
            Player player = placeholder.get(Placeholder.PLAYER);
            if (player != null) {
                return worldList.contains(player.getWorld().getName()) ? null :
                        placeholder.clone().put("worlds", ph -> String.join(", ", worldList)).replace("{language.condition.worlds}");
            }
        }
        return null;
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        if (worldList != null && !worldList.isEmpty()) {
            WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
            if (data != null) {
                WaystoneLocation waystoneLocation = data.getLocation();
                UUID worldUUID = waystoneLocation.getWorldUUID();
                return worldList.contains(worldUUID == null ? "" : worldUUID.toString()) ?
                        placeholder.clone().put("worlds", ph -> String.join(", ", worldList)).replace("{language.condition.not-worlds}") : null;
            }
            Player player = placeholder.get(Placeholder.PLAYER);
            if (player != null) {
                return worldList.contains(player.getWorld().getName()) ?
                        placeholder.clone().put("worlds", ph -> String.join(", ", worldList)).replace("{language.condition.not-worlds}") : null;
            }
        }
        return placeholder.clone().put("worlds", ph -> String.join(", ", worldList)).replace("{language.condition.not-worlds}");
    }

    public List<String> getWorldList() {
        return worldList;
    }

}
