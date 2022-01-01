package thito.fancywaystones.task;

import org.bukkit.*;
import thito.fancywaystones.*;

import java.util.*;

public abstract class ParallelSafetyCheckTask implements Runnable {

    private Location location;
    private int checkRadius, checkHeight;

    public ParallelSafetyCheckTask(Location location, int checkRadius, int checkHeight) {
        this.location = location;
        this.checkRadius = checkRadius;
        this.checkHeight = checkHeight;
    }

    private int index = 0;
    private List<ChunkCheckTask> checkTaskList;
    private Map<Long, Chunk> cachedChunkMap = new HashMap<>();

    private Location safest;

    @Override
    public void run() {
        index = 0;
        checkTaskList = new ArrayList<>(checkRadius * checkRadius);
        for (int radius = 1; radius <= checkRadius; radius++) {
            int xMin = location.getBlockX() - radius;
            int xMax = location.getBlockX() + radius;
            int zMin = location.getBlockZ() - radius;
            int zMax = location.getBlockZ() + radius;
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    if (x == xMin || x == xMax || z == zMin || z == zMax) {
                        int finalX = x;
                        int finalZ = z;
                        ChunkCheckTask chunkCheckTask = new ChunkCheckTask(cachedChunkMap, location.getWorld(), chunkX, chunkZ) {

                            List<PositionSafetyCheckTask> safetyCheckTaskList;
                            int taskIndex;
                            @Override
                            protected void done() {
                                safetyCheckTaskList = new ArrayList<>((checkHeight + 1) * 2);
                                for (int height = 0; height <= checkHeight; height++) {
                                    int yMin = location.getBlockY() - height;
                                    if (height > 0) {
                                        int yMax = location.getBlockY() + height;
                                        PositionSafetyCheckTask positionSafetyCheckTask2 = new PositionSafetyCheckTask(getChunk(), finalX, yMax, finalZ) {
                                            @Override
                                            public void done() {
                                                if (isSafe()) {
                                                    safest = new Location(getChunk().getWorld(), finalX + 0.5, yMax, finalZ + 0.5);
                                                    proceed();
                                                } else {
                                                    nextTask();
                                                }
                                            }
                                        };
                                        safetyCheckTaskList.add(positionSafetyCheckTask2);
                                    }
                                    PositionSafetyCheckTask positionSafetyCheckTask = new PositionSafetyCheckTask(getChunk(), finalX, yMin, finalZ) {
                                        @Override
                                        public void done() {
                                            if (isSafe()) {
                                                safest = new Location(getChunk().getWorld(), finalX + 0.5, yMin, finalZ + 0.5);
                                                proceed();
                                            } else {
                                                nextTask();
                                            }
                                        }
                                    };

                                    safetyCheckTaskList.add(positionSafetyCheckTask);
                                }
                                nextTask();
                            }
                            private void nextTask() {
                                if (taskIndex >= safetyCheckTaskList.size()) {
                                    nextChunk();
                                    return;
                                }
                                int index = taskIndex++;
                                if (FancyWaystones.getPlugin().isEnabled()) {
                                    Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), safetyCheckTaskList.get(index));
                                }
                            }
                        };
                        checkTaskList.add(chunkCheckTask);
                    }
                }
            }
        }
        nextChunk();
    }

    public Location getSafest() {
        return safest;
    }

    protected abstract void proceed();

    protected void nextChunk() {
        if (index < checkTaskList.size()) {
            ChunkCheckTask task = checkTaskList.get(index++);
            if (task != null && FancyWaystones.getPlugin().isEnabled()) {
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), task);
            }
        } else {
            proceed();
        }
    }

    protected static double distance(Location a, Location b) {
        return a.getWorld() != b.getWorld() ? Double.MAX_VALUE : a.distance(b);
    }

}
