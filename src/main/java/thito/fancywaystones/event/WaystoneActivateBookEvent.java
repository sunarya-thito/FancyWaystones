package thito.fancywaystones.event;

import org.bukkit.entity.Player;
import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;

public class WaystoneActivateBookEvent extends WaystoneEvent {
    private final Player playerData;

    public WaystoneActivateBookEvent(WaystoneData waystoneData, Player playerData) {
        super(waystoneData);
        this.playerData = playerData;
    }

    public Player getPlayer() {
        return playerData;
    }
}
