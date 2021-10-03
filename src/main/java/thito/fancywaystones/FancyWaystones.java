package thito.fancywaystones;

import com.comphenix.protocol.wrappers.*;
import com.zaxxer.hikari.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.*;
import org.mozilla.javascript.*;
import thito.fancywaystones.books.*;
import thito.fancywaystones.economy.*;
import thito.fancywaystones.effect.Effect;
import thito.fancywaystones.model.*;
import thito.fancywaystones.proxy.*;
import thito.fancywaystones.recipes.*;
import thito.fancywaystones.storage.*;
import thito.fancywaystones.task.*;
import thito.fancywaystones.types.*;
import thito.fancywaystones.ui.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;

public class FancyWaystones extends JavaPlugin {

    private static FancyWaystones instance;

    public static FancyWaystones getPlugin() {
        return instance;
    }

    private long[] storageReadWriteSpeed = new long[5];
    private Context context;
    private Configuration guiYml;
    private Configuration messagesYml;
    private Configuration recipesYml;
    private Configuration booksYml;
    private Configuration effectsYml;
    private Configuration modelsYml;
    private Configuration waystonesYml;
    private Language language;
    private RecipeManager recipeManager;
    private DeathBook deathBook;
    private TeleportationBook teleportationBook;
    private Scriptable root;
    private ScheduledThreadPoolExecutor service;
    private ScheduledThreadPoolExecutor IOService;

    private Thread serviceThread, IOServiceThread;

    private WaystoneInactivityCheckTask checkTask;
    private ProxyWaystone proxyWaystone;
    private ServerUUID serverUUID;
    private boolean success = false;

    public long[] getStorageReadWriteSpeed() {
        return storageReadWriteSpeed;
    }

    public void pushSRWSpeed(long speed) {
        for (int i = storageReadWriteSpeed.length - 1; i > 0; i--) {
            storageReadWriteSpeed[i] = storageReadWriteSpeed[i - 1];
        }
        storageReadWriteSpeed[0] = speed;
    }

