package thito.fancywaystones;

import java.util.*;

public class WaystoneMember {
    private UUID uuid;
    private String name;

    public WaystoneMember(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public WaystoneMember(UUID uuid) {
        this(uuid, "Unknown");
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return "WaystoneMember{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaystoneMember)) return false;
        WaystoneMember that = (WaystoneMember) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
