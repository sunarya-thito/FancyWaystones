package thito.fancywaystones.model;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.protocol.*;

import java.util.*;
import java.util.function.*;

public class ClientSideStandardModel extends WaystoneModel {

    public static boolean supportsMarker;
    static {
        try {
            ArmorStand.class.getDeclaredMethod("setMarker", boolean.class);
            supportsMarker = true;
        } catch (Throwable t) {
        }
    }
    public ClientSideStandardModel() {
        maxY = 3;
    }

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String getName() {
        return "Default Model";
    }

    @Override
    public WaystoneModelHandler createHandler(WaystoneData data) {
        Location location = ((LocalLocation) data.getLocation()).getLocation().clone();
        location.setYaw(0);
        location.setPitch(0);
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return new Handler(data, location);
    }

    static ArmorStandMeta createBlockMeta() {
        ArmorStandMeta meta = new ArmorStandMeta();
        meta.setInvisible(true);
        meta.setMarker(true);
        return meta;
    }

    public static ArmorStandMeta createHologramMeta() {
        ArmorStandMeta meta = new ArmorStandMeta();
        meta.setInvisible(true);
        meta.setCustomNameVisible(true);
        meta.setMarker(true);
        return meta;
    }

    public class Handler extends WaystoneModelHandler {

        private FakeArmorStand hologramTop, hologramBottom;
        private FakeArmorStand cap, top;
        private FakeArmorStand[] pillars = new FakeArmorStand[2];
        private FakeArmorStand[] active = new FakeArmorStand[4];
        private Location loc;
        private ItemStack activeBlock;
        private XMaterial pillarMaterial;
        private WaystoneData data;

        private Map<Player, NoAccessAnimation> runningTask = new HashMap<>();

        public Handler(WaystoneData data, Location location) {
            super(ClientSideStandardModel.this);
            ACTIVE_HANDLERS.add(this);
            try {
                activeBlock = Util.material(FancyWaystones.getPlugin().getConfig().getString("Model.Active." + data.getType().name(), "DIAMOND_BLOCK")).parseItem();
            } catch (Throwable t) {
                activeBlock = XMaterial.BEDROCK.parseItem();
            }
            this.data = data;
            loc = location;

            XMaterial capMaterial, topMaterial, footMaterial;
            switch (data.getEnvironment()) {
                case NORMAL:
                    capMaterial = XMaterial.POLISHED_ANDESITE;
                    topMaterial = XMaterial.SMOOTH_STONE_SLAB;
                    if (topMaterial.parseMaterial() == null) {
                        topMaterial = XMaterial.STONE_SLAB;
                    }
                    footMaterial = XMaterial.CHISELED_STONE_BRICKS;
                    pillarMaterial = XMaterial.POLISHED_ANDESITE;
                    break;
                case NETHER:
                    capMaterial = XMaterial.NETHER_BRICKS;
                    topMaterial = XMaterial.NETHER_BRICK_SLAB;
                    footMaterial = XMaterial.NETHER_BRICKS;
                    pillarMaterial = XMaterial.NETHER_BRICKS;
                    break;
                case THE_END:
                    capMaterial = Util.material("PURPUR_BLOCK;END_STONE");
                    topMaterial = Util.material("PURPUR_SLAB;SANDSTONE_SLAB");
                    footMaterial = Util.material("PURPUR_PILLAR;CHISELED_SANDSTONE");
                    pillarMaterial = Util.material("PURPUR_PILLAR;CUT_SANDSTONE");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + data.getEnvironment());
            }

            hologramTop = new FakeArmorStand(location.clone().add(0.5, !supportsMarker ? .95 : 2.95, 0.5));
            hologramTop.setAsyncMetaFactory((player, result) -> {
                result.accept(createHologramMeta());
                FancyWaystones.getPlugin().submit(() -> {
                    update(player);
                });
            });
            hologramTop.setLockMarker(true);
            hologramTop.spawn();

            hologramBottom = new FakeArmorStand(location.clone().add(0.5, !supportsMarker ? .7 : 2.7, 0.5));
            hologramBottom.setAsyncMetaFactory((player, result) -> {
                result.accept(createHologramMeta());
                FancyWaystones.getPlugin().submit(() -> {
                    update(player);
                });
            });
            hologramBottom.setLockMarker(true);
            hologramBottom.spawn();

            cap = new FakeArmorStand(location.clone().add(0.5, 1.7, 0.5));
            cap.setMetaFactory(player -> {
                ArmorStandMeta meta = createBlockMeta();
                meta.setSmall(true);
                meta.setHelmet(capMaterial.parseItem());
                return meta;
            });
            cap.spawn();

            top = new FakeArmorStand(location.clone().add(0.5, 0.67, 0.5));
            top.setMetaFactory(player -> {
                ArmorStandMeta meta = createBlockMeta();
                meta.setHelmet(capMaterial.parseItem());
                return meta;
            });
            top.spawn();

            for (int i = 0; i < pillars.length; i++) {
                pillars[i] = new FakeArmorStand(location.clone().add(0.5, -0.48 + (0.62 * i), 0.5));
                pillars[i].setAsyncMetaFactory((player, result) -> {
                    result.accept(createBlockMeta());
                    FancyWaystones.getPlugin().submit(() -> {
                        update(player);
                    });
                });
                pillars[i].spawn();
            }

            double d = 0.2, d2 = -1.19, d3 = 0.8;
            active[0] = new FakeArmorStand(location.clone().add(d, d2, 0.5));
            active[1] = new FakeArmorStand(location.clone().add(0.5, d2, d));
            active[2] = new FakeArmorStand(location.clone().add(d3, d2, 0.5));
            active[3] = new FakeArmorStand(location.clone().add(0.5, d2, d3));
            for (FakeArmorStand a : active) {
                a.setAsyncMetaFactory((player, result) -> {
                    result.accept(createBlockMeta());
                    FancyWaystones.getPlugin().submit(() -> {
                        update(player);
                    });
                });
                a.spawn();
            }

