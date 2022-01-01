package thito.fancywaystones.event;

import thito.fancywaystones.PlayerData;
import thito.fancywaystones.location.DeathLocation;

public class DeathBookTeleportEvent extends FWEvent {
    private DeathLocation deathLocation;
    private PlayerData playerData;

    public DeathBookTeleportEvent(DeathLocation deathLocation, PlayerData playerData) {
        this.deathLocation = deathLocation;
        this.playerData = playerData;
    }

    public DeathLocation getDeathLocation() {
        return deathLocation;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
