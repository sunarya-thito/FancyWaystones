package thito.fancywaystones;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

public class Util {
    private static double[] cached_cos = new double[360];
    private static double[] cached_sin = new double[360];

    static {
        for (int i = 0; i < 360; i++) {
            cached_sin[i] = Math.sin(Math.toRadians(i));
            cached_cos[i] = Math.cos(Math.toRadians(i));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        time(player, fadeIn, stay, fadeOut);
        if (title != null) title(player, title);
        if (subtitle != null) subtitle(player, subtitle);
    }

    public static void time(Player player, int fadeIn, int stay, int fadeOut) {
        if (PacketType.Play.Server.TITLE.isSupported()) {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE);
            packet.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);
            packet.getIntegers()
                    .write(0, fadeIn)
                    .write(1, stay)
                    .write(2, fadeOut);
            packet(player, packet);
        } else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION);
            packet.getIntegers()
                    .write(0, fadeIn)
                    .write(1, stay)
                    .write(2, fadeOut);
            packet(player, packet);
        }
    }

    public static void title(Player player, String text) {
        if (PacketType.Play.Server.TITLE.isSupported()) {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE);
            packet.getTitleActions().write(0, EnumWrappers.TitleAction.TITLE);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
            packet(player, packet);
        } else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLE_TEXT);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
            packet(player, packet);
        }
    }

    public static void subtitle(Player player, String text) {
        if (PacketType.Play.Server.TITLE.isSupported()) {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE);
            packet.getTitleActions().write(0, EnumWrappers.TitleAction.SUBTITLE);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
            packet(player, packet);
        } else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SUBTITLE_TEXT);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
            packet(player, packet);
        }
    }

    public static void packet(Player player, PacketContainer packet) {
        if (player.isOnline()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (IllegalArgumentException ignored) {
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static Vector rotateAroundAxisX(Vector v, int angle) {
        double y, z, cos, sin;
        cos = cos(angle);
        sin = sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static Vector rotateAroundAxisY(Vector v, int angle) {
        angle = -angle;
        double x, z, cos, sin;
        cos = cos(angle);
        sin = sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static Vector rotateAroundAxisZ(Vector v, int angle) {
        double x, y, cos, sin;
        cos = cos(angle);
        sin = sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    public static Location rotate(Location pivot, Location loc, int x, int y, int z) {
        Vector diff = loc.clone().subtract(pivot).toVector();
        diff = rotateAroundAxisX(diff, x);
        diff = rotateAroundAxisY(diff, y);
        diff = rotateAroundAxisZ(diff, z);
        return pivot.clone().add(diff);
    }

    public static void placeInHand(Player player, ItemStack itemStack) {
        ItemStack inHand = player.getItemInHand();
        if (inHand.isSimilar(itemStack)) {
            inHand.setAmount(inHand.getAmount() + itemStack.getAmount());
            player.setItemInHand(inHand);
        } else {
            if (inHand.getType() == XMaterial.AIR.parseMaterial()) {
                player.setItemInHand(itemStack);
            } else {
                try {
                    inHand = player.getInventory().getItemInOffHand();
                    if (inHand.isSimilar(itemStack)) {
                        inHand.setAmount(inHand.getAmount() + itemStack.getAmount());
                        player.getInventory().setItemInOffHand(inHand);
                        return;
                    } else {
                        if (inHand.getType() == XMaterial.AIR.parseMaterial()) {
                            player.getInventory().setItemInOffHand(itemStack);
                            return;
                        }
                    }
                } catch (Throwable ignored) {
                }
                player.getInventory().addItem(itemStack);
            }
        }
    }

    public static void removeItemInHand(Player player, ItemStack itemStack) {
        ItemStack inHand = player.getItemInHand().clone();
        if (inHand != null && itemStack.isSimilar(inHand)) {
            if (inHand.getAmount() < itemStack.getAmount()) {
                player.setItemInHand(XMaterial.AIR.parseItem());
                itemStack = itemStack.clone();
                itemStack.setAmount(itemStack.getAmount() - inHand.getAmount());
                removeItemInHand(player, itemStack);
            } else {
                inHand.setAmount(inHand.getAmount() - itemStack.getAmount());
                player.setItemInHand(inHand);
            }
            return;
        }
        player.getInventory().removeItem(itemStack);
    }

    public static long tickToMillis(long ticks) {
        return ticks * 50;
    }

    public static long parseTime(String string) {
        if (string == null) return 0;
        if (string.equalsIgnoreCase("unlimited")) return -1;
        long time = 0;
        int multiplierUnit = 1;
        for (int i = string.length() - 1; i >= 0; i--) {
            char c = string.charAt(i);
            if (c >= '0' && c <= '9') {
                if (i + 1 < string.length() && string.charAt(i + 1) >= '0' && string.charAt(i + 1) <= '9') multiplierUnit *= 10;
                time += (long) (c - '0') * multiplierUnit;
            } else {
                multiplierUnit = 1;
                switch (c) {
                    case 'd':
                    case 'D':
                        multiplierUnit *= 24;
                    case 'h':
                    case 'H':
                        multiplierUnit *= 60;
                    case 'm':
                    case 'M':
                        multiplierUnit *= 60;
                    case 's':
                    case 'S':
                        multiplierUnit *= 20;
                }
            }
        }
        return time;
    }

    public static double cos(int degrees) {
        return degrees >= 0 ? cached_cos[degrees % 360] : -cached_cos[-degrees % 360];
    }

    public static double sin(int degrees) {
        return degrees >= 0 ? cached_sin[degrees % 360] : -cached_sin[-degrees % 360];
    }

    public static Class<?> getCraftClass(String className) throws ClassNotFoundException {
        try {
            final String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
            final Class<?> clazz = Class.forName(fullName);
            return clazz;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        final String fullName = "net.minecraft.server." + getVersion() + className;
        final Class<?> clazz = Class.forName(fullName);
        return clazz;
    }

    public static String getVersion() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1) + ".";
    }

    public static int getVersionNumber() {
        final String name = getVersion().substring(3);
        return Integer.valueOf(name.substring(0, name.length() - 4));
    }

    public static int getX(long xy) {
        return (int)(xy >> 32);
    }
    public static int getY(long xy) {
        return (int)(xy);
    }
    public static long getXY(int x, int y) {
        return (((long)x) << 32) | (y & 0xffffffffL);
    }

    static LinkedList<BlockBreakEvent> dummy = new LinkedList<>();
    public static void checkAccess(Player player, Location blockLocation, Consumer<Boolean> result) {
        Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
            Block block = blockLocation.getBlock();
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                BlockBreakEvent event = new BlockBreakEvent(block, player);
                event.setCancelled(false);
                dummy.add(event);
                Bukkit.getPluginManager().callEvent(event);
                result.accept(!event.isCancelled());
                dummy.remove(event);
            });
        });
    }

    public static void submitSync(Runnable r) {
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), r);
        }
    }

    public static boolean checkDirectAccess(Player player, Block block) {
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        event.setCancelled(false);
        dummy.add(event);
        Bukkit.getPluginManager().callEvent(event);
        dummy.remove(event);
        return !event.isCancelled();
    }

    public static XMaterial material(String name) {
        for (String n : name.split(";")) {
            try {
                XMaterial mat = XMaterial.valueOf(n);
                if (mat.isSupported()) {
                    return mat;
                }
            } catch (Throwable t) {
            }
        }
        return XMaterial.AIR;
    }
    public static boolean hasData(ItemStack item, String key) {
        final byte[] data = NBTUtil.getData(item, key);
        return data != null;
    }
    public static String toRoman(int value) {
        if (value < 1 || value > 3999) return "?";
        StringBuilder s = new StringBuilder();
        while (value >= 1000) {
            s.append("M");
            value-=1000;
        }
        while (value >= 900) {
            s.append("CM");
            value-=900;
        }
        while (value >= 500) {
            s.append("D");
            value-=500;
        }
        while (value >= 400) {
            s.append("CD");
            value-=400;
        }
        while (value >= 100) {
            s.append("C");
            value-=100;
        }
        while (value >= 90) {
            s.append("XC");
            value-=90;
        }
        while (value >= 50) {
            s.append("L");
            value-=50;
        }
        while (value >= 40) {
            s.append("XL");
            value-=40;
        }
        while (value >= 10) {
            s.append("X");
            value-=10;
        }
        while (value >= 9) {
            s.append("IX");
            value-=9;
        }
        while (value >= 5) {
            s.append("V");
            value-=5;
        }
        while (value >= 4) {
            s.append("IV");
            value-=4;
        }
        while (value >= 1) {
            s.append("I");
            value--;
        }
        return s.toString();
    }

    public static ItemStack deserializeItemStack(Map<String, Object> map, Placeholder placeholder) {
        Object className = map.getOrDefault("class", "org.bukkit.inventory.ItemStack");
        if ("Oraxen".equals(className)) {
            try {
                return OraxenItems.getItemById(placeholder.replace(String.valueOf(map.get("item-id")))).build();
            } catch (Throwable ignored) {
            }
        }
        if ("ItemsAdder".equals(className)) {
            try {
                return CustomStack.getInstance(placeholder.replace(String.valueOf(map.get("namespace-id")))).getItemStack();
            } catch (Throwable ignored) {
            }
        }
        return (ItemStack) ConfigurationSerialization.deserializeObject(validateItemStack(map, placeholder));
    }

    private static void validateItemMeta(Map<String, Object> map, Placeholder placeholder) {
        if (!map.containsKey("meta-type")) map.put("meta-type", "UNSPECIFIED");
        replaceString(map, "display-name", placeholder);
        replaceList(map, "lore", true, placeholder);
        replaceString(map, "title", placeholder);
        replaceString(map, "author", placeholder);
        replaceList(map, "pages", false, placeholder);
    }

    private static void validateItemMeta(ItemMeta itemMeta, Placeholder placeholder) {
        itemMeta.setDisplayName(placeholder.replace(itemMeta.getDisplayName()));
        itemMeta.setLore(placeholder.replaceWithBreakableLines(itemMeta.getLore()));
        if (itemMeta instanceof BookMeta) {
            ((BookMeta) itemMeta).setAuthor(placeholder.replace(((BookMeta) itemMeta).getAuthor()));
            ((BookMeta) itemMeta).setPages(placeholder.replace(((BookMeta) itemMeta).getPages()));
            ((BookMeta) itemMeta).setTitle(placeholder.replace(((BookMeta) itemMeta).getTitle()));
        }
    }

    private static void replaceString(Map<String, Object> map, String key, Placeholder placeholder) {
        Object displayName = map.get("display-name");
        if (displayName != null) {
            map.put(key, placeholder.replace(String.valueOf(displayName)));
        }
    }

    private static void replaceList(Map<String, Object> map, String key, boolean splitNewLines, Placeholder placeholder) {
        Object lore = map.get(key);
        if (lore != null) {
            if (!(lore instanceof List)) {
                if (splitNewLines) {
                    lore = Arrays.asList(String.valueOf(lore).split("\n"));
                } else {
                    lore = Collections.singletonList(String.valueOf(lore));
                }
            }
            ArrayList<String> copy = new ArrayList<>();
            for (Object o : (List) lore) {
                if (splitNewLines) {
                    copy.addAll(placeholder.replaceWithNewLines(String.valueOf(o)));
                } else {
                    copy.add(placeholder.replace(String.valueOf(o)));
                }
            }
            map.put(key, copy);
        }
    }

    private static Map<String, Object> validateItemStack(Map<String, Object> map, Placeholder placeholder) {
        map = new HashMap<>(map);
        map.putIfAbsent("==", map.getOrDefault("class", "org.bukkit.inventory.ItemStack"));
        try {
            map.put("v", Bukkit.getUnsafe().getDataVersion());
        } catch (Throwable ignored) {
        }
        boolean found = false;
        Object type = map.get("type");
        if (type instanceof String) {
            String[] split = placeholder.replace((String) type).split(";");
            for (String s : split) {
                try {
                    map.put("type", XMaterial.valueOf(s).parseMaterial().name());
                    found = true;
                    break;
                } catch (Throwable ignored) {
                }
            }
        }
        Object meta = map.get("meta");
        if (meta instanceof ItemMeta) {
            meta = ((ItemMeta) meta).clone();
            validateItemMeta((ItemMeta) meta, placeholder);
            map.put("meta", meta);
        } else if (meta instanceof Map) {
            HashMap metaMap = new HashMap<>((Map) meta);
            validateItemMeta(metaMap, placeholder);
            map.put("meta", metaMap);
        }
        if (!found) {
            map.put("type", "AIR");
        }
        return map;
    }
}
