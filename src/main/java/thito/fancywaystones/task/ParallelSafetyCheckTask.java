package thito.fancywaystones.task;

import org.bukkit.*;
import thito.fancywaystones.*;

import java.util.*;

public abstract class ParallelSafetyCheckTask implements Runnable {

//    private WaystoneModelHandler modelHandler;
    private Location location;
    private int checkRadius, checkHeight;

    public ParallelSafetyCheckTask(Location location, int checkRadius, int checkHeight) {
//        this.modelHandler = handler;
        this.location = location;
        this.checkRadius = checkRadius;
        this.checkHeight = checkHeight;
    }

    private List<ChunkCheckTask> checkTaskList = new ArrayList<>();

    private Iterator<ChunkCheckTask> iterator;
    private Location safest;
    private double safestDistance = Double.MAX_VALUE;

    @Override
    public void run() {
        double minX = location.getBlockX() - checkRadius;
        double maxX = location.getBlockX() + checkRadius;
        double minY = location.getBlockY() - checkHeight;
        double maxY = location.getBlockY() + checkHeight;
        double minZ = location.getBlockZ() - checkRadius;
        double maxZ = location.getBlockZ() + checkRadius;
        for (double x = minX; x <= maxX; x++) {
            for (double z = minZ; z <= maxZ; z++) {
                int chunkX = (int)x >> 4;
                int chunkZ = (int)z >> 4;
                double finalX = x;
                double finalZ = z;
                ChunkCheckTask chunkCheckTask = new ChunkCheckTask(location.getWorld(), chunkX, chunkZ) {
                    @Override
                    protected void done() {
                        for (double y = minY; y <= maxY; y++) {
                            double finalY = y;
                            PositionSafetyCheckTask positionSafetyCheckTask = new PositionSafetyCheckTask(getChunk(), (int) finalX,  (int) finalY, (int) finalZ) {
                                @Override
                                public void done() {
                                    if (isSafe()) {
                                        Location location = new Location(getChunk().getWorld(), finalX, finalY, finalZ);
                                        double distance = distance(location, ParallelSafetyCheckTask.this.location);
                                        if (safest == null || distance < safestDistance) {
                                            safest = location;
                                            safestDistance = distance;
                                        }
                                    }
                                }
                            };
                            if (FancyWaystones.getPlugin().isEnabled()) {
                                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), positionSafetyCheckTask);
                            }
                        }
                        checkNext();
                    }
                };
                checkTaskList.add(chunkCheckTask);
            }
        }
        iterator = checkTaskList.iterator();
        checkNext();
    }

    public Location getSafest() {
        return safest;
    }

    protected abstract void proceed();

    protected void checkNext() {
        if (iterator.hasNext()) {
            ChunkCheckTask task = iterator.next();
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
