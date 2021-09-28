package thito.fancywaystones.proxy.message;

import java.io.*;

public class SerializableLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serverName;
    private String worldName;
    private double x, y, z;

    public SerializableLocation(String serverName, String worldName, double x, double y, double z) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getServerName() {
        return serverName;
    }

    public String getWorldName() {
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
}
