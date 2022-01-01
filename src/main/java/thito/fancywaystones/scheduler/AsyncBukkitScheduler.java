package thito.fancywaystones.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import thito.fancywaystones.FancyWaystones;

import java.util.concurrent.TimeUnit;

public class AsyncBukkitScheduler implements Scheduler {
    @Override
    public void submit(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), r);
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(FancyWaystones.getPlugin(), r, tickDelay);
        return new Scheduled() {
            @Override
            public void cancel() {
                bukkitTask.cancel();
            }
        };
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay, long tickInterval) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(FancyWaystones.getPlugin(), r, tickDelay, tickInterval);
        return new Scheduled() {
            @Override
            public void cancel() {
                bukkitTask.cancel();
            }
        };
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
