package thito.fancywaystones.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import thito.fancywaystones.PlayerData;

public class DeathBookPostTeleportEvent extends FWEvent {
    private Location deathLocation;
    private Player playerData;

    public DeathBookPostTeleportEvent(Location deathLocation, Player playerData) {
        this.deathLocation = deathLocation;
        this.playerData = playerData;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

    public Player getPlayer() {
        return playerData;
    }
}
