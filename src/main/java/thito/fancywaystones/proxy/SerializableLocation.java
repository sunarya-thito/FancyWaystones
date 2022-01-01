package thito.fancywaystones.proxy;

import java.io.*;
import java.util.*;

public class SerializableLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID waystoneId;
    private String serverName;
    private UUID worldName;
    private double x, y, z;

    public SerializableLocation(UUID waystoneId, String serverName, UUID worldName, double x, double y, double z) {
        this.waystoneId = waystoneId;
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID getWaystoneId() {
        return waystoneId;
    }

    public String getServerName() {
        return serverName;
    }

    public UUID getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "SerializableLocation{" +
                "waystoneId=" + waystoneId +
                ", serverName='" + serverName + '\'' +
                ", worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
