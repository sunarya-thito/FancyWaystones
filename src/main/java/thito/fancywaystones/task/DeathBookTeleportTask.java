package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;

import java.util.Comparator;
import java.util.*;

public class DeathBookTeleportTask implements Runnable {

    private FancyWaystones plugin;
    private Location center;
    private Player player;
    private int checkRadius, checkHeight, noDamageTick;
    private boolean force;

    public DeathBookTeleportTask(FancyWaystones plugin, Player player, Location center, int checkRadius, int checkHeight, boolean force) {
        this.plugin = plugin;
        this.center = center;
        this.player = player;
        this.checkRadius = checkRadius;
        this.checkHeight = checkHeight;
        this.force = force;
    }

    public void setNoDamageTicks(int noDamageTick) {
        this.noDamageTick = noDamageTick;
    }

    public int getNoDamageTicks() {
        return noDamageTick;
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
    }

    Map<Long, ChunkLoadRequest> map = new HashMap<>();

    @Override
    public void run() {
        WaystoneModel model = WaystoneManager.getManager().getDefaultModel();
        double minX = center.getBlockX() - model.getMinX() - checkRadius;
        double maxX = center.getBlockX() + model.getMaxX() + checkRadius;
        double minY = center.getBlockY() - model.getMinY() - checkHeight;
        double maxY = center.getBlockY() + model.getMaxY() + checkHeight;
        double minZ = center.getBlockZ() - model.getMinZ() - checkRadius;
        double maxZ = center.getBlockZ() + model.getMaxZ() + checkRadius;
        double minXBox = center.getBlockX() - model.getMinX();
        double maxXBox = center.getBlockX() + model.getMaxX();
        double minYBox = center.getBlockY() - model.getMinY();
        double maxYBox = center.getBlockY() + model.getMaxY();
        double minZBox = center.getBlockZ() - model.getMinZ();
        double maxZBox = center.getBlockZ() + model.getMaxZ();
        for (double x = minX; x <= maxX; x++) {
            for (double z = minZ; z <= maxZ; z++) {
                int chunkX = (int)x >> 4;
                int chunkZ = (int)z >> 4;
                long xz = Util.getXY(chunkX, chunkZ);
                ChunkLoadRequest request = map.computeIfAbsent(xz, key -> new ChunkLoadRequest(chunkX, chunkZ));
                for (double y = minY; y <= maxY; y++) {
                    if (x >= minXBox && x <= maxXBox && y >= minYBox && y <= maxYBox && z >= minZBox && z <= maxZBox) continue;
                    Location loc = new Location(center.getWorld(), x, y, z);
                    request.checkBlock.add(loc);
                }
            }
        }
        List<ChunkLoadRequest> chunkLoadRequests = new ArrayList<>(map.values());
        chunkLoadRequests.forEach(ChunkLoadRequest::sort);
        chunkLoadRequests.sort(Comparator.comparingDouble(ChunkLoadRequest::getDistance));
        streamLoad(chunkLoadRequests.iterator());
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
            List<ChunkLoadRequest> chunkLoadRequests = new ArrayList<>(map.values());
            chunkLoadRequests.forEach(ChunkLoadRequest::sort);
            chunkLoadRequests.sort(Comparator.comparingDouble(ChunkLoadRequest::getDistance));
            streamBlock(chunkLoadRequests.iterator());
        }
    }
    private boolean isUnsafe(Material type) {
        return type.name().contains("LAVA") || type.name().equals("CACTUS") || type.name().equals("MAGMA_BLOCK") || type.name().equals("MAGMA");
    }

    private void streamBlock(Iterator<ChunkLoadRequest> iterator) {
        if (iterator.hasNext()) {
            ChunkLoadRequest request = iterator.next();
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (Location check : request.checkBlock) {
                        Block block = request.getBlock(check);
                        if (block != null && !block.getType().isSolid() && !isUnsafe(block.getType())) {
                            Block above = request.getBlock(check.clone().add(0, 1, 0));
                            Block below = request.getBlock(check.clone().subtract(0, 1, 0));
                            if (below != null && (above == null || (!above.getType().isSolid() && !isUnsafe(above.getType()))) && below.getType().isSolid() && !isUnsafe(below.getType())) {
                                if (plugin.isEnabled()) {
                                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                                        confirmTeleport(block.getLocation().clone().add(.5, 0, .5));
                                    });
                                }
                                return;
                            }
                        }
                    }
                    streamBlock(iterator);
                });
            }
        } else {
            cancelTeleportation();
        }
    }

    private void confirmTeleport(Location safePlace) {
        player.setNoDamageTicks(player.getNoDamageTicks() + getNoDamageTicks());
        player.teleport(safePlace);
        player.sendMessage(new Placeholder()
                .putContent(Placeholder.PLAYER, player)
                .replace("{language.teleported-death}"));
        FancyWaystones.getPlugin().postTeleport("Death Book", player, null, null);
    }

    private void cancelTeleportation() {
        if (force) {
            if (plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                    player.setNoDamageTicks(player.getNoDamageTicks() + getNoDamageTicks());
                    player.teleport(center);
                });
            }
        } else {
            player.sendMessage(new Placeholder().putContent(Placeholder.PLAYER, player)
                    .replace("{language.unsafe-destination}"));
        }
    }

    private class ChunkLoadRequest {
        int x, z;
        List<Location> checkBlock = new ArrayList<>();
        Chunk loaded;

        public ChunkLoadRequest(int x, int z) {
            this.x = x;
            this.z = z;
        }

        void sort() {
            checkBlock.sort(Comparator.comparingDouble(x -> x.distance(center)));
        }

        public double getDistance() {
            return Math.sqrt(Math.pow(x - (center.getBlockX() >> 4), 2) + Math.pow(z - (center.getBlockZ() >> 4), 2));
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
