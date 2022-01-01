package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneDestroyMessage extends Message {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String reason;

    public WaystoneDestroyMessage(UUID id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}
