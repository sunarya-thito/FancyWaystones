package thito.fancywaystones.proxy.bungeecord;

import net.md_5.bungee.api.config.*;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.event.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.proxy.message.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class FancyWaystonesBungeeCord extends Plugin implements Listener {

    private Map<String, ServerInfo> serverMap = new HashMap<>();

    @Override
    public void onEnable() {
        File target = new File(getDataFolder(), "config.yml");
        if (target.exists()) {
            try (FileReader reader = new FileReader(target)) {
                MapSection section = Section.parseToMap(reader);
                section.forEach((key, value) -> {
                    ServerInfo serverInfo = getProxy().getServerInfo(String.valueOf(value));
                    if (serverInfo != null) {
                        getLogger().log(Level.INFO, "Loaded "+serverInfo.getName()+" as "+key);
                        serverMap.put(key, serverInfo);
                    }
                });
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
        Section section = new MapSection();
        serverMap.forEach((key, value) -> section.set(key, value.getName()));
        try {
            Files.write(target.toPath(), Section.toString(section).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save config.yml");
        }
    }

    @EventHandler
    public void handle(PluginMessageEvent event) {
        if ("fancywaystones:waystone".equals(event.getTag())) {
            Message message = Message.read(event.getData());
            if (message instanceof ServerIntroductionMessage) {
                if (event.getSender() instanceof Server) {
                    if (serverMap.put(((ServerIntroductionMessage) message).getServerName(), ((Server) event.getSender()).getInfo()) == null) {
                        getLogger().log(Level.INFO, "New Server: "+((ServerIntroductionMessage) message).getServerName()+" = "+((Server) event.getSender()).getInfo().getName());
                    }
                }
            } else if (message instanceof TeleportMessage) {
                String targetServerName = ((TeleportMessage) message).getTarget().getServerName();
                ServerInfo targetServer = serverMap.get(targetServerName);
                SerializableLocation source = ((TeleportMessage) message).getSource();
                ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();
                if (targetServer != null) {
                    receiver.connect(targetServer, (result, error) -> {
                        if (result) {
                            getProxy().getScheduler().schedule(this, () -> {
                                System.out.println("SENDING DATA TO "+receiver.getServer().getInfo().getName());
                                receiver.getServer().getInfo().sendData("fancywaystones:waystone", event.getData(), false);
                            }, 500, TimeUnit.MILLISECONDS);
                        } else {
                            if (((TeleportMessage) message).isSendFeedback() && source != null) {
                                receiver.sendData("fancywaystones:waystone",
                                        new TeleportMessage(((TeleportMessage) message).getPlayerUUID(), false, null, ((TeleportMessage) message).getSource()).write());
                            }
                        }
                    }, ServerConnectEvent.Reason.PLUGIN);
                } else {
                    if (((TeleportMessage) message).isSendFeedback() && source != null) {
                        receiver.sendData("fancywaystones:waystone",
                                new TeleportMessage(((TeleportMessage) message).getPlayerUUID(), false, null, ((TeleportMessage) message).getSource()).write());
                    }
                }
            } else {
                if (event.getSender() instanceof Server) {
                    ServerInfo source = ((Server) event.getSender()).getInfo();
                    for (ServerInfo server : getProxy().getServers().values()) {
                        if (source == server) continue;
                        // forward message
                        server.sendData("fancywaystones:waystone", event.getData(), false);
                    }
                }
            }
        }
    }

}
