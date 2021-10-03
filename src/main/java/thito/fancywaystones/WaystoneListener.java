package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.*;
import org.bukkit.util.Vector;
import thito.fancywaystones.location.*;
import thito.fancywaystones.proxy.*;
import thito.fancywaystones.task.*;
import xyz.xenondevs.particle.*;

import java.io.*;
import java.util.*;

public class WaystoneListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        FancyWaystones.getPlugin().submitIO(() -> {
            WaystoneManager.getManager().loadChunk(e.getChunk());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (FancyWaystones.getPlugin().getDeathBook().isEnableListener()) {
            FancyWaystones.getPlugin().submitIO(() -> {
                PlayerData data = WaystoneManager.getManager().getPlayerData(e.getEntity());
                data.dispatchDeath(e.getEntity().getLocation());
            });
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        FancyWaystones.getPlugin().submitIO(() -> {
            try {
                WaystoneManager.getManager().unloadChunk(e.getChunk());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void quit(PlayerQuitEvent e) {
        WarmUpTask task = WarmUpTask.TASKS.get(e.getPlayer());
        if (task != null) {
            task.cancel();
            task.onCancelled();
        }
        FancyWaystones.getPlugin().submitIO(() -> {
            WaystoneManager.getManager().unloadPlayerData(e.getPlayer().getUniqueId());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(EntityExplodeEvent e) {
        Set<WaystoneData> destroyed = new HashSet<>();
        String reason;
        if (e.getEntityType() == EntityType.CREEPER) {
            reason = "{language.reason-creeper-explosion}";
        } else if (e.getEntityType() == EntityType.MINECART_TNT || e.getEntityType() == EntityType.PRIMED_TNT) {
            reason = "{language.reason-tnt-explosion}";
        } else {
            reason = "{language-reason-unknown-explosion}";
        }
        for (Block b : new ArrayList<>(e.blockList())) {
            WaystoneData wb = WaystoneManager.getManager().getDataAt(b.getLocation());
            if (wb != null) {
                e.blockList().remove(b);
                destroyed.add(wb);
            }
        }
        for (WaystoneData wb : destroyed) {
            if (!wb.getType().isBreakableByExplosion(wb)) {
                continue;
            }
            FancyWaystones.getPlugin().submitIO(() -> {
                wb.destroy(reason);
                WaystoneManager.getManager().createWaystoneItem(wb, false, result -> {
                    Location location = ((LocalLocation) wb.getLocation()).getLocation();
                    location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), result);
                });
            });
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        ProxyWaystone proxyWaystone = FancyWaystones.getPlugin().getProxyWaystone();
        if (proxyWaystone != null) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                // Introduce the server UUID to the proxy server
                proxyWaystone.introduceServer(e.getPlayer());
            });
        }
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void preBlockBreak(BlockBreakEvent e) {
//        Block block = e.getBlock();
//        WaystoneData data = WaystoneManager.getManager().getDataAt(block.getLocation());
//        if (data != null) {
//            // prevents other plugin to do further actions on this block
//
//            // making the event only accessible by FancyWaystones
//            // of course this would work if theres no bad coded plugin
//            // that does not check Cancellable#isCancelled or EventHandler#ignoreCancelled
//            // for the handler
////            if (FancyWaystones.getPlugin().getConfig().getBoolean("Waystone Protection."+data.getType().name()+".Enable")) {
////
////            }
//        }
//    }

    private boolean hasAccess(Player player, WaystoneData waystoneData) {
        WaystoneType type = waystoneData.getType();
        return type.hasAccess(player, waystoneData);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent e) {
        if (Util.dummy.remove(e)) return;
        boolean wasCancelled = e.isCancelled();
        Block block = e.getBlock();
        WaystoneData data = WaystoneManager.getManager().getDataAt(block.getLocation());
        if (data != null) {
            e.setCancelled(true);
            if (!data.getType().isBreakable(e.getPlayer(), data)) {
                if (!e.getPlayer().getUniqueId().equals(data.getOwnerUUID())) {
                    e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer())
                    .putContent(Placeholder.WAYSTONE, data).replace("{language.cannot-break-not-owner}"));
                    return;
                }
            } else if (wasCancelled) return;
            ItemStack used = e.getPlayer().getItemInHand();
            FancyWaystones.getPlugin().submitIO(() -> {
                data.destroy(e.getPlayer().getName());
                if (data.getType().shouldDrop(e.getPlayer(), data)) {
                    WaystoneManager.getManager().createWaystoneItem(data,
                            used.hasItemMeta() && used.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH),
                            result -> {
                                if (FancyWaystones.getPlugin().isEnabled()) {
                                    Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                                        block.getWorld().dropItemNaturally(block.getLocation().clone().add(.5, 0, .5), result);
                                    });
                                }
                            });
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent e) {
        WarmUpTask task = WarmUpTask.TASKS.get(e.getPlayer());
        if (task != null) {
            task.cancel();
            task.onCancelled();
        }
        ItemStack item = e.getItem();
        if (item != null) {
            item = item.clone();
            if (!e.isCancelled()) {
                if (handleDeathBook(e, item)) return;
                if (handleTeleportationBook(e, item)) return;
            }
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block != null && !e.getPlayer().isSneaking()) {
                synchronized (WaystoneModel.ACTIVE_HANDLERS) {
                    for (WaystoneModelHandler handler : WaystoneModel.ACTIVE_HANDLERS) {
                        if (handler.isPart(block.getLocation())) {
                            try {
                                if (e.getHand() != EquipmentSlot.HAND) {
                                    e.setCancelled(true);
                                    return;
                                }
                            } catch (Throwable ignored) {
                            }
                            e.setCancelled(true);
                            if (handler.getData().getEnvironment() != e.getPlayer().getLocation().getWorld().getEnvironment()) {
                                e.getPlayer().sendMessage(new Placeholder()
                                        .putContent(Placeholder.PLAYER, e.getPlayer())
                                        .putContent(Placeholder.WAYSTONE, handler.getData())
                                        .replace("{language.invalid-environment}"));
                                ParticleEffect.SMOKE_LARGE
                                        .display(((LocalLocation) handler.getData().getLocation()).getLocation().clone()
                                                        .subtract(handler.getMinX() - .5, handler.getMinY() - .5, handler.getMinZ() - .5),
                                                new Vector(handler.getMaxX() + .5, handler.getMaxY() + .5, handler.getMaxZ() + .5),
                                                0, 50, null, e.getPlayer());
                            } else {
                                FancyWaystones.getPlugin().submitIO(() -> {
                                    PlayerData data = WaystoneManager.getManager().getPlayerData(e.getPlayer());
                                    handlePlayerClick(e, handler, data);
                                });
                            }
                            return;
                        }
                    }
                }
            }
            if (item != null && block != null) {
                if (WaystoneManager.getManager().hasWaystone(item)) {
                    BlockFace face = e.getBlockFace();
                    Location location = block.getLocation().add(face.getModX(), face.getModY(), face.getModZ());
                    BlockPlaceEvent blockPlaceEvent;
                    try {
                        blockPlaceEvent = new BlockPlaceEvent(
                                location.getBlock(), location.getBlock().getState(), block, item, e.getPlayer(), true, e.getHand() == null ?
                                EquipmentSlot.HAND : e.getHand()
                        );
                    } catch (Throwable t) {
                        blockPlaceEvent = new BlockPlaceEvent(
                                location.getBlock(), location.getBlock().getState(), block, item, e.getPlayer(), true
                        );
                    }
                    Bukkit.getPluginManager().callEvent(blockPlaceEvent);
                    if (blockPlaceEvent.isCancelled()) {
                        e.setCancelled(true);
                        return;
                    }
                    WaystoneData snapshot = WaystoneManager.getManager().getWaystoneFromItem(item);
                    if (snapshot != null) {
                        snapshot.claim(e.getPlayer());
                        FancyWaystones.getPlugin().submitIO(() -> {
                            handlePlacement(e, location, snapshot);
                        });
                    }
                    item.setAmount(item.getAmount() - 1);
                    try {
                        if (e.getHand() == EquipmentSlot.HAND) {
                            e.getPlayer().setItemInHand(item);
                        } else {
                            e.getPlayer().getInventory().setItemInOffHand(item);
                        }
                    } catch (Throwable t) {
                        e.getPlayer().setItemInHand(item);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    private void handlePlacement(PlayerInteractEvent e, Location location, WaystoneData data) {
        PlaceWaystoneTask task = new PlaceWaystoneTask(FancyWaystones.getPlugin(), e.getPlayer(), location, data);
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), task);
        }
    }

    private void handlePlayerClick(PlayerInteractEvent e, WaystoneModelHandler handler, PlayerData data) {
        boolean res = data.knowWaystone(handler.getData());
        if (res) {
            if (hasAccess(e.getPlayer(), handler.getData())) {
                WaystoneManager.getManager().openWaystoneMenu(data, handler.getData());
            } else {
                e.getPlayer().sendMessage(new Placeholder()
                        .putContent(Placeholder.PLAYER, e.getPlayer())
                        .putContent(Placeholder.WAYSTONE, handler.getData())
                        .replace("{language.blacklisted}"));
                handler.sendNoAccess(e.getPlayer());
            }
        } else {
            if (!hasAccess(e.getPlayer(), handler.getData())) {
                handler.sendNoAccess(e.getPlayer());
            } else {
                if (!handler.getData().getBlacklist().contains(new WaystoneMember(data.getUUID())) &&
                        handler.getData().getType().hasActivationAccess(e.getPlayer(), handler.getData())) {
                    data.addWaystone(handler.getData().getUUID());
                } else {
                    e.getPlayer().sendMessage(new Placeholder()
                            .putContent(Placeholder.PLAYER, e.getPlayer())
                            .putContent(Placeholder.WAYSTONE, handler.getData())
                            .replace("{language.blacklisted}"));
                    handler.sendNoAccess(e.getPlayer());
                }
            }
        }
    }

    private boolean handleDeathBook(PlayerInteractEvent e, ItemStack item) {
        if (FancyWaystones.getPlugin().getDeathBook().isEnable() &&
                FancyWaystones.getPlugin().getDeathBook().isItem(item)) {
            try {
                if (e.getHand() != EquipmentSlot.HAND) {
                    e.setCancelled(true);
                    return true;
                }
            } catch (Throwable ignored) {
            }
            ItemStack finalItem = item.clone();
            item.setAmount(item.getAmount() - 1);
            e.getPlayer().setItemInHand(item);
            e.setCancelled(true);
            finalItem.setAmount(1);
            FancyWaystones.getPlugin().submitIO(() -> {
                PlayerData data = WaystoneManager.getManager().getPlayerData(e.getPlayer());
                DeathLocation death = data.getDeathLocation();
                if (death == null || (System.currentTimeMillis() - data.getDeathTime()) >= Util.tickToMillis(
                        FancyWaystones.getPlugin().getDeathBook().getDeathLocationTimeout()
                )) {
                    e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer()).replace("{language.no-death}"));
                    Util.placeInHand(e.getPlayer(), finalItem);
                } else {
                    new DeathBookWarmUpTask(e.getPlayer()) {
                        @Override
                        public void onDone() {
                            death.teleport(e.getPlayer());
                        }

                        @Override
                        public void onCancelled() {
                            Util.placeInHand(e.getPlayer(), finalItem);
                        }
                    }.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
                }
            });
            return true;
        }
        return false;
    }

    private boolean handleTeleportationBook(PlayerInteractEvent e, ItemStack item) {
        if (FancyWaystones.getPlugin().getTeleportationBook().isEnable()) {
            if (handleEmptyTpBook(e, item)) return true;
            return handleTpBook(e, item);
        }
        return false;
    }

    private boolean handleEmptyTpBook(PlayerInteractEvent e, ItemStack item) {
        if (FancyWaystones.getPlugin().getTeleportationBook().isEmptyItem(item)) {
            try {
                if (e.getHand() != EquipmentSlot.HAND) {
                    e.setCancelled(true);
                    return true;
                }
            } catch (Throwable ignored) {
            }
            e.setCancelled(true);
            Block block = e.getClickedBlock();
            if (block != null) {
                for (WaystoneModelHandler handler : WaystoneModel.ACTIVE_HANDLERS) {
                    if (handler.isPart(block.getLocation())) {
                        if (handler.getData().getEnvironment() != e.getPlayer().getLocation().getWorld().getEnvironment()) {
                            e.getPlayer().sendMessage(new Placeholder()
                                    .putContent(Placeholder.PLAYER, e.getPlayer())
                                    .putContent(Placeholder.WAYSTONE, handler.getData())
                                    .replace("{language.invalid-environment}"));
                            ParticleEffect.SMOKE_LARGE
                                    .display(((LocalLocation) handler.getData().getLocation()).getLocation().clone()
                                                    .subtract(handler.getMinX() - .5, handler.getMinY() - .5, handler.getMinZ() - .5),
                                            new Vector(handler.getMaxX() + .5, handler.getMaxY() + .5, handler.getMaxZ() + .5),
                                            0, 50, null, e.getPlayer());
                        } else {
                            item.setAmount(item.getAmount() - 1);
                            try {
                                if (e.getHand() == EquipmentSlot.HAND) {
                                    e.getPlayer().setItemInHand(item);
                                } else {
                                    e.getPlayer().getInventory().setItemInOffHand(item);
                                }
                            } catch (Throwable t) {
                                e.getPlayer().setItemInHand(item);
                            }
                            ItemStack itemStack = FancyWaystones.getPlugin().getTeleportationBook().createItem(handler.getData());
                            Util.placeInHand(e.getPlayer(), itemStack);
                            break;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean handleTpBook(PlayerInteractEvent e, ItemStack item) {
        if (FancyWaystones.getPlugin().getTeleportationBook().isItem(item)) {
            try {
                if (e.getHand() != EquipmentSlot.HAND) {
                    e.setCancelled(true);
                    return true;
                }
            } catch (Throwable ignored) {
            }
            e.setCancelled(true);
            ItemStack usedItem = item.clone();
            item.setAmount(item.getAmount() - 1);
            e.getPlayer().setItemInHand(item);
            usedItem.setAmount(1);
            FancyWaystones.getPlugin().submitIO(() -> {
                WaystoneData data = FancyWaystones.getPlugin().getTeleportationBook().getWaystoneData(usedItem);
                if (data != null) {
                    new TeleportationBookWarmUpTask(e.getPlayer(), data) {
                        @Override
                        public void onDone() {
                            data.getLocation().transport(e.getPlayer(), null, data, state -> {
                                if (state != TeleportState.SUCCESS) {
                                    Util.placeInHand(e.getPlayer(), usedItem);
                                } else {
                                    e.getPlayer().setNoDamageTicks((int) (e.getPlayer().getNoDamageTicks() + FancyWaystones.getPlugin().getNoDamageTicks()));
                                }
                                if (state == TeleportState.UNSAFE) {
                                    e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, data).replace("{language.unsafe-waystone}"));
                                } else if (state == TeleportState.INVALID) {
                                    e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, data).replace("{language.invalid-waystone}"));
                                }
                            });
                        }

                        @Override
                        public void onCancelled() {
                            Util.placeInHand(e.getPlayer(), usedItem);
                        }
                    }.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
                } else {
                    if (FancyWaystones.getPlugin().getTeleportationBook().canChargeBack()) {
                        Util.placeInHand(e.getPlayer(), FancyWaystones.getPlugin().getTeleportationBook().createEmptyItem());
                    }
                    e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer()).replace("{language.invalid-unknown-waystone}"));
                }
            });
            return true;
        }
        return false;
    }

}
