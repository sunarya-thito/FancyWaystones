package thito.fancywaystones.proxy.message;

import java.util.*;

public class WaystoneUnloadMessage extends Message {
    private UUID id;
    private String reason;

    public WaystoneUnloadMessage(UUID id, String reason) {
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
