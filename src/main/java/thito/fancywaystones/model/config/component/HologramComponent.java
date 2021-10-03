package thito.fancywaystones.model.config.component;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.model.*;
import thito.fancywaystones.model.config.*;
import thito.fancywaystones.protocol.*;

import java.util.*;
import java.util.function.*;

public class HologramComponent implements ComponentType {
    private static final boolean supportsMarker = ClientSideStandardModel.supportsMarker;
    private ComponentData defaultData = new ComponentData(new StyleRuleCompound(), new MapSection());

    @Override
    public ComponentData getDefaultData() {
        return defaultData;
    }

    @Override
    public void bakeData(ComponentData[] componentData) {
    }

    @Override
    public ComponentHandler createHandler(WaystoneData waystoneData, Component component) {
        return new Handler(component, waystoneData);
    }

    public class Handler implements ComponentHandler {
        private FakeArmorStand hologramTop, hologramBottom;
        private WaystoneData waystoneData;
        private Map<Player, NoAccessAnimation> runningTask = new HashMap<>();

        public Handler(Component component, WaystoneData waystoneData) {
            this.waystoneData = waystoneData;
            Location bottomLocation = component.getLocation();
            Location topLocation = component.getLocation().clone().add(0, .25, 0);
            if (!supportsMarker) {
                topLocation = topLocation.subtract(0, 2, 0);
                bottomLocation = bottomLocation.clone().subtract(0, 2, 0);
            }
            hologramBottom = new FakeArmorStand(bottomLocation);
            hologramBottom.setLockMarker(true);
            hologramBottom.setAsyncMetaFactory((player, cons) -> {
                if (!runningTask.containsKey(player)) {
                    supplyIsActive(player, isActive -> {
                        Placeholder placeholder = new Placeholder();
                        placeholder.putContent(Placeholder.PLAYER, player);
                        placeholder.putContent(Placeholder.WAYSTONE, waystoneData);
                        placeholder.put("waystone_hologram_status", ph -> isActive ? "{language.hologram.active}" : "{language.hologram.inactive}");
                        ArmorStandMeta meta = ClientSideStandardModel.createHologramMeta();
                        meta.setCustomName(isActive ? placeholder.replace("{language.hologram.active}") : placeholder.replace("{language.hologram.tip-click}"));
                        cons.accept(meta);
                    });
                }
            });
            hologramBottom.spawn();
            hologramTop = new FakeArmorStand(topLocation);
            hologramTop.setLockMarker(true);
            hologramTop.setAsyncMetaFactory((player, cons) -> {
                supplyIsActive(player, isActive -> {
                    Placeholder placeholder = new Placeholder();
                    placeholder.putContent(Placeholder.PLAYER, player);
                    placeholder.putContent(Placeholder.WAYSTONE, waystoneData);
                    placeholder.put("waystone_hologram_status", ph -> isActive ? "{language.hologram.active}" : "{language.hologram.inactive}");
                    ArmorStandMeta meta = ClientSideStandardModel.createHologramMeta();
                    meta.setCustomName(placeholder.replace("{language.hologram.name}"));
                    cons.accept(meta);
                });
            });
            hologramTop.spawn();
        }

        @Override
        public boolean hasBlockHitBox() {
            return false;
        }

        public void supplyIsActive(Player player, Consumer<Boolean> result) {
            if (!waystoneData.getType().isActivationRequired()) {
                result.accept(true);
            } else {
                FancyWaystones.getPlugin().submitIO(() -> {
                    PlayerData playerData = WaystoneManager.getManager().getPlayerData(player);
                    result.accept(playerData.knowWaystone(this.waystoneData));
                });
            }
        }

        @Override
        public ComponentType getType() {
            return HologramComponent.this;
        }

        private ComponentData oldComponentData;
        private WaystoneState oldWaystoneState;
        public void update(Player player) {
            if (oldComponentData == null || oldWaystoneState == null) return;
            update(oldComponentData, oldWaystoneState, player);
        }

        @Override
        public void update(ComponentData data, WaystoneState state, Player player) {
            oldComponentData = data;
            oldWaystoneState = state;

            boolean isActive = state == WaystoneState.ACTIVE;
            Placeholder placeholder = new Placeholder();
            placeholder.putContent(Placeholder.PLAYER, player);
            placeholder.putContent(Placeholder.WAYSTONE, waystoneData);
            placeholder.put("waystone_hologram_status", ph -> isActive ? "{language.hologram.active}" : "{language.hologram.inactive}");
            ArmorStandMeta meta = ClientSideStandardModel.createHologramMeta();
            meta.setCustomName(placeholder.replace("{language.hologram.name}"));
            hologramTop.update(player, meta);

            if (!runningTask.containsKey(player)) {
                meta = ClientSideStandardModel.createHologramMeta();
                meta.setCustomName(isActive ? placeholder.replace("{language.hologram.active}") : placeholder.replace("{language.hologram.tip-click}"));
                hologramBottom.updateMetadata(player, meta);
            }
        }

        @Override
        public void destroyImmediately() {
            destroy();
        }

        @Override
        public void destroy() {
            hologramBottom.remove();
            hologramTop.remove();
        }

        public void sendNoAccess(Player player) {
            Placeholder placeholder = new Placeholder();
            placeholder.putContent(Placeholder.PLAYER, player)
                    .putContent(Placeholder.WAYSTONE, waystoneData);
            NoAccessAnimation current = new NoAccessAnimation(player, placeholder);
            NoAccessAnimation old = runningTask.put(player, current);
            if (old != null) old.cancel();
            current.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
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
                Location loc = ((LocalLocation) waystoneData.getLocation()).getLocation();
                if (!player.isOnline() || player.getWorld() != loc.getWorld() || player.getLocation().distance(loc) > 10 || shift > 150) {
                    stop();
                    return;
                }
                if (tickTime % 2 == 0) {
                    ArmorStandMeta meta = ClientSideStandardModel.createHologramMeta();
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
