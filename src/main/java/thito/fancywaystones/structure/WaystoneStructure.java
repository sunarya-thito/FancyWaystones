package thito.fancywaystones.structure;

import org.bukkit.World;
import thito.fancywaystones.WaystoneModel;
import thito.fancywaystones.WaystoneType;

import java.util.Collections;
import java.util.List;

public class WaystoneStructure {
    private StructureSpawnCondition spawnCondition;
    private World.Environment environment;
    private WaystoneType waystoneType;
    private WaystoneModel model;
    private Structure structure;
    private List<String> names;

    public WaystoneStructure(StructureSpawnCondition spawnCondition, World.Environment environment, WaystoneType waystoneType, WaystoneModel model, Structure structure, List<String> names) {
        this.spawnCondition = spawnCondition;
        this.environment = environment;
        this.waystoneType = waystoneType;
        this.model = model;
        this.structure = structure;
        this.names = names;
    }

    public List<String> getNames() {
        return names == null ? null : Collections.unmodifiableList(names);
    }

    public StructureSpawnCondition getSpawnCondition() {
        return spawnCondition;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public WaystoneType getWaystoneType() {
        return waystoneType;
    }

    public WaystoneModel getModel() {
        return model;
    }

    public Structure getStructure() {
        return structure;
    }
}
