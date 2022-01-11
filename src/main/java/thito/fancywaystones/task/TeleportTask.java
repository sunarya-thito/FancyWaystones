package thito.fancywaystones.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import thito.fancywaystones.FancyWaystones;
import thito.fancywaystones.IAttachedEntities;

public abstract class TeleportTask implements Runnable {
    private Player player;
    private Location location;
    private IAttachedEntities attachedEntities;
    private int checkRadius, checkHeight;
    private boolean force;

    public TeleportTask(Player player, Location location, int checkRadius, int checkHeight, boolean force, IAttachedEntities attachedEntities) {
        this.player = player;
        this.location = location;
        this.checkRadius = checkRadius;
        this.checkHeight = checkHeight;
        this.force = force;
        this.attachedEntities = attachedEntities;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
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


    protected void confirmTeleport(Location safePlace) {
        player.teleport(safePlace);
        if (attachedEntities != null) {
            attachedEntities.teleportAndRestore(safePlace);
        }
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
