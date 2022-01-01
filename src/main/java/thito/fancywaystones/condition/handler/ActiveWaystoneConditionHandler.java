package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneManager;
import thito.fancywaystones.condition.ConditionHandler;

public class ActiveWaystoneConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        PlayerData loaded = WaystoneManager.getManager().getLoadedPlayerData(placeholder.get(Placeholder.PLAYER).getUniqueId());
        return loaded != null && loaded.knowWaystone(placeholder.get(Placeholder.WAYSTONE)) ? null :
                placeholder.replace("{language.condition.active-waystone}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        PlayerData loaded = WaystoneManager.getManager().getLoadedPlayerData(placeholder.get(Placeholder.PLAYER).getUniqueId());
        return loaded != null && loaded.knowWaystone(placeholder.get(Placeholder.WAYSTONE)) ? placeholder.replace("{language.condition.not-active-waystone}") :
                null;
    }
}
