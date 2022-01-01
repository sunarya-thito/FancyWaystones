package thito.fancywaystones;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NBTUtil {
    private static Field nbtTagCompoundMap, nbtByteArrayData;
    private static Class<?> nbtTagByteArray;
    static {
        Class<?> nbtTagCompound = null;
        try {
            nbtTagCompound = Util.getNMSClass("NBTTagCompound");
            nbtTagByteArray = Util.getNMSClass("NBTTagByteArray");
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
    public static byte[] getData(ItemStack item, String key) {
        try {
            if (item == null) {
                return null;
            }
            Method asNMSCopy = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class);
            Object ItemNMS = asNMSCopy.invoke(null, item);
            if (getTag == null) {
                try {
                    getTag = asNMSCopy.getReturnType().getMethod("getTag");
                } catch (Throwable t) {
                    getTag = asNMSCopy.getReturnType().getMethod("t");
                }
                getTag.setAccessible(true);
            }
            final Object tagCompound = getTag.invoke(ItemNMS);
            if (tagCompound == null) return null;
            if (getByteArray == null) {
                try {
                    getByteArray = tagCompound.getClass().getMethod("getByteArray", String.class);
                } catch (Throwable t) {
                    getByteArray = tagCompound.getClass().getMethod("m", String.class);
                }
                getByteArray.setAccessible(true);
            }
            final Object result = getByteArray.invoke(tagCompound, key);
            if (((byte[]) result).length == 0) return null;
            return (byte[]) result;
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static boolean newCompose;
    static Method setByteArray, getTag, getByteArray, setTag;
    public static void setData(ItemStack item, String key, byte[] value) {
        if (item == null) {
            return;
        }
        Method asNMSCopy;
        try {
            asNMSCopy = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            return;
        }
        if (!newCompose) {
            try {
                final Class<?> tag = Util.getNMSClass("NBTTagCompound");
                final Object ItemNMS = asNMSCopy.invoke(null, item);
                if (getTag == null) {
                    try {
                        getTag = asNMSCopy.getReturnType().getMethod("getTag");
                    } catch (Throwable t) {
                        getTag = asNMSCopy.getReturnType().getMethod("t");
                    }
                    getTag.setAccessible(true);
                }
                Object tagCompound = getTag.invoke(ItemNMS);
                if (tagCompound == null) {
                    tagCompound = tag.getConstructor().newInstance();
                }
                if (setByteArray == null) {
                    try {
                        setByteArray = tagCompound.getClass().getMethod("setByteArray", String.class, byte[].class);
                    } catch (Throwable t) {
                        setByteArray = tagCompound.getClass().getMethod("a", String.class, byte[].class);
                    }
                    setByteArray.setAccessible(true);
                }
                setByteArray.invoke(tagCompound, key, value);
                if (setTag == null) {
                    try {
                        setTag = ItemNMS.getClass().getMethod("setTag", tag);
                    } catch (Throwable t) {
                        setTag = ItemNMS.getClass().getMethod("c", tag);
                    }
                    setTag.setAccessible(true);
                }
                setTag.invoke(ItemNMS, tagCompound);
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
        try {
            Class<?> tag = Class.forName("net.minecraft.nbt.NBTTagCompound");
            final Object ItemNMS = asNMSCopy.invoke(null, item);
            if (getTag == null) {
                try {
                    getTag = asNMSCopy.getReturnType().getMethod("getTag");
                } catch (Throwable t) {
                    getTag = asNMSCopy.getReturnType().getMethod("t");
                }
                getTag.setAccessible(true);
            }
            Object tagCompound = getTag.invoke(ItemNMS);
            if (tagCompound == null) {
                tagCompound = tag.getConstructor().newInstance();
            }
            if (setByteArray == null) {
                try {
                    setByteArray = tagCompound.getClass().getMethod("setByteArray", String.class, byte[].class);
                } catch (Throwable t) {
                    setByteArray = tagCompound.getClass().getMethod("a", String.class, byte[].class);
                }
                setByteArray.setAccessible(true);
            }
            setByteArray.invoke(tagCompound, key, value);
            if (setTag == null) {
                try {
                    setTag = ItemNMS.getClass().getMethod("setTag", tag);
                } catch (Throwable t) {
                    setTag = ItemNMS.getClass().getMethod("c", tag);
                }
                setTag.setAccessible(true);
            }
            setTag.invoke(ItemNMS, tagCompound);
            final ItemStack result = (ItemStack) Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asBukkitCopy", ItemNMS.getClass()).invoke(null, ItemNMS);
            item.setItemMeta(result.getItemMeta());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public static Map<String, byte[]> getDataMap(ItemStack item) {
        Map<String, byte[]> map = new HashMap<>();
        try {
            if (item == null) {
                return null;
            }
            Method asNMSCopy = Util.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class);
            final Object ItemNMS = asNMSCopy.invoke(null, item);
            if (getTag != null) {
                try {
                    getTag = asNMSCopy.getReturnType().getMethod("getTag");
                } catch (Throwable t) {
                    getTag = asNMSCopy.getReturnType().getMethod("t");
                }
                getTag.setAccessible(true);
            }
            final Object tagCompound = getTag.invoke(ItemNMS);
            if (tagCompound == null) return null;
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
}
