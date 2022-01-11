package thito.fancywaystones.ui;

import net.wesjd.anvilgui.*;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.*;
import thito.fancywaystones.*;

import java.util.*;
import java.util.stream.*;

public class MembersMenu implements AttachedMenu {

    public enum Category {
        WHITELIST, BLACKLIST;
    }

    public static class MenuData {
        private Category lastCategory;
        private Map<Category, Integer> lastPage = new HashMap<>();

        public int getLastPage(Category type) {
            Integer result = lastPage.get(type);
            if (result == null) return 0;
            return result;
        }
    }
    public static MenuData getMenuData(Player player) {
        List<MetadataValue> values = player.getMetadata("FancyWaystones:WaystoneMenu");
        for (MetadataValue value : values) {
            if (value.getOwningPlugin() == FancyWaystones.getPlugin()) {
                Object obj = value.value();
                if (obj instanceof MenuData) {
                    return (MenuData) obj;
                }
            }
        }
        MenuData newMenuData = new MenuData();
        player.setMetadata("FancyWaystones:WaystoneMenu", new FixedMetadataValue(FancyWaystones.getPlugin(), newMenuData));
        return newMenuData;
    }
    private PlayerData playerData;
    private WaystoneData waystoneData;
    private String title;
    private MinecraftMenu menu;
    private ConfigurationSection itemDisplay, previousPageItem, nextPageItem, categoryItem, closeItem, addMember;
    private Map<Character, ConfigurationSection> otherItems = new HashMap<>();
    private int itemsPerPage;
    private char[] layout;

    public MembersMenu(PlayerData playerData, WaystoneData waystoneData) {
        this.playerData = playerData;
        this.waystoneData = waystoneData;
        ConfigurationSection root = FancyWaystones.getPlugin().getGuiYml().getConfig().getConfigurationSection("Members Menu");
        title = root.getString("Title");
        itemDisplay = root.getConfigurationSection("Item Display");
        previousPageItem = root.getConfigurationSection("Previous Page Item");
        nextPageItem = root.getConfigurationSection("Next Page Item");
        categoryItem = root.getConfigurationSection("Category");
        closeItem = root.getConfigurationSection("Close Item");
        addMember = root.getConfigurationSection("Add Member Item");
        ConfigurationSection oI = root.getConfigurationSection("Other Items");
        for (String key : oI.getKeys(false)) {
            otherItems.put(key.charAt(0), oI.getConfigurationSection(key+".Item"));
        }
        List<String> stringList = root.getStringList("Layout");
        layout = new char[stringList.size() * 9];
        for (int i = 0; i < stringList.size(); i++) {
            String string = stringList.get(i);
            for (int j = 0; j < 9; j++) {
                if (j < string.length()) {
                    layout[i * 9 + j] = string.charAt(j);
                    if (string.charAt(j) == itemDisplay.getString("Layout Key").charAt(0)) {
                        itemsPerPage++;
                    }
                } else {
                    layout[i * 9 + j] = ' ';
                }
            }
        }
        if (itemsPerPage == 0) itemsPerPage = 1; // cannot be zero, otherwise will throw ArithmeticError
        menu = new MinecraftMenu(false);
        menu.setAlwaysUpdate(true);
        menu.setRows(stringList.size());
    }

