package thito.fancywaystones.proxy;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import thito.fancywaystones.EntityStream;

import java.io.Serializable;

public class SerializableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private byte[] nbt;

    public SerializableEntity(EntityType type, byte[] nbt) {
        this.type = type.name();
        this.nbt = nbt;
    }

    public String getType() {
        return type;
    }

    public byte[] getNbt() {
        return nbt;
    }

    public void spawn(Location location) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(type);
        } catch (Throwable ignored) {
            return;
        }
        Entity entity = location.getWorld().spawnEntity(location, entityType);
        EntityStream.deserializeEntity(entity, nbt);
    }
}
