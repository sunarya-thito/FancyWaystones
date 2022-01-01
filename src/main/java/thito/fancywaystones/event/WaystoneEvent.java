package thito.fancywaystones.event;

import thito.fancywaystones.WaystoneData;

public class WaystoneEvent extends FWEvent {
    private WaystoneData waystoneData;

    public WaystoneEvent(WaystoneData waystoneData) {
        this.waystoneData = waystoneData;
    }

    public WaystoneData getWaystoneData() {
        return waystoneData;
    }
}
