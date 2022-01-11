package thito.fancywaystones.reflection;

import org.bukkit.Bukkit;
import thito.fancywaystones.Util;

public class ClassFinder {

    private static boolean nmsWithVersion;
    private static String version;
    static {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        try {
            Class.forName("net.minecraft.server."+Util.getVersion()+".Entity");
            nmsWithVersion = true;
        } catch (ClassNotFoundException ignored) {
        }
    }
    public static Class<?> find(Object... classNames) {
        for (Object c : classNames) {
            Class<?> aClass = get(c);
            if (aClass != null) {
                return aClass;
            }
        }
        return null;
    }
    public static Class<?> get(Object className) {
        if (className instanceof Class) {
            return (Class<?>) className;
        }
        try {
            String string = (String) className;
            string = string.replace("{cb}", "org.bukkit.craftbukkit."+version);
            if (nmsWithVersion) {
                string = string.replace("{nms}", "net.minecraft.server." + version);
            } else {
                string = string.replace("{nms}", "net.minecraft.server");
            }
            return Class.forName(string);
        } catch (Throwable ignored) {
            return null;
        }
    }

}
