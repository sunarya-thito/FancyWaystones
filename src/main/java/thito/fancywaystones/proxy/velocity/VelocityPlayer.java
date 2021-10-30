package thito.fancywaystones.proxy.velocity;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import thito.fancywaystones.proxy.ProxyPlayer;
import thito.fancywaystones.proxy.ProxyServer;

import java.util.function.BiConsumer;

public class VelocityPlayer implements ProxyPlayer {
    private Player player;

    public VelocityPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String getServerName() {
        return player.getCurrentServer().map(x -> x.getServerInfo().getName()).orElse("");
    }

    @Override
    public void sendMessage(String text) {
        player.sendMessage(Component.text(text));
    }

    @Override
    public void connect(ProxyServer server, BiConsumer<Boolean, Throwable> callback) {
        if (server instanceof VelocityServer) {
            player.createConnectionRequest(((VelocityServer) server).getServer()).connect().thenAccept(result -> {
                callback.accept(result.isSuccessful(), null);
            });
        }
    }
}
