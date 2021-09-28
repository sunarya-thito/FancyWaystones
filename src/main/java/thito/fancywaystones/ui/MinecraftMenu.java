package thito.fancywaystones.ui;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class MinecraftMenu {
    private String title;
    private int rows = 6;
    private MinecraftItem[] items = new MinecraftItem[54];
    private Set<ActiveUI> activeUISet = new HashSet<>();
    private Placeholder placeholder = new Placeholder();
    private boolean autoUpdate;
    private boolean alwaysUpdate;
    private Task task;
    public MinecraftMenu(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        for (int i = 0; i < items.length; i++) {
            items[i] = new MinecraftItem();
            if (autoUpdate) {
                items[i].addUpdateListener(getFunctionConsumer(i));
            }
        }
        task = new Task() {
            @Override
            public void run() {
                for (int i = 0; i < items.length; i++) {
                    for (ActiveUI activeUI : new ArrayList<>(activeUISet)) {
                        if (i < activeUI.inventory.getSize()) {
                            ItemStack itemStack = items[i].getItemStack(activeUI.placeholder);
                            activeUI.inventory.setItem(i, itemStack);
                        }
                    }
                }
            }
        };
    }

    public void setAlwaysUpdate(boolean alwaysUpdate) {
        this.alwaysUpdate = alwaysUpdate;
    }

    private Consumer<Function<Placeholder, ItemStack>> cached;
    private Consumer<Function<Placeholder, ItemStack>> getFunctionConsumer(int index) {
        return cached != null ? cached : (cached = function -> {
            for (ActiveUI activeUI : new ArrayList<>(activeUISet)) {
                if (index < activeUI.inventory.getSize()) {
                    ItemStack itemStack = function.apply(activeUI.placeholder);
                    activeUI.inventory.setItem(index, itemStack);
                }
            }
        });
    }

    public List<Player> getViewers() {
        return activeUISet.stream().map(ActiveUI::getViewer).collect(Collectors.toList());
    }

    public MinecraftItem getItem(int index) {
        return items[index];
    }

    public int getRows() {
        return rows;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (autoUpdate) update();
    }

    public void setRows(int rows) {
        this.rows = rows;
        if (autoUpdate) update();
    }

    public void update() {
        for (ActiveUI ui : new ArrayList<>(activeUISet)) {
            ui.updateWindow();
        }
    }

    public Placeholder getPlaceholder() {
        return placeholder;
    }

    public void open(Player player) {
        ActiveUI active = new ActiveUI(player, placeholder);
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                player.openInventory(active.inventory);
                activeUISet.add(active);
                check();
            });
        }
    }

    private void check() {
        if (activeUISet.isEmpty()) {
            task.cancel();
        } else {
            if (!task.isScheduled() && alwaysUpdate) {
                task.schedule(FancyWaystones.getPlugin().getService(), 20L, 20L);
            }
        }
    }

    protected void patchItem(Inventory inventory, int index, MinecraftItem item, Placeholder placeholder) {
        inventory.setItem(index, item.getItemStack(placeholder));
    }

    static int[] limit = {32, 48, -1};
    static int cachedLimit = -1;
    public class ActiveUI implements InventoryHolder {
        private Player viewer;
        private Inventory inventory;
        private Placeholder placeholder = new Placeholder();

        public ActiveUI(Player viewer, Placeholder placeholder) {
            this.placeholder.putContent(Placeholder.VIEWER, viewer);
            this.placeholder.combine(placeholder);
            this.viewer = viewer;
            if (cachedLimit >= 0) {
                int l = limit[cachedLimit];
                String t = placeholder.replace(title);
                if (l >= 0) {
                    if (t.length() >= l) {
                        t = t.substring(0, l);
                        if (t.charAt(l - 1) == ChatColor.COLOR_CHAR) {
                            t = t.substring(0, l - 1);
                        }
                    }
                }
                try {
                    inventory = Bukkit.createInventory(this, getRows() * 9, t);
                } catch (Throwable ignored) {
                }
            } else {
                for (int i = limit.length - 1; i >= 0; i--) {
                    int l = limit[i];
                    String t = placeholder.replace(title);
                    if (l >= 0) {
                        if (t.length() >= l) {
                            t = t.substring(0, l);
                            if (t.charAt(l - 1) == ChatColor.COLOR_CHAR) {
                                t = t.substring(0, l - 1);
                            }
                        }
                    }
                    try {
                        inventory = Bukkit.createInventory(this, getRows() * 9, t);
                        cachedLimit = i;
                        break;
                    } catch (Throwable ignored) {
                    }
                }
            }
            if (inventory == null) {
                inventory = Bukkit.createInventory(this, getRows() * 9, "");
            }
            for (int i = 0; i < inventory.getSize(); i++) {
                patchItem(inventory, i, items[i], placeholder);
            }
        }

        public void dispatchClose(InventoryCloseEvent e) {
            if (FancyWaystones.getPlugin().isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                    activeUISet.remove(this);
                    check();
                });
            }
        }

        public void dispatchClick(InventoryClickEvent e) {
            int slot = e.getRawSlot();
            items[slot].dispatchClick(e, placeholder);
        }

        public void updateWindow() {
            if (FancyWaystones.getPlugin().isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                    open(viewer);
                });
            }
        }

        public Player getViewer() {
            return viewer;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
