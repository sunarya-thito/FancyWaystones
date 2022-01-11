package thito.fancywaystones.model.config.component;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import thito.fancywaystones.Util;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.model.config.*;

public class ModelEngineComponent implements ComponentType {
    private ModelEngineComponentData defaultData;

    public ModelEngineComponent() {
        defaultData = new ModelEngineComponentData(new ComponentData(new StyleRuleCompound(), new MapSection()));
    }

    @Override
    public void bakeData(ComponentData[] componentData) {
        for (int i = 0; i < componentData.length; i++) {
            componentData[i] = new ModelEngineComponentData(componentData[i]);
        }
    }

    @Override
    public ComponentData getDefaultData() {
        return defaultData;
    }

    @Override
    public ComponentHandler createHandler(WaystoneData waystoneData, Component component) {
        return new Handler(waystoneData, component);
    }

    public static class ModelEngineComponentData extends ComponentData {
        private final EntityType type;
        private final boolean disableInteraction;
        private final ActiveModel activeModel;

        public ModelEngineComponentData(ComponentData other) {
            super(other);
            type = EntityType.valueOf(other.getConfig().getString("entity-type").orElse("ARMOR_STAND"));
            disableInteraction = other.getConfig().getBoolean("disable-interaction").orElse(true);
            activeModel = ModelEngineAPI.api.getModelManager().createActiveModel(other.getConfig().getString("active-model-id").orElse(null));
            if (activeModel == null) throw new IllegalArgumentException("Cannot find ActiveModel with Id "+other.getConfig().getString("active-model-id").orElse(null));
        }

        public ActiveModel getActiveModel() {
            return activeModel;
        }

        public boolean isDisableInteraction() {
            return disableInteraction;
        }

        public EntityType getType() {
            return type;
        }
    }

    public class Handler implements ComponentHandler {
        private WaystoneData waystoneData;
        private Component component;
        private Entity entity;
        private ModeledEntity modeledEntity;

        public Handler(WaystoneData waystoneData, Component component) {
            this.waystoneData = waystoneData;
            this.component = component;
        }

        @Override
        public ComponentType getType() {
            return ModelEngineComponent.this;
        }

        @Override
        public void update(ComponentData data, WaystoneState state, Player player) {
            Util.submitSync(() -> {
                if (entity == null) {
                    ModelEngineComponentData d = (ModelEngineComponentData) data;
                    entity = component.getLocation().getWorld().spawnEntity(component.getLocation(), d.getType());
                    if (d.isDisableInteraction()) {
                        if (entity instanceof LivingEntity) {
                            try {
                                ((LivingEntity) entity).setAI(false);
                            } catch (Throwable ignored) {
                            }
                        }
                        try {
                            entity.setGravity(false);
                        } catch (Throwable ignored) {
                        }
                    }
                    modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(entity);
                    modeledEntity.addActiveModel(d.getActiveModel());
                }
            });
        }

        @Override
        public void destroy() {
            Util.submitSync(() -> {
                if (entity != null) {
                    entity.remove();
                }
            });
        }

        @Override
        public void destroyImmediately() {
            if (Bukkit.isPrimaryThread()) {
                if (entity != null) {
                    entity.remove();
                }
            } else {
                Util.submitSync(() -> {
                    if (entity != null) {
                        entity.remove();
                    }
                });
            }
        }

        @Override
        public boolean hasBlockHitBox() {
            return false;
        }
    }
}
