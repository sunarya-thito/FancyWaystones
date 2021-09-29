package thito.fancywaystones.proxy;

import thito.fancywaystones.proxy.message.*;

import java.util.*;

public abstract class ProxyServer {
    private final String alias;
    private Set<UUID> loadRequest = new HashSet<>();
    private Set<UUID> unloadRequest = new HashSet<>();

    public ProxyServer(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void addRequest(WaystoneLoadRequestMessage message) {
        loadRequest.add(message.getId());
        unloadRequest.remove(message.getId());
    }

    public void removeRequest(WaystoneUnloadRequestMessage message) {
        loadRequest.remove(message.getId());
        unloadRequest.add(message.getId());
    }

    public WaystoneUpdateMessage flushRequest() {
        WaystoneUpdateMessage message = new WaystoneUpdateMessage(loadRequest, unloadRequest);
        loadRequest = new HashSet<>();
        unloadRequest = new HashSet<>();
        return message;
    }

    public abstract int getPlayerCount();
    public abstract String getName();
    public abstract void sendData(String channel, byte[] message, boolean queue);
}
