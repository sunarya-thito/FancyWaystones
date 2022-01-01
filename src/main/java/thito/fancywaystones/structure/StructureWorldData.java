package thito.fancywaystones.structure;

import org.bukkit.Chunk;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class StructureWorldData {
    private static final int dataLength = 8 + 8 + 4 + 4;
    private FileChannel fileChannel;
    private ByteBuffer byteBuffer;
    public void open(File file) throws IOException {
        fileChannel = FileChannel.open(file.toPath(),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE);
        byteBuffer = ByteBuffer.allocateDirect(dataLength);
    }

    public void add(Chunk chunk) throws IOException {
        if (fileChannel == null) return;
        UUID worldUUID = chunk.getWorld().getUID();
        long least = worldUUID.getLeastSignificantBits();
        long most = worldUUID.getMostSignificantBits();
        int x = chunk.getX();
        int z = chunk.getZ();
        byteBuffer.position(0);
        byteBuffer.putLong(most);
        byteBuffer.putLong(least);
        byteBuffer.putInt(x);
        byteBuffer.putInt(z);
        byteBuffer.position(0);
        fileChannel.position(fileChannel.size());
        fileChannel.write(byteBuffer);
    }

    public boolean contains(Chunk chunk) throws IOException {
        if (fileChannel == null) return true;
        fileChannel.position(0);
        while (fileChannel.position() < fileChannel.size()) {
            byteBuffer.position(0);
            fileChannel.read(byteBuffer);
            byteBuffer.position(0);
            long most = byteBuffer.getLong();
            long least = byteBuffer.getLong();
            int x = byteBuffer.getInt();
            int z = byteBuffer.getInt();
            UUID uid = new UUID(most, least);
            if (uid.equals(chunk.getWorld().getUID()) && chunk.getX() == x && chunk.getZ() == z) {
                return true;
            }
        }
        return false;
    }

    public void close() throws IOException {
        if (fileChannel == null) return;
        fileChannel.close();
    }
}
