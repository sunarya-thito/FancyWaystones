package thito.fancywaystones.proxy.bungeecord;

import net.md_5.bungee.api.chat.*;
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
    public void sendMessage(String text) {
        player.sendMessage(TextComponent.fromLegacyText(text));
    }

    @Override
    public void connect(ProxyServer server, BiConsumer<Boolean, Throwable> callback) {
        if (server instanceof BungeeCordServer) {
            player.connect(((BungeeCordServer) server).getInfo(), (r, e) -> {
                if (r) {
                    FancyWaystonesBungeeCord.getInstance().getCallbackMap().put(player, callback);
                } else {
                    callback.accept(false, e);
                }
            });
        } else throw new IllegalArgumentException("server must be a BungeeCordServer");
    }

//    @Override
//    public void sendData(String channel, byte[] data) {
//        player.sendData(channel, data);
//    }
}
