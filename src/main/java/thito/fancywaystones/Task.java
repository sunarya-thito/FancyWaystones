package thito.fancywaystones;

import java.util.concurrent.*;

public abstract class Task implements Runnable {
    private ScheduledFuture<?> scheduledFuture;
    public void schedule(ScheduledExecutorService service, long delay, long interval) {
        cancel();
        scheduledFuture = service.scheduleAtFixedRate(this, delay * 50, interval * 50, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    public boolean isScheduled() {
        return scheduledFuture != null;
    }

}
