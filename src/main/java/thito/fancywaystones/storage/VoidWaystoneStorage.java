package thito.fancywaystones.storage;

import thito.fancywaystones.*;

import java.io.*;
import java.util.*;

public class VoidWaystoneStorage implements WaystoneStorage {
    @Override
    public void migrateTo(WaystoneStorage storage) {

    }

    @Override
    public void close() {

    }

    @Override
    public void removePlayerData(UUID id) {
    }

    @Override
    public void removeWaystoneData(UUID id) {
    }

    @Override
    public byte[] readWaystoneData(UUID id) throws IOException {
        return null;
    }

    @Override
    public byte[] readPlayerData(UUID id) throws IOException {
        return null;
    }

    @Override
    public void writeWaystoneData(WaystoneType type, UUID id, byte[] data) throws IOException {
    }

    @Override
    public void writePlayerData(UUID id, byte[] data) throws IOException {
    }

    @Override
    public List<byte[]> readWaystones(WaystoneType type) {
        return Collections.emptyList();
    }

    @Override
    public boolean containsName(String contextName, String name) {
        return true;
    }

    @Override
    public void putName(String contextName, String name) {
    }

    @Override
    public void removeName(String contextName, String name) {
    }
}
