package thito.fancywaystones.proxy;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import thito.fancywaystones.AttachedEntities;
import thito.fancywaystones.EntityStream;
import thito.fancywaystones.IAttachedEntities;
import thito.fancywaystones.Util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SerializableAttachedEntities implements Serializable, IAttachedEntities {
    private final Set<SerializableEntity> entitySet = new HashSet<>();
    private final Set<AttachedEntities.Action> actionSet;

    public SerializableAttachedEntities(AttachedEntities attachedEntities) {
        attachedEntities.getEntitySet().forEach(entity -> {
            if (entity instanceof Creature) {
                byte[] nbt = EntityStream.serializeEntity(entity);
                entitySet.add(new SerializableEntity(entity.getType(), nbt));
            }
        });
        actionSet = attachedEntities.getActionSet();
    }

    private static Entity find(Set<Entity> entitySet, UUID uuid) {
        return entitySet.stream().filter(x -> x.getUniqueId().equals(uuid)).findAny().orElse(null);
    }

    @Override
    public void teleportAndRestore(Location location) {
        Set<Entity> spawnedEntities = new HashSet<>();
        for (SerializableEntity entity : entitySet) {
            Util.submitSync(() -> {
                try {
                    EntityType entityType = EntityType.valueOf(entity.getType());
                    Entity ent = location.getWorld().spawnEntity(location, entityType);
                    EntityStream.deserializeEntity(ent, entity.getNbt());
                    spawnedEntities.add(ent);
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
        for (AttachedEntities.Action action : actionSet) {
            Util.submitSync(() -> {
                Entity master = find(spawnedEntities, action.getMaster());
                Entity slave = find(spawnedEntities, action.getSlave());
                if (master != null && slave != null) {
                    action.doAction(master, slave);
                }
            });
        }
    }

}
