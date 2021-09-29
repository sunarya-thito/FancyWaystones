package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneUnloadRequestMessage extends Message {
    private UUID id;

    public WaystoneUnloadRequestMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
