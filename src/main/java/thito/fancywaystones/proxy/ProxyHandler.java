package thito.fancywaystones.proxy;

public interface ProxyHandler {
    ProxyServer createProxyServer(ProxyPlayer player, String alias);
    ProxyServer createProxyServer(String name, String alias);
}
