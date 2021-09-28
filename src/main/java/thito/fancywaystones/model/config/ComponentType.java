package thito.fancywaystones.model.config;

import thito.fancywaystones.*;

public interface ComponentType {
    void bakeData(ComponentData[] componentData);
    ComponentData getDefaultData();
    ComponentHandler createHandler(WaystoneData waystoneData, Component component);
}
