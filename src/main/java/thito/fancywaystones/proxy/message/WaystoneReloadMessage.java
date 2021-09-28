package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneReloadMessage extends Message {
    private UUID id;

    public WaystoneReloadMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
