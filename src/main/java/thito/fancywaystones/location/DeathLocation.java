package thito.fancywaystones.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import thito.fancywaystones.FancyWaystones;
import thito.fancywaystones.proxy.ProxyWaystone;
import thito.fancywaystones.proxy.SerializableLocation;
import thito.fancywaystones.proxy.message.TeleportMessage;
import thito.fancywaystones.task.DeathBookTeleportTask;

import java.util.UUID;

public class DeathLocation {
    private String serverName;
    private UUID worldName;
    private double x, y, z;

    public DeathLocation(String serverName, UUID worldName, double x, double y, double z) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public DeathLocation(Location location) {
        this(FancyWaystones.getPlugin().getServerName(), location.getWorld().getUID(), location.getX(), location.getY(), location.getZ());
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return new Location(world, x, y, z);
        }
        return null;
    }

    public void teleport(Player player) {
        if (serverName.equals(FancyWaystones.getPlugin().getServerName())) {
            Location location = toLocation();
            if (location != null) {
                DeathBookTeleportTask task = new DeathBookTeleportTask(player, location, FancyWaystones.getPlugin().getDeathBook().getCheckRadius(), FancyWaystones.getPlugin().getDeathBook().getCheckHeight(), FancyWaystones.getPlugin().getDeathBook().ignoreSafeTeleport());
                if (FancyWaystones.getPlugin().isEnabled()) {
                    Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), task);
                }
            }
        } else {
            ProxyWaystone ws = FancyWaystones.getPlugin().getProxyWaystone();
            if (ws != null) {
                Location location = player.getLocation();
                ws.sendMessage(player, new TeleportMessage(player.getUniqueId(), true,
                        new SerializableLocation(null, FancyWaystones.getPlugin().getServerName(), location.getWorld().getUID(), location.getX(), location.getY(), location.getZ()),
                        new SerializableLocation(null, serverName, worldName, x, y, z)));
            }
        }
    }

    public String getServerName() {
        return serverName;
    }

    public UUID getWorldUID() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
