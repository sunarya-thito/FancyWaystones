package thito.fancywaystones;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

public class Util {
    private static double[] cached_cos = new double[360];
    private static double[] cached_sin = new double[360];
    private static Field nbtTagCompoundMap, nbtByteArrayData;
    private static Class<?> nbtTagByteArray;
    static {
        for (int i = 0; i < 360; i++) {
            cached_cos[i] = Math.cos(Math.toRadians(i));
            cached_sin[i] = Math.sin(Math.toRadians(i));
        }
        Class<?> nbtTagCompound = null;
        try {
            nbtTagCompound = getNMSClass("NBTTagCompound");
            nbtTagByteArray = getNMSClass("NBTTagByteArray");
        } catch (Throwable t) {
            try {
                nbtTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");
                nbtTagByteArray = Class.forName("net.minecraft.nbt.NBTTagByteArray");
            } catch (Throwable t2) {
            }
        }
        if (nbtTagCompound != null) {
            for (Field field : nbtTagCompound.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    nbtTagCompoundMap = field;
                    field.setAccessible(true);
                    break;
                }
            }
            for (Field field : nbtTagByteArray.getDeclaredFields()) {
                if (byte[].class.isAssignableFrom(field.getType())) {
                    nbtByteArrayData = field;
                    field.setAccessible(true);
                    break;
                }
            }
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
    public static void removeData(ItemStack item, String key) {
        try {
            if (item == null) {
                return;
            }
            final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            final boolean hasTag = (boolean) ItemNMS.getClass().getMethod("hasTag").invoke(ItemNMS);
            if (!hasTag) {
                return;
            }
            // NBTTagCompound
            final Object tagCompound = ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS);
            final Field mapField = tagCompound.getClass().getDeclaredField("map");
            mapField.setAccessible(true);
            ((Map<?, ?>) mapField.get(tagCompound)).remove(key);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
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

    static boolean newCompose;

    public static Map<String, byte[]> getDataMap(ItemStack item) {
        Map<String, byte[]> map = new HashMap<>();
        try {
            if (item == null) {
                return null;
            }
            final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            final boolean hasTag = (boolean) ItemNMS.getClass().getMethod("hasTag").invoke(ItemNMS);
            if (!hasTag) {
                return null;
            }
            final Object tagCompound = ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS);
            Map<String, Object> m = (Map) nbtTagCompoundMap.get(tagCompound);
            for (Map.Entry<String, Object> entry : m.entrySet()) {
                if (entry.getKey() != null && nbtTagByteArray != null && nbtTagByteArray.isInstance(entry.getValue())) {
                    map.put(entry.getKey(), (byte[]) nbtByteArrayData.get(entry.getValue()));
                }
            }
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        return map;
    }

    public static void setData(ItemStack item, String key, byte[] value) {
        if (item == null) {
            return;
        }
        if (!newCompose) {
            try {
                final Class<?> tag = Util.getNMSClass("NBTTagCompound");
                final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                        .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
                final boolean hasTag = (boolean) ItemNMS.getClass().getMethod("hasTag").invoke(ItemNMS);
                final Object tagCompound = hasTag ? ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS)
                        : tag.newInstance();
                tagCompound.getClass().getMethod("setByteArray", String.class, byte[].class).invoke(tagCompound, new Object[]{key, value});
                ItemNMS.getClass().getMethod("setTag", tag).invoke(ItemNMS, tagCompound);
                final ItemStack result = (ItemStack) Util.getCraftClass("inventory.CraftItemStack")
                        .getMethod("asBukkitCopy", ItemNMS.getClass()).invoke(null, ItemNMS);
                item.setItemMeta(result.getItemMeta());
                return;
            } catch (ClassNotFoundException e) {
                newCompose = true;
            } catch (final Throwable t) {
                t.printStackTrace();
                return;
            }
        }
        if (newCompose) {
            try {
                Class<?> tag = Class.forName("net.minecraft.nbt.NBTTagCompound");
                final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                        .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
                Object tagCompound = ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS);
                if (tagCompound == null) {
                    tagCompound = tag.newInstance();
                }
                tagCompound.getClass().getMethod("setByteArray", String.class, byte[].class).invoke(tagCompound, key, value);
                ItemNMS.getClass().getMethod("setTag", tag).invoke(ItemNMS, tagCompound);
                final ItemStack result = (ItemStack) Util.getCraftClass("inventory.CraftItemStack")
                        .getMethod("asBukkitCopy", ItemNMS.getClass()).invoke(null, ItemNMS);
                item.setItemMeta(result.getItemMeta());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
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
    public static byte[] getData(ItemStack item, String key) {
        try {
            if (item == null) {
                return null;
            }
            final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            final Object tagCompound = ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS);
            if (tagCompound == null) return null;
            final Object result = tagCompound.getClass().getMethod("getByteArray", String.class).invoke(tagCompound, key);
            return (byte[]) result;
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    public static boolean hasData(ItemStack item, String key) {
        try {
            if (item == null) {
                return false;
            }
            final Object ItemNMS = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            final Object tagCompound = ItemNMS.getClass().getMethod("getTag").invoke(ItemNMS);
            if (tagCompound == null) return false;
            final Object result = tagCompound.getClass().getMethod("hasKey", String.class).invoke(tagCompound, key);
            return (boolean) result;
        } catch (final Throwable t) {
            final byte[] data = getData(item, key);
            return data != null;
        }
    }
    public static String toRoman(int value) {
        if (value < 1 || value > 3999) return "?";
        String s = "";
        while (value >= 1000) {
            s+="M";
            value-=1000;
        }
        while (value >= 900) {
            s+="CM";
            value-=900;
        }
        while (value >= 500) {
            s+="D";
            value-=500;
        }
        while (value >= 400) {
            s+="CD";
            value-=400;
        }
        while (value >= 100) {
            s+="C";
            value-=100;
        }
        while (value >= 90) {
            s+="XC";
            value-=90;
        }
        while (value >= 50) {
            s+="L";
            value-=50;
        }
        while (value >= 40) {
            s+="XL";
            value-=40;
        }
        while (value >= 10) {
            s+="X";
            value-=10;
        }
        while (value >= 9) {
            s+="IX";
            value-=9;
        }
        while (value >= 5) {
            s+="V";
            value-=5;
        }
        while (value >= 4) {
            s+="IV";
            value-=4;
        }
        while (value >= 1) {
            s+="I";
            value--;
        }
        return s;
    }
}
