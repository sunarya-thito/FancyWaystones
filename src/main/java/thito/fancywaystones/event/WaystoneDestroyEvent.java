package thito.fancywaystones.event;

import org.bukkit.entity.Player;
import thito.fancywaystones.WaystoneData;

public class WaystoneDestroyEvent extends WaystoneEvent {
    private Object cause;

    public WaystoneDestroyEvent(WaystoneData waystoneData, Object cause) {
        super(waystoneData);
        this.cause = cause;
    }

    public Object getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
