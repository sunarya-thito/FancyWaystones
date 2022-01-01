package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.effect.*;
import thito.fancywaystones.effect.Effect;
import thito.fancywaystones.scheduler.Scheduler;

import java.util.*;
import java.util.stream.*;

public abstract class WarmUpTask extends Task {
    public final static Map<Player, WarmUpTask> TASKS = Collections.synchronizedMap(new HashMap<>());
    private Player player;
    private Location location;
    private WaystoneData targetWaystone;
    private List<EffectHandler> effects;
    private int tick, targetTick;

    protected String cancelledTitle, cancelledSubtitle, title, subtitle;

    public WarmUpTask(Player player, WaystoneData sourceWaystone, WaystoneData targetWaystone, List<Effect> effects, int targetTick) {
        this.player = player;
        this.location = player.getLocation();
        this.targetWaystone = targetWaystone;
        this.effects = effects.stream().map(x -> x.createHandler(player, sourceWaystone, targetWaystone)).collect(Collectors.toList());
        this.targetTick = targetTick;
    }

    @Override
    public void schedule(Scheduler service, long delay, long interval) {
        super.schedule(service, delay, interval);
        TASKS.put(player, this);
    }

    public abstract void onDone();
    public abstract void onCancelled();

    @Override
    public void run() {
        if (!player.isOnline()) {
            // already cancelled in the listener
            return;
        }
        if (tick >= targetTick) {
            cancel();
            onDone();
            return;
        }
        if (isMoving()) {
            player.sendMessage(new Placeholder()
                    .putContent(Placeholder.PLAYER, player)
                    .putContent(Placeholder.WAYSTONE, targetWaystone)
                    .replace("{language.teleport-cancelled-moved}"));
            Placeholder placeholder = new Placeholder().putContent(Placeholder.PLAYER, player)
                    .putContent(Placeholder.WAYSTONE, targetWaystone)
                    .put("countdown", ph -> (targetTick - tick) / 20 + 1);
            Util.sendTitle(player, placeholder.replace(cancelledTitle), placeholder.replace(cancelledSubtitle), 0, 35, 20);
            cancel();
            onCancelled();
            return;
        }
        for (int i = 0; i < effects.size(); i++) {
            try {
                effects.get(i).tick(tick);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (title != null || subtitle != null) {
            Placeholder placeholder = new Placeholder().putContent(Placeholder.PLAYER, player)
                    .putContent(Placeholder.WAYSTONE, targetWaystone)
                    .put("countdown", ph -> (targetTick - tick) / 20 + 1);
            Util.sendTitle(player, placeholder.replace(title), placeholder.replace(subtitle), 0, 35, 20);
        }
        tick++;
    }

    public boolean isMoving() {
        Location other = player.getLocation();
        return other.getWorld() != location.getWorld() || location.getBlockX() != other.getBlockX() || location.getBlockY() != other.getBlockY() || location.getBlockZ() != other.getBlockZ();
    }

    @Override
    public void cancel() {
        super.cancel();
        TASKS.remove(player);
    }
}
