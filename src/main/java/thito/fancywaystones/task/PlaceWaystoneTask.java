package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.proxy.*;

import java.util.*;

public class PlaceWaystoneTask implements Runnable {
    private FancyWaystones plugin;
    private Player player;
    private Location center;
    private WaystoneData data;
    private WaystoneModel model;

    public PlaceWaystoneTask(FancyWaystones plugin, Player player, Location center, WaystoneData data) {
        this.plugin = plugin;
        this.player = player;
        this.center = center;
        this.data = data;
        this.model = data.getModel();
    }

    Map<Long, ChunkLoadRequest> map = new HashMap<>();

    @Override
    public void run() {
        for (double x = center.getBlockX() - model.getMinX(); x <= center.getBlockX() + model.getMaxX(); x++) {
            for (double z = center.getBlockZ() - model.getMinZ(); z <= center.getBlockZ() + model.getMaxZ(); z++) {
                int chunkX = (int)x >> 4;
                int chunkZ = (int)z >> 4;
                long xz = Util.getXY(chunkX, chunkZ);
                ChunkLoadRequest request = map.computeIfAbsent(xz, key -> new ChunkLoadRequest(chunkX, chunkZ));
                for (double y = center.getBlockY() - model.getMinY(); y <= center.getBlockY() + model.getMaxY(); y++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    request.checkBlock.add(loc);
                }
            }
        }
        streamLoad(map.values().iterator());
    }

    private void streamLoad(Iterator<ChunkLoadRequest> iterator) {
        if (iterator.hasNext()) {
            ChunkLoadRequest request = iterator.next();
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    request.loadChunk();
                    streamLoad(iterator);
                });
            }
        } else {
            streamBlock(map.values().iterator());
        }
    }

    private void streamBlock(Iterator<ChunkLoadRequest> iterator) {
        if (iterator.hasNext()) {
            ChunkLoadRequest request = iterator.next();
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (Location l : request.checkBlock) {
                        Block block = request.getBlock(l);
                        if (block == null) {
                            cancel();
                            return;
                        }
                        Material type = block.getType();
                        String name = type.name();
                        if (!name.equals("AIR") && !name.equals("CAVE_AIR")) {
                            cancel();
                            return;
                        }
                    }
                    streamBlock(iterator);
                });
            }
        } else {
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTask(plugin, this::confirm);
            }
        }
    }

    private void confirm() {
        data.getStatistics().setDateCreated(System.currentTimeMillis());
        FancyWaystones.getPlugin().submitIO(() -> {
            WaystoneManager.getManager().placeWaystone(data, center);
            WaystoneManager.getManager().saveWaystone(data);
            ProxyWaystone pw = plugin.getProxyWaystone();
            if (pw != null && data.getType().isAlwaysListed()) {
                pw.dispatchWaystoneLoad(data.getUUID());
            }
        });
    }

    private void cancel() {
        FancyWaystones.getPlugin().submitIO(() -> {
            WaystoneManager.getManager().directUnloadData(data);
        });
        Placeholder placeholder = new Placeholder();
        placeholder.putContent(Placeholder.PLAYER, player);
        placeholder.putContent(Placeholder.WAYSTONE, data);
        player.sendMessage(placeholder.replace("{language.not-enough-space-place}"));
        WaystoneManager.getManager().createWaystoneItem(data, true, result -> {
            Util.placeInHand(player, result);
        });
    }

    private class ChunkLoadRequest {
        int x, z;
        List<Location> checkBlock = new ArrayList<>();
        Chunk loaded;

        public ChunkLoadRequest(int x, int z) {
            this.x = x;
            this.z = z;
        }

        void loadChunk() {
            loaded = attemptLoad();
        }

        Block getBlock(Location loc) {
            int x = loc.getBlockX() & 0xF;
            int y = loc.getBlockY();
            int z = loc.getBlockZ() & 0xF;
            if (y >= loc.getWorld().getMaxHeight() || y < 0) return null;
            return loaded.getBlock(x, y, z);
        }

        private Chunk attemptLoad() {
            return center.getWorld().getChunkAt(x, z);
        }
    }
}
