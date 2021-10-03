package thito.fancywaystones.proxy;

import net.md_5.bungee.api.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.location.TeleportState;
import thito.fancywaystones.proxy.message.*;

import java.sql.Ref;
import java.sql.SQLOutput;
import java.util.*;
import java.util.logging.*;

public class ProxyWaystoneBridge {
    private List<ProxyServer> proxyServerList = new ArrayList<>();
    private ProxyHandler handler;

    public ProxyWaystoneBridge(ProxyHandler handler) {
        this.handler = handler;
    }

    public Section saveConfig() {
        Section section = new MapSection();
        for (ProxyServer server : proxyServerList) {
            section.set(server.getAlias(), server.getName());
        }
        return section;
    }

    public void loadConfig(Section section) {
        proxyServerList.clear();
        if (section instanceof MapSection) {
            ((MapSection) section).forEach((key, value) -> {
                if (value instanceof String) {
                    ProxyServer proxyServer = handler.createProxyServer((String) value, key);
                    if (proxyServer != null) {
                        proxyServerList.add(proxyServer);
                    }
                }
            });
        }
    }

    public List<ProxyServer> getProxyServerList() {
        return proxyServerList;
    }

    public ProxyServer getServerByAlias(String alias) {
        return proxyServerList.stream().filter(x -> x.getAlias().equals(alias)).findAny().orElse(null);
    }

    public ProxyServer getServerByName(String name) {
        return proxyServerList.stream().filter(x -> x.getName().equals(name)).findAny().orElse(null);
    }

    public void dispatchPluginMessage(String channel, byte[] data, ProxyPlayer player) {
        if (!channel.equals("fancywaystones:waystone")) return;
        Message message = Message.read(data);
        if (message instanceof ServerIntroductionMessage) {
            for (ProxyServer server : proxyServerList) {
                if (server.getAlias().equals(((ServerIntroductionMessage) message).getServerName())) {
                    return;
                }
            }
            proxyServerList.add(handler.createProxyServer(player, ((ServerIntroductionMessage) message).getServerName()));
            handler.getLogger().log(Level.INFO, "New Server: " + ((ServerIntroductionMessage) message).getServerName() + " = " + player.getServerName());
        } else if (message instanceof RequestInfoMessage) {
            player.sendMessage(ChatColor.GRAY + "Server List");
            for (ProxyServer server : proxyServerList) {
                player.sendMessage(ChatColor.YELLOW + server.getName() + ChatColor.GRAY + " - " + ChatColor.WHITE + server.getAlias());
            }
        } else if (message instanceof RefundTeleportationMessage) {
            String sourceServerName = ((RefundTeleportationMessage) message).getSource().getServerName();
            ProxyServer sourceServer = getServerByAlias(sourceServerName);
            if (sourceServer != null) {
                player.connect(sourceServer, (result, error) -> {
                    if (result != null) {
                        sourceServer.sendData("fancywaystones:waystone", data, false);
                    }
                });
            }
        } else if (message instanceof TeleportMessage) {
            String targetServerName = ((TeleportMessage) message).getTarget().getServerName();
            ProxyServer targetServer = getServerByAlias(targetServerName);
            SerializableLocation source = ((TeleportMessage) message).getSource();
            if (targetServer != null) {
                player.connect(targetServer, (result, error) -> {
                    if (result) {
                        targetServer.sendData("fancywaystones:waystone", data, false);
                    } else {
                        sendRefund((TeleportMessage) message, source, TeleportState.INVALID);
                    }
                });
            } else {
                sendRefund((TeleportMessage) message, source, TeleportState.INVALID);
            }
        } else if (message instanceof WaystoneLoadRequestMessage) {
            String sourceServerName = player.getServerName();
            for (ProxyServer server : getProxyServerList()) {
                if (!server.getName().equals(sourceServerName)) {
                    if (server.getPlayerCount() <= 0) {
                        server.addRequest((WaystoneLoadRequestMessage) message);
                    } else {
                        server.sendData("fancywaystones:waystone", data, false);
                    }
                }
            }
        } else if (message instanceof WaystoneReloadRequestMessage) {
            String sourceServerName = player.getServerName();
            for (ProxyServer server : getProxyServerList()) {
                if (!server.getName().equals(sourceServerName)) {
                    if (server.getPlayerCount() <= 0) {
                        server.addRequest((WaystoneReloadRequestMessage) message);
                    } else {
                        server.sendData("fancywaystones:waystone", data, false);
                    }
                }
            }
        } else if (message instanceof WaystoneUnloadRequestMessage) {
            String sourceServerName = player.getServerName();
            for (ProxyServer server : getProxyServerList()) {
                if (!server.getName().equals(sourceServerName)) {
                    if (server.getPlayerCount() <= 0) {
                        server.removeRequest((WaystoneUnloadRequestMessage) message);
                    } else {
                        server.sendData("fancywaystones:waystone", data, false);
                    }
                }
            }
        } else {
            for (ProxyServer server : proxyServerList) {
                if (server.getName().equals(player.getServerName())) continue;
                server.sendData("fancywaystones:waystone", data, false);
            }
        }
    }

    private void sendRefund(TeleportMessage message, SerializableLocation source, TeleportState state) {
        if (message.isSendFeedback() && source != null) {
            ProxyServer sourceServer = getServerByAlias(source.getServerName());
            if (sourceServer != null) {
                sourceServer.sendData("fancywaystones:waystone",
                        new RefundTeleportationMessage(
                                message.getSource(),
                                message.getTarget(),
                                message.getPlayerUUID(),
                                state
                        ).write(), false);
            }
        }
    }
}
