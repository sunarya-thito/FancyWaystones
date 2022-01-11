package thito.fancywaystones.proxy;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.messaging.*;
import thito.fancywaystones.*;
import thito.fancywaystones.economy.Cost;
import thito.fancywaystones.economy.EconomyService;
import thito.fancywaystones.location.TeleportState;
import thito.fancywaystones.proxy.message.*;
import thito.fancywaystones.task.*;

import java.util.*;

public class ProxyWaystoneListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if ("fancywaystones:waystone".equals(s)) {
            Message message = Message.read(bytes);
            if (message instanceof WaystoneReloadRequestMessage) {
                FancyWaystones.getPlugin().submitIO(() -> {
                    WaystoneManager.getManager().refresh(((WaystoneReloadRequestMessage) message).getId());
                });
            } else if (message instanceof ServerIntroductionResponseMessage) {
//                FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Found a response from a Proxy server! Do \"/ws info\" to see available server list.");
            } else if (message instanceof WaystoneDestroyMessage) {
                FancyWaystones.getPlugin().submitIO(() -> {
                    WaystoneData waystoneData = WaystoneManager.getManager().getData(((WaystoneDestroyMessage) message).getId());
                    if (waystoneData != null) {
                        if (waystoneData.getType().isActivationRequired()) {
                            for (PlayerData d : waystoneData.getAttached()) {
                                d.removeWaystone(waystoneData.getUUID());
                                Player online = d.getPlayer();
                                if (online != null) {
                                    Placeholder placeholder = new Placeholder()
                                            .putContent(Placeholder.PLAYER, online)
                                            .putContent(Placeholder.WAYSTONE, waystoneData)
                                            .put("reason", ph -> ((WaystoneDestroyMessage) message).getReason());
                                    online.sendMessage(placeholder.replace("{language.destroyed}"));
                                }
                            }
                        }
                        WaystoneManager.getManager().directUnloadData(waystoneData);
                    }
                });
            } else if (message instanceof WaystoneLoadRequestMessage) {
                FancyWaystones.getPlugin().submitIO(() -> {
                    WaystoneManager.getManager().getData(((WaystoneLoadRequestMessage) message).getId());
                });
            } else if (message instanceof WaystoneUnloadRequestMessage) {
                FancyWaystones.getPlugin().submitIO(() -> {
                    WaystoneManager.getManager().unloadData(((WaystoneUnloadRequestMessage) message).getId());
                });
            } else if (message instanceof WaystoneUpdateMessage) {
                for (UUID id : ((WaystoneUpdateMessage) message).getLoad()) {
                    FancyWaystones.getPlugin().submitIO(() -> {
                        if (WaystoneManager.getManager().prepare(id)) {
                            ((WaystoneUpdateMessage) message).getRefresh().remove(id);
                        }
                    });
                }
                for (UUID id : ((WaystoneUpdateMessage) message).getUnload()) {
                    FancyWaystones.getPlugin().submitIO(() -> {
                        WaystoneManager.getManager().unloadData(id);
                    });
                }
                for (UUID id : ((WaystoneUpdateMessage) message).getRefresh()) {
                    FancyWaystones.getPlugin().submitIO(() -> {
                        WaystoneManager.getManager().refresh(id);
                    });
                }
            } else if (message instanceof RefundTeleportationMessage) {
                FancyWaystones.getPlugin().submitIO(() -> {
                    Player targetPlayer = Bukkit.getPlayer(((RefundTeleportationMessage) message).getPlayerUUID());
                    SerializableLocation source = ((RefundTeleportationMessage) message).getSource();
                    SerializableLocation targetLoc = ((RefundTeleportationMessage) message).getTarget();
                    if (source != null && targetLoc != null && targetPlayer != null) {
                        WaystoneData data = WaystoneManager.getManager().getData(source.getWaystoneId());
                        WaystoneData target = WaystoneManager.getManager().getData(targetLoc.getWaystoneId());
                        if (data != null && target != null) {
                            List<Cost> cost = target.getType().calculateCost(data.getLocation(), target);
                            if (cost != null) {
                                for (Cost c : cost) {
                                    EconomyService service = c.getService();
                                    service.deposit(targetPlayer, c.getAmount());
                                }
                            }
                            TeleportState state = ((RefundTeleportationMessage) message).getState();
                            if (state == TeleportState.UNSAFE) {
                                targetPlayer.sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, target).replace("{language.unsafe-waystone}"));
                            } else if (state == TeleportState.INVALID) {
                                targetPlayer.sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, target).replace("{language.invalid-waystone}"));
                            }
                        }
                    }
                });
            } else if (message instanceof TeleportMessage) {
                Bukkit.getScheduler().runTaskLater(FancyWaystones.getPlugin(), () -> {
                    SerializableLocation target = ((TeleportMessage) message).getTarget();
                    World world = Bukkit.getWorld(target.getWorldName());
                    SerializableLocation source = ((TeleportMessage) message).getSource();
                    if (world != null) {
                        Location location = new Location(world, target.getX(), target.getY(), target.getZ());
                        Player targetPlayer = Bukkit.getPlayer(((TeleportMessage) message).getPlayerUUID());
                        if (targetPlayer != null) {
                            Util.submitSync(new TeleportTask(
                                    targetPlayer, location, FancyWaystones.getPlugin().getCheckRadius(), FancyWaystones.getPlugin().getCheckHeight(),
                                    FancyWaystones.getPlugin().isForceTeleportation(), ((TeleportMessage) message).getAttachedEntities()) {
                                @Override
                                protected void done() {
                                    if (!isSuccess() && source != null && ((TeleportMessage) message).isSendFeedback()) {
                                        player.sendPluginMessage(FancyWaystones.getPlugin(), "fancywaystones:waystone",
                                                new RefundTeleportationMessage(
                                                        ((TeleportMessage) message).getSource(),
                                                        ((TeleportMessage) message).getTarget(),
                                                        ((TeleportMessage) message).getPlayerUUID(),
                                                        TeleportState.UNSAFE
                                                ).write());
                                    }
                                }
                            });
                        } else if (source != null && ((TeleportMessage) message).isSendFeedback()) {
                            player.sendPluginMessage(FancyWaystones.getPlugin(), "fancywaystones:waystone",
                                    new RefundTeleportationMessage(
                                            ((TeleportMessage) message).getSource(),
                                            ((TeleportMessage) message).getTarget(),
                                            ((TeleportMessage) message).getPlayerUUID(),
                                            TeleportState.INVALID
                                    ).write());
                        }
                    }
                }, 5L);
            }
        }
    }
}
