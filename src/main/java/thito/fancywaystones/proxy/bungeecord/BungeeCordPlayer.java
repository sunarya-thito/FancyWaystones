package thito.fancywaystones.proxy.bungeecord;

import net.md_5.bungee.api.connection.*;
import thito.fancywaystones.proxy.*;

import java.util.function.*;

public class BungeeCordPlayer implements ProxyPlayer {
    private ProxiedPlayer player;

    public BungeeCordPlayer(ProxiedPlayer player) {
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public String getServerName() {
        return player.getServer().getInfo().getName();
    }

    @Override
    public void connect(ProxyServer server, BiConsumer<Boolean, Throwable> callback) {
        if (server instanceof BungeeCordServer) {
            player.connect(((BungeeCordServer) server).getInfo(), callback::accept);
        } else throw new IllegalArgumentException("server must be a BungeeCordServer");
    }

    @Override
    public void sendData(String channel, byte[] data) {
        player.sendData(channel, data);
    }
}
