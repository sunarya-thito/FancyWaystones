package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import thito.fancywaystones.*;
import thito.fancywaystones.location.*;

import java.util.*;
import java.util.logging.*;

public class WaystoneInactivityCheckTask implements Runnable {

    private boolean run;
    public void start() {
        if (run) return;
        run = true;
        Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), this);
    }

    public void stop() {
        run = false;
    }

    @Override
    public void run() {
        if (!run) return;
        if (FancyWaystones.getPlugin().isEnabled() && FancyWaystones.getPlugin().getConfig().getBoolean("Waystone Inactivity.Enable")) {
            try {
                List<WaystoneData> waystoneSnapshots = WaystoneManager.getManager().getLoadedData();
                synchronized (WaystoneManager.getManager().getLoadedData()) {
                    for (WaystoneData snapshot : waystoneSnapshots) {
                        if (!snapshot.getType().shouldPurgeInactive()) continue;
                        validateInactivity(snapshot);
                    }
                }
            } catch (Throwable t) {
                FancyWaystones.getPlugin().getLogger()
                        .log(Level.WARNING, "Failed to check for Waystone Inactivity", t);
            }
        }
        if (FancyWaystones.getPlugin().isEnabled()) {
            // submit the task later
            Bukkit.getScheduler().runTaskLaterAsynchronously(FancyWaystones.getPlugin(), this, Util.parseTime(FancyWaystones.getPlugin().getConfig().getString("Waystone Inactivity.Check Interval")));
        }
    }

    private void validateInactivity(WaystoneData waystoneData) {
        boolean inactive = WaystoneManager.getManager().isInactive(waystoneData);
        if (inactive) {
            if (FancyWaystones.getPlugin().isEnabled()) {
                WaystoneLocation loc = waystoneData.getLocation();
                if (loc instanceof LocalLocation) {
                    Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), () -> {
                        Location location = ((LocalLocation) loc).getLocation();
                        waystoneData.destroy(Language.getLanguage().get("waystone-inactivity-reason"));
                        if (FancyWaystones.getPlugin().isEnabled()) {
                            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                                if (waystoneData.getType().shouldDropPurge(waystoneData)) {
                                    ItemStack item = WaystoneManager.getManager().createWaystoneItem(waystoneData, true);
                                    location.getWorld().dropItemNaturally(location, item);
                                }
                            });
                        }
                    });
                }
            }
        }
    }
}
