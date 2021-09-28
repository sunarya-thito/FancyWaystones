package thito.fancywaystones;

import java.io.*;
import java.util.*;

public interface WaystoneStorage {
    void removePlayerData(UUID id);
    void removeWaystoneData(UUID id);
    byte[] readWaystoneData(UUID id) throws IOException;
    byte[] readPlayerData(UUID id) throws IOException;
    void writeWaystoneData(WaystoneType type, UUID id, byte[] data) throws IOException;
    void writePlayerData(UUID id, byte[] data) throws IOException;
    List<byte[]> readWaystones(WaystoneType type);
    boolean containsName(String contextName, String name);
    void putName(String contextName, String name);
    void removeName(String contextName, String name);
    void close();
    void migrateTo(WaystoneStorage storage);
}
