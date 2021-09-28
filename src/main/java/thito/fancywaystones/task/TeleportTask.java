package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;

public abstract class TeleportTask implements Runnable {
    private Player player;
    private Location location;
    private int checkRadius, checkHeight;
    private boolean force;

    public TeleportTask(Player player, Location location, int checkRadius, int checkHeight, boolean force) {
        this.player = player;
        this.location = location;
        this.checkRadius = checkRadius;
        this.checkHeight = checkHeight;
        this.force = force;
    }

    private boolean success;

    @Override
    public void run() {
        if (FancyWaystones.getPlugin().isEnabled()) {
            ParallelSafetyCheckTask checkTask = new ParallelSafetyCheckTask(location, checkRadius, checkHeight) {
                @Override
                protected void proceed() {
                    Location safest = getSafest();
                    if (safest == null) {
                        cancelTeleportation();
                    } else {
                        confirmTeleport(safest);
                    }
                }
            };
            Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), checkTask);
        }
    }

    public boolean isSuccess() {
        return success;
    }


    private void confirmTeleport(Location safePlace) {
        player.teleport(safePlace);
        success = true;
        done();
    }

    private void cancelTeleportation() {
        FancyWaystones plugin = FancyWaystones.getPlugin();
        if (force) {
            if (plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(location);
                    success = true;
                    done();
                });
            }
        } else {
            done();
        }
    }

    protected abstract void done();
}
