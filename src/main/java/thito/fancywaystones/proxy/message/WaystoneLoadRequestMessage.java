package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneLoadRequestMessage extends Message {
    private static final long serialVersionUID = 1L;
    private UUID id;

    public WaystoneLoadRequestMessage(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
