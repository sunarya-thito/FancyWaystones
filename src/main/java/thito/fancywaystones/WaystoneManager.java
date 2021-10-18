package thito.fancywaystones;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.economy.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.model.*;
import thito.fancywaystones.model.config.*;
import thito.fancywaystones.model.config.component.*;
import thito.fancywaystones.model.config.rule.*;
import thito.fancywaystones.proxy.message.*;
import thito.fancywaystones.types.*;
import thito.fancywaystones.ui.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

import static thito.fancywaystones.FancyWaystones.checkIOThread;

public class WaystoneManager {
    private static WaystoneManager manager;

    public static WaystoneManager getManager() {
        return manager;
    }

    private WaystoneStorage storage;
    private FancyWaystones plugin;
    private List<PlayerData> playerDataList = new ArrayList<>();
    private Set<EconomyService> economyServices = new HashSet<>();
    private final List<WaystoneData> loadedData = new ArrayList<>();
    private Map<String, ComponentType> componentTypeMap = new HashMap<>();
    private Map<String, StyleRule> styleRuleMap = new HashMap<>();
    private Map<String, WaystoneModel> modelMap = new HashMap<>();
    private WaystoneModel model = new ClientSideStandardModel();
    private Map<String, WaystoneType> typeMap = new HashMap<>();
    private Map<World, Map<Long, List<UUID>>> blockDataMap = new HashMap<>();
    private ConfigurationSection waystoneItem;
    private WaystoneData dummy;

    WaystoneManager(FancyWaystones plugin) {
        this.plugin = plugin;

        economyServices.add(new LevelEconomyService());
        try {
            economyServices.add(new VaultEconomyService());
        } catch (Throwable ignored) {
        }

        manager = this;

        dummy = new WaystoneData(UUID.randomUUID(), new DummyWaystoneType(), getDefaultModel(), World.Environment.NORMAL);
        dummy.setOwnerUUID(UUID.randomUUID());
        dummy.setOwnerName("Server");
        dummy.setName("Admin");

        styleRuleMap.put("state", new WaystoneStateRule());
        styleRuleMap.put("environment", new EnvironmentRule());
        styleRuleMap.put("waystone_type", new WaystoneTypeRule());

        componentTypeMap.put("hologram", new HologramComponent());
        componentTypeMap.put("block", new BlockComponent());
        componentTypeMap.put("armorstand", new ArmorStandComponent());

    }

    protected void putBlockData(Location location, UUID id) {
        Map<Long, List<UUID>> map = blockDataMap.computeIfAbsent(location.getWorld(), x -> new HashMap<>());
        List<UUID> list = map.computeIfAbsent(Util.getXY(location.getBlockX() >> 4, location.getBlockZ() >> 4), x -> new ArrayList<>());
        if (!list.contains(id)) {
            list.add(id);
        }
    }

    protected void removeBlockData(Location location, UUID id) {
        Map<Long, List<UUID>> map = blockDataMap.get(location.getWorld());
        if (map != null) {
            List<UUID> list = map.get(Util.getXY(location.getBlockX() >> 4, location.getBlockZ() >> 4));
            if (list != null) {
                list.remove(id);
                if (list.isEmpty()) {
                    map.remove(Util.getXY(location.getBlockX() >> 4, location.getBlockZ() >> 4));
                }
            }
            if (map.isEmpty()) {
                blockDataMap.remove(location.getWorld());
            }
        }
    }

    public WaystoneData getDataAt(Location location) {
        synchronized (WaystoneModel.ACTIVE_HANDLERS) {
            for (WaystoneModelHandler handler : WaystoneModel.ACTIVE_HANDLERS) {
                if (handler.isPart(location)) {
                    return handler.getData();
                }
            }
        }
        return null;
    }

    public void registerWaystoneType(WaystoneType type) {
        typeMap.put(type.name(), type);
    }

    public void registerWaystoneModel(WaystoneModel model) {
        modelMap.put(model.getId(), model);
    }

