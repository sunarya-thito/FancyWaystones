import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class TaskTest {
    public static void main(String[] args) throws Throwable {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ReentrantLock reentrantLock = new ReentrantLock();
        executorService.submit(() -> {
            reentrantLock.lock();
            sleep(1000);
            System.out.println("Test");
            reentrantLock.unlock();
        });
        executorService.submit(() -> {
            reentrantLock.lock();
            System.out.println("Test2");
            reentrantLock.unlock();
        });
    }

    static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
