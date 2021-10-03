package thito.fancywaystones.books;

import org.bukkit.inventory.*;
import thito.fancywaystones.*;
import thito.fancywaystones.task.*;
import thito.fancywaystones.ui.*;

public class DeathBook {

    public boolean isEnable() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getBoolean("Books.Death Book.Enable");
    }

    public boolean ignoreSafeTeleport() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getBoolean("Books.Death Book.Ignore Unsafe Destination");
    }

    public long getExtraNoDamageTime() {
        return Util.parseTime(FancyWaystones.getPlugin().getBooksYml().getConfig().getString("Books.Death Book.Extra No Damage Time"));
    }

    public long getDeathLocationTimeout() {
        return Util.parseTime(FancyWaystones.getPlugin().getBooksYml().getConfig().getString("Books.Death Book.Location Time Out"));
    }

    public int getCheckRadius() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getInt("Books.Death Book.Check Radius");
    }

    public int getCheckHeight() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getInt("Books.Death Book.Check Height");
    }

    public boolean isEnableListener() {
        return FancyWaystones.getPlugin().getBooksYml().getConfig().getBoolean("Books.Death Book.Enable Listener",true);
    }

    public ItemStack createItem() {
        MinecraftItem item = new MinecraftItem();
        item.load(FancyWaystones.getPlugin().getBooksYml().getConfig().getConfigurationSection("Books.Death Book.Item"));
        item.setData("FancyWaystones:DeathBook", new byte[0]);
        return item.getItemStack(new Placeholder());
    }

    public boolean isItem(ItemStack itemStack) {
        return Util.hasData(itemStack, "FancyWaystones:DeathBook");
    }
}
