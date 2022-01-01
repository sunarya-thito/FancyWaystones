package thito.fancywaystones.model.config.component;

import org.bukkit.entity.EntityType;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.model.config.Component;
import thito.fancywaystones.model.config.ComponentData;
import thito.fancywaystones.model.config.ComponentHandler;
import thito.fancywaystones.model.config.ComponentType;

public class ModelEngineComponent implements ComponentType {
    @Override
    public void bakeData(ComponentData[] componentData) {

    }

    @Override
    public ComponentData getDefaultData() {
        return null;
    }

    @Override
    public ComponentHandler createHandler(WaystoneData waystoneData, Component component) {
        return null;
    }

    public static class ModelEngineComponentData extends ComponentData {
        private EntityType type;

        public ModelEngineComponentData(ComponentData other) {
            super(other);
        }
    }
}
