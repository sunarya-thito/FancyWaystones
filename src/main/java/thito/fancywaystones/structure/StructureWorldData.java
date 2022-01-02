package thito.fancywaystones.structure;

import org.bukkit.Chunk;
import thito.fancywaystones.FancyWaystones;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class StructureWorldData {

    public static class ChunkData {
        private final long worldUUIDLeast;
        private long worldUUIDMost;
        private int x, y;

        public ChunkData(long worldUUIDLeast, long worldUUIDMost, int x, int y) {
            this.worldUUIDLeast = worldUUIDLeast;
            this.worldUUIDMost = worldUUIDMost;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkData chunkData = (ChunkData) o;
            return worldUUIDLeast == chunkData.worldUUIDLeast && worldUUIDMost == chunkData.worldUUIDMost && x == chunkData.x && y == chunkData.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldUUIDLeast, worldUUIDMost, x, y);
        }
    }

    private static final int dataLength = 8 + 8 + 4 + 4;
    private ArrayList<ChunkData> chunkDataList = new ArrayList<>();
    private FileChannel fileChannel;
    private ByteBuffer byteBuffer;
    private final ReentrantLock lock = new ReentrantLock();
    public void open(File file) throws IOException {
        lock.lock();
        if (fileChannel != null) fileChannel.close();
        chunkDataList.clear();
        fileChannel = FileChannel.open(file.toPath(),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE);
        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Loading generated_structures.bin with "+(fileChannel.size() / dataLength) + " entries");
        chunkDataList.ensureCapacity((int) Math.max(32, fileChannel.size() / dataLength));
        fileChannel.position(0);
        byteBuffer = ByteBuffer.allocateDirect(dataLength);
        while (fileChannel.position() < fileChannel.size()) {
            byteBuffer.position(0);
            fileChannel.read(byteBuffer);
            byteBuffer.position(0);
            long most = byteBuffer.getLong();
            long least = byteBuffer.getLong();
            int x = byteBuffer.getInt();
            int z = byteBuffer.getInt();
            ChunkData e = new ChunkData(least, most, x, z);
            chunkDataList.add(e);
        }
        lock.unlock();
    }

    public void add(Chunk chunk) throws IOException {
        if (fileChannel == null) return;
        lock.lock();
        UUID worldUUID = chunk.getWorld().getUID();
        long least = worldUUID.getLeastSignificantBits();
        long most = worldUUID.getMostSignificantBits();
        int x = chunk.getX();
        int z = chunk.getZ();
        chunkDataList.add(new ChunkData(least, most, x, z));
        lock.unlock();
    }

    public boolean contains(Chunk chunk) throws IOException {
        if (fileChannel == null) return true;
        try {
            lock.lock();
            UUID worldUUID = chunk.getWorld().getUID();
            long least = worldUUID.getLeastSignificantBits();
            long most = worldUUID.getMostSignificantBits();
            int x = chunk.getX();
            int z = chunk.getZ();
            return chunkDataList.contains(new ChunkData(least, most, x, z));
        } finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        if (fileChannel == null) return;
        lock.lock();
        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Saving generated_structures.bin with "+chunkDataList.size()+" entries");
        fileChannel.truncate(0);
        fileChannel.position(0);
        for (ChunkData chunkData : chunkDataList) {
            long least = chunkData.worldUUIDLeast;
            long most = chunkData.worldUUIDMost;
            int x = chunkData.x;
            int z = chunkData.y;
            byteBuffer.position(0);
            byteBuffer.putLong(most);
            byteBuffer.putLong(least);
            byteBuffer.putInt(x);
            byteBuffer.putInt(z);
            byteBuffer.position(0);
            fileChannel.write(byteBuffer);
            fileChannel.position(fileChannel.size());
        }
        chunkDataList.clear();
        fileChannel.close();
        fileChannel = null;
        lock.unlock();
    }
}
