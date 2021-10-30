package thito.fancywaystones.model.config;

import org.bukkit.*;
import org.bukkit.util.Vector;
import thito.fancywaystones.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.location.*;

import java.util.*;
import java.util.logging.Level;

public class ComponentConfig {
    private ComponentType type;
    private Vector offset;
    private float yaw, pitch;
    private ComponentData[] data;
    public ComponentConfig(Section section) {
        type = WaystoneManager.getManager().getComponentTypeMap().get(section.getString("type").orElse(null));
        offset = new Vector(section.getDouble("offset.x").orElse(0d),
                section.getDouble("offset.y").orElse(0d),
                section.getDouble("offset.z").orElse(0d));
        yaw = section.getFloat("offset.yaw").orElse(0f);
        pitch = section.getFloat("offset.pitch").orElse(0f);
        List<ComponentData> componentDataList = new ArrayList<>();
        MapSection componentsSection = section.getMap("data").orElse(MapSection.empty());
        for (String key : componentsSection.keySet()) {
            MapSection conf = componentsSection.getMap(key).orElse(null);
            if (conf == null) continue;
            StyleRuleCompound compound = StyleRuleCompound.parse(key);
            ComponentData componentData = new ComponentData(compound, conf);
            componentDataList.add(componentData);
        }
        data = componentDataList.toArray(new ComponentData[0]);
        if (type == null) {
            FancyWaystones.getPlugin().getLogger().log(Level.SEVERE, "Failed to find type "+section.getString("type").orElse(null));
            return;
        }
        type.bakeData(data);
    }

    public Vector getOffset() {
        return offset;
    }

    public Component createComponent(WaystoneData waystoneData) {
        Location loc = ((LocalLocation) waystoneData.getLocation()).getLocation().clone().add(offset);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        Component component = new Component(loc, data);
        component.setHandler(type.createHandler(waystoneData, component));
        return component;
    }
}
