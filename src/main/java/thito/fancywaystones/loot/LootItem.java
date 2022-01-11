package thito.fancywaystones.loot;

import thito.fancywaystones.condition.Condition;
import thito.fancywaystones.ui.MinecraftItem;

import java.util.List;

public class LootItem {
    private final Condition condition;
    private final List<MinecraftItem> minecraftItems;
    private final List<String> conflict;

    public LootItem(Condition condition, List<MinecraftItem> minecraftItems, List<String> conflict) {
        this.condition = condition;
        this.minecraftItems = minecraftItems;
        this.conflict = conflict;
    }

    public Condition getCondition() {
        return condition;
    }

    public List<MinecraftItem> getMinecraftItems() {
        return minecraftItems;
    }

    public List<String> getConflict() {
        return conflict;
    }
}
