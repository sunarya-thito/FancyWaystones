package thito.fancywaystones.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import thito.fancywaystones.FancyWaystones;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BukkitScheduler implements Scheduler {
    private final ReentrantLock lock = new ReentrantLock();

    public BukkitScheduler(String threadName) {
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public void submit(Runnable r) {
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(FancyWaystones.getPlugin(), () -> {
                lock.lock();
                try {
                    r.run();
                } finally {
                    lock.unlock();
                }
            });
        }
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay) {
        if (FancyWaystones.getPlugin().isEnabled()) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(FancyWaystones.getPlugin(), () -> {
                lock.lock();
                try {
                    r.run();
                } finally {
                    lock.unlock();
                }
            }, tickDelay);
            return bukkitTask::cancel;
        }
        return () -> {};
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay, long tickInterval) {
        if (FancyWaystones.getPlugin().isEnabled()) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(FancyWaystones.getPlugin(), () -> {
                lock.lock();
                try {
                    r.run();
                } finally {
                    lock.unlock();
                }
            }, tickDelay, tickInterval);
            return bukkitTask::cancel;
        }
        return () -> {};
    }
}
