package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneUpdateMessage extends Message {
    private Set<UUID> load, unload;

    public WaystoneUpdateMessage(Set<UUID> load, Set<UUID> unload) {
        this.load = load;
        this.unload = unload;
    }

    public Set<UUID> getLoad() {
        return load;
    }

    public Set<UUID> getUnload() {
        return unload;
    }
}
