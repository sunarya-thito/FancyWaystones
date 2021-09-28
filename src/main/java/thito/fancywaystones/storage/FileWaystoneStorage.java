package thito.fancywaystones.storage;

import thito.fancywaystones.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class FileWaystoneStorage implements WaystoneStorage {
    private File waystonesDirectory;
    private File playersDirectory;
    private File namesDirectory;

    public FileWaystoneStorage(File waystonesDirectory, File playersDirectory, File namesDirectory) {
        this.waystonesDirectory = waystonesDirectory;
        this.playersDirectory = playersDirectory;
        this.namesDirectory = namesDirectory;
    }

    @Override
    public void migrateTo(WaystoneStorage storage) {
        for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
            File target = new File(waystonesDirectory, type.name());
            File[] list = target.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.getName().endsWith(".yml")) {
                        try {
                            storage.writeWaystoneData(type, UUID.fromString(f.getName().replace(".yml", "")), Files.readAllBytes(f.toPath()));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
        File[] list = playersDirectory.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.getName().endsWith(".yml")) {
                    try {
                        storage.writePlayerData(UUID.fromString(f.getName().replace(".yml", "")), Files.readAllBytes(f.toPath()));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
        list = namesDirectory.listFiles();
        if (list != null) {
            for (File f : list) {
                try {
                    String[] split = new String(Base64.getUrlDecoder().decode(f.getName())).split("/");
                    if (split.length == 2) {
                        storage.putName(split[0], split[1]);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void removePlayerData(UUID id) {
        File target = new File(playersDirectory, id + ".yml");
        if (target.exists()) target.delete();
    }

    @Override
    public void removeWaystoneData(UUID id) {
        for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
            File target = new File(waystonesDirectory, type.name() + "/" + id + ".yml");
            if (target.exists()) {
                target.delete();
            }
        }
    }

    @Override
    public byte[] readWaystoneData(UUID id) throws IOException {
        for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
            File target = new File(waystonesDirectory, type.name() + "/" + id + ".yml");
            if (target.exists()) {
                return Files.readAllBytes(target.toPath());
            }
        }
        return null;
    }

    @Override
    public byte[] readPlayerData(UUID id) throws IOException {
        File target = new File(playersDirectory, id + ".yml");
        if (target.exists()) {
            return Files.readAllBytes(target.toPath());
        }
        return null;
    }

    @Override
    public void writeWaystoneData(WaystoneType type, UUID id, byte[] data) throws IOException {
        File target = new File(waystonesDirectory, type.name() + "/" + id + ".yml");
        target.getParentFile().mkdirs();
        Files.write(target.toPath(), data);
    }

    @Override
    public void writePlayerData(UUID id, byte[] data) throws IOException {
        File target = new File(playersDirectory, id + ".yml");
        target.getParentFile().mkdirs();
        Files.write(target.toPath(), data);
    }

    @Override
    public List<byte[]> readWaystones(WaystoneType type) {
        File directory = new File(waystonesDirectory, type.name());
        File[] list = directory.listFiles();
        if (list != null) {
            ArrayList<byte[]> dataList = new ArrayList<>();
            for (File f : list) {
                try {
                    dataList.add(Files.readAllBytes(f.toPath()));
                } catch (IOException t) {
                    t.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean containsName(String contextName, String name) {
        return new File(namesDirectory, Base64.getUrlEncoder().encodeToString((contextName + "/" + name.toUpperCase()).getBytes(StandardCharsets.UTF_8))).exists();
    }

    @Override
    public void putName(String contextName, String name) {
        File file = new File(namesDirectory, Base64.getUrlEncoder().encodeToString((contextName + "/" + name.toUpperCase()).getBytes(StandardCharsets.UTF_8)));
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeName(String contextName, String name) {
        File file = new File(name, Base64.getUrlEncoder().encodeToString((contextName + "/" + name.toUpperCase()).getBytes(StandardCharsets.UTF_8)));
        file.delete();
    }
}
