package thito.fancywaystones.event;

import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;

public class WaystoneActivateEvent extends WaystoneEvent {
    private final PlayerData playerData;

    public WaystoneActivateEvent(WaystoneData waystoneData, PlayerData playerData) {
        super(waystoneData);
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
