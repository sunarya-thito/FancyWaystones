package thito.fancywaystones;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import thito.fancywaystones.condition.handler.ExplosionConditionHandler;
import thito.fancywaystones.event.*;
import thito.fancywaystones.location.DeathLocation;
import thito.fancywaystones.location.LocalLocation;
import thito.fancywaystones.location.TeleportState;
import thito.fancywaystones.loot.LootTable;
import thito.fancywaystones.proxy.ProxyWaystone;
import thito.fancywaystones.structure.Selection;
import thito.fancywaystones.task.DeathBookWarmUpTask;
import thito.fancywaystones.task.PlaceWaystoneTask;
import thito.fancywaystones.task.TeleportationBookWarmUpTask;
import thito.fancywaystones.task.WarmUpTask;
import xyz.xenondevs.particle.ParticleEffect;

import java.io.IOException;
import java.util.*;

public class WaystoneListener implements Listener {

    static Map<Player, List<Entity>> leashedEntitiesMap = new HashMap<>();
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeash(PlayerLeashEntityEvent event) {
        leashedEntitiesMap.computeIfAbsent(event.getPlayer(), player -> new ArrayList<>()).add(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        entity.setMetadata("FW:SR", new FixedMetadataValue(FancyWaystones.getPlugin(), event.getSpawnReason()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnleash(PlayerUnleashEntityEvent event) {
        List<Entity> entities = leashedEntitiesMap.get(event.getPlayer());
        if (entities != null) {
            entities.remove(event.getEntity());
            if (entities.isEmpty()) {
                leashedEntitiesMap.remove(event.getPlayer());
            }
        }
    }

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
        leashedEntitiesMap.remove(e.getPlayer());
        FancyWaystones.getPlugin().submitIO(() -> {
            WaystoneManager.getManager().unloadPlayerData(e.getPlayer().getUniqueId());
        });
    }

    static void handleExplosion(Set<WaystoneData> destroyed, String reason, List<Block> blocks) {
        for (Block b : new ArrayList<>(blocks)) {
            WaystoneData wb = WaystoneManager.getManager().getDataAt(b.getLocation());
            if (wb != null) {
                blocks.remove(b);
                destroyed.add(wb);
            }
        }
        for (WaystoneData wb : destroyed) {
            if (FWEvent.call(new WaystoneDestroyEvent(wb, reason)).isCancelled()) continue;
            if (!wb.getType().isBreakableByExplosion(wb)) {
                continue;
            }
            FancyWaystones.getPlugin().submitIO(() -> {
                if (wb.getWaystoneBlock() != null) {
                    wb.destroy(reason);
//                    ItemStack result = WaystoneManager.getManager().createWaystoneItem(wb, false);
                    Location location = ((LocalLocation) wb.getLocation()).getLocation();
//                    Util.submitSync(() -> {
//                        location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), result);
//                    });
                    LootTable lootTable = wb.getType().getLootTable();
                    List<ItemStack> itemList = lootTable.getLootItems(new Placeholder()
                            .putContent(Placeholder.WAYSTONE, wb)
                            .putContent(ExplosionConditionHandler.EXPLOSION_CAUSE, true));
                    Util.submitSync(() -> {
                        for (ItemStack itemStack : itemList) {
                            location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), itemStack);
                        }
                    });
                }
            });
        }
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
        handleExplosion(destroyed, reason, e.blockList());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interactWand(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        ItemStack item = e.getItem();
        if (block != null && item != null && Util.hasData(item, "FW:WAND")) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                String prefix = ChatColor.translateAlternateColorCodes('&', Language.getLanguage().get("prefix"));
                Selection selection = Selection.getSelection(e.getPlayer());
                selection.setPos2(block.getLocation());
                e.getPlayer().sendMessage(prefix + "Pos 2 has been set!" + FancyWaystonesCommand.getSelectionSize(selection));
                e.setCancelled(true);
            } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                String prefix = ChatColor.translateAlternateColorCodes('&', Language.getLanguage().get("prefix"));
                Selection selection = Selection.getSelection(e.getPlayer());
                selection.setPos1(block.getLocation());
                e.getPlayer().sendMessage(prefix + "Pos 1 has been set!" + FancyWaystonesCommand.getSelectionSize(selection));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        ProxyWaystone proxyWaystone = FancyWaystones.getPlugin().getProxyWaystone();
        if (proxyWaystone != null) {
            Bukkit.getScheduler().runTaskLater(FancyWaystones.getPlugin(), () -> {
                // Introduce the server UUID to the proxy server
                proxyWaystone.introduceServer(e.getPlayer());
            }, 20L);
        }
    }

    private String[] hasAccess(Player player, WaystoneData waystoneData) {
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
            String[] breakable = data.getType().isBreakable(e.getPlayer(), data);
            if (breakable != null) {
                e.getPlayer().sendMessage(breakable);
                return;
            } else if (wasCancelled) return;
            if (FWEvent.call(new WaystoneDestroyEvent(data, e.getPlayer())).isCancelled()) return;
//            ItemStack used = e.getPlayer().getItemInHand();
            FancyWaystones.getPlugin().submitIO(() -> {
                if (data.getWaystoneBlock() != null) {
                    data.destroy(e.getPlayer().getName());
                    Util.submitSync(() -> {
                        if (data.getType().shouldDrop(e.getPlayer(), data)) {
                            LootTable lootTable = data.getType().getLootTable();
                            List<ItemStack> itemList = lootTable.getLootItems(new Placeholder()
                                    .putContent(Placeholder.PLAYER, e.getPlayer())
                                    .putContent(Placeholder.WAYSTONE, data));
                            Util.submitSync(() -> {
                                for (ItemStack itemStack : itemList) {
                                    block.getWorld().dropItemNaturally(block.getLocation().clone().add(.5, 0, .5), itemStack);
                                }
                            });
//                            ItemStack result = WaystoneManager.getManager().createWaystoneItem(data,
//                                    used.hasItemMeta() && used.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH));
//                            if (FancyWaystones.getPlugin().isEnabled()) {
//                                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
//                                    block.getWorld().dropItemNaturally(block.getLocation().clone().add(.5, 0, .5), result);
//                                });
//                            }
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
                if (handleTeleportationBook(e, item)) {
                    return;
                }
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
                            if (item != null && item.getType() == Material.COMPASS && !e.getPlayer().isSneaking()) {
                                String[] strings = handler.getData().getType().canRedirectCompass(e.getPlayer(), handler.getData());
                                if (strings == null) {
                                    WaystoneLocation location = handler.getData().getLocation();
                                    if (location instanceof LocalLocation) {
                                        if (((LocalLocation) location).getLocation().equals(e.getPlayer().getCompassTarget())) {
                                            e.getPlayer().setCompassTarget(e.getPlayer().getWorld().getSpawnLocation());
                                        } else {
                                            e.getPlayer().setCompassTarget(((LocalLocation) location).getLocation());
                                        }
                                    }
                                } else {
                                    e.getPlayer().sendMessage(strings);
                                }
                                return;
                            }
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
                                    Util.submitSync(() -> {
                                        handlePlayerClick(e, handler, data);
                                    });
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
                    if (FWEvent.call(new WaystonePlaceEvent(snapshot, e.getPlayer(), location)).isCancelled()) {
                        e.setCancelled(true);
                        return;
                    }
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
        Debug.debug("handlePlacement attempt");
        PlaceWaystoneTask task = new PlaceWaystoneTask(FancyWaystones.getPlugin(), e.getPlayer(), location, data);
        if (FancyWaystones.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), task);
        }
    }

    private void handlePlayerClick(PlayerInteractEvent e, WaystoneModelHandler handler, PlayerData data) {
        boolean res = data.knowWaystone(handler.getData());
        String[] strings = hasAccess(e.getPlayer(), handler.getData());
        if (res) {
            if (strings == null) {
                if (!FWEvent.call(new WaystoneUseEvent(handler.getData(), data)).isCancelled()) {
                    Debug.debug("Player attempt click OPEN");
                    WaystoneManager.getManager().openWaystoneMenu(data, handler.getData());
                }
            } else {
                Debug.debug("Player attempt click NO ACCESS 1");
                e.getPlayer().sendMessage(strings);
                handler.sendNoAccess(e.getPlayer());
            }
        } else {
            if (strings != null) {
                Debug.debug("Player attempt click NO ACCESS 2");
                e.getPlayer().sendMessage(strings);
                handler.sendNoAccess(e.getPlayer());
            } else {
                String[] reasons = handler.getData().getType().hasActivationAccess(e.getPlayer(), handler.getData());
                if (!handler.getData().getBlacklist().contains(new WaystoneMember(data.getUUID())) &&
                        reasons == null) {
                    if (!FWEvent.call(new WaystoneActivateEvent(handler.getData(), data)).isCancelled()) {
                        Debug.debug("Player attempt click ACTIVATED");
                        FancyWaystones.getPlugin().submitIO(() -> {
                            data.addWaystone(handler.getData().getUUID());
                            Debug.debug("Player attempt click ACTIVATED [DONE]");
                        });
                    }
                } else {
                    if (reasons == null) {
                        e.getPlayer().sendMessage(new Placeholder()
                                .putContent(Placeholder.PLAYER, e.getPlayer())
                                .putContent(Placeholder.WAYSTONE, handler.getData())
                                .replace("{language.blacklisted}"));
                    } else {
                        e.getPlayer().sendMessage(reasons);
                    }
                    Debug.debug("Player attempt click NO ACCESS 3");
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
            e.setCancelled(true);
            String[] reasons = FancyWaystones.getPlugin().getDeathBook().getUseCondition()
                    .getFormattedReason(new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer()));
            if (reasons != null) {
                e.getPlayer().sendMessage(reasons);
                return true;
            }
            ItemStack finalItem = item.clone();
            item.setAmount(item.getAmount() - 1);
            e.getPlayer().setItemInHand(item);
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
                    if (!FWEvent.call(new DeathBookPreTeleportEvent(death, data)).isCancelled()) {
                        new DeathBookWarmUpTask(e.getPlayer()) {
                            @Override
                            public void onDone() {
                                if (!FWEvent.call(new DeathBookTeleportEvent(death, data)).isCancelled()) {
                                    death.teleport(e.getPlayer());
                                }
                            }

                            @Override
                            public void onCancelled() {
                                Util.placeInHand(e.getPlayer(), finalItem);
                            }
                        }.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
                    }
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
                synchronized (WaystoneModel.ACTIVE_HANDLERS) {
                    for (WaystoneModelHandler handler : WaystoneModel.ACTIVE_HANDLERS) {
                        if (handler.isPart(block.getLocation())) {
                            if (!FWEvent.call(new WaystoneActivateBookEvent(handler.getData(), e.getPlayer())).isCancelled()) {
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
                                    String[] reasons = FancyWaystones.getPlugin().getTeleportationBook().getActivationCondition().getFormattedReason(
                                            new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer())
                                                    .putContent(Placeholder.WAYSTONE, handler.getData())
                                    );
                                    if (reasons != null) {
                                        e.getPlayer().sendMessage(reasons);
                                        return true;
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
                                    ItemStack itemStack = FancyWaystones.getPlugin().getTeleportationBook().createItem(handler.getData());
                                    Util.placeInHand(e.getPlayer(), itemStack);
                                }
                            }
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
                PlayerData playerData = WaystoneManager.getManager().getPlayerData(e.getPlayer());
                FancyWaystones.getPlugin().submit(() -> {
                    if (data != null) {
                        String[] reasons = FancyWaystones.getPlugin().getTeleportationBook().getUseCondition().getFormattedReason(
                                new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer())
                                        .putContent(Placeholder.WAYSTONE, data)
                        );
                        if (reasons != null) {
                            e.getPlayer().sendMessage(reasons);
                            return;
                        }
                        if (!FWEvent.call(new WaystonePreTeleportEvent(null, playerData, data, null)).isCancelled()) {
                            new TeleportationBookWarmUpTask(e.getPlayer(), data) {
                                @Override
                                public void onDone() {
                                    if (!FWEvent.call(new WaystoneTeleportEvent(null, playerData, data, null)).isCancelled()) {
                                        data.getLocation().transport(e.getPlayer(), null, data, state -> {
                                            if (state != TeleportState.SUCCESS) {
                                                Util.placeInHand(e.getPlayer(), usedItem);
                                            } else {
                                                FWEvent.call(new WaystonePostTeleportEvent(null, playerData.getPlayer(), data));
                                                FancyWaystones.getPlugin().postTeleport("Teleportation Book", e.getPlayer(), null, data);
                                                e.getPlayer().setNoDamageTicks((int) (e.getPlayer().getNoDamageTicks() + FancyWaystones.getPlugin().getNoDamageTicks()));
                                            }
                                            if (state == TeleportState.UNSAFE) {
                                                e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, data).replace("{language.unsafe-waystone}"));
                                            } else if (state == TeleportState.INVALID) {
                                                e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.WAYSTONE, data).replace("{language.invalid-waystone}"));
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled() {
                                    Util.placeInHand(e.getPlayer(), usedItem);
                                }
                            }.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
                        }
                    } else {
                        if (FancyWaystones.getPlugin().getTeleportationBook().canChargeBack()) {
                            Util.placeInHand(e.getPlayer(), FancyWaystones.getPlugin().getTeleportationBook().createEmptyItem());
                        }
                        e.getPlayer().sendMessage(new Placeholder().putContent(Placeholder.PLAYER, e.getPlayer()).replace("{language.invalid-unknown-waystone}"));
                    }
                });
            });
            return true;
        }
        return false;
    }

}
