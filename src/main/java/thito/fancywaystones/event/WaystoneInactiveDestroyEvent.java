package thito.fancywaystones.event;

import thito.fancywaystones.WaystoneData;

public class WaystoneInactiveDestroyEvent extends WaystoneDestroyEvent {
    public WaystoneInactiveDestroyEvent(WaystoneData waystoneData) {
        super(waystoneData, null);
    }
}
