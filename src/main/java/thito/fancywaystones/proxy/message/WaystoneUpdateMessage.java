package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneUpdateMessage extends Message {
    private Set<UUID> load, unload, refresh;

    public WaystoneUpdateMessage(Set<UUID> load, Set<UUID> unload, Set<UUID> refresh) {
        this.load = load;
        this.unload = unload;
        this.refresh = refresh;
    }

    public Set<UUID> getRefresh() {
        return refresh;
    }

    public Set<UUID> getLoad() {
        return load;
    }

    public Set<UUID> getUnload() {
        return unload;
    }
}
