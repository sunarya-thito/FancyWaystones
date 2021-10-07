package thito.fancywaystones.model.config.component;

import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import thito.fancywaystones.FancyWaystones;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.model.config.*;

public class OraxenCustomBlockComponent implements ComponentType {
    private final OraxenCustomBlockComponentData defaultData = new OraxenCustomBlockComponentData(new ComponentData(new StyleRuleCompound(), new MapSection()));
    @Override
    public void bakeData(ComponentData[] componentData) {
        for (int i = 0; i < componentData.length; i++) componentData[i] = new OraxenCustomBlockComponentData(componentData[i]);
    }

    @Override
    public ComponentData getDefaultData() {
        return defaultData;
    }

    @Override
    public ComponentHandler createHandler(WaystoneData waystoneData, Component component) {
        return new Handler(component, waystoneData);
    }

    public static class OraxenCustomBlockComponentData extends ComponentData {
        private String itemId;
        public OraxenCustomBlockComponentData(ComponentData other) {
            super(other);
            itemId = getConfig().getString("item-id").orElse(null);
        }

        public String getItemId() {
            return itemId;
        }
    }

    public class Handler implements ComponentHandler {
        private Component component;
        private WaystoneData waystoneData;

        public Handler(Component component, WaystoneData waystoneData) {
            this.component = component;
            this.waystoneData = waystoneData;
        }

        @Override
        public ComponentType getType() {
            return OraxenCustomBlockComponent.this;
        }

        @Override
        public void update(ComponentData data, WaystoneState state, Player player) {
            if (!Bukkit.isPrimaryThread()) {
                if (!FancyWaystones.getPlugin().isEnabled()) return;
                ComponentData finalData = data;
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> update(finalData, state, player));
                return;
            }
            if (data != null) {
                BlockMechanicFactory.setBlockModel(component.getLocation().getBlock(), ((OraxenCustomBlockComponentData) data).itemId);
            }
        }

        @Override
        public void destroy() {
            if (!Bukkit.isPrimaryThread()) {
                if (!FancyWaystones.getPlugin().isEnabled()) return;
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), this::destroy);
                return;
            }
            component.getLocation().getBlock().setType(Material.AIR);
        }

        @Override
        public void destroyImmediately() {
        }

        @Override
        public boolean hasBlockHitBox() {
            return true;
        }
    }
}
