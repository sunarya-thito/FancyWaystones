package thito.fancywaystones;

import java.util.*;

public abstract class WaystoneModel {
    public static final List<WaystoneModelHandler> ACTIVE_HANDLERS = Collections.synchronizedList(new ArrayList<>());

    public abstract String getId();
    public abstract String getName();

    protected double minX, minY, minZ, maxX, maxY, maxZ;

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public abstract WaystoneModelHandler createHandler(WaystoneData waystoneData);

}
