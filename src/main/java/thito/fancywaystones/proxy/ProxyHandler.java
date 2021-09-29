package thito.fancywaystones.proxy;

import java.util.logging.*;

public interface ProxyHandler {
    ProxyServer createProxyServer(ProxyPlayer player, String alias);
    ProxyServer createProxyServer(String name, String alias);
    void runLater(Runnable r);
    Logger getLogger();
}
