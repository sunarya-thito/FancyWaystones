package thito.fancywaystones.scheduler;

import java.util.concurrent.TimeUnit;

public interface Scheduler {
    default void lock() {}
    default void unlock() {}
    void submit(Runnable r);
    Scheduled submit(Runnable r, long tickDelay);
    Scheduled submit(Runnable r, long tickDelay, long tickInterval);
    void shutdown();
    boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException;
}
