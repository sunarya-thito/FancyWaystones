package thito.fancywaystones.event;

import thito.fancywaystones.PlayerData;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.economy.Cost;

import java.util.List;

public class WaystonePreTeleportEvent extends WaystoneEvent {
    private final PlayerData playerData;
    private WaystoneData targetWaystone;
    private List<Cost> costList;

    public WaystonePreTeleportEvent(WaystoneData waystoneData, PlayerData playerData, WaystoneData targetWaystone, List<Cost> costList) {
        super(waystoneData);
        this.playerData = playerData;
        this.targetWaystone = targetWaystone;
        this.costList = costList;
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

    public List<Cost> getCostList() {
        return costList;
    }

    public void setCostList(List<Cost> costList) {
        this.costList = costList;
    }
}
