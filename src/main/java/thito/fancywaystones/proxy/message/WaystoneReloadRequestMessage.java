package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneReloadRequestMessage extends Message {
    private static final long serialVersionUID = 1L;
    private UUID id;

    public WaystoneReloadRequestMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
