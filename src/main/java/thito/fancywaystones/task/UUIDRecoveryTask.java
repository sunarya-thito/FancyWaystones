package thito.fancywaystones.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import thito.fancywaystones.FancyWaystones;
import thito.fancywaystones.ServerUUID;
import thito.fancywaystones.WaystoneManager;
import thito.fancywaystones.WaystoneType;
import thito.fancywaystones.config.Section;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UUIDRecoveryTask extends BukkitRunnable {
    @Override
    public void run() {
        for (WaystoneType type : WaystoneManager.getManager().getTypes()) {
            List<byte[]> bytes = WaystoneManager.getManager().getStorage().readWaystones(type);
            for (byte[] r : bytes) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(r);
                Section section = Section.parseToMap(new InputStreamReader(byteArrayInputStream));
                String worldUID = section.getString("worldUID").orElse(null);
                if (worldUID != null)  {
                    try {
                        UUID uuid = UUID.fromString(worldUID);
                        if (Bukkit.getWorld(uuid) != null) {
                            String serverName = section.getString("serverName").orElse(null);
                            if (serverName != null) {
                                UUID serverID = UUID.fromString(serverName);
                                found(serverID);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        notFound();
    }

    public void notFound() {

    }

    public void found(UUID serverName) throws IOException {
        ServerUUID serverUUID = FancyWaystones.getPlugin().getServerUUID();
        serverUUID.setId(serverName);
        serverUUID.save();
    }
}
