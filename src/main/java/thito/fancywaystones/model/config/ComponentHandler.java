package thito.fancywaystones.model.config;

import org.bukkit.entity.*;

public interface ComponentHandler {
    ComponentType getType();
    void update(ComponentData data, WaystoneState state, Player player);
    void destroy();
    void destroyImmediately();
    boolean hasBlockHitBox();
}
