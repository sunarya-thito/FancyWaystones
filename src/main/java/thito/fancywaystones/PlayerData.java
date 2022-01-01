package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.location.DeathLocation;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class PlayerData {
    private Player player;
    private UUID uuid;
    private final List<WaystoneData> knownWaystones = Collections.synchronizedList(new ArrayList<>());
    private long deathTime;
    private DeathLocation deathLocation;

    public PlayerData(Player player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    public boolean shouldSave() {
        long time = System.currentTimeMillis() - deathTime;
        return knownWaystones.size() > 0 || (deathLocation != null && time < Util.tickToMillis(FancyWaystones.getPlugin().getDeathBook().getDeathLocationTimeout()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData)) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(long deathTime) {
        this.deathTime = deathTime;
        attemptSave();
    }

    public void dispatchDeath(Location location) {
        this.deathLocation = new DeathLocation(location);
        this.deathTime = System.currentTimeMillis();
        attemptSave();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public boolean knowWaystone(WaystoneData data) {
        if (!data.getType().isActivationRequired()) {
            return true;
        }
        return knownWaystones.contains(data);
    }

    public void addWaystone(WaystoneData waystoneData) {
        if (waystoneData != null && waystoneData.getType().isActivationRequired() && !knownWaystones.contains(waystoneData)) {
            if (waystoneData.getBlacklist().contains(new WaystoneMember(getUUID()))) return;
            waystoneData.getStatistics().setTotalUsers(waystoneData.getStatistics().getTotalUsers() + 1);
            waystoneData.addAttached(this);
            knownWaystones.add(waystoneData);
            WaystoneBlock waystoneBlock = waystoneData.getWaystoneBlock();
            WaystoneMember m = new WaystoneMember(getUUID(), getPlayer().getName());
            if (!waystoneData.getMembers().contains(m) && waystoneData.getMembers().add(m)) {
                waystoneData.attemptSave();
            }
            if (waystoneBlock != null) {
                waystoneBlock.update(player);
            }
            attemptSave();
        }
    }

    public DeathLocation getDeathLocation() {
        return deathLocation;
    }

    public void setDeathLocation(DeathLocation deathLocation) {
        this.deathLocation = deathLocation;
        attemptSave();
    }

    public void addWaystone(UUID waystoneData) {
        WaystoneData data = WaystoneManager.getManager().getData(waystoneData);
        if (data != null) {
            addWaystone(data);
        } else FancyWaystones.getPlugin().getLogger()
            .log(Level.INFO, "Missing WaystoneData for "+getUUID()+": "+waystoneData);
    }

    public void removeWaystone(UUID waystoneData) {
        knownWaystones.removeIf(x -> {
            if (x.getUUID().equals(waystoneData)) {
                x.removeAttached(this);
                if (x.getMembers().removeIf(d -> d.getUUID().equals(getUUID()))) {
                    x.attemptSave();
                }
                return true;
            }
            return false;
        });
        attemptSave();
    }

    public void attemptSave() {
        FancyWaystones.getPlugin().submitIO(() -> {
            if (shouldSave()) {
                try {
                    WaystoneManager.getManager().savePlayerData(this);
                } catch (IOException e) {
                    FancyWaystones.getPlugin().getLogger().log(Level.SEVERE, "Failed to save Player Data of "+player+"/"+uuid, e);
                }
            } else {
                WaystoneManager.getManager().getStorage().removePlayerData(getUUID());
            }
        });
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<WaystoneData> getKnownWaystones() {
        return knownWaystones;
    }

    void load(ConfigurationSection section) {
        knownWaystones.clear();
        if (section != null) {
            deathTime = section.getLong("deathTime");
            ConfigurationSection death = section.getConfigurationSection("deathLocation");
            if (death != null) {
                String server = death.getString("server");
                String world = death.getString("worldUID");
                if (server != null && world != null) {
                    deathLocation = new DeathLocation(server, UUID.fromString(world),
                            death.getDouble("x"), death.getDouble("y"), death.getDouble("z"));
                }
            }
            List<String> known = section.getStringList("knownWaystones");
            for (String k : known) {
                WaystoneData wd = WaystoneManager.getManager().getData(UUID.fromString(k));
                if (wd != null) {
                    addWaystone(wd);
                }
            }
        }
    }


    void save(ConfigurationSection section) {
        if (section != null) {
            section.set("deathTime", deathTime);
            synchronized (knownWaystones) {
                section.set("knownWaystones", knownWaystones.stream().map(x -> x.getUUID().toString()).collect(Collectors.toList()));
            }
            if (deathLocation != null) {
                section.set("deathLocation.server", deathLocation.getServerName());
                section.set("deathLocation.worldUID", deathLocation.getWorldUID().toString());
                section.set("deathLocation.x", deathLocation.getX());
                section.set("deathLocation.y", deathLocation.getY());
                section.set("deathLocation.z", deathLocation.getZ());
            }
        }
    }
}
