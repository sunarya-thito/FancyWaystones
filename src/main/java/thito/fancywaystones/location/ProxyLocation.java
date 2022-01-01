package thito.fancywaystones.location;

import com.google.common.base.Objects;
import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.proxy.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.Level;

public class ProxyLocation implements WaystoneLocation {
    private String serverName;
    private UUID worldUUID;
    private int x, y, z;
    private World.Environment environment;

    public ProxyLocation(String serverName, UUID worldUUID, int x, int y, int z, World.Environment environment) {
        this.serverName = serverName;
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.environment = environment;
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public int getBlockX() {
        return x;
    }

    @Override
    public int getBlockY() {
        return y;
    }

    @Override
    public int getBlockZ() {
        return z;
    }

    @Override
    public World.Environment getEnvironment() {
        return environment;
    }

    public String getServerName() {
        return serverName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public void transport(Player player, WaystoneData source, WaystoneData target, Consumer<TeleportState> stateConsumer) {
        ProxyWaystone proxyWaystone = FancyWaystones.getPlugin().getProxyWaystone();
        if (proxyWaystone != null) {
            proxyWaystone.transportPlayer(player, this, source == null ? null : source.getUUID(), target == null ? null : target.getUUID());
            stateConsumer.accept(TeleportState.SUCCESS);
        } else {
            FancyWaystones.getPlugin().getLogger().log(Level.WARNING, "Attempting to teleport to other server with Proxy Mode off");
        }
    }

    @Override
    public double distance(WaystoneLocation location) {
        if (location instanceof ProxyLocation) {
            if (Objects.equal(serverName, ((ProxyLocation) location).serverName)) {
                if (Objects.equal(worldUUID, ((ProxyLocation) location).worldUUID)) {
                    return Math.sqrt(Math.pow(x - ((ProxyLocation) location).getX(), 2) +
                            Math.pow(y - ((ProxyLocation) location).getY(), 2) +
                            Math.pow(z - ((ProxyLocation) location).getZ(), 2));
                }
                return Double.MAX_VALUE;
            }
        }
        return Double.NaN;
    }

}
