package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneReloadRequestMessage extends Message {
    private UUID id;

    public WaystoneReloadRequestMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
