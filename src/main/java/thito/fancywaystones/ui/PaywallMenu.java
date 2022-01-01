package thito.fancywaystones.ui;

import org.bukkit.configuration.ConfigurationSection;
import thito.fancywaystones.*;
import thito.fancywaystones.economy.Cost;
import thito.fancywaystones.economy.EconomyService;
import thito.fancywaystones.economy.ItemEconomyService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PaywallMenu implements AttachedMenu {
    private PlayerData playerData;
    private List<Cost> costList;
    private WaystoneData source, target;

    private String title;
    private ConfigurationSection economyDisplay, closeItem, otherItems;
    private char[] layout;

    private MinecraftMenu menu;

    public PaywallMenu(PlayerData playerData, List<Cost> costList, WaystoneData source, WaystoneData target) {
        this.playerData = playerData;
        this.costList = costList;
        this.source = source;
        this.target = target;
        ConfigurationSection root = FancyWaystones.getPlugin().getGuiYml().getConfig().getConfigurationSection("Paywall Menu");
        title = Objects.requireNonNull(root).getString("Title");
        economyDisplay = root.getConfigurationSection("Economy Display");
        closeItem = root.getConfigurationSection("Close Item");
        otherItems = root.getConfigurationSection("Other Items");
        List<String> stringList = root.getStringList("Layout");
        layout = new char[stringList.size() * 9];
        for (int i = 0; i < stringList.size(); i++) {
            String string = stringList.get(i);
            for (int j = 0; j < 9; j++) {
                if (j < string.length()) {
                    layout[i * 9 + j] = string.charAt(j);
                } else {
                    layout[i * 9 + j] = ' ';
                }
            }
        }
        menu = new MinecraftMenu(false);
        menu.setAlwaysUpdate(true);
        menu.setRows(stringList.size());
    }

    public void open() {
        Iterator<Cost> iterator = costList.iterator();
        for (int i = 0; i < layout.length; i++) {
            char current = layout[i];
            MinecraftItem item = menu.getItem(i);
            item.clear();
            if (current == economyDisplay.getString("Layout Key").charAt(0)) {
                if (iterator.hasNext()) {
                    Cost cost = iterator.next();
                    EconomyService service = cost.getService();
                    item.getPlaceholder().put("cost_value", ph -> cost.getAmount());
                    if (service instanceof ItemEconomyService) {
                        item.getPlaceholder().put("player_economy_value", ph -> (int) service.balance(playerData.getPlayer()));
                        item.load(((ItemEconomyService) service).getItemStack());
                        List<String> lore = item.getLore();
                        if (lore == null) lore = new ArrayList<>();
                        lore.addAll(item.getPlaceholder().replaceWithBreakableLines(economyDisplay.getStringList("Item.Lore")));
                        item.setLore(lore);
                    } else {
                        item.getPlaceholder().put("player_economy_value", ph -> service.balance(playerData.getPlayer()));
                        item.load(economyDisplay.getConfigurationSection(service.getId()));
                    }
                    item.addClickListener((event, ph) -> {
                        Util.submitSync(() -> {
                            playerData.getPlayer().closeInventory();
                            if (WaystoneMenu.checkEconomy(target, cost, playerData)) return;
                            WaystoneMenu.scheduleTeleport(target, cost, playerData, source);
                        });
                    });
                }
            } else if (current == closeItem.getString("Layout Key").charAt(0)) {
                item.load(closeItem);
                item.addClickListener((event, ph) -> {
                    Util.submitSync(() -> {
                        playerData.getPlayer().closeInventory();
                        WaystoneManager.getManager().openWaystoneMenu(playerData, source);
                    });
                });
            } else {
                ConfigurationSection section = otherItems.getConfigurationSection(current +".Item");
                if (section != null) {
                    item.load(section);
                }
            }
        }
        menu.setTitle(title);
        menu.open(playerData.getPlayer());
    }

    @Override
    public void close() {

    }
}
