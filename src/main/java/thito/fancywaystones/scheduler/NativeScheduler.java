package thito.fancywaystones.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NativeScheduler implements Scheduler {
    private ScheduledExecutorService service;

    public NativeScheduler(String threadName) {
        this.service = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, threadName));
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return service.awaitTermination(time, unit);
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }

    @Override
    public void submit(Runnable r) {
        service.submit(r);
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay) {
        if (service.isShutdown()) return () -> {};
        ScheduledFuture<?> schedule = service.schedule(r, tickDelay * 50, TimeUnit.MILLISECONDS);
        return () -> schedule.cancel(false);
    }

    @Override
    public Scheduled submit(Runnable r, long tickDelay, long tickInterval) {
        if (service.isShutdown()) return () -> {};
        ScheduledFuture<?> scheduledFuture = service.scheduleWithFixedDelay(r, tickDelay * 50L, tickInterval * 50L, TimeUnit.MILLISECONDS);
        return () -> scheduledFuture.cancel(false);
    }
}
