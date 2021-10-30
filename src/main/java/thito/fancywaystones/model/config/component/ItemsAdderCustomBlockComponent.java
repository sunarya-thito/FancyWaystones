package thito.fancywaystones.model.config.component;

import dev.lone.itemsadder.api.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.model.config.*;

public class ItemsAdderCustomBlockComponent implements ComponentType {
    private ItemsAdderCustomBlockComponentData defaultData = new ItemsAdderCustomBlockComponentData(new ComponentData(new StyleRuleCompound(), new MapSection()));

    @Override
    public ItemsAdderCustomBlockComponentData getDefaultData() {
        return defaultData;
    }

    @Override
    public void bakeData(ComponentData[] componentData) {
        for (int i = 0; i < componentData.length; i++) componentData[i] = new ItemsAdderCustomBlockComponentData(componentData[i]);
    }

    @Override
    public ComponentHandler createHandler(WaystoneData waystoneData, Component component) {
        return new Handler(component, waystoneData);
    }

    public static class ItemsAdderCustomBlockComponentData extends ComponentData {
//        private CustomBlock customBlock;
        private String namespaceId;
        public ItemsAdderCustomBlockComponentData(ComponentData other) {
            super(other);
            namespaceId = getConfig().getString("custom-block").orElse(null);
//            customBlock = namespacedID == null ? null : CustomBlock.getInstance(namespacedID);
        }


        public String getNamespaceId() {
            return namespaceId;
        }

//        public CustomBlock getCustomBlock() {
//            return customBlock;
//        }
    }

    public class Handler implements ComponentHandler {
        private Component component;
        private CustomBlock oldCustomBlock;

        public Handler(Component component, WaystoneData waystoneData) {
            this.component = component;
            update(component.requestData(waystoneData, WaystoneState.INACTIVE), WaystoneState.INACTIVE, null);
        }

        @Override
        public boolean hasBlockHitBox() {
            return true;
        }

        @Override
        public ComponentType getType() {
            return ItemsAdderCustomBlockComponent.this;
        }

        @Override
        public void update(ComponentData data, WaystoneState state, Player player) {
            if (!Bukkit.isPrimaryThread()) {
                if (!FancyWaystones.getPlugin().isEnabled()) return;
                ComponentData finalData = data;
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> update(finalData, state, player));
                return;
            }
            String nm = ((ItemsAdderCustomBlockComponentData) data).getNamespaceId();
            if (nm == null) return;
            CustomBlock customBlock = CustomBlock.getInstance(nm);
            if (customBlock != null && oldCustomBlock != customBlock && !customBlock.isPlaced()) {
                customBlock.place(component.getLocation());
                oldCustomBlock = customBlock;
            }
        }

        @Override
        public void destroyImmediately() {
            // DO NOTHING AS IT REQUIRES TO BE EXECUTED IN DIFFERENT THREAD
        }

        @Override
        public void destroy() {
            if (!Bukkit.isPrimaryThread()) {
                if (!FancyWaystones.getPlugin().isEnabled()) return;
                Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), this::destroy);
                return;
            }
            if (oldCustomBlock != null) {
                oldCustomBlock.remove();
            }
        }
    }
}
