package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import thito.fancywaystones.proxy.*;
import thito.fancywaystones.storage.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class FancyWaystonesCommand implements CommandExecutor, TabCompleter {
    enum BookType {
        DEATH("Death"), TELEPORTATION("Teleportation");

        String s;
        BookType(String s) {
            this.s = s;
        }

        public String getName() {
            return s;
        }
    }

    World.Environment[] ENVIRONMENTS = { World.Environment.NORMAL, World.Environment.THE_END, World.Environment.NETHER};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', Language.getLanguage().get("prefix"));
        try {
            if (sender.hasPermission("fancywaystones.admin")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        FancyWaystones.getPlugin().reloadConfig();
                        sender.sendMessage(prefix + "Configuration has been reloaded! Loaded waystone might not reloaded, server restart is required for efficient waystone data reload.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("introduce")) {
                        ProxyWaystone proxyWaystone = FancyWaystones.getPlugin().getProxyWaystone();
                        if (proxyWaystone == null) {
                            sender.sendMessage(prefix + "FancyWaystone is not on Proxy Mode!");
                            return true;
                        }
                        Player player = sender instanceof Player ? (Player) sender : Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
                        if (player == null) {
                            sender.sendMessage(prefix + "Must be at least 1 player online on the server!");
                            return true;
                        }
                        proxyWaystone.introduceServer(player);
                        sender.sendMessage(prefix + "Server introduced to the proxy server for Player: "+player.getName());
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("mysqltolocal")) {
                        if (args.length > 1 && args[1].equals("CONFIRM")) {
                            sender.sendMessage(prefix + "Moving data from MySQL to Local...");
                            FancyWaystones.getPlugin().submitIO(() -> {
                                try {
                                    MySQLWaystoneStorage storage = FancyWaystones.getPlugin().createMySQLStorage();
                                    FileWaystoneStorage target = FancyWaystones.getPlugin().createFileStorage();
                                    storage.migrateTo(target);
                                    storage.close();
                                    target.close();
                                    sender.sendMessage(prefix + "Data migration from mysql to local is done!");
                                } catch (SQLException e) {
                                    sender.sendMessage(prefix + "SQL connection error: "+e);
                                    e.printStackTrace();
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(prefix + "Please create a backup before moving data from mysql to local and then do /"+s+" mysqltolocal CONFIRM");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        sender.sendMessage(prefix + "Loaded waystones: " + WaystoneManager.getManager().getLoadedCount());
                        sender.sendMessage(prefix + "Database RW Speed: " +
                                Arrays.stream(FancyWaystones.getPlugin().getStorageReadWriteSpeed())
                                        .mapToObj(Long::toString)
                                        .collect(Collectors.joining(", ")));
                        ProxyWaystone pw = FancyWaystones.getPlugin().getProxyWaystone();
                        if (pw != null) {
                            if (sender instanceof Player) {
                                pw.showInfo((Player) sender);
                            }
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("localtomysql")) {
                        if (args.length > 1 && args[1].equals("CONFIRM")) {
                            sender.sendMessage(prefix + "Moving data from Local to MySQL...");
                            FancyWaystones.getPlugin().submitIO(() -> {
                                try {
                                    MySQLWaystoneStorage storage = FancyWaystones.getPlugin().createMySQLStorage();
                                    FileWaystoneStorage target = FancyWaystones.getPlugin().createFileStorage();
                                    target.migrateTo(storage);
                                    storage.close();
                                    target.close();
                                    sender.sendMessage(prefix + "Data migration from local to mysql is done!");
                                } catch (SQLException e) {
                                    sender.sendMessage(prefix + "SQL connection error: "+e);
                                    e.printStackTrace();
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(prefix + "Please create a backup before moving data from mysql to local and then do /"+s+" localtomysql CONFIRM");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("migrate")) {
                        if (args.length > 1) {
                            if (args[1].equals("CONFIRM")) {
                                sender.sendMessage(prefix + "Migrating OLD DATA... This might take a while! See console for more details");
                                FancyWaystones.getPlugin().submitIO(() -> {
                                    File playersDirectory = new File(FancyWaystones.getPlugin().getDataFolder(), "players");
                                    File waystonesDirectory = new File(FancyWaystones.getPlugin().getDataFolder(), "waystones");
                                    File serverWaystonesDirectory = new File(FancyWaystones.getPlugin().getDataFolder(), "server_waystones");
                                    File[] list = playersDirectory.listFiles();
                                    WaystoneStorage storage = WaystoneManager.getManager().getStorage();
                                    if (list != null) {
                                        for (File f : list) {
                                            if (f.getName().endsWith(".yml")) {
                                                try {
                                                    FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Migrating "+f);
                                                    byte[] data = Files.readAllBytes(f.toPath());
                                                    UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
                                                    storage.writePlayerData(uuid, data);
                                                } catch (Throwable t) {
                                                    t.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                    migrateWaystoneData(waystonesDirectory, storage);
                                    migrateWaystoneData(serverWaystonesDirectory, storage);
                                    FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Done migrating!");
                                    sender.sendMessage(prefix + "Data Migration done!");
                                });
                                return true;
                            }
                        }
                        sender.sendMessage(prefix + "Please create a backup of your old data and then do /"+s+" migrate CONFIRM");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("open")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(prefix + "You must be a player to do this!");
                            return true;
                        }
                        if (args.length > 1) {
                            if (args[1].equalsIgnoreCase("uuid")) {
                                if (args.length > 2) {
                                    try {
                                        UUID uuid = UUID.fromString(args[2]);
                                        FancyWaystones.getPlugin().submitIO(() -> {
                                            PlayerData data = WaystoneManager.getManager().getPlayerData((Player) sender, uuid);
                                            if (data == null) {
                                                sender.sendMessage(prefix + "Data with that ID could not be found!");
                                            } else {
                                                WaystoneManager.getManager().openWaystoneMenu(data, WaystoneManager.getManager().getDummy());
                                            }
                                        });
                                        return true;
                                    } catch (Throwable t) {
                                        sender.sendMessage(prefix + "Invalid UUID!");
                                        return true;
                                    }
                                }
                            } else {
                                if (args.length > 2) {
                                    Player target = Bukkit.getPlayerExact(args[2]);
                                    if (target == null) {
                                        sender.sendMessage(prefix + args[2]+" is not online!");
                                        return true;
                                    }
                                    FancyWaystones.getPlugin().submitIO(() -> {
                                        PlayerData data = WaystoneManager.getManager().getPlayerData((Player) sender, target.getUniqueId());
                                        if (data == null) {
                                            sender.sendMessage(prefix + "Data with that Player could not be found!");
                                        } else {
                                            WaystoneManager.getManager().openWaystoneMenu(data, WaystoneManager.getManager().getDummy());
                                        }
                                    });
                                    return true;
                                }
                            }
                        }
                        sender.sendMessage(prefix + "Open Waystone GUI as other player. Usage:");
                        sender.sendMessage(prefix + "/"+s+" open uuid <uuid>");
                        sender.sendMessage(prefix + "/"+s+" open name <name>");
                        return true;
                    }
//                    if (args[0].equalsIgnoreCase("cleanUpEntities")) {
//                        int count = 0;
//                        for (World world : Bukkit.getWorlds()) {
//                            for (Chunk chunk : world.getLoadedChunks()) {
//                                for (Entity en : chunk.getEntities()) {
//                                    if (StandardModel.UNIQUE_STRING.equals(en.getCustomName()) && !StandardModel.preventDeath(en)) {
//                                        en.remove();
//                                        count++;
//                                    }
//                                }
//                            }
//                        }
//                        sender.sendMessage(prefix + "Cleaned up "+count+" entities!");
//                        return true;
//                    }
                    if (args[0].equalsIgnoreCase("giveBook")) {
                        String attemptedPlayerLookup;
                        Player target = null;
                        BookType bookType = null;
                        int amount = 1;
                        if (sender instanceof Player) {
                            target = (Player) sender;
                        }
                        if (args.length > 1) {
                            Player lookup = player(attemptedPlayerLookup = args[1]);
                            if (lookup == null) {
                                bookType = Arrays.stream(BookType.values()).filter(x -> x.name().equalsIgnoreCase(args[1])).findAny().orElse(null);
                                if (args.length > 2) {
                                    try {
                                        amount = Integer.parseInt(args[2]);
                                    } catch (Throwable t) {
                                    }
                                }
                            } else {
                                target = lookup;
                                if (args.length > 2) {
                                    bookType = Arrays.stream(BookType.values()).filter(x -> x.name().equalsIgnoreCase(args[2])).findAny().orElse(null);
                                    if (args.length > 3) {
                                        try {
                                            amount = Integer.parseInt(args[3]);
                                        } catch (Throwable t) {
                                        }
                                    }
                                }
                            }
                            if (target == null) {
                                if (attemptedPlayerLookup == null) {
                                    sender.sendMessage(prefix + "You must specify the player!");
                                } else {
                                    sender.sendMessage(prefix + "Cannot find player "+attemptedPlayerLookup+"!");
                                }
                                return true;
                            }
                            if (bookType == null) {
                                sender.sendMessage(prefix + "Invalid Book type! Possible values: "+ Arrays.stream(BookType.values()).map(Enum::name).collect(Collectors.joining(", ")));
                                return true;
                            }
                            switch (bookType) {
                                case DEATH:
                                    Util.placeInHand(target, FancyWaystones.getPlugin().getDeathBook().createItem());
                                    break;
                                case TELEPORTATION:
                                    Util.placeInHand(target, FancyWaystones.getPlugin().getTeleportationBook().createEmptyItem());
                                    break;
                            }
                            if (target == sender) {
                                sender.sendMessage(prefix + "Added "+amount+"x "+bookType.getName()+" Book to your inventory!");
                            } else {
                                sender.sendMessage(prefix + "Given "+amount+"x "+bookType.getName()+" Book to "+target.getName()+"!");
                                target.sendMessage(prefix + sender.getName()+" gave you "+amount+"x "+bookType.getName()+" Book!");
                            }
                            return true;
                        }
                        sender.sendMessage(prefix + "Usage: /"+s+" "+args[0]+" [target] <DEATH|TELEPORTATION> [amount]");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("give")) {
                        String attemptedPlayerLookup = null;
                        Player target = null;
                        World.Environment environment = null;
                        WaystoneType type = null;
                        int amount = 1;
                        if (sender instanceof Player) {
                            target = (Player) sender;
                        }
                        Player lookup = null;
                        String model = null;
                        if (args.length > 1) {
                            lookup = player(attemptedPlayerLookup = args[1]);
                            if (lookup == null) {
                                environment = Arrays.stream(ENVIRONMENTS).filter(x -> x.name().equalsIgnoreCase(args[1])).findAny().orElse(null);
                                if (args.length > 2) {
                                    type = WaystoneManager.getManager().getTypes().stream().filter(x -> x.name().equalsIgnoreCase(args[2])).findAny().orElse(null);
                                    if (args.length > 3) {
                                        try {
                                            amount = Integer.parseInt(args[3]);
                                            if (args.length > 4) {
                                                model = args[4];
                                            }
                                        } catch (Throwable ignored) {
                                            model = args[3];
                                            try {
                                                if (args.length > 4) {
                                                    amount = Integer.parseInt(args[4]);
                                                }
                                            } catch (Throwable ignored1) {
                                            }
                                        }
                                    }
                                }
                            } else {
                                target = lookup;
                                if (args.length > 2) {
                                    environment = Arrays.stream(ENVIRONMENTS).filter(x -> x.name().equalsIgnoreCase(args[2])).findAny().orElse(null);
                                    if (args.length > 3) {
                                        type = WaystoneManager.getManager().getTypes().stream().filter(x -> x.name().equalsIgnoreCase(args[3])).findAny().orElse(null);
                                        if (args.length > 4) {
                                            try {
                                                amount = Integer.parseInt(args[4]);
                                                if (args.length > 5) {
                                                    model = args[5];
                                                }
                                            } catch (Throwable t) {
                                                model = args[4];
                                                if (args.length > 5) {
                                                    try {
                                                        amount = Integer.parseInt(args[5]);
                                                    } catch (Throwable ignored) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (environment == null && type == null) {
                            sender.sendMessage(prefix + "Usage: /"+s+" "+args[0]+" [target] <"+ Arrays.stream(World.Environment.values()).map(Enum::name).collect(Collectors.joining("|"))+"> <"+
                                    WaystoneManager.getManager().getTypes().stream().map(WaystoneType::name).collect(Collectors.joining("|"))+
                                    "> ["+String.join("|", WaystoneManager.getManager().getModelMap().keySet())+"] [amount]");
                            return true;
                        }
                        if (target == null) {
                            if (attemptedPlayerLookup == null) {
                                sender.sendMessage(prefix + "You must specify the player!");
                            } else {
                                sender.sendMessage(prefix + "Cannot find player "+attemptedPlayerLookup+"!");
                            }
                            return true;
                        }
                        if (environment == null) {
                            sender.sendMessage(prefix + "Invalid dimension! Possible values: "+ Arrays.stream(World.Environment.values()).map(Enum::name).collect(Collectors.joining(", ")));
                            return true;
                        }
                        if (type == null) {
                            sender.sendMessage(prefix + "Invalid waystone type! Possible values: "+WaystoneManager.getManager().getTypes().stream().map(x -> x.name()).collect(Collectors.joining(", ")));
                            return true;
                        }
                        Player finalTarget = target;
                        int finalAmount = amount;
                        WaystoneModel waystoneModel = WaystoneManager.getManager().getModelMap().getOrDefault(model, WaystoneManager.getManager().getDefaultModel());
                        WaystoneManager.getManager().createWaystoneItem(
                                WaystoneManager.getManager().createData(type, environment, waystoneModel), false,
                                item -> {
                                    Util.placeInHand(finalTarget, item);
                                    if (finalTarget == sender) {
                                        sender.sendMessage(prefix + "Added "+ finalAmount +"x waystone to your inventory!");
                                    } else {
                                        sender.sendMessage(prefix + "Given " + finalAmount + "x waystone to "+finalTarget.getName()+"!");
                                        finalTarget.sendMessage(prefix + sender.getName()+" gave you "+finalAmount+"x waystone!");
                                    }
                                }
                        );
                        return true;
                    }
                }
                sender.sendMessage(prefix + "FancyWaystones v" + FancyWaystones.getPlugin().getDescription().getVersion() + ". Usage: /" + s + " <reload|give|giveBook|open|migrate|localtomysql|mysqltolocal>");
                return true;
            }
            sender.sendMessage(prefix + "You don't have permission to do this!");
            return true;
        } catch (Throwable t) {
            sender.sendMessage(prefix + "An error occurred while executing this command: "+t);
            t.printStackTrace();
            return true;
        }
    }

    private void migrateWaystoneData(File waystonesDirectory, WaystoneStorage storage) {
        File[] list;
        list = waystonesDirectory.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.getName().endsWith(".yml")) {
                    try {
                        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Migrating "+f);
                        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                        configuration.set("serverName", FancyWaystones.getPlugin().getServerName());
                        WaystoneType type = WaystoneManager.getManager().getType(configuration.getString("type"));
                        if (type == null) continue;
                        storage.writeWaystoneData(type, UUID.fromString(f.getName().replace(".yml", "")), configuration.saveToString().getBytes(StandardCharsets.UTF_8));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> suggestions = new ArrayList<>();
        try {
            if (sender.hasPermission("fancywaystones.admin")) {
                if (args.length == 1) {
                    suggestions.add("introduce");
                    suggestions.add("reload");
                    suggestions.add("give");
                    suggestions.add("giveBook");
                    suggestions.add("open");
                    suggestions.add("migrate");
                    suggestions.add("mysqltolocal");
                    suggestions.add("localtomysql");
                    suggestions.add("info");
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("give")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getName());
                        }
                        for (World.Environment e : ENVIRONMENTS) {
                            suggestions.add(e.name().toLowerCase());
                        }
                    } else if (args[0].equalsIgnoreCase("giveBook")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getName());
                        }
                        for (BookType b : BookType.values()) {
                            suggestions.add(b.name().toLowerCase());
                        }
                    } else if (args[0].equalsIgnoreCase("open")) {
                        suggestions.add("uuid");
                        suggestions.add("player");
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("give")) {
                        if (player(args[1]) != null) {
                            for (World.Environment e : ENVIRONMENTS) {
                                suggestions.add(e.name());
                            }
                        } else {
                            for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
                                suggestions.add(type.name());
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("giveBook")) {
                        if (player(args[1]) != null) {
                            for (BookType b : BookType.values()) {
                                suggestions.add(b.name().toLowerCase());
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("open")) {
                        if (args[1].equalsIgnoreCase("uuid")) {
                            suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(x -> x.getUniqueId().toString()).limit(50).collect(Collectors.toList()));
                        } else if (args[1].equalsIgnoreCase("player")) {
                            suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).limit(50).collect(Collectors.toList()));
                        }
                    }
                } else if (args.length == 4) {
                    if (args[0].equalsIgnoreCase("give")) {
                        if (player(args[1]) != null) {
                            for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
                                suggestions.add(type.name());
                            }
                        } else {
                            suggestions.addAll(WaystoneManager.getManager().getModelMap().keySet());
                        }
                    }
                } else if (args.length == 5) {
                    if (args[0].equalsIgnoreCase("give")) {
                        if (player(args[1]) != null) {
                            suggestions.addAll(WaystoneManager.getManager().getModelMap().keySet());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (args.length > 0) {
            return StringUtil.copyPartialMatches(args[args.length - 1], suggestions, new ArrayList<>());
        }
        return suggestions;
    }

    public Player player(String name) {
        Player ignoreCase = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equals(name)) {
                return player;
            }
            if (player.getName().equalsIgnoreCase(name)) {
                ignoreCase = player;
            }
        }
        return ignoreCase;
    }
}
