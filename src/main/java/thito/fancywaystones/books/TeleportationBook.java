package thito.fancywaystones.books;

import org.bukkit.inventory.*;
import thito.fancywaystones.*;
import thito.fancywaystones.ui.*;

import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

public class TeleportationBook {

    public boolean isEnable() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getBoolean("Books.Teleportation Book.Enable");
    }

    public boolean canChargeBack() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getBoolean("Books.Teleportation Book.Charge Back On Invalid Waystone");
    }

    public ItemStack createEmptyItem() {
        MinecraftItem item = new MinecraftItem();
        item.load(FancyWaystones.getPlugin().getBooksYml().getConfig().getConfigurationSection("Books.Teleportation Book.Empty Item"));
        item.setData("FancyWaystones:EmptyTeleportScroll", new byte[0]);
        return item.getItemStack(new Placeholder());
    }

    public ItemStack createItem(WaystoneData waystoneData) {
        MinecraftItem item = new MinecraftItem();
        item.load(FancyWaystones.getPlugin().getBooksYml().getConfig().getConfigurationSection("Books.Teleportation Book.Item"));
        item.setData("FW:TS", waystoneData.getUUID().toString().getBytes(StandardCharsets.UTF_8));
        ItemStack result = item.getItemStack(new Placeholder().putContent(Placeholder.WAYSTONE, waystoneData));
        return result;
    }

    public boolean isItem(ItemStack itemStack) {
        return Util.hasData(itemStack, "FW:TS");
    }

    public boolean isEmptyItem(ItemStack itemStack) {
        return Util.hasData(itemStack, "FancyWaystones:EmptyTeleportScroll");
    }

    public WaystoneData getWaystoneData(ItemStack itemStack) {
        FancyWaystones.checkIOThread();
        byte[] data = Util.getData(itemStack, "FW:TS");
        if (data != null) {
            try {
                UUID uuid = UUID.fromString(new String(data, StandardCharsets.UTF_8));
                return WaystoneManager.getManager().getData(uuid);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

}
