package thito.fancywaystones.model.config;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;

import java.util.*;

public class Component {
    private ComponentHandler handler;
    private Location location;
    private ComponentData[] data;

    public Component(Location location, ComponentData[] data) {
        this.location = location;
        this.data = data;
    }

    public void setHandler(ComponentHandler handler) {
        this.handler = handler;
    }

    public Location getLocation() {
        return location;
    }

    public ComponentData[] getData() {
        return data;
    }

    public ComponentHandler getHandler() {
        return handler;
    }

    public void destroy() {
        handler.destroy();
    }

    public void destroyImmediately() {
        handler.destroyImmediately();
    }

    public void update(WaystoneData waystoneData, WaystoneState state, Player player) {
        ComponentData data = requestData(waystoneData, state);
        if (data != null) {
            handler.update(data, state, player);
        }
    }

    public ComponentData requestData(WaystoneData waystoneData, WaystoneState state) {
        for (ComponentData data : data) {
            StyleRuleCompound styleRuleCompound = data.getRule();
            if (styleRuleCompound.matches(waystoneData, state)) {
                return data;
            }
        }
        return handler.getType().getDefaultData();
    }

    @Override
    public String toString() {
        return "Component{" +
                "handler=" + handler +
                ", location=" + location +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
