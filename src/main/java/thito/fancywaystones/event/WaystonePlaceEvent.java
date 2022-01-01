package thito.fancywaystones.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import thito.fancywaystones.WaystoneData;

public class WaystonePlaceEvent extends WaystoneEvent {
    private Player playerData;
    private Location location;

    public WaystonePlaceEvent(WaystoneData waystoneData, Player playerData, Location location) {
        super(waystoneData);
        this.playerData = playerData;
        this.location = location;
    }

    public Player getPlayer() {
        return playerData;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
