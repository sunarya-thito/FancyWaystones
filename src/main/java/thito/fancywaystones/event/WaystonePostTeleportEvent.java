package thito.fancywaystones.event;

import org.bukkit.entity.Player;
import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.economy.Cost;

import java.util.List;

public class WaystonePostTeleportEvent extends WaystoneEvent {
    private final Player playerData;
    private WaystoneData targetWaystone;

    public WaystonePostTeleportEvent(WaystoneData waystoneData, Player playerData, WaystoneData targetWaystone) {
        super(waystoneData);
        this.playerData = playerData;
        this.targetWaystone = targetWaystone;
    }

    public WaystoneData getTargetWaystone() {
        return targetWaystone;
    }

    public void setTargetWaystone(WaystoneData targetWaystone) {
        this.targetWaystone = targetWaystone;
    }

    public Player getPlayer() {
        return playerData;
    }

}
