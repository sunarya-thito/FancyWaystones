package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneLoadRequestMessage extends Message {
    private UUID id;

    public WaystoneLoadRequestMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
