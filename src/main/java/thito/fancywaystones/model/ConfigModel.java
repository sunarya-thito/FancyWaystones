package thito.fancywaystones.model;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import thito.fancywaystones.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.model.config.*;
import thito.fancywaystones.model.config.component.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;

public class ConfigModel extends WaystoneModel {

    private String id;
    private String name;
    private ComponentConfig[] componentConfigs;
    public ConfigModel(String id, ConfigurationSection config) {
        this.id = id;
        name = config.getString("name", id);
        ArrayList<ComponentConfig> configs = new ArrayList<>();
        for (Map map : config.getMapList("components")) {
            try {
                MapSection conf = new MapSection(map);
                ComponentConfig c = new ComponentConfig(conf);
                Vector offset = c.getOffset();
                minX = Math.min(minX, offset.getBlockX());
                minY = Math.min(minY, offset.getBlockY());
                minZ = Math.min(minZ, offset.getBlockZ());
                maxX = Math.max(minX, offset.getBlockX());
                maxY = Math.max(maxY, offset.getBlockY());
                maxZ = Math.max(maxZ, offset.getBlockZ());
                configs.add(c);
            } catch (Throwable t) {
                FancyWaystones.getPlugin().getLogger().log(Level.WARNING, "Failed to load component "+map.get("type"), t);
            }
        }
        componentConfigs = configs.toArray(new ComponentConfig[0]);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public WaystoneModelHandler createHandler(WaystoneData waystoneData) {
        return new Handler(this, waystoneData);
    }

    public class Handler extends WaystoneModelHandler {
        private WaystoneData data;
        private Component[] components;

        public Handler(WaystoneModel model, WaystoneData data) {
            super(model);
            this.data = data;
            WaystoneModel.ACTIVE_HANDLERS.add(this);
            components = Arrays.stream(componentConfigs).map(x -> x.createComponent(data)).toArray(Component[]::new);
        }

        @Override
        public WaystoneData getData() {
            return data;
        }

        @Override
        public void destroyImmediately() {
            WaystoneModel.ACTIVE_HANDLERS.remove(this);
            for (Component c : components) {
                c.destroyImmediately();
            }
        }

        @Override
        public void destroy() {
            WaystoneModel.ACTIVE_HANDLERS.remove(this);
            for (Component c : components) c.destroy();
        }

        @Override
        public boolean isPart(Location loc) {
            for (Component c : components) {
                if (!c.getHandler().hasBlockHitBox()) continue;
                Location o = c.getLocation();
                if (o.getWorld() == loc.getWorld() &&
                    o.getBlockX() == loc.getBlockX() &&
                    o.getBlockY() == loc.getBlockY() &&
                    o.getBlockZ() == loc.getBlockZ()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void update(Player player) {
            supplyIsActive(player, isActive -> {
                for (Component c : components) {
                    c.update(data, isActive ? WaystoneState.ACTIVE : WaystoneState.INACTIVE, player);
                }
            });
        }

        public void supplyIsActive(Player player, Consumer<Boolean> result) {
            if (!data.getType().isActivationRequired()) {
                result.accept(true);
            } else {
                FancyWaystones.getPlugin().submitIO(() -> {
                    PlayerData playerData = WaystoneManager.getManager().getPlayerData(player);
                    result.accept(playerData.knowWaystone(this.data));
                });
            }
        }

        @Override
        public void update() {
            Location loc = ((LocalLocation) data.getLocation()).getLocation();
            double viewDistance = Bukkit.getViewDistance() * 16;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() == loc.getWorld() && player.getLocation().distance(loc) < viewDistance) {
                    update(player);
                }
            }
        }

        @Override
        public void sendNoAccess(Player player) {
            for (Component c : components) {
                ComponentHandler handler = c.getHandler();
                if (handler instanceof HologramComponent.Handler) {
                    ((HologramComponent.Handler) handler).sendNoAccess(player);
                }
            }
        }
    }
}
