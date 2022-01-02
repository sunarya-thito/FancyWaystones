import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class TaskTest {
    public static void main(String[] args) throws Throwable {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("TEST", "自然生成的传送锚点");
        configuration.loadFromString(configuration.saveToString());
        System.out.println(new String(configuration.saveToString().getBytes(StandardCharsets.UTF_8)));
    }

    static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
