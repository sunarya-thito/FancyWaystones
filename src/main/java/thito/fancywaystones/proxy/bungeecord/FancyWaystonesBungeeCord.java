package thito.fancywaystones.proxy.bungeecord;

import net.md_5.bungee.api.config.*;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.event.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.proxy.*;
import thito.fancywaystones.proxy.message.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class FancyWaystonesBungeeCord extends Plugin implements Listener, ProxyHandler {

    private Map<String, ServerInfo> serverMap = new HashMap<>();
    private ProxyWaystoneBridge bridge;

    @Override
    public void onEnable() {
        bridge = new ProxyWaystoneBridge(this);
        File target = new File(getDataFolder(), "config.yml");
        if (target.exists()) {
            try (FileReader reader = new FileReader(target)) {
                MapSection section = Section.parseToMap(reader);
                bridge.loadConfig(section);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to load config.yml", e);
            }
        }
        getLogger().log(Level.INFO, "Registering listener...");
        getProxy().registerChannel("fancywaystones:waystone");
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().log(Level.INFO, "Done!");
    }

    @Override
    public void onDisable() {
        File target = new File(getDataFolder(), "config.yml");
        getDataFolder().mkdirs();
        try {
            Files.write(target.toPath(), Section.toString(bridge.saveConfig()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save config.yml");
        }
    }

    @Override
    public ProxyServer createProxyServer(ProxyPlayer player, String alias) {
        if (player instanceof BungeeCordPlayer) {
            return new BungeeCordServer(((BungeeCordPlayer) player).getPlayer().getServer().getInfo(), alias);
        }
        return null;
    }

    @Override
    public ProxyServer createProxyServer(String name, String alias) {
        ServerInfo info = getProxy().getServerInfo(name);
        if (info == null) return null;
        return new BungeeCordServer(info, alias);
    }

    @Override
    public void runLater(Runnable r) {
        getProxy().getScheduler().schedule(this, r, 1, TimeUnit.MILLISECONDS);
    }

    @EventHandler
    public void handle(ServerSwitchEvent event) {
        ProxyServer server = bridge.getServerByName(event.getPlayer().getServer().getInfo().getName());
        if (server != null) {
            WaystoneUpdateMessage message = server.flushRequest();
            event.getPlayer().getServer().getInfo().sendData("fancywaystones:waystone", message.write(), false);
        }
    }

    @EventHandler
    public void handle(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer) {
            bridge.dispatchPluginMessage(event.getTag(), event.getData(), new BungeeCordPlayer((ProxiedPlayer) event.getReceiver()));
        }
    }

}
