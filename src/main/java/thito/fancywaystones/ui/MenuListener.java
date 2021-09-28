package thito.fancywaystones.ui;

import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void drag(InventoryDragEvent e) {
        InventoryView view = e.getView();
        if (view != null) {
            Inventory top = view.getTopInventory();
            if (top != null) {
                InventoryHolder holder = top.getHolder();
                if (holder instanceof MinecraftMenu.ActiveUI) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void click(InventoryClickEvent e) {
        InventoryView view = e.getView();
        if (view != null) {
            Inventory top = view.getTopInventory();
            if (top != null) {
                InventoryHolder holder = top.getHolder();
                if (holder instanceof MinecraftMenu.ActiveUI) {
                    e.setCancelled(true);
                    int rawSlot = e.getRawSlot();
                    if (rawSlot >= 0 && rawSlot < top.getSize()) {
                        ((MinecraftMenu.ActiveUI) holder).dispatchClick(e);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void close(InventoryCloseEvent e) {
        InventoryView view = e.getView();
        if (view != null) {
            Inventory top = view.getTopInventory();
            if (top != null) {
                InventoryHolder holder = top.getHolder();
                if (holder instanceof MinecraftMenu.ActiveUI) {
                    ((MinecraftMenu.ActiveUI) holder).dispatchClose(e);
                }
            }
        }
    }
}
