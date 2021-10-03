package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.entity.*;

public abstract class WaystoneModelHandler {
    private WaystoneModel model;

    public WaystoneModelHandler(WaystoneModel model) {
        this.model = model;
    }

    public WaystoneModel getModel() {
        return model;
    }

    public double getMinX() {
        return model.getMinX();
    }

    public double getMinY() {
        return model.getMinY();
    }

    public double getMinZ() {
        return model.getMinZ();
    }

    public double getMaxX() {
        return model.getMaxX();
    }

    public double getMaxY() {
        return model.getMaxY();
    }

    public double getMaxZ() {
        return model.getMaxZ();
    }

    public abstract WaystoneData getData();
    public abstract void destroy();
    public abstract void destroyImmediately();
    public abstract boolean isPart(Location loc);
    public abstract void update(Player player);
    public abstract void update();
    public abstract void sendNoAccess(Player player);
}
