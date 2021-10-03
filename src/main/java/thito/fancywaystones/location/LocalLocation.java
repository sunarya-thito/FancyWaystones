package thito.fancywaystones.location;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.task.*;

import java.util.function.*;

public class LocalLocation implements WaystoneLocation {
    private Location location;

    public LocalLocation(Location location) {
        this.location = location;
    }

    @Override
    public int getBlockX() {
        return location.getBlockX();
    }

    @Override
    public int getBlockY() {
        return location.getBlockY();
    }

    @Override
    public int getBlockZ() {
        return location.getBlockZ();
    }

    @Override
    public String getWorldName() {
        return location.getWorld().getName();
    }

    @Override
    public World.Environment getEnvironment() {
        return location.getWorld().getEnvironment();
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void transport(Player player, WaystoneData source, WaystoneData target, Consumer<TeleportState> stateConsumer) {
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), new TeleportTask(
                    player, location, FancyWaystones.getPlugin().getCheckRadius(),
                    FancyWaystones.getPlugin().getCheckHeight(), FancyWaystones.getPlugin().isForceTeleportation()) {
                @Override
                protected void done() {
                    if (isSuccess()) {
                        stateConsumer.accept(TeleportState.SUCCESS);
                    } else {
                        stateConsumer.accept(TeleportState.UNSAFE);
                    }
                }
            });
        }
    }

    @Override
    public double distance(WaystoneLocation location) {
        if (location instanceof LocalLocation) {
            Location other = ((LocalLocation) location).getLocation();
            if (other.getWorld() == getLocation().getWorld()) {
                return getLocation().distance(other);
            }
            return Double.MAX_VALUE; // multi world
        }
        return Double.NaN; // multi server
    }

}
