package thito.fancywaystones.structure;

import org.bukkit.Chunk;
import org.bukkit.World;
import thito.fancywaystones.WaystoneManager;
import thito.fancywaystones.WaystoneModel;
import thito.fancywaystones.WaystoneType;
import thito.fancywaystones.config.ListSection;
import thito.fancywaystones.config.Section;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class StructureManager {
    private static final StructureManager instance = new StructureManager();

    public static StructureManager getInstance() {
        return instance;
    }

    private boolean enable;
    private StructureWorldData structureWorldData;
    private Map<String, Structure> structureMap = new HashMap<>();
    private Set<WaystoneStructure> structureSet = new HashSet<>();

    public StructureWorldData getStructureWorldData() {
        return structureWorldData;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setStructureWorldData(StructureWorldData structureWorldData) {
        this.structureWorldData = structureWorldData;
    }

    public boolean hasBeenGenerated(Chunk chunk) throws IOException {
        return structureWorldData == null || structureWorldData.contains(chunk);
    }

    public WaystoneStructure loadWaystoneStructure(Section section) {
        WaystoneType waystoneType = WaystoneManager.getManager().getType(section.getString("Waystone Type").orElse(null));
        if (waystoneType == null) return null;
        World.Environment environment;
        try {
            environment = World.Environment.valueOf(section.getString("Waystone Environment").orElse("NORMAL"));
        } catch (Throwable t) {
            return null;
        }
        WaystoneModel model = WaystoneManager.getManager().getModelMap().get(section.getString("Waystone Model").orElse(null));
        if (model == null) return null;
        Structure structure = getStructure(section.getString("Structure Name").orElse(null));
        if (structure == null) return null;
        Section conditionSection = section.getMap("Condition").orElse(null);
        if (conditionSection == null) return null;
        StructureSpawnCondition spawnCondition = new StructureSpawnCondition();
        spawnCondition.loadFromConfig(conditionSection);
        return new WaystoneStructure(spawnCondition, environment, waystoneType, model, structure, section.getList("Auto Rename").orElse(new ListSection()).stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public Map<String, Structure> getStructureMap() {
        return Collections.unmodifiableMap(structureMap);
    }

    public void addWaystoneStructure(WaystoneStructure waystoneStructure) {
        structureSet.add(waystoneStructure);
    }

    public void removeWaystoneStructure(WaystoneStructure waystoneStructure) {
        structureSet.remove(waystoneStructure);
    }

    public void clearWaystoneStructures() {
        structureSet.clear();
    }

    public void clearStructures() {
        structureSet.clear();
    }

    public Set<WaystoneStructure> getStructureSet() {
        return Collections.unmodifiableSet(structureSet);
    }

    public Structure getStructure(String name) {
        return structureMap.get(name);
    }

    public Structure createStructure(String name, Selection selection) {
        Structure structure = new Structure();
        structure.fromSelection(selection);
        structureMap.put(name, structure);
        return structure;
    }

    public Structure loadFromInputStream(String name, InputStream inputStream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Structure structure = (Structure) objectInputStream.readObject();
            structureMap.put(name, structure);
            return structure;
        }
    }

    public void writeToOutputStream(Structure structure, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(structure);
        }
    }
}
