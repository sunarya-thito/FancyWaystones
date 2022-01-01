package thito.fancywaystones.task;

import org.bukkit.*;
import thito.fancywaystones.Util;

import java.util.Map;

public abstract class ChunkCheckTask implements Runnable {
    private Chunk chunk;

    private World world;
    private int x, z;
    private Map<Long, Chunk> cached;

    public ChunkCheckTask(Map<Long, Chunk> cached, World world, int x, int z) {
        this.cached = cached;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public void run() {
        chunk = cached.computeIfAbsent(Util.getXY(x, z), xy -> world.getChunkAt(x, z));
        done();
    }

    public Chunk getChunk() {
        return chunk;
    }

    protected abstract void done();
}
