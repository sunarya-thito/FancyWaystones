package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.location.*;

import java.util.UUID;
import java.util.function.*;

public interface WaystoneLocation {
    static boolean isCrossWorld(double distance) {
        return distance == Double.MAX_VALUE;
    }
    static boolean isCrossServer(double distance) {
        return Double.isNaN(distance);
    }
    void transport(Player player, WaystoneData source, WaystoneData target, Consumer<TeleportState> stateConsumer);
    double distance(WaystoneLocation location);
    int getBlockX();
    int getBlockY();
    int getBlockZ();
    UUID getWorldUUID();
    World.Environment getEnvironment();
}