    @Override
    public void onLoad() {
        try {
            String serverVersion = Bukkit.getVersion();
            String apiVersion = "1.13";
            String[] versions = { "1.15", "1.16", "1.17", "1.18", "1.19" };
            for (String v : versions) {
                if (serverVersion.contains(v)) {
                    apiVersion = v;
                    break;
                }
            }
            Field field = PluginDescriptionFile.class.getDeclaredField("apiVersion");
            field.setAccessible(true);
            field.set(getDescription(), apiVersion);
            getLogger().log(Level.INFO, "Plugin API version has been changed to "+getDescription().getAPIVersion());
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        guiYml = new Configuration("gui.yml", false);
        messagesYml = new Configuration("messages.yml", true);
        recipesYml = new Configuration("recipes.yml", false);
        booksYml = new Configuration("books.yml", false);
        effectsYml = new Configuration("effects.yml", false);
        modelsYml = new Configuration("models.yml", false);
        waystonesYml = new Configuration("waystones.yml", false);
        new WaystoneManager(this);

        if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().log(Level.SEVERE, "This plugin requires ProtocolLib to be installed and enabled on your server!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        success = true;

        checkTask = new WaystoneInactivityCheckTask();

        service = new ScheduledThreadPoolExecutor(1, runnable -> serviceThread = new Thread(runnable, "FW"));
        IOService = new ScheduledThreadPoolExecutor(1, runnable -> IOServiceThread = new Thread(runnable, "FWIO"));

        service.submit(() -> {
            context = Context.enter();
            root = context.initSafeStandardObjects();
        });

        // also initializes the registries
        try {
            WrappedDataWatcher.Registry.getVectorSerializer();
        } catch (Throwable t) {
        }

        try {
            EnumWrappers.getChatTypeClass();
        } catch (Throwable t) {
        }

        getServer().getPluginManager().registerEvents(new WaystoneListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInput(), this);
        if (XMaterial.isNewVersion()) {
            getServer().getPluginManager().registerEvents(new ModernWaystoneListener(), this);
        }
        deathBook = new DeathBook();
        teleportationBook = new TeleportationBook();

        reloadConfig();

        FancyWaystonesCommand command = new FancyWaystonesCommand();
        PluginCommand cmd = getCommand("fancywaystones");
        cmd.setTabCompleter(command);
        cmd.setExecutor(command);

//        submitIO(() -> {
//            for (World world : Bukkit.getWorlds()) {
//                for (Chunk chunk : world.getLoadedChunks()) {
//                    try {
//                        WaystoneManager.getManager().loadChunk(chunk);
//                    } catch (Throwable t) {
//                        getLogger().log(Level.SEVERE, "Failed to load chunk data X:"+chunk.getX()+" Z:"+chunk.getZ()+" W:"+world.getName(), t);
//                    }
//                }
//            }
//        });

        checkTask.start();
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public String getServerName() {
        return getServerUUID().getId().toString();
    }

    public ScheduledExecutorService getIOService() {
        return IOService;
    }

    public void submitIO(Runnable task) {
        IOService.submit(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                getLogger().log(Level.SEVERE, "Failed to run IO task", t);
            }
        });
    }

    public ScheduledExecutorService getService() {
        return service;
    }

    public void submit(Runnable task) {
        service.submit(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                getLogger().log(Level.SEVERE, "Failed to run a task", t);
            }
        });
    }

    public Script compile(String script) {
        return context.compileString(script, "effects.yml", 0, null);
    }

    public Scriptable getRoot() {
        return root;
    }

    public Context getContext() {
        return context;
    }

    private Map<String, List<Effect>> compiledEffects = new HashMap<>();

    public PostTeleportTask postTeleport(String type, Player player, WaystoneData source, WaystoneData target) {
        if (effectsYml.getConfig().getBoolean("Post Teleport "+type+".Enable")) {
            PostTeleportTask task = new PostTeleportTask(compiledEffects.getOrDefault("Post Teleport "+type, Collections.emptyList()),
                    (int) Util.parseTime(effectsYml.getConfig().getString("Post Teleport "+type+".Time")),
                    player,
                    source,
                    target,
                    effectsYml.getConfig().getString("Post Teleport "+type+".Overlay.Title"),
                    effectsYml.getConfig().getString("Post Teleport "+type+".Overlay.Subtitle"));
            if (type.equals("Death Book")) {
                task.getPlaceholder().put("waystone_name", ph -> "{language.death-location}");
            }
            task.schedule(service, 1L, 1L);
            return task;
        }
        return null;
    }

    public DeathBook getDeathBook() {
        return deathBook;
    }

    public TeleportationBook getTeleportationBook() {
        return teleportationBook;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public long getNoDamageTime() {
        return Util.parseTime(getConfig().getString("Safe Teleport.No Damage Time"));
    }

    public static void checkIOThread() {
        if (Thread.currentThread() != getPlugin().IOServiceThread)
            throw new IllegalStateException("must be on IO thread");
    }

    public static void checkThread() {
        if (Thread.currentThread() != getPlugin().serviceThread)
            throw new IllegalStateException("must be on FW thread");
    }

    @Override
    public void onDisable() {
        checkTask.stop();
        if (success) {
            HandlerList.unregisterAll(this);
            for (World world : Bukkit.getWorlds()) {
                getLogger().log(Level.INFO, "Saving chunks for "+world.getName());
                for (Chunk chunk : world.getLoadedChunks()) {
                    submitIO(() -> {
                        try {
                            WaystoneManager.getManager().unloadChunk(chunk);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            submitIO(() -> {
                WaystoneManager.getManager().shutdown();
            });
            recipeManager.clearCustomRecipes();
            getServer().getScheduler().cancelTasks(this);
        }
        getLogger().log(Level.INFO, "Shutting down tasks...");
        if (service != null) {
            getLogger().log(Level.INFO, "Shutting down "+ service.getActiveCount()+" tasks...");
            service.shutdown();
            try {
                service.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        if (IOService != null) {
            getLogger().log(Level.INFO, "Shutting down "+ IOService.getActiveCount()+" IO tasks...");
            IOService.shutdown();
            try {
                if (!IOService.awaitTermination(30, TimeUnit.SECONDS)) {
                    getLogger().log(Level.SEVERE, "IO Service has been terminated too early! There is still a lot of tasks remaining!");
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public Configuration getModelsYml() {
        return modelsYml;
    }

    public Configuration getGuiYml() {
        return guiYml;
    }

    public Configuration getBooksYml() {
        return booksYml;
    }

    public Configuration getRecipesYml() {
        return recipesYml;
    }

    public Configuration getEffectsYml() {
        return effectsYml;
    }

    @Override
    public void reloadConfig() {
        if (recipeManager != null) {
            recipeManager.clearCustomRecipes();
        }

        proxyWaystone = null;
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        getDataFolder().mkdirs();
        saveDefaultConfig();

//        WaystoneStorage storage = WaystoneManager.getManager().getStorage();
//        if (storage != null) storage.close();

        super.reloadConfig();

        messagesYml.reload();
        guiYml.reload();
        recipesYml.reload();
        booksYml.reload();
        effectsYml.reload();
        modelsYml.reload();
        waystonesYml.reload();

        serverUUID = new ServerUUID();
        try {
            serverUUID.loadOrGenerate();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load server UUID", e);
        }

        if (getConfig().getBoolean("Proxy Mode")) {
            getLogger().log(Level.INFO, "Proxy Mode is enabled!");
            getServer().getMessenger().registerIncomingPluginChannel(this, "fancywaystones:waystone", new ProxyWaystoneListener());
            getServer().getMessenger().registerOutgoingPluginChannel(this, "fancywaystones:waystone");
            proxyWaystone = new ProxyWaystone();
            for (Player p : Bukkit.getOnlinePlayers()) {
                proxyWaystone.introduceServer(p);
            }
        }

        WaystoneManager.getManager().getModelMap().clear();

        for (String key : waystonesYml.getConfig().getConfigurationSection("Waystone Types").getKeys(false)) {
            ConfigurationSection section = waystonesYml.getConfig().getConfigurationSection("Waystone Types."+key);
            if (section != null) {
                ConfigWaystoneType type = new ConfigWaystoneType(section);
                WaystoneManager.getManager().registerWaystoneType(type);
                getLogger().log(Level.INFO, "Registered waystone type "+type.name());
            }
        }

        for (String key : Objects.requireNonNull(modelsYml.getConfig().getConfigurationSection("model")).getKeys(false)) {
            ConfigModel configModel = new ConfigModel(key, Objects.requireNonNull(modelsYml.getConfig().getConfigurationSection("model." + key)));
            WaystoneManager.getManager().registerWaystoneModel(configModel);
        }

        service.submit(() -> {
            for (String string : new String[] {"Waystone", "Teleportation Book", "Death Book"}) {
                compiledEffects.put("Post Teleport "+string, Effect.deserializeEffects(effectsYml.getConfig().getConfigurationSection("Post Teleport "+string+".Effects")));
                compiledEffects.put("Warm Up "+string, Effect.deserializeEffects(effectsYml.getConfig().getConfigurationSection("Warm Up "+string+".Effects")));
            }
        });

        language = new Language(messagesYml.getConfig());

        WaystoneManager.getManager().loadWaystoneItem(getConfig().getConfigurationSection("Waystone Item"));

        Set<EconomyService> services = WaystoneManager.getManager().getEconomyServices();
        services.removeIf(x -> x instanceof ItemEconomyService);
        if (getConfig().getBoolean("Economy.Item.Enable")) {
            ConfigurationSection section = getConfig().getConfigurationSection("Economy.Item.Items");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    MinecraftItem minecraftItem = new MinecraftItem();
                    minecraftItem.load(section.getConfigurationSection(key));
                    ItemStack itemStack = minecraftItem.getItemStack(new Placeholder());
                    ItemEconomyService itemEconomyService = new ItemEconomyService(key, itemStack, getConfig().getString("Economy.Item.Currency Format"));
                    getLogger().log(Level.INFO, "Added Item Economy with id "+key+": "+itemStack);
                    services.add(itemEconomyService);
                }
            }
        }


        if (XMaterial.isNewVersion()) {
            getLogger().log(Level.INFO, "Preparing recipe manager: "+ModernRecipeManager.class);
            recipeManager = new ModernRecipeManager(this);
        } else {
            getLogger().log(Level.INFO, "Preparing recipe manager: "+LegacyRecipeManager.class);
            recipeManager = new LegacyRecipeManager(this);
        }
        recipeManager.registerCustomRecipes();
        submitIO(() -> {
            String storageLoc = getConfig().getString("Storage.Location");
            if (storageLoc.equals("FILE")) {
                WaystoneManager.getManager().setStorage(createFileStorage());
                getLogger().log(Level.INFO, "Uses File Waystone Storage");
            } else if (storageLoc.equals("MYSQL")) {
                try {
                    MySQLWaystoneStorage mysqlStorage = createMySQLStorage();
                    WaystoneManager.getManager().setStorage(mysqlStorage);
                    getLogger().log(Level.INFO, "Uses MySQL Waystone Storage");
                } catch (SQLException t) {
                    getLogger().log(Level.SEVERE, "SQL connection error: ", t);
                    t.printStackTrace();
                    getLogger().log(Level.SEVERE, "Using VoidWaystoneStorage");
                    WaystoneManager.getManager().setStorage(new VoidWaystoneStorage());
                }
            } else {
                getLogger().log(Level.SEVERE, "Unknown Waystone Storage: "+storageLoc);
                WaystoneManager.getManager().setStorage(new VoidWaystoneStorage());
            }
        });
    }

    public MySQLWaystoneStorage createMySQLStorage() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        if (getConfig().isString("Storage.MySQL.Url")) {
            config.setJdbcUrl(getConfig().getString("Storage.MySQL.Url"));
        } else {
            config.setJdbcUrl("jdbc:mysql://" + getConfig().getString("Storage.MySQL.Host") + ":" + getConfig().getInt("Storage.MySQL.Port") + "/" +
                    getConfig().getString("Storage.MySQL.Database Name")+"?autoreconnect=true");
        }
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPassword(getConfig().getString("Storage.MySQL.Password"));
        config.setUsername(getConfig().getString("Storage.MySQL.Username"));
        HikariDataSource dataSource = new HikariDataSource(config);
        MySQLWaystoneStorage mySQLWaystoneStorage = new MySQLWaystoneStorage(
                dataSource,
                getConfig().getString("Storage.MySQL.Table.Player"),
                getConfig().getString("Storage.MySQL.Table.Waystone"),
                getConfig().getString("Storage.MySQL.Table.Names")
        );
        mySQLWaystoneStorage.connect();
        return mySQLWaystoneStorage;
    }

    public FileWaystoneStorage createFileStorage() {
        return new FileWaystoneStorage(
                new File(getDataFolder(), getConfig().getString("Storage.File.Waystone Directory")),
                new File(getDataFolder(), getConfig().getString("Storage.File.Player Directory")),
                new File(getDataFolder(), getConfig().getString("Storage.File.Names Directory"))
        );
    }

    public Language getLanguage() {
        return language;
    }

    public ProxyWaystone getProxyWaystone() {
        return proxyWaystone;
    }

    public byte[] readResource(String name) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = getResource(name)) {
            int len; byte[] buff = new byte[1024 * 8];
            while ((len = inputStream.read(buff, 0, buff.length)) != -1) {
                outputStream.write(buff, 0, len);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return outputStream.toByteArray();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6FW&8] &cThe plugin was not initialized properly! Please check your console and report the bug to the author at the resource discussion tab!"));
        return true;
    }

    public int getCheckRadius() {
        return getConfig().getInt("Safe Teleport.Check Radius");
    }

    public int getCheckHeight() {
        return getConfig().getInt("Safe Teleport.Check Height");
    }

    public long getNoDamageTicks() {
        return Util.parseTime(getConfig().getString("Safe Teleport.No Damage Time"));
    }

    public boolean isForceTeleportation() {
        return getConfig().getBoolean("Safe Teleport.Force If Fail");
    }
}
