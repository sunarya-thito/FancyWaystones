package thito.fancywaystones.event;

import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.economy.Cost;

import java.util.List;

public class WaystoneTeleportEvent extends WaystoneEvent {
    private final PlayerData playerData;
    private WaystoneData targetWaystone;
    private Cost cost;

    public WaystoneTeleportEvent(WaystoneData waystoneData, PlayerData playerData, WaystoneData targetWaystone, Cost costList) {
        super(waystoneData);
        this.playerData = playerData;
        this.targetWaystone = targetWaystone;
        this.cost = costList;
    }

    public WaystoneData getTargetWaystone() {
        return targetWaystone;
    }

    public void setTargetWaystone(WaystoneData targetWaystone) {
        this.targetWaystone = targetWaystone;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }
}
