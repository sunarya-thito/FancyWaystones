package thito.fancywaystones.loot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.Condition;
import thito.fancywaystones.config.ListSection;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.config.Section;
import thito.fancywaystones.ui.MinecraftItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootTable {
    private final Map<String, LootItem> lootItemMap = new HashMap<>();

    public void load(ConfigurationSection configurationSection) {
        for (String key : configurationSection.getKeys(false)) {
            ConfigurationSection section = configurationSection.getConfigurationSection(key);
            Condition condition = Condition.fromConfig(new ListSection(section.getList("Condition")));
            List<MinecraftItem> minecraftItems = section.getList("Items")
                    .stream().map(item -> {
                        MinecraftItem minecraftItem = new MinecraftItem();
                        minecraftItem.load((Section) Section.wrap(item));
                        return minecraftItem;
                    }).collect(Collectors.toList());
            List<String> conflicting = section.getStringList("Conflicting");
            LootItem lootItem = new LootItem(condition, minecraftItems, conflicting);
            lootItemMap.put(key, lootItem);
        }
    }

    public void load(Section section) {
        for (String key : ((MapSection) section).keySet()) {
            MapSection map = section.getMap(key).orElseGet(MapSection::empty);
            Condition condition = Condition.fromConfig(map.getList("Condition").orElseGet(ListSection::empty));
            List<MinecraftItem> minecraftItems = map.getList("Items").orElseGet(ListSection::empty)
                    .stream().map(item -> {
                        MinecraftItem minecraftItem = new MinecraftItem();
                        minecraftItem.load((Section) Section.wrap(item));
                        return minecraftItem;
                    }).collect(Collectors.toList());
            List<String> conflicting = map.getList("Conflicting").orElseGet(ListSection::empty).stream().map(String::valueOf).collect(Collectors.toList());
            LootItem lootItem = new LootItem(condition, minecraftItems, conflicting);
            lootItemMap.put(key, lootItem);
        }
    }

    public Map<String, LootItem> getLootItemMap() {
        return lootItemMap;
    }

    public List<LootItem> getLoot(Placeholder placeholder) {
        List<String> blacklisted = new ArrayList<>();
        List<LootItem> selected = new ArrayList<>();
        for (Map.Entry<String, LootItem> entry : lootItemMap.entrySet()) {
            if (blacklisted.contains(entry.getKey()) ||
                    entry.getValue().getCondition().test(placeholder).isEmpty()) continue;
            selected.add(entry.getValue());
            blacklisted.add(entry.getKey());
            blacklisted.addAll(entry.getValue().getConflict());
        }
        return selected;
    }

    public List<ItemStack> getLootItems(Placeholder placeholder) {
        List<String> blacklisted = new ArrayList<>();
        List<ItemStack> selected = new ArrayList<>();
        for (Map.Entry<String, LootItem> entry : lootItemMap.entrySet()) {
            if (blacklisted.contains(entry.getKey()) ||
                    entry.getValue().getCondition().test(placeholder).isEmpty()) continue;
            selected.addAll(entry.getValue().getMinecraftItems().stream().map(item -> item.getItemStack(placeholder)).collect(Collectors.toList()));
            blacklisted.add(entry.getKey());
            blacklisted.addAll(entry.getValue().getConflict());
        }
        return selected;
    }
}
