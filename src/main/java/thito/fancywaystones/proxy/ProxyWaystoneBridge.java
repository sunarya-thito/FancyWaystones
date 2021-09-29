package thito.fancywaystones.proxy;

import net.md_5.bungee.api.config.*;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.event.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.proxy.message.*;

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
            handler.getLogger().log(Level.INFO, "New Server: "+((ServerIntroductionMessage) message).getServerName()+" = "+player.getServerName());
        } else if (message instanceof TeleportMessage) {
            String targetServerName = ((TeleportMessage) message).getTarget().getServerName();
            ProxyServer targetServer = getServerByAlias(targetServerName);
            SerializableLocation source = ((TeleportMessage) message).getSource();
            if (targetServer != null) {
                player.connect(targetServer, (result, error) -> {
                    if (result) {
                        handler.runLater(() -> {
                            targetServer.sendData("fancywaystones:waystone", data, false);
                        });
                    } else {
                        if (((TeleportMessage) message).isSendFeedback() && source != null) {
                            player.sendData("fancywaystones:waystone",
                                    new TeleportMessage(((TeleportMessage) message).getPlayerUUID(), false, null, ((TeleportMessage) message).getSource()).write());
                        }
                    }
                });
            } else {
                if (((TeleportMessage) message).isSendFeedback() && source != null) {
                    player.sendData("fancywaystones:waystone",
                            new TeleportMessage(((TeleportMessage) message).getPlayerUUID(), false, null, ((TeleportMessage) message).getSource()).write());
                }
            }
        } else if (message instanceof WaystoneLoadRequestMessage) {
            ProxyServer server = getServerByName(player.getServerName());
            if (server != null) {
                if (server.getPlayerCount() <= 0) {
                    server.addRequest((WaystoneLoadRequestMessage) message);
                } else {
                    server.sendData("fancywaystones:waystone", message.write(), false);
                }
            }
        } else if (message instanceof WaystoneUnloadRequestMessage) {
            ProxyServer server = getServerByName(player.getServerName());
            if (server != null) {
                if (server.getPlayerCount() <= 0) {
                    server.removeRequest((WaystoneUnloadRequestMessage) message);
                } else {
                    server.sendData("fancywaystones:waystone", message.write(), false);
                }
            }
        } else {
            for (ProxyServer server : proxyServerList) {
                if (server.getName().equals(player.getServerName())) continue;
                server.sendData("fancywaystones:waystone", data, false);
            }
        }
    }
}
