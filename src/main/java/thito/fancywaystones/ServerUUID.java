package thito.fancywaystones;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class ServerUUID {
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void save() throws IOException {
        File target = new File(FancyWaystones.getPlugin().getDataFolder(), "server-uuid.dat");
        target.getParentFile().mkdirs();
        Files.write(target.toPath(), id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void loadOrGenerate() throws IOException {
        File target = new File(FancyWaystones.getPlugin().getDataFolder(), "server-uuid.dat");
        if (target.exists()) {
            id = UUID.fromString(new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8));
        } else {
            target.getParentFile().mkdirs();
            id = UUID.randomUUID();
            Files.write(target.toPath(), id.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