    public void unregisterWaystoneModel(WaystoneModel model) {
        modelMap.values().remove(model);
    }

    public boolean isInactive(WaystoneData waystoneData) {
        ConfigurationSection section = FancyWaystones.getPlugin().getConfig().getConfigurationSection("Waystone Inactivity");
        if (section != null && section.getBoolean("Enable")) {
            // Util.parseTime results in Ticks, multiply it by 50 to turn it into milliseconds
            long duration = Util.parseTime(section.getString("Duration", "40d")) * 50;
            String mode = section.getString("Mode");
            if ("ONLINE_TIME_OWNER".equals(mode)) {
                try {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(waystoneData.getOwnerUUID());
                    long time = System.currentTimeMillis() - offlinePlayer.getLastPlayed();
                    return (time > duration) && !offlinePlayer.isOnline();
                } catch (Throwable ignored) {
                }
            } else if ("LAST_USED".equals(mode)) {
                long time = System.currentTimeMillis() - waystoneData.getStatistics().getLastUsed();
                return (time > duration);
            } else if ("LAST_VISIT".equals(mode)) {
                long time = System.currentTimeMillis() - waystoneData.getStatistics().getLastVisit();
                return (time > duration);
            } else if ("LAST_VISITED".equals(mode)) {
                long time = System.currentTimeMillis() - waystoneData.getStatistics().getLastVisited();
                return (time > duration);
            }
        }
        return false;
    }

    public Map<String, WaystoneModel> getModelMap() {
        return modelMap;
    }

    public void unloadData(UUID id) {
        checkIOThread();
        WaystoneData data = getLoadedData().stream().filter(x -> x.getUUID().equals(id)).findAny().orElse(null);
        unloadData(data);
    }

    public void unloadData(WaystoneData data) {
        checkIOThread();
        if (data != null) {
            directUnloadData(data);
            for (PlayerData playerData : data.getAttached()) {
                playerData.getKnownWaystones().remove(data);
            }
        }
    }

    public void directUnloadData(WaystoneData data) {
        checkIOThread();
        loadedData.remove(data);
    }

    public Map<String, ComponentType> getComponentTypeMap() {
        return componentTypeMap;
    }

    public Map<String, StyleRule> getStyleRuleMap() {
        return styleRuleMap;
    }

    public WaystoneData getDummy() {
        return dummy;
    }

    public void shutdown() {
        checkIOThread();
        loadedData.forEach(snapshot -> {
            if (snapshot != null) {
                saveWaystone(snapshot);
            }
        });
        playerDataList.forEach(data -> {
            try {
                savePlayerData(data);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
            }
        });
    }

    public Set<EconomyService> getEconomyServices() {
        return economyServices;
    }

    public List<WaystoneData> getLoadedData() {
        return loadedData;
    }

    public Collection<WaystoneType> getTypes() {
        return typeMap.values();
    }

    public WaystoneType getType(String name) {
        return typeMap.get(name);
    }

    public void openWaystoneMenu(PlayerData player, WaystoneData data) {
        WaystoneMenu waystoneMenu = new WaystoneMenu(player, data);
        waystoneMenu.open();
        data.getOpenedMenus().add(waystoneMenu);
    }

    public void createWaystoneItem(WaystoneData data, boolean storeID, Consumer<ItemStack> result) {
        Placeholder placeholder = new Placeholder();
        placeholder.putContent(Placeholder.WAYSTONE, data);
        MinecraftItem item = new MinecraftItem();
        item.load(waystoneItem);
        saveToStringWaystoneData(data, storeID, string -> {
            item.setData("fancywaystones:waystoneData", string.getBytes(StandardCharsets.UTF_8));
            result.accept(item.getItemStack(placeholder));
        });
    }

