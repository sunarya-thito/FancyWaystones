package thito.fancywaystones.proxy.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import org.slf4j.Logger;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.config.Section;
import thito.fancywaystones.proxy.ProxyHandler;
import thito.fancywaystones.proxy.ProxyPlayer;
import thito.fancywaystones.proxy.ProxyWaystoneBridge;
import thito.fancywaystones.proxy.message.WaystoneUpdateMessage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "fancywaystones", name = "FancyWaystones", version="1.0", url = "https://www.spigotmc.org/resources/94376/",
    description = "Teleport fairly using waystones", authors = { "Septogeddon" })
public class FancyWaystonesVelocity implements ProxyHandler {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ProxyWaystoneBridge bridge;

    @Inject
    public FancyWaystonesVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("FancyWaystones has been initialized!");
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(sv -> {
            thito.fancywaystones.proxy.ProxyServer server = bridge.getServerByName(sv.getServerInfo().getName());
            if (server != null) {
                WaystoneUpdateMessage message = server.flushRequest();
                sv.sendPluginMessage(new LegacyChannelIdentifier("fancywaystones:waystone"), message.write());
            }
        });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        File dataFolder = dataDirectory.toFile();
        File target = new File(dataFolder, "config.yml");
        dataFolder.mkdirs();
        try {
            Files.write(target.toPath(), Section.toString(bridge.saveConfig()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Failed to save config.yml", e);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(new LegacyChannelIdentifier("fancywaystones:waystone"));
        bridge = new ProxyWaystoneBridge(this);
        File target = new File(dataDirectory.toFile(), "config.yml");
        if (target.exists()) {
            try (FileReader reader = new FileReader(target)) {
                MapSection section = Section.parseToMap(reader);
                bridge.loadConfig(section);
            } catch (IOException e) {
                logger.error("Failed to load config.yml", e);
            }
        }
        logger.info("FancyWaystones has been enabled");
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (event.getTarget() instanceof Player && event.getSource() instanceof ServerConnection) {
            bridge.dispatchPluginMessage(event.getIdentifier().getId(), event.getData(), new VelocityPlayer((Player) event.getTarget()));
        }
    }

    @Override
    public thito.fancywaystones.proxy.ProxyServer createProxyServer(ProxyPlayer player, String alias) {
        return ((VelocityPlayer) player).getPlayer().getCurrentServer().map(x -> new VelocityServer(alias, x.getServer())).orElse(null);
    }

    @Override
    public thito.fancywaystones.proxy.ProxyServer createProxyServer(String name, String alias) {
        return server.getServer(name).map(x -> new VelocityServer(alias, x)).orElse(null);
    }

}
