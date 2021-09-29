package thito.fancywaystones.proxy.bungeecord;

import net.md_5.bungee.api.config.*;
import thito.fancywaystones.proxy.*;

public class BungeeCordServer extends ProxyServer {
    private ServerInfo info;

    public BungeeCordServer(ServerInfo info, String alias) {
        super(alias);
        this.info = info;
    }

    public ServerInfo getInfo() {
        return info;
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public int getPlayerCount() {
        return info.getPlayers().size();
    }

    @Override
    public void sendData(String channel, byte[] message, boolean queue) {
        info.sendData(channel, message, queue);
    }
}
