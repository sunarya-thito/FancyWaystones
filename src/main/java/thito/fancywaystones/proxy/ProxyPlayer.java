package thito.fancywaystones.proxy;

import java.util.function.*;

public interface ProxyPlayer {
    String getServerName();
    void sendMessage(String text);
    void connect(ProxyServer server, BiConsumer<Boolean, Throwable> callback);
//    void sendData(String channel, byte[] data);
}