            XMaterial finalTopMaterial = topMaterial;
            if (FancyWaystones.getPlugin().isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                    location.getBlock().setType(footMaterial.parseMaterial());
                    location.clone().add(0, 1, 0).getBlock().setType(XMaterial.COBBLESTONE_WALL.parseMaterial());
                    location.clone().add(0, 2, 0).getBlock().setType(finalTopMaterial.parseMaterial());
                });
            }
        }

        @Override
        public WaystoneData getData() {
            return data;
        }


        public void update() {
            double viewDistance = Bukkit.getViewDistance() * 16;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() == loc.getWorld() && player.getLocation().distance(loc) < viewDistance) {
                    update(player);
                }
            }
        }

        @Override
        public void update(Player player) {
            supplyIsActive(player, isActive -> {
                for (FakeArmorStand a : pillars) {
                    ArmorStandMeta meta = createBlockMeta();
                    if (isActive) {
                        meta.setHelmet(activeBlock);
                    } else {
                        meta.setHelmet(pillarMaterial.parseItem());
                    }
                    a.update(player, meta);
                }
                for (FakeArmorStand a : active) {
                    ArmorStandMeta meta = createBlockMeta();
                    if (isActive) {
                        meta.setHelmet(activeBlock);
                    } else {
                        meta.setHelmet(null);
                    }
                    a.update(player, meta);
                }
                Placeholder placeholder = new Placeholder();
                placeholder.putContent(Placeholder.PLAYER, player);
                placeholder.putContent(Placeholder.WAYSTONE, data);
                placeholder.put("waystone_hologram_status", ph -> isActive ? "{language.hologram.active}" : "{language.hologram.inactive}");
                ArmorStandMeta meta = createHologramMeta();
                meta.setCustomName(placeholder.replace("{language.hologram.name}"));
                hologramTop.update(player, meta);

                if (!runningTask.containsKey(player)) {
                    meta = createHologramMeta();
                    meta.setCustomName(isActive ? placeholder.replace("{language.hologram.active}") : placeholder.replace("{language.hologram.tip-click}"));
                    hologramBottom.updateMetadata(player, meta);
                }
            });
        }

        public void sendNoAccess(Player player) {
            Placeholder placeholder = new Placeholder();
            placeholder.putContent(Placeholder.PLAYER, player)
                    .putContent(Placeholder.WAYSTONE, data);
            NoAccessAnimation current = new NoAccessAnimation(player, placeholder);
            NoAccessAnimation old = runningTask.put(player, current);
            if (old != null) old.cancel();
            current.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
        }

        public void supplyIsActive(Player player, Consumer<Boolean> result) {
            if (!data.getType().isActivationRequired()) {
                result.accept(true);
            } else {
                FancyWaystones.getPlugin().submitIO(() -> {
                    PlayerData data = WaystoneManager.getManager().getPlayerData(player);
                    if (data != null) {
                        result.accept(data.knowWaystone(this.data));
                    }
                });
            }
        }

        @Override
        public boolean isPart(Location l) {
            return loc.getWorld() == l.getWorld() && loc.getBlockX() == l.getBlockX() && loc.getBlockY() <= l.getBlockY() && loc.getBlockY() + 2 >= l.getBlockY() && l.getBlockZ() == loc.getBlockZ();
        }

        @Override
        public void destroyImmediately() {
            ACTIVE_HANDLERS.remove(this);
            hologramTop.remove();
            hologramBottom.remove();
            for (FakeArmorStand a : pillars) {
                a.remove();
            }
            for (FakeArmorStand a : active) {
                a.remove();
            }
            top.remove();
            cap.remove();
        }

        @Override
        public void destroy() {
            ACTIVE_HANDLERS.remove(this);
            hologramTop.remove();
            hologramBottom.remove();
            for (FakeArmorStand a : pillars) {
                a.remove();
            }
            for (FakeArmorStand a : active) {
                a.remove();
            }
            top.remove();
            cap.remove();
            if (FancyWaystones.getPlugin().isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                    loc.getBlock().setType(XMaterial.AIR.parseMaterial());
                    loc.clone().add(0, 1, 0).getBlock().setType(XMaterial.AIR.parseMaterial());
                    loc.clone().add(0, 2, 0).getBlock().setType(XMaterial.AIR.parseMaterial());
                });
            }
        }

        public class NoAccessAnimation extends Task {
            private TrimmedMessage trimmedMessage;
            private Player player;
            int index = 0, shift = 0;
            int tickTime = 0;

            public NoAccessAnimation(Player player, Placeholder placeholder) {
                this.player = player;
                trimmedMessage = new TrimmedMessage(placeholder.replace("{language.hologram.no-access}"));
            }

            @Override
            public void run() {
                if (!player.isOnline() || player.getWorld() != loc.getWorld() || player.getLocation().distance(loc) > 10 || shift > 150) {
                    stop();
                    return;
                }
                if (tickTime % 2 == 0) {
                    ArmorStandMeta meta = createHologramMeta();
                    if (index < trimmedMessage.getMaxRadius()) {
                        meta.setCustomName(trimmedMessage.trimFromCenter(index));
                        index++;
                    } else {
                        meta.setCustomName(trimmedMessage.shiftLeft(shift));
                        shift++;
                    }
                    hologramBottom.updateMetadata(player, meta);
                }
                tickTime++;
            }

            public void stop() {
                runningTask.remove(player);
                cancel();
                update(player);
            }
        }
    }

}
