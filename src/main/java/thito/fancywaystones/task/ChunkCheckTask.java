package thito.fancywaystones.task;

import org.bukkit.*;

public abstract class ChunkCheckTask implements Runnable {
    private Chunk chunk;

    private World world;
    private int x, z;

    public ChunkCheckTask(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public void run() {
        chunk = world.getChunkAt(x, z);
        done();
    }

    public Chunk getChunk() {
        return chunk;
    }

    protected abstract void done();
}
