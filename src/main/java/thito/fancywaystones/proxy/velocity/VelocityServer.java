package thito.fancywaystones.proxy.velocity;

import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import thito.fancywaystones.proxy.ProxyServer;

public class VelocityServer extends ProxyServer {
    private RegisteredServer server;
    public VelocityServer(String alias, RegisteredServer server) {
        super(alias);
        this.server = server;
    }

    public RegisteredServer getServer() {
        return server;
    }

    @Override
    public int getPlayerCount() {
        return server.getPlayersConnected().size();
    }

    @Override
    public String getName() {
        return server.getServerInfo().getName();
    }

    @Override
    public void sendData(String channel, byte[] message, boolean queue) {
        if (!queue) {
            if (getPlayerCount() > 0) server.sendPluginMessage(new LegacyChannelIdentifier(channel), message);
        } else {
            server.sendPluginMessage(new LegacyChannelIdentifier(channel), message);
        }
    }
}
