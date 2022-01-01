package thito.fancywaystones.event;

import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;

public class WaystoneUseEvent extends WaystoneEvent {
    private PlayerData playerData;

    public WaystoneUseEvent(WaystoneData waystoneData, PlayerData playerData) {
        super(waystoneData);
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
