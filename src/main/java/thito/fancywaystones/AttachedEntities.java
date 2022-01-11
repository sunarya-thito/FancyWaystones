package thito.fancywaystones;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.*;

public class AttachedEntities implements IAttachedEntities {
    private final Set<Entity> entitySet = new HashSet<>();
    private final Set<Action> actionSet = new HashSet<>();

    public Set<Entity> getEntitySet() {
        return entitySet;
    }

    public Set<Action> getActionSet() {
        return actionSet;
    }

    @Override
    public void teleportAndRestore(Location location) {
        teleport(location);
        Util.submitSync(this::restore);
    }

    public void teleport(Location location) {
        entitySet.forEach(entity -> entity.teleport(location));
    }

    private Entity find(UUID id) {
        return entitySet.stream().filter(x -> x.getUniqueId().equals(id)).findAny().orElse(null);
    }

    public void restore() {
        actionSet.forEach(value -> {
            Entity master = find(value.getMaster());
            Entity slave = find(value.getSlave());
            if (master != null && slave != null) {
                value.doAction(master, slave);
            }
        });
    }

    public void collect(Entity entity) {
        if (!entitySet.add(entity)) return;
        for (Entity en : entity.getPassengers()) {
            actionSet.add(new AsPassenger(entity.getUniqueId(), en.getUniqueId()));
            collect(en);
        }
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            actionSet.add(new AsPassenger(vehicle.getUniqueId(), entity.getUniqueId()));
            collect(vehicle);
        }
        if (entity instanceof Player) {
            List<Entity> leashed = WaystoneListener.leashedEntitiesMap.get(entity);
            if (leashed != null) {
                for (Entity l : leashed) {
                    actionSet.add(new AsLeashed(entity.getUniqueId(), l.getUniqueId()));
                    collect(l);
                }
            }
        }
    }

    public interface Action extends Serializable {
        UUID getMaster();
        UUID getSlave();
        void doAction(Entity master, Entity slave);
    }

    public static class AsLeashed implements Action {
        private static final long serialVersionUID = 1L;
        private final UUID master, slave;

        public AsLeashed(UUID master, UUID slave) {
            this.master = master;
            this.slave = slave;
        }

        @Override
        public UUID getMaster() {
            return master;
        }

        @Override
        public UUID getSlave() {
            return slave;
        }

        @Override
        public void doAction(Entity master, Entity slave) {
            if (slave instanceof LivingEntity) {
                ((LivingEntity) slave).setLeashHolder(master);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AsLeashed asLeashed = (AsLeashed) o;
            return master.equals(asLeashed.master) && slave.equals(asLeashed.slave);
        }

        @Override
        public int hashCode() {
            return Objects.hash(master, slave);
        }
    }

    public static class AsPassenger implements Action {
        private static final long serialVersionUID = 1L;
        private final UUID master, slave;

        public AsPassenger(UUID master, UUID slave) {
            this.master = master;
            this.slave = slave;
        }

        @Override
        public UUID getMaster() {
            return master;
        }

        @Override
        public UUID getSlave() {
            return slave;
        }

        @Override
        public void doAction(Entity master, Entity slave) {
            try {
                master.addPassenger(slave);
            } catch (Throwable t) {
                master.setPassenger(slave);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AsPassenger that = (AsPassenger) o;
            return master.equals(that.master) && slave.equals(that.slave);
        }

        @Override
        public int hashCode() {
            return Objects.hash(master, slave);
        }
    }
}
