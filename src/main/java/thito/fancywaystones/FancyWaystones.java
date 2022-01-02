package thito.fancywaystones;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mozilla.javascript.*;
import thito.fancywaystones.books.DeathBook;
import thito.fancywaystones.books.TeleportationBook;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.config.Section;
import thito.fancywaystones.economy.EconomyService;
import thito.fancywaystones.economy.ItemEconomyService;
import thito.fancywaystones.effect.Effect;
import thito.fancywaystones.hook.LateHookLoader;
import thito.fancywaystones.model.ConfigModel;
import thito.fancywaystones.model.ItemModel;
import thito.fancywaystones.proxy.ProxyWaystone;
import thito.fancywaystones.proxy.ProxyWaystoneListener;
import thito.fancywaystones.proxy.ServerIntroductionTask;
import thito.fancywaystones.recipes.LegacyRecipeManager;
import thito.fancywaystones.recipes.ModernRecipeManager;
import thito.fancywaystones.scheduler.*;
import thito.fancywaystones.storage.FileWaystoneStorage;
import thito.fancywaystones.storage.MySQLWaystoneStorage;
import thito.fancywaystones.storage.VoidWaystoneStorage;
import thito.fancywaystones.structure.StructureManager;
import thito.fancywaystones.structure.StructureWorldData;
import thito.fancywaystones.structure.WaystoneStructure;
import thito.fancywaystones.structure.WaystoneStructureListener;
import thito.fancywaystones.task.PostTeleportTask;
import thito.fancywaystones.task.WaystoneInactivityCheckTask;
import thito.fancywaystones.types.ConfigWaystoneType;
import thito.fancywaystones.ui.MenuListener;
import thito.fancywaystones.ui.MinecraftItem;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FancyWaystones extends JavaPlugin {

    private static FancyWaystones instance;

    public static FancyWaystones getPlugin() {
        return instance;
    }

    private final long[] storageReadWriteSpeed = new long[5];
//    private Context context;
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
//    private Scriptable root;
    private Scheduler service;
    private Scheduler IOService;
    private Scheduler structureService;

//    private Thread serviceThread, IOServiceThread;

    private ServerIntroductionTask introductionTask;
    private WaystoneInactivityCheckTask checkTask;
    private ProxyWaystone proxyWaystone;
    private ServerUUID serverUUID;
    private boolean success = false;
    private boolean informStructureGeneration;
    private long threadPoolTime;
    private long oldThreadPoolTime;
    private long IOThreadPoolTime;
    private long oldIOThreadPoolTime;
    private long structureThreadPoolTime;
    private long oldStructureThreadPoolTime;
//    private final long[] serviceThreadPoolTime = new long[20];
//    private final long[] IOServiceThreadPoolTime = new long[20];

    public long[] getStorageReadWriteSpeed() {
        return storageReadWriteSpeed;
    }

    public void pushSRWSpeed(long speed) {
        if (storageReadWriteSpeed.length - 1 >= 0)
            System.arraycopy(storageReadWriteSpeed, 0, storageReadWriteSpeed, 1, storageReadWriteSpeed.length - 1);
        storageReadWriteSpeed[0] = speed;
    }

    void pushThreadPoolTime() {
        oldThreadPoolTime = threadPoolTime;
        threadPoolTime = System.currentTimeMillis();
    }

    void pushIOThreadPoolTime() {
        oldIOThreadPoolTime = IOThreadPoolTime;
        IOThreadPoolTime = System.currentTimeMillis();
    }

    void pushStructureThreadPoolTime() {
        oldStructureThreadPoolTime = structureThreadPoolTime;
        structureThreadPoolTime = System.currentTimeMillis();
    }

//    void push(long[] array, long time) {
//        if (array.length - 1 >= 0) System.arraycopy(array, 0, array, 1, array.length - 1);
//        array[0] = time;
//    }

    public long getServiceBusyness() {
        return threadPoolTime - oldThreadPoolTime - 1000;
    }

    public long getStructureServiceBusyness() {
        return structureThreadPoolTime - oldStructureThreadPoolTime - 1000;
    }

    public long getIOServiceBusyness() {
        return IOThreadPoolTime - oldIOThreadPoolTime - 1000;
    }

//    long getBusiness(long[] array) {
//        long business = 0;
//        long lastTime = -1;
//        for (int i = array.length - 1; i > 0; i--) {
//            if (lastTime != -1) {
//                if (array[i] == 0 || array[i - 1] == 0) continue;
//                long b = array[i - 1] - array[i];
//                business += Math.max(business, b);
//            }
//            lastTime = array[i];
//        }
//        return business;
//    }

    @Override
    public void onLoad() {

        // we have it first! >:(
//        service = new ScheduledThreadPoolExecutor(1, runnable -> serviceThread = new Thread(runnable, "FW"));
//        IOService = new ScheduledThreadPoolExecutor(1, runnable -> IOServiceThread = new Thread(runnable, "FWIO"));

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

        PluginManager pluginManager = getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("ProtocolLib")) {
            getLogger().log(Level.SEVERE, "This plugin requires ProtocolLib to be installed and enabled on your server!");
            pluginManager.disablePlugin(this);
            return;
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPISupport.enableSupport = true;
            getLogger().log(Level.INFO, "Enabled support for PlaceholderAPI");
        }

        StructureWorldData structureWorldData = new StructureWorldData();
        StructureManager.getInstance().setStructureWorldData(structureWorldData);

        try {
            JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(Context.class);
            getLogger().log(Level.INFO, "RhinoJS Library provided by "+providingPlugin.getName());
        } catch (Throwable t) {
            getLogger().log(Level.INFO, "RhinoJS Library provided by "+Context.class.getClassLoader());
        }

        File demoBin = new File(getDataFolder(), "structures/demo.bin");
        if (!demoBin.exists()) {
            saveResource("structures/demo.bin", true);
        }

        guiYml = new Configuration("gui.yml", false);
        messagesYml = new Configuration("messages.yml", true);
        recipesYml = new Configuration("recipes.yml", false);
        booksYml = new Configuration("books.yml", false);
        effectsYml = new Configuration("effects.yml", false);
        modelsYml = new Configuration("models.yml", false);
        waystonesYml = new Configuration("waystones.yml", false);
        new WaystoneManager(this);

        introductionTask = new ServerIntroductionTask();

        success = true;

        checkTask = new WaystoneInactivityCheckTask();

        try {
            Field field = VMBridge.class.getDeclaredField("instance");
            field.setAccessible(true);
            Object instance = field.get(null);
            getLogger().log(Level.INFO, "Uses VMBridge "+instance);
            if (!(instance instanceof VMBridge_custom)) {
                getLogger().log(Level.SEVERE, "The server uses outdated RhinoJS");
            }
        } catch (Throwable ignored) {
        }

        // also initializes the registries
        try {
            WrappedDataWatcher.Registry.getVectorSerializer();
        } catch (Throwable ignored) {
        }

        try {
            EnumWrappers.getChatTypeClass();
        } catch (Throwable ignored) {
        }

        pluginManager.registerEvents(new WaystoneListener(), this);
        try {
            pluginManager.registerEvents(new WaystoneModernListener(), this);
            getLogger().log(Level.INFO, "Using latest version compability listener");
        } catch (Throwable ignored) {
        }
        pluginManager.registerEvents(new MenuListener(), this);
        pluginManager.registerEvents(new WaystoneStructureListener(), this);
        LateHookLoader lateHookLoader = new LateHookLoader();
        lateHookLoader.checkStatus();
        pluginManager.registerEvents(lateHookLoader, this);
        if (XMaterial.isNewVersion()) {
            pluginManager.registerEvents(new ModernWaystoneListener(), this);
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

    public ServerIntroductionTask getIntroductionTask() {
        return introductionTask;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public String getServerName() {
        return getServerUUID().getId().toString();
    }

    public Scheduler getIOService() {
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

    public Scheduler getService() {
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

//    public Script compile(String script) {
//        return context.compileString(script, "effects.yml", 0, null);
//    }
//
//    public Scriptable getRoot() {
//        return root;
//    }
//
//    public Context getContext() {
//        return context;
//    }

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

    @Override
    public void onDisable() {
        if (success) {
            checkTask.stop();
            structureService.lock();
            try {
                StructureManager.getInstance().getStructureWorldData().close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to save generated_structures.bin", e);
            }
            structureService.unlock();
            HandlerList.unregisterAll(this);
            IOService.lock();
            for (World world : Bukkit.getWorlds()) {
                getLogger().log(Level.INFO, "Saving chunks for "+world.getName());
                for (Chunk chunk : world.getLoadedChunks()) {
                    try {
                        WaystoneManager.getManager().unloadChunk(chunk);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            WaystoneManager.getManager().shutdown();
            IOService.unlock();
            if (recipeManager != null) {
                recipeManager.clearCustomRecipes();
            }
            shutdownTasks();
            getServer().getScheduler().cancelTasks(this);
        }

    }

    private void shutdownTasks() {
        if (structureService != null) {
            getLogger().log(Level.INFO, "Shutting down Structure Service tasks...");
            structureService.shutdown();
        }
        if (service != null) {
            getLogger().log(Level.INFO, "Shutting down Service tasks...");
            service.shutdown();
            try {
                service.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        if (IOService != null) {
            getLogger().log(Level.INFO, "Shutting down IO tasks...");
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

    private void checkConfig(String name) {
        File file = new File(getDataFolder(), name + ".yml");
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(getResource(name + ".yml"))) {
                Section section = Section.parseToMap(reader);
                section.getString("Configuration Version").ifPresent(version -> {
                    try (FileReader fileReader = new FileReader(file)) {
                        Section section1 = Section.parseToMap(fileReader);
                        String configuration_version = section1.getString("Configuration Version").orElse(null);
                        if (configuration_version == null) configuration_version = "Old";
                        if (configuration_version.equals(version)) {
                            return;
                        }
                        getLogger().log(Level.WARNING, "Old configuration detected \""+name+".yml\", generating a new one...");
                        fileReader.close();
                        File dest = new File(getDataFolder(), name + "-" + configuration_version + ".yml");
                        if (file.renameTo(dest)) {
                            saveResource(name+".yml", true);
                        } else {
                            getLogger().log(Level.SEVERE, "Failed to rename file "+file+" to "+dest);
                        }
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Failed to validate \""+name+".yml\"!", e);
                    }
                });
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to validate \""+name+".yml\"!", e);
            }
        } else {
            getLogger().log(Level.INFO, "Can't find \""+name+".yml\", generating a new one...");
            saveResource(name + ".yml", false);
        }
    }

    private Scheduler createScheduler(String name, String threadName) {
        switch (name) {
            case "BukkitScheduler": return new BukkitScheduler(threadName);
            case "AsyncBukkitScheduler": return new AsyncBukkitScheduler();
            case "SyncBukkitScheduler": return new SyncBukkitScheduler();
            default: return new NativeScheduler(threadName);
        }
    }

    @Override
    public void reloadConfig() {
        if (structureService != null) {
            structureService.submit(() -> {
                try {
                    StructureManager.getInstance().getStructureWorldData().close();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Failed to save generated_structures.bin", e);
                }
            });
        }
        shutdownTasks();
        if (recipeManager != null) {
            recipeManager.clearCustomRecipes();
        }
        StructureManager.getInstance().setEnable(false);
        proxyWaystone = null;
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        checkConfig("books");
        checkConfig("config");
        checkConfig("effects");
        checkConfig("gui");
        checkConfig("messages");
        checkConfig("models");
        checkConfig("recipes");
        checkConfig("structures");
        checkConfig("waystones");

        getDataFolder().mkdirs();
        saveDefaultConfig();

//        WaystoneStorage storage = WaystoneManager.getManager().getStorage();
//        if (storage != null) storage.close();
        super.reloadConfig();

        service = createScheduler(getConfig().getString("Service Scheduler"), "FW");
        IOService = createScheduler(getConfig().getString("IO Service Scheduler"), "FWIO");
        structureService = createScheduler(getConfig().getString("Structure Service Scheduler"), "FWS");

        service.submit(() -> {
            pushThreadPoolTime();
            Debug.debug("Service Thread is active "+ getServiceBusyness()+"ms");
        }, 0, 20);

        IOService.submit(() -> {
            pushIOThreadPoolTime();
            Debug.debug("IO Service Thread is active "+ getIOServiceBusyness()+"ms");
        }, 0, 20);

        structureService.submit(() -> {
            pushStructureThreadPoolTime();
            Debug.debug("Structure Service Thread is active "+ getStructureServiceBusyness()+"ms");
        }, 0, 20);

        structureService.submit(() -> {
            try {
                getDataFolder().mkdirs();
                StructureManager.getInstance().getStructureWorldData().open(new File(getDataFolder(), "generated_structures.bin"));
                getLogger().log(Level.INFO, "File channel open for generated_structures.bin");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to open file channel for generated_structures.bin", e);
            }
        });

//        service.submit(() -> {
//            context = Context.enter();
//            root = context.initSafeStandardObjects();
//        });

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

        for (String key : waystonesYml.getConfig().getConfigurationSection("Waystone Types").getKeys(false)) {
            ConfigurationSection section = waystonesYml.getConfig().getConfigurationSection("Waystone Types."+key);
            if (section != null) {
                ConfigWaystoneType type = new ConfigWaystoneType(section);
                WaystoneManager.getManager().registerWaystoneType(type);
                getLogger().log(Level.INFO, "Registered waystone type "+type.name());
            }
        }

        WaystoneManager.getManager().getItemModelSet().clear();

        for (Map<?, ?> map : modelsYml.getConfig().getMapList("item-model")) {
            ItemModel itemModel = new ItemModel(map);
            WaystoneManager.getManager().getItemModelSet().add(itemModel);
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

        boolean modernRecipe = false;
        try {
            Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            ShapedRecipe.class.getConstructor(namespacedKeyClass, ItemStack.class);
            modernRecipe = true;
        } catch (Throwable ignored) {
        }
        if (modernRecipe) {
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

            getServer().getScheduler().runTask(this, () -> {
                // Structure loading are done in Bukkit's thread as the registry list for the structure are accessed in Bukkit's Thread
                StructureManager structureManager = StructureManager.getInstance();
                structureManager.clearWaystoneStructures();
                structureManager.clearStructures();
                File[] list = new File(getDataFolder(), getConfig().getString("Storage.Structure Directory")).listFiles();
                if (list != null) {
                    for (File f : list) {
                        String name = f.getName();
                        if (name.endsWith(".bin")) {
                            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                                structureManager.loadFromInputStream(name.substring(0, name.length() - 4), fileInputStream);
                                getLogger().log(Level.INFO, "Loaded structure: "+name+" ("+f+")");
                            } catch (Throwable t) {
                                getLogger().log(Level.SEVERE, "Failed to load structure "+ name, t);
                            }
                        }
                    }
                }
                File structureYml = new File(getDataFolder(), "structures.yml");
                if (!structureYml.exists()) saveResource("structures.yml", true);
                try (FileReader fileReader = new FileReader(structureYml)) {
                    Section section = Section.parseToMap(fileReader);
                    boolean enable_structure_generation = section.getBoolean("Enable Structure Generation").orElse(false);
                    informStructureGeneration = section.getBoolean("Inform Generation").orElse(true);
                    structureManager.setEnable(enable_structure_generation);
                    if (enable_structure_generation) {
                        section.getList("Structures").ifPresent(structure -> {
                            structure.stream().filter(x -> x instanceof MapSection).forEach(element -> {
                                WaystoneStructure waystoneStructure = structureManager.loadWaystoneStructure((MapSection) element);
                                if (waystoneStructure != null) {
                                    getLogger().log(Level.INFO, "Added structure: "+waystoneStructure+" ("+structure.indexOf(element)+")");
                                    structureManager.addWaystoneStructure(waystoneStructure);
                                }
                            });
                        });
                    }
                } catch (Throwable t) {
                    getLogger().log(Level.SEVERE, "Failed to load structures.yml", t);
                }
            });
        });
    }

    public boolean isInformStructureGeneration() {
        return informStructureGeneration;
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
        config.setMaxLifetime(config.getConnectionTimeout());
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

    public Scheduler getStructureService() {
        return structureService;
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
