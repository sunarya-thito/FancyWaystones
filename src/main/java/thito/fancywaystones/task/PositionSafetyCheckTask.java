package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.block.*;

public abstract class PositionSafetyCheckTask implements Runnable {

    private Chunk chunk;
    private int x, y, z;

    private boolean safe;

    public PositionSafetyCheckTask(Chunk chunk, int x, int y, int z) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void run() {
        Block current = getBlock(x, y, z);
        Material type = current.getType();
        if (!isUnsafe(type) && !type.isSolid()) {
            Block above = getBlock(x, y + 1, z);
            Material aboveType = above.getType();
            if (!isUnsafe(aboveType) && !aboveType.isSolid()) {
                Block below = getBlock(x, y - 1, z);
                Material belowType = below.getType();
                if (!isUnsafe(belowType) && belowType.isSolid()) {
                    safe = true;
                }
            }
        }
        done();
    }

    public boolean isSafe() {
        return safe;
    }

    private Block getBlock(int x, int y, int z) {
        return chunk.getBlock(x & 0xF, y, z & 0xF);
    }

    public abstract void done();

    private static boolean isUnsafe(Material type) {
        return type.name().contains("LAVA") || type.name().equals("CACTUS") || type.name().equals("MAGMA_BLOCK") || type.name().equals("MAGMA");
    }
}
