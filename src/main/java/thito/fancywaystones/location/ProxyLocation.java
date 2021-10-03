package thito.fancywaystones.location;

import com.google.common.base.Objects;
import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.proxy.*;

import java.util.*;
import java.util.function.*;

public class ProxyLocation implements WaystoneLocation {
    private String serverName;
    private String worldName;
    private int x, y, z;
    private World.Environment environment;

    public ProxyLocation(String serverName, String worldName, int x, int y, int z, World.Environment environment) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.environment = environment;
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

    public String getWorldName() {
        return worldName;
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
        }
    }

    @Override
    public double distance(WaystoneLocation location) {
        if (location instanceof ProxyLocation) {
            if (Objects.equal(serverName, ((ProxyLocation) location).serverName)) {
                if (Objects.equal(worldName, ((ProxyLocation) location).worldName)) {
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
