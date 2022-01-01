package thito.fancywaystones.structure;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import thito.fancywaystones.*;
import thito.fancywaystones.task.PlaceWaystoneTask;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class WaystoneStructureListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        StructureManager structureManager = StructureManager.getInstance();
        if (!structureManager.isEnable()) return;
        FancyWaystones.getPlugin().getStructureService().submit(() -> {
            try {
                if (!structureManager.hasBeenGenerated(event.getChunk()) && FancyWaystones.getPlugin().isEnabled()) {
                    structureManager.getStructureWorldData().add(event.getChunk());
                    Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                        for (WaystoneStructure waystoneStructure : structureManager.getStructureSet()) {
                            StructureSpawnCondition spawnCondition = waystoneStructure.getSpawnCondition();
                            if (spawnCondition.isOnlyNewChunks() && !event.isNewChunk()) continue;
                            Location location = spawnCondition.canSpawn(waystoneStructure.getStructure(), event.getChunk());
                            if (location != null) {
                                Structure structure = waystoneStructure.getStructure();
                                structure.placeAt(location, (loc, structureBlock) -> {
                                    if (structureBlock.getType().equals("BEDROCK")) {
                                        WaystoneData waystoneData = WaystoneManager.getManager().createData(
                                                waystoneStructure.getWaystoneType(),
                                                waystoneStructure.getEnvironment(),
                                                waystoneStructure.getModel()
                                        );
                                        FancyWaystones.getPlugin().submitIO(() -> {
                                            waystoneData.getStatistics().setDateCreated(System.currentTimeMillis());
                                            waystoneData.setOwnerName("?");
                                            waystoneData.setOwnerUUID(new UUID(0, 0));
                                            List<String> autoNames = waystoneStructure.getNames();
                                            if (autoNames != null && !autoNames.isEmpty()) {
                                                Random random = new Random();
                                                String name = autoNames.get(random.nextInt(autoNames.size()));
                                                waystoneData.setName(name);
                                            }
                                            WaystoneManager.getManager().getLoadedData().add(waystoneData);
                                            WaystoneManager.getManager().placeWaystone(waystoneData, loc);
                                            WaystoneManager.getManager().saveWaystone(waystoneData);
                                            if (FancyWaystones.getPlugin().isInformStructureGeneration()) {
                                                FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Waystone Structure spawned at "+location.getWorld().getName()+" "+location.getBlockX()+" "+location.getBlockY()+" "+location.getBlockZ());
                                            }
                                        });
                                        return false;
                                    }
                                    return true;
                                });
                                return;
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