    public void close() {
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                new ArrayList<>(menu.getViewers()).forEach(HumanEntity::closeInventory);
            });
        }
    }

    public void open() {
        MenuData menuData = getMenuData(playerData.getPlayer());
        if (menuData.lastCategory != null) {
            switchPage(menuData.lastCategory, menuData.getLastPage(menuData.lastCategory));
        } else {
            switchPage(Category.WHITELIST, menuData.getLastPage(Category.WHITELIST));
        }
    }

    public void switchPage(Category category, int page) {
        MenuData menuData = getMenuData(playerData.getPlayer());
        menuData.lastCategory = category;
        menuData.lastPage.put(category, page);
        Placeholder placeholder = menu.getPlaceholder();
        placeholder.putContent(Placeholder.PLAYER, playerData.getPlayer());
        placeholder.putContent(Placeholder.WAYSTONE, waystoneData);
        List<WaystoneMember> waystoneDataList = new ArrayList<>(category == Category.WHITELIST ? waystoneData.getMembers() : waystoneData.getBlacklist());
        int maxPage = waystoneDataList.size() % itemsPerPage == 0 ? waystoneDataList.size() / itemsPerPage : waystoneDataList.size() / itemsPerPage + 1;
        placeholder.put("page", ph -> page + 1);
        placeholder.put("max_page", ph -> Math.max(1, maxPage));
        waystoneDataList = waystoneDataList.subList(Math.min(waystoneDataList.size(), Math.max(0, page * itemsPerPage)), Math.min(waystoneDataList.size(), Math.max(0, page * itemsPerPage + itemsPerPage)));
        Iterator<WaystoneMember> iterator = waystoneDataList.iterator();
        for (int i = 0; i < layout.length; i++) {
            char current = layout[i];
            MinecraftItem item = menu.getItem(i);
            item.clear();
            if (current == itemDisplay.getString("Layout Key").charAt(0)) {
                if (iterator.hasNext()) {
                    WaystoneMember next = iterator.next();
                    if (next.getUUID().equals(waystoneData.getOwnerUUID())) continue;
                    ConfigurationSection itemSection = itemDisplay;
                    item.getPlaceholder().putContent(Placeholder.MEMBER, next);
                    if (itemSection == null) {
                        continue;
                    }
                    item.setSkull(next.getUUID().toString());
                    item.load(itemSection);
                    item.addClickListener((inv, ph) -> {
                        if (inv.isShiftClick() && inv.isRightClick()) {
                            if (category == Category.WHITELIST) {
                                waystoneData.removeMember(next.getUUID());
                            } else {
                                waystoneData.getBlacklist().remove(next);
                            }
                            switchPage(category, page);
                        }
                    });
                }
            } else if (current == addMember.getString("Layout Key").charAt(0)) {
                item.load(addMember);
                item.addClickListener((inv, ph) -> {
                    AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(FancyWaystones.getPlugin())
                            .title(new Placeholder().putContent(Placeholder.PLAYER, playerData.getPlayer()).replace("{language.edit-members-gui-title}"))
                            .onComplete((pl, name) -> {
                                Player player = Bukkit.getPlayerExact(name);
                                if (player != null) {
                                    if (player.getUniqueId().equals(waystoneData.getOwnerUUID())) {
                                        playerData.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, playerData.getPlayer())
                                                .put("target", px -> name).replace("{language.not-owner}"));
                                    } else {
                                        Set<WaystoneMember> members = category == Category.WHITELIST ?
                                                waystoneData.getMembers() : waystoneData.getBlacklist();
                                        if (members.contains(new WaystoneMember(pl.getUniqueId()))) {
                                            playerData.getPlayer().sendMessage(new Placeholder().replace("{member-already-added}"));
                                        } else {
                                            if (category == Category.WHITELIST) {
                                                waystoneData.addMember(player);
                                            } else {
                                                waystoneData.getBlacklist().add(new WaystoneMember(player.getUniqueId(), player.getName()));
                                            }
                                        }
                                    }
                                } else {
                                    playerData.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, playerData.getPlayer())
                                            .put("target", px -> name).replace("{language.player-not-found}"));
                                }
                                open();
                                return AnvilGUI.Response.close();
                            })
                            .text(playerData.getPlayer().getName())
                            .plugin(FancyWaystones.getPlugin())
                            .itemLeft(Util.material("PLAYER_HEAD").parseItem())
                            .onClose(p -> {
                                open();
                            });
                    Util.submitSync(() -> {
                        playerData.getPlayer().closeInventory();
                        builder.open(playerData.getPlayer());
                    });
                });
            } else if (current == previousPageItem.getString("Layout Key").charAt(0)) {
                if (page > 0) {
                    item.load(previousPageItem.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> {
                        switchPage(category, page - 1);
                    });
                } else {
                    item.load(previousPageItem.getConfigurationSection("Disable"));
                }
            } else if (current == nextPageItem.getString("Layout Key").charAt(0)) {
                if (page + 1 < maxPage) {
                    item.load(nextPageItem.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> {
                        switchPage(category, page + 1);
                    });
                } else {
                    item.load(nextPageItem.getConfigurationSection("Disable"));
                }
            } else if (current == closeItem.getString("Layout Key").charAt(0)) {
                item.load(closeItem);
                item.addClickListener((inv, ph) -> {
                    Util.submitSync(() -> inv.getWhoClicked().closeInventory());
                });
            } else {
                boolean found = false;
                for (Category t : Category.values()) {
                    if (categoryItem.isString(t.name()+".Layout Key") && categoryItem.getString(t.name()+".Layout Key").charAt(0) == current) {
                        Set<WaystoneMember> waystones = t == Category.WHITELIST ? waystoneData.getMembers() : waystoneData.getBlacklist();
                        waystones.remove(new WaystoneMember(waystoneData.getOwnerUUID()));
                        if (waystones.isEmpty()) {
                            item.load(categoryItem.getConfigurationSection(t.name()+".Empty"));
                        } else {
                            item.load(categoryItem.getConfigurationSection(t.name()+".Not Empty"));
                        }
                        if (category == t) {
                            item.addEnchantment("KNOCKBACK", 1);
                            item.setFlags(Arrays.asList(ItemFlag.values()).stream().map(ItemFlag::name).collect(Collectors.toList()));
                        }
                        item.addClickListener((inv, ph) -> {
                            switchPage(t, menuData.getLastPage(t));
                        });
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ConfigurationSection other  = otherItems.get(current);
                    if (other != null) {
                        item.load(other);
                    }
                }
            }
        }
        menu.setTitle(title);
        menu.open(playerData.getPlayer());
    }

}
