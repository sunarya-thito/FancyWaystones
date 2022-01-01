package thito.fancywaystones;

import thito.fancywaystones.scheduler.Scheduled;
import thito.fancywaystones.scheduler.Scheduler;

import java.util.concurrent.*;

public abstract class Task implements Runnable {
    private Scheduled scheduledFuture;
    public void schedule(Scheduler service, long delay, long interval) {
        cancel();
        scheduledFuture = service.submit(() -> {
            try {
                run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delay, interval);
    }

    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel();
            scheduledFuture = null;
        }
    }

    public boolean isScheduled() {
        return scheduledFuture != null;
    }

}