    public void setStorage(WaystoneStorage storage) {
        checkIOThread();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk c : world.getLoadedChunks()) {
                try {
                    unloadChunk(c);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (WaystoneData snapshot : loadedData) {
            WaystoneBlock waystoneBlock = snapshot.getWaystoneBlock();
            if (waystoneBlock != null) {
                waystoneBlock.destroyModel();
            }
            saveWaystone(snapshot);
        }
        for (PlayerData playerData : playerDataList) {
            try {
                savePlayerData(playerData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.storage != null) {
            this.storage.close();
        }
        playerDataList.clear();
        loadedData.clear();
        this.storage = storage;
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                FancyWaystones.getPlugin().submitIO(() -> {
                    // PAPER FORKS TENDS TO HAVE THE WORLD LAZY LOADED
                    for (WaystoneType type : getTypes()) {
                        if (type.isAlwaysLoaded()) {
                            try {
                                List<byte[]> dataList = storage.readWaystones(type);
                                if (dataList != null) {
                                    for (byte[] data : dataList) {
                                        try {
                                            WaystoneData waystoneData = _loadWaystoneData(data);
                                        } catch (Throwable t) {
                                            plugin.getLogger().log(Level.SEVERE, "Failed to load waystone data", t);
                                        }
                                    }
                                }
                            } catch (Throwable t) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to list waystone data", t);
                            }
                        }
                    }
                });
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk c : world.getLoadedChunks()) {
                        FancyWaystones.getPlugin().submitIO(() -> {
                            loadChunk(c);
                        });
                    }
                }
            });
        }
    }

    public WaystoneStorage getStorage() {
        return storage;
    }

    public WaystoneModel getDefaultModel() {
        return model;
    }

    public void setModel(WaystoneModel model) {
        this.model = model;
    }

    public boolean hasWaystone(ItemStack item) {
        return Util.hasData(item, "fancywaystones:waystoneData");
    }

    public WaystoneData createData(WaystoneType type, World.Environment environment, WaystoneModel waystoneModel) {
        return new WaystoneData(
                UUID.randomUUID(),
                type,
                waystoneModel == null ? getDefaultModel() : waystoneModel,
                environment);
    }

    public String getDefaultWaystoneName() {
        return FancyWaystones.getPlugin().getConfig().getString("Default Waystone Name");
    }

    public WaystoneData getWaystoneFromItem(ItemStack item) {
        byte[] data = Util.getData(item, "fancywaystones:waystoneData");
        if (data != null) {
            try {
                WaystoneData d = loadWaystoneDataFromString(new String(data, StandardCharsets.UTF_8));
                return d;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    public void loadWaystoneItem(ConfigurationSection section) {
        waystoneItem = section;
    }

    public boolean containsIllegalWord(String string) {
        string = string.toLowerCase();
        for (String key : FancyWaystones.getPlugin().getConfig().getStringList("Blacklisted Names")) {
            key = key.toLowerCase();
            if (string.contains(key)) {
                return true;
            }
        }
        return false;
    }

    public WaystoneBlock placeWaystone(WaystoneData data, Location location) {
        checkIOThread();
        data.setLocation(new LocalLocation(location));
        WaystoneBlock block = new WaystoneBlock(data);
        block.spawn();
        data.setWaystoneBlock(block);
        return block;
    }

    public void refresh(UUID id) {
        checkIOThread();
        for (WaystoneData wd : loadedData) {
            if (wd.getUUID().equals(id)) {
                WaystoneLocation location = wd.getLocation();
                if (location instanceof LocalLocation) {
                    try {
                        refresh(wd);
                    } catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    public void refresh(WaystoneData data) throws IOException, InvalidConfigurationException {
        checkIOThread();
        if (!(data.getLocation() instanceof LocalLocation)) return;
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(new String(getStorage().readWaystoneData(data.getUUID()), StandardCharsets.UTF_8));
        data.getStatistics().load(config.getConfigurationSection("statistics"));
        data.setName(config.getString("name"));
        data.setLocation(new LocalLocation(new Location(Bukkit.getWorld(config.getString("world")), config.getInt("x"), config.getInt("y"), config.getInt("z"))));
        if (config.isConfigurationSection("members")) {
            data.getMembers().clear();
            for (String key : config.getConfigurationSection("members").getKeys(false)) {
                try {
                    data.getMembers().add(new WaystoneMember(UUID.fromString(key), config.getString("members." + key)));
                } catch (Exception e) {
                    // invalid UUID?
                }
            }
        }
        if (config.isConfigurationSection("blacklist")) {
            data.getBlacklist().clear();
            for (String key : config.getConfigurationSection("blacklist").getKeys(false)) {
                try {
                    data.getBlacklist().add(new WaystoneMember(UUID.fromString(key), config.getString("members." + key)));
                } catch (Exception e) {
                    // invalid UUID?
                }
            }
        }
    }

    public void loadChunk(Chunk chunk) {
        loadChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public void loadChunk(World world, int x, int z) {
        checkIOThread();
        long time = System.currentTimeMillis();
        File target = new File(FancyWaystones.getPlugin().getDataFolder(), getWorldPath()+"/"+world.getName()+"/"+x+"."+z+".yml");
        if (target.exists()) {
            YamlConfiguration configuration = new YamlConfiguration();
            try {
                configuration.load(target);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            List<String> list = configuration.getStringList("blocks");
            for (String next : list) {
                WaystoneData waystoneData = getData(UUID.fromString(next));
                if (waystoneData != null) waystoneData.directValidateBlock();
            }
        }
        long elapsed = System.currentTimeMillis() - time;
        FancyWaystones.getPlugin().pushSRWSpeed(elapsed);
    }

    private String getWorldPath() {
        return FancyWaystones.getPlugin().getConfig().getString("Storage.Block Directory");
    }

    public void unloadChunk(Chunk chunk) throws IOException {
        checkIOThread();
        for (int i = loadedData.size() - 1; i >= 0; i--) {
            WaystoneData data = loadedData.get(i);
            WaystoneBlock waystoneBlock = data.getWaystoneBlock();
            WaystoneLocation loc = data.getLocation();
            if (loc instanceof LocalLocation && waystoneBlock != null) {
                Location location = ((LocalLocation) loc).getLocation();
                if (location.getWorld() == chunk.getWorld() && location.getBlockX() >> 4 == chunk.getX() && location.getBlockZ() >> 4 == chunk.getZ()) {
                    waystoneBlock.destroyModelImmediately();
                    data._setWaystoneBlock(null);
                }
            }
            if (data.shouldUnload()) {
                loadedData.remove(i);
            }
        }
        Map<Long, List<UUID>> dataMap = blockDataMap.get(chunk.getWorld());
        List<UUID> list = dataMap == null ? Collections.emptyList() : dataMap.get(Util.getXY(chunk.getX(), chunk.getZ()));
        if (list == null || list.isEmpty()) {
            File target = new File(FancyWaystones.getPlugin().getDataFolder(), getWorldPath()+"/"+chunk.getWorld().getName()+"/"+chunk.getX()+"."+chunk.getZ()+".yml");
            target.delete();
        } else {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.set("blocks", list.stream().map(UUID::toString).collect(Collectors.toList()));
            File target = new File(FancyWaystones.getPlugin().getDataFolder(), getWorldPath()+"/"+chunk.getWorld().getName()+"/"+chunk.getX()+"."+chunk.getZ()+".yml");
            target.getParentFile().mkdirs();
            configuration.save(target);
        }
    }

    private WaystoneData getLoadedData(UUID id) {
        checkIOThread();
        for (WaystoneData n : loadedData) {
            if (n.getUUID().equals(id)) {
                return n;
            }
        }
        return null;
    }

    public WaystoneData getData(UUID id) {
        checkIOThread();
        WaystoneData data = getLoadedData(id);
        if (data == null) {
            data = loadWaystoneDataPrintError(id);
        }
        return data;
    }

    public boolean prepare(UUID id) {
        checkIOThread();
        WaystoneData data = getLoadedData(id);
        if (data == null) {
            loadWaystoneDataPrintError(id);
            return true;
        }
        return false;
    }

    public void unloadPlayerData(UUID id) {
        checkIOThread();
        playerDataList.removeIf(x -> {
            if (x.getUUID().equals(id)) {
                for (WaystoneData data : x.getKnownWaystones()) {
                    data.removeAttached(x);
                }
                return true;
            }
            return false;
        });
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player, player.getUniqueId());
    }

    public PlayerData getPlayerData(Player player, UUID uuid) {
        checkIOThread();
        for (PlayerData data : playerDataList) {
            if (data.getUUID().equals(uuid)) {
                return data;
            }
        }
        PlayerData data = new PlayerData(player, uuid);
        if (player != null && uuid.equals(player.getUniqueId())) {
            playerDataList.add(data);
        }
        byte[] bytes = null;
        try {
            bytes = storage.readPlayerData(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bytes != null) {
            try {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.loadFromString(new String(bytes, StandardCharsets.UTF_8));
                data.load(configuration);
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data: "+uuid, t);
            }
        }
        return data;
    }

    public int getLoadedCount() {
        return loadedData.size();
    }

    public void savePlayerData(PlayerData data) throws IOException {
        checkIOThread();
        long time = System.currentTimeMillis();
        YamlConfiguration configuration = new YamlConfiguration();
        data.save(configuration);
        getStorage().writePlayerData(data.getUUID(), configuration.saveToString().getBytes(StandardCharsets.UTF_8));
        long elapsed = System.currentTimeMillis() - time;
        FancyWaystones.getPlugin().pushSRWSpeed(elapsed);
    }

    public void saveWaystone(WaystoneData data) {
        checkIOThread();
        long time = System.currentTimeMillis();
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.set("uuid", data.getUUID().toString());
            configuration.set("name", data.getName());
            UUID ownerUUID = data.getOwnerUUID();
            configuration.set("ownerUUID", ownerUUID == null ? null : ownerUUID.toString());
            configuration.set("ownerName", data.getOwnerName());
            configuration.set("type", data.getType().name());
            WaystoneLocation waystoneLocation = data.getLocation();
            if (waystoneLocation instanceof LocalLocation) {
                Location location = ((LocalLocation) waystoneLocation).getLocation();
                configuration.set("world", location.getWorld().getName());
                configuration.set("x", location.getBlockX());
                configuration.set("y", location.getBlockY());
                configuration.set("z", location.getBlockZ());
                configuration.set("serverName", FancyWaystones.getPlugin().getServerName());
            } else if (waystoneLocation instanceof ProxyLocation) {
                configuration.set("world", waystoneLocation.getWorldName());
                configuration.set("x", ((ProxyLocation) waystoneLocation).getX());
                configuration.set("y", ((ProxyLocation) waystoneLocation).getY());
                configuration.set("z", ((ProxyLocation) waystoneLocation).getZ());
                configuration.set("serverName", ((ProxyLocation) waystoneLocation).getServerName());
            }
            configuration.set("environment", data.getEnvironment().name());
            WaystoneModel model = data.getModel();
            if (model != null) {
                configuration.set("model", model.getId());
            }
            for (WaystoneMember member : data.getMembers()) {
                configuration.set("members."+member.getUUID(), member.getName());
            }
            for (WaystoneMember member : data.getBlacklist()) {
                configuration.set("blacklist."+member.getUUID(), member.getName());
            }
            ConfigurationSection statistics = configuration.createSection("statistics");
            data.getStatistics().save(statistics);
            storage.writeWaystoneData(data.getType(), data.getUUID(), configuration.saveToString().getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save waystone data: "+ data.getUUID(), t);
        }
        long elapsed = System.currentTimeMillis() - time;
        FancyWaystones.getPlugin().pushSRWSpeed(elapsed);
    }
    private void saveToStringWaystoneData(WaystoneData data, boolean storeID, Consumer<String> consumer) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            if (storeID) {
                configuration.set("uuid", data.getUUID().toString());
            }
            configuration.set("type", data.getType().name());
            configuration.set("environment", data.getEnvironment().name());
            WaystoneModel model = data.getModel();
            if (model != null) {
                configuration.set("model", model.getId());
            }
            for (WaystoneMember member : data.getBlacklist()) {
                configuration.set("blacklist."+member.getUUID(), member.getName());
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save waystone data", t);
        }
        consumer.accept(configuration.saveToString());
    }

    private WaystoneData loadWaystoneDataPrintError(UUID id) {
        try {
            return _loadWaystoneData(id);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected WaystoneData _loadWaystoneData(byte[] bytes) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(new String(bytes, StandardCharsets.UTF_8));
        WaystoneType type = getType(config.getString("type"));
        if (type == null) {
            return null;
        }
        WaystoneData data = new WaystoneData(
                UUID.fromString(Objects.requireNonNull(config.getString("uuid"))),
                type, getModelMap().getOrDefault(config.getString("model"), getDefaultModel()),
                World.Environment.valueOf(config.getString("environment", "NORMAL")));
        data.getStatistics().load(config.getConfigurationSection("statistics"));
        data.setName(config.getString("name"));
        data.setOwnerUUID(config.isString("ownerUUID") ? UUID.fromString(config.getString("ownerUUID")) : null);
        data.setOwnerName(config.getString("ownerName"));
        String serverName = config.getString("serverName");
        if (FancyWaystones.getPlugin().getServerName().equals(serverName)) {
            World world = Bukkit.getWorld(config.getString("world"));
            if (world == null) {
                return null;
            }
            data.setLocation(new LocalLocation(new Location(world, config.getInt("x"), config.getInt("y"), config.getInt("z"))));
        } else {
            data.setLocation(new ProxyLocation(serverName == null ? FancyWaystones.getPlugin().getServerName() : serverName, config.getString("world"),
                    config.getInt("x"), config.getInt("y"), config.getInt("z"), data.getEnvironment()));
        }
        loadMembers(config, data);
        loadedData.add(data);
        data.validateBlock();
        return data;
    }

    private void loadMembers(YamlConfiguration config, WaystoneData data) {
        if (config.isConfigurationSection("members")) {
            for (String key : config.getConfigurationSection("members").getKeys(false)) {
                try {
                    data.getMembers().add(new WaystoneMember(UUID.fromString(key), config.getString("members." + key)));
                } catch (Exception e) {
                    // invalid UUID?
                }
            }
        }
        if (config.isConfigurationSection("blacklist")) {
            for (String key : config.getConfigurationSection("blacklist").getKeys(false)) {
                try {
                    data.getBlacklist().add(new WaystoneMember(UUID.fromString(key), config.getString("members." + key)));
                } catch (Exception e) {
                    // invalid UUID?
                }
            }
        }
    }

    protected WaystoneData _loadWaystoneData(UUID target) throws IOException {
        checkIOThread();
        long time = System.currentTimeMillis();
        byte[] bytes = storage.readWaystoneData(target);
        if (bytes != null) {
            try {
                WaystoneData data = _loadWaystoneData(bytes);
                long elapsed = System.currentTimeMillis() - time;
                FancyWaystones.getPlugin().pushSRWSpeed(elapsed);
                return data;
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load data: "+target, t);
            }
        }
        return null;
    }

    private WaystoneData loadWaystoneDataFromString(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(string);
            UUID uuid;
            if (config.isString("uuid")) {
                try {
                    uuid = UUID.fromString(Objects.requireNonNull(config.getString("uuid")));
                } catch (Throwable t) {
                    uuid = UUID.randomUUID();
                }
            } else {
                uuid = UUID.randomUUID();
            }
            WaystoneModel model = getModelMap().getOrDefault(config.getString("model"), getDefaultModel());
            WaystoneData data = new WaystoneData(uuid, getType(config.getString("type")),
                    model, World.Environment.valueOf(config.getString("environment")));
            loadMembers(config, data);
            data.setName(getDefaultWaystoneName());
            loadedData.add(data);
            return data;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load waystone data", t);
        }
        return null;
    }
}
