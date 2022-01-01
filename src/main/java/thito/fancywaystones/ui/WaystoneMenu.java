package thito.fancywaystones.ui;

import net.wesjd.anvilgui.*;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.*;
import thito.fancywaystones.*;
import thito.fancywaystones.economy.*;
import thito.fancywaystones.event.FWEvent;
import thito.fancywaystones.event.WaystonePreTeleportEvent;
import thito.fancywaystones.event.WaystoneTeleportEvent;
import thito.fancywaystones.task.*;

import java.util.*;
import java.util.stream.*;

public class WaystoneMenu implements AttachedMenu {
    public static class MenuData {
        private WaystoneType lastCategory;
        private ItemSort sort = ItemSort.NAME;
        private ItemOrder order = ItemOrder.ASCEND;
        private String searchFilter;
        private Map<WaystoneType, Integer> lastPage = new HashMap<>();

        public String getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(String searchFilter) {
            this.searchFilter = searchFilter;
        }

        public ItemOrder getOrder() {
            return order;
        }

        public ItemSort getSort() {
            return sort;
        }

        public void setOrder(ItemOrder order) {
            this.order = order;
        }

        public void setSort(ItemSort sort) {
            this.sort = sort;
        }

        public int getLastPage(WaystoneType type) {
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

    private final PlayerData playerData;
    private final WaystoneData waystoneData;
    private final String title;
    private final MinecraftMenu menu;
    private final ConfigurationSection itemDisplay, previousPageItem, nextPageItem, categoryItem, closeItem, renameItem, sortItem, orderItem, editMembers, searchItem;
    private final Map<Character, ConfigurationSection> otherItems = new HashMap<>();
    private int itemsPerPage;
    private final char[] layout;
    private final Map<WaystoneType, List<WaystoneData>> categorized = new HashMap<>();

    public WaystoneMenu(PlayerData playerData, WaystoneData waystoneData) {
        this.playerData = playerData;
        this.waystoneData = waystoneData;
        ConfigurationSection root = FancyWaystones.getPlugin().getGuiYml().getConfig().getConfigurationSection("Waystone Menu");
        title = Objects.requireNonNull(root).getString("Title");
        itemDisplay = root.getConfigurationSection("Item Display");
        previousPageItem = root.getConfigurationSection("Previous Page Item");
        nextPageItem = root.getConfigurationSection("Next Page Item");
        categoryItem = root.getConfigurationSection("Category");
        closeItem = root.getConfigurationSection("Close Item");
        renameItem = root.getConfigurationSection("Rename Item");
        sortItem = root.getConfigurationSection("Sort Item");
        orderItem = root.getConfigurationSection("Order Item");
        editMembers = root.getConfigurationSection("Edit Members");
        searchItem = root.getConfigurationSection("Search Item");
        ConfigurationSection oI = root.getConfigurationSection("Other Items");
        for (String key : Objects.requireNonNull(oI).getKeys(false)) {
            otherItems.put(key.charAt(0), oI.getConfigurationSection(key+".Item"));
        }
        List<String> stringList = root.getStringList("Layout");
        layout = new char[stringList.size() * 9];
        for (int i = 0; i < stringList.size(); i++) {
            String string = stringList.get(i);
            for (int j = 0; j < 9; j++) {
                if (j < string.length()) {
                    layout[i * 9 + j] = string.charAt(j);
                    if (string.charAt(j) == Objects.requireNonNull(Objects.requireNonNull(itemDisplay).getString("Layout Key")).charAt(0)) {
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
        for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
            if (type != waystoneData.getType() && !waystoneData.getType().isVisible(type)) {
                continue;
            }
            List<WaystoneData> waystones = new ArrayList<>();
            if (type.isAlwaysListed()) {
                WaystoneManager.getManager().getLoadedData().stream().filter(x -> x.getType() == type).forEach(x -> {
                    if (x.getType().canBeListed(playerData.getPlayer(), waystoneData, x)) {
                        waystones.add(x);
                    }
                });
            } else {
                for (WaystoneData known : playerData.getKnownWaystones()) {
                    if (known.getType() == type && type.canBeListed(playerData.getPlayer(), waystoneData, known)) {
                        waystones.add(known);
                    }
                }
            }
            waystones.remove(waystoneData);
            categorized.put(type, waystones);
        }
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
            switchPage(menuData.lastCategory, menuData.getLastPage(menuData.lastCategory), menuData.sort, menuData.order, menuData.searchFilter);
        } else {
            WaystoneManager.getManager().getTypes().stream().findFirst().ifPresent(category ->
                    switchPage(category, menuData.getLastPage(category), menuData.sort, menuData.order, null));
        }
    }

    public void switchPage(WaystoneType category, int page, ItemSort sort, ItemOrder order, String searchFilter) {
        MenuData menuData = getMenuData(playerData.getPlayer());
        menuData.lastCategory = category;
        menuData.searchFilter = searchFilter;
        menuData.lastPage.put(category, page);
        waystoneData.getStatistics().setLastUsed(System.currentTimeMillis());
        Placeholder placeholder = menu.getPlaceholder();
        placeholder.putContent(Placeholder.PLAYER, playerData.getPlayer());
        placeholder.putContent(Placeholder.WAYSTONE, waystoneData);
        placeholder.putContent(Placeholder.SORT, sort);
        List<WaystoneData> waystoneDataList = categorized.get(category);
        if (waystoneDataList == null) waystoneDataList = Collections.emptyList();
        if (searchFilter != null) {
            waystoneDataList = new ArrayList<>(waystoneDataList);
            waystoneDataList.removeIf(x -> {
                String name = x.getName();
                if (name == null) name = WaystoneManager.getManager().getDefaultWaystoneName();
                return name == null || !name.toLowerCase().contains(searchFilter.toLowerCase());
            });
        }
        int maxPage = waystoneDataList.size() % itemsPerPage == 0 ? waystoneDataList.size() / itemsPerPage : waystoneDataList.size() / itemsPerPage + 1;
        placeholder.put("page", ph -> page + 1);
        placeholder.put("max_page", ph -> Math.max(1, maxPage));
        placeholder.put("search_keyword", ph -> searchFilter);
        waystoneDataList = waystoneDataList.subList(Math.min(waystoneDataList.size(), Math.max(0, page * itemsPerPage)), Math.min(waystoneDataList.size(), Math.max(0, page * itemsPerPage + itemsPerPage)));
        waystoneDataList.sort((a, b) -> {
            switch (sort) {
                case NAME:
                    return a.getName().compareToIgnoreCase(b.getName()) * order.multiplier();
                case USERS:
                    return Integer.compare(b.getStatistics().getTotalUsers(), a.getStatistics().getTotalUsers()) * order.multiplier();
                case VISITS:
                    return Integer.compare(b.getStatistics().getTotalVisits(), a.getStatistics().getTotalVisits()) * order.multiplier();
                case VISITORS:
                    return Integer.compare(b.getStatistics().getTotalVisitors(), a.getStatistics().getTotalVisitors()) * order.multiplier();
                case DATE_CREATED:
                    return Long.compare(b.getStatistics().getDateCreated(), a.getStatistics().getDateCreated()) * order.multiplier();
                case USED_TIME:
                    return Long.compare(b.getStatistics().getLastVisited(), a.getStatistics().getLastVisited()) * order.multiplier();
                default: return 0;
            }
        });
        Iterator<WaystoneData> iterator = waystoneDataList.iterator();
        for (int i = 0; i < layout.length; i++) {
            char current = layout[i];
            MinecraftItem item = menu.getItem(i);
            item.clear();
            if (current == Objects.requireNonNull(itemDisplay.getString("Layout Key")).charAt(0)) {
                if (iterator.hasNext()) {
                    WaystoneData next = iterator.next();
                    if (next == null) continue;
                    if (playerData.knowWaystone(next)) {
                        ConfigurationSection itemSection = itemDisplay.getConfigurationSection("ACTIVE." + next.getEnvironment().name() + "." + next.getType().name());
                        if (itemSection == null) {
                            itemSection = itemDisplay.getConfigurationSection("ACTIVE." + next.getEnvironment().name() + ".UNSPECIFIED");
                            if (itemSection == null) continue;
                        }
                        createWaystoneIcon(item, next, itemSection);
                    } else {
                        ConfigurationSection itemSection = itemDisplay.getConfigurationSection("INACTIVE." + next.getEnvironment().name() + "." + next.getType().name());
                        if (itemSection == null) {
                            itemSection = itemDisplay.getConfigurationSection("INACTIVE." + next.getEnvironment().name() + ".UNSPECIFIED");
                            if (itemSection == null) continue;
                        }
                        createWaystoneIcon(item, next, itemSection);
                    }
                }
            } else if (current == Objects.requireNonNull(sortItem.getString("Layout Key")).charAt(0)) {
                int index = sort.ordinal();
                item.load(sortItem.getConfigurationSection(sort.getPath()));
                item.addClickListener((inv, ph) -> {
                    int newIndex = (inv.isRightClick() ? index - 1 : index + 1) % ItemSort.values().length;
                    if (newIndex < 0) newIndex = ItemSort.values().length - 1;
                    switchPage(category, page, ItemSort.values()[newIndex], order, searchFilter);
                });
            } else if (current == Objects.requireNonNull(searchItem.getString("Layout Key")).charAt(0)) {
                if (searchFilter == null || searchFilter.isEmpty()) {
                    item.load(searchItem.getConfigurationSection("Inactive"));
                } else {
                    item.load(searchItem.getConfigurationSection("Active"));
                }
                item.addClickListener((inv, ph) -> {
                    if (inv.isRightClick()) {
                        switchPage(category, page, sort, order, null);
                    } else {
                        AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(FancyWaystones.getPlugin())
                                .title(new Placeholder().putContent(Placeholder.PLAYER, playerData.getPlayer()).replace("{language.search-filter-title}"))
                                .onComplete((pl, name) -> {
                                    switchPage(category, page, sort, order, name.trim());
                                    return AnvilGUI.Response.close();
                                })
                                .text(searchFilter == null || searchFilter.trim().isEmpty() ? WaystoneManager.getManager().getDefaultWaystoneName() : searchFilter.trim())
                                .plugin(FancyWaystones.getPlugin())
                                .itemLeft(Util.material(Objects.requireNonNull(searchItem.getString("Active.Type"))).parseItem())
                                .onClose(p -> open());
                        Util.submitSync(() -> {
                            builder.open(playerData.getPlayer());
                        });
                    }
                });
            } else if (current == Objects.requireNonNull(editMembers.getString("Layout Key")).charAt(0)) {
                if ((waystoneData.getOwnerUUID() != null && waystoneData.getOwnerUUID().equals(playerData.getUUID())) || playerData.getPlayer().hasPermission("fancywaystones.admin")) {
                    item.load(editMembers.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> {
                        MembersMenu membersMenu = new MembersMenu(playerData, waystoneData);
                        membersMenu.open();
                        waystoneData.getOpenedMenus().add(membersMenu);
                    });
                } else {
                    item.load(editMembers.getConfigurationSection("No Access"));
                }
            } else if (current == Objects.requireNonNull(orderItem.getString("Layout Key")).charAt(0)) {
                if (order == ItemOrder.ASCEND) {
                    item.load(orderItem.getConfigurationSection("Ascend Order"));
                    item.addClickListener((inv, ph) -> switchPage(category, page, sort, ItemOrder.DESCEND, searchFilter));
                } else {
                    item.load(orderItem.getConfigurationSection("Descend Order"));
                    item.addClickListener((inv, ph) -> switchPage(category, page, sort, ItemOrder.ASCEND, searchFilter));
                }
            } else if (current == Objects.requireNonNull(previousPageItem.getString("Layout Key")).charAt(0)) {
                if (page > 0) {
                    item.load(previousPageItem.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> switchPage(category, page - 1, sort, order, searchFilter));
                } else {
                    item.load(previousPageItem.getConfigurationSection("Disable"));
                }
            } else if (current == Objects.requireNonNull(nextPageItem.getString("Layout Key")).charAt(0)) {
                if (page + 1 < maxPage) {
                    item.load(nextPageItem.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> switchPage(category, page + 1, sort, order, searchFilter));
                } else {
                    item.load(nextPageItem.getConfigurationSection("Disable"));
                }
            } else if (current == Objects.requireNonNull(renameItem.getString("Layout Key")).charAt(0)) {
                if (waystoneData.getOwnerUUID() != null && waystoneData.getOwnerUUID().equals(playerData.getPlayer().getUniqueId()) ||
                        playerData.getPlayer().hasPermission("fancywaystones.admin")) {
                    item.load(renameItem.getConfigurationSection("Enable"));
                    item.addClickListener((inv, ph) -> {
                        Player player = (Player) inv.getWhoClicked();
                        AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(FancyWaystones.getPlugin())
                                .title(new Placeholder().putContent(Placeholder.PLAYER, player).replace("{language.rename-gui-title}"))
                                .onComplete((pl, name) -> {
                                    if (WaystoneManager.getManager().containsIllegalWord(name)) {
                                        pl.sendMessage(
                                                new Placeholder().putContent(Placeholder.PLAYER, pl)
                                                        .putContent(Placeholder.WAYSTONE, waystoneData)
                                                        .replace("{language.illegal-waystone-name}")
                                        );
                                        return AnvilGUI.Response.close();
                                    }
                                    WaystoneStorage storage = WaystoneManager.getManager().getStorage();
                                    String uniqueNamesContext = waystoneData.getType().getUniqueNamesContext();
                                    if (uniqueNamesContext != null) {
                                        FancyWaystones.getPlugin().submitIO(() -> {
                                            if (storage.containsName(uniqueNamesContext, name)) {
                                                pl.sendMessage(
                                                        new Placeholder().putContent(Placeholder.PLAYER, pl)
                                                                .putContent(Placeholder.WAYSTONE, waystoneData)
                                                                .replace("{language.duplicate-waystone-name}")
                                                );
                                                return;
                                            }
                                            String oldName = waystoneData.getName();
                                            if (oldName != null) {
                                                storage.removeName(uniqueNamesContext, oldName);
                                            }
                                            storage.putName(uniqueNamesContext, name);
                                            waystoneData.setName(name);
                                            WaystoneBlock waystoneBlock = waystoneData.getWaystoneBlock();
                                            if (waystoneBlock != null) {
                                                waystoneBlock.update();
                                            }
                                            open();
                                        });
                                        return AnvilGUI.Response.close();
                                    }
                                    waystoneData.setName(name);
                                    WaystoneBlock waystoneBlock = waystoneData.getWaystoneBlock();
                                    if (waystoneBlock != null) {
                                        waystoneBlock.update();
                                    }
                                    open();
                                    return AnvilGUI.Response.close();
                                })
                                .text(waystoneData.getName())
                                .plugin(FancyWaystones.getPlugin())
                                .itemLeft(Util.material(Objects.requireNonNull(FancyWaystones.getPlugin().getConfig().getString("Waystone Item.Type"))).parseItem())
                                .onClose(p -> open());
                        Util.submitSync(() -> {
                            player.closeInventory();
                            builder.open(player);
                        });
                    });
                } else {
                    item.load(renameItem.getConfigurationSection("Disable"));
                }
            } else if (current == Objects.requireNonNull(closeItem.getString("Layout Key")).charAt(0)) {
                item.load(closeItem);
                item.addClickListener((inv, ph) -> Util.submitSync(() -> inv.getWhoClicked().closeInventory()));
            } else {
                boolean found = false;
                for (WaystoneType t : WaystoneManager.getManager().getTypes()) {
                    if (categoryItem.isString(t.name()+".Layout Key") && Objects.requireNonNull(categoryItem.getString(t.name() + ".Layout Key")).charAt(0) == current) {
                        List<WaystoneData> waystones = categorized.get(t);
                        if (!waystoneData.getType().isVisible(t)) {
                            item.load(categoryItem.getConfigurationSection(t.name()+".Disabled"));
                        } else if (waystones == null || waystones.isEmpty()) {
                            item.load(categoryItem.getConfigurationSection(t.name()+".Empty"));
                        } else {
                            item.load(categoryItem.getConfigurationSection(t.name()+".Not Empty"));
                        }
                        if (category == t) {
                            item.addEnchantment("KNOCKBACK", 1);
                            item.setFlags(Arrays.stream(ItemFlag.values()).map(ItemFlag::name).collect(Collectors.toList()));
                        }
                        item.addClickListener((inv, ph) -> switchPage(t, menuData.getLastPage(t), sort, order, searchFilter));
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

    private void createWaystoneIcon(MinecraftItem item, WaystoneData next, ConfigurationSection itemSection) {
        item.getPlaceholder().putContent(Placeholder.WAYSTONE, next);
        if (itemSection == null) {
            return;
        }
        item.load(itemSection);
        item.addClickListener((inv, ph) -> {
            Util.submitSync(() -> {
                inv.getWhoClicked().closeInventory();
                FancyWaystones.getPlugin().submitIO(() -> {
                    WaystoneData waystoneData = WaystoneManager.getManager().getData(next.getUUID());
                    if (waystoneData != null) {
                        proceedTeleport(waystoneData);
                    }
                });
            });
        });
    }

    private void proceedTeleport(WaystoneData data) {
        if (data == null) return;
        Util.submitSync(() -> {
            String[] strings = data.getType().canBeVisited(playerData.getPlayer(), waystoneData, data);
            if (strings != null) {
                playerData.getPlayer().sendMessage(strings);
                return;
            }
            List<Cost> costs = data.getType().calculateCost(waystoneData.getLocation(), data);
            if (FWEvent.call(new WaystonePreTeleportEvent(waystoneData, playerData, data, costs)).isCancelled()) return;
            if (costs.isEmpty()) {
                proceedEconomyTeleport(data, null);
//            }
//            if (costs.size() == 1) {
//                proceedEconomyTeleport(data, costs.get(0));
            } else {
                PaywallMenu paywallMenu = new PaywallMenu(playerData, costs, waystoneData, data);
                paywallMenu.open();
            }
        });
    }

    private void proceedEconomyTeleport(WaystoneData data, Cost cost) {
        if (cost != null) {
            if (checkEconomy(data, cost, playerData)) return;
        }
        scheduleTeleport(data, cost, playerData, waystoneData);
    }

    static void scheduleTeleport(WaystoneData data, Cost cost, PlayerData playerData, WaystoneData waystoneData) {
        FancyWaystones.getPlugin().submit(() -> {
            WaystoneWarmUpTask waystoneWarmUpTask = new WaystoneWarmUpTask(playerData.getPlayer(), waystoneData, data) {
                @Override
                public void onDone() {
                    WaystoneTeleportEvent event = new WaystoneTeleportEvent(waystoneData, playerData, data, cost);
                    if (FWEvent.call(event).isCancelled()) return;
                    Cost costNow = event.getCost();
                    if (FancyWaystones.getPlugin().isEnabled()) {
                        Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                            if (costNow != null) {
                                if (!costNow.getService().has(playerData.getPlayer(), costNow.getAmount())) return;
                                costNow.getService().withdraw(playerData.getPlayer(), costNow.getAmount());
                            }
                            data.teleport(waystoneData, playerData.getPlayer());
                        });
                    };
                }
                @Override
                public void onCancelled() {
                }
            };
            waystoneWarmUpTask.schedule(FancyWaystones.getPlugin().getService(), 1, 1);
        });
    }

    static boolean checkEconomy(WaystoneData data, Cost cost, PlayerData playerData) {
        if (!cost.getService().has(playerData.getPlayer(), cost.getAmount())) {
            playerData.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, playerData.getPlayer())
                    .putContent(Placeholder.WAYSTONE, data).put("cost",
                            px -> cost.getService().getDisplayName(px)).replace(
                            "{language.cost-not-enough}"
                    ));
            Util.submitSync(() -> {
                playerData.getPlayer().closeInventory();
            });
            return true;
        }
        return false;
    }

}
