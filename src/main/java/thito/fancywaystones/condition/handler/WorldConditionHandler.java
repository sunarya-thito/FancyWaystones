package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.WaystoneLocation;
import thito.fancywaystones.condition.ConditionHandler;

import java.util.List;

public class WorldConditionHandler implements ConditionHandler {
    private List<String> worldList;

    public WorldConditionHandler(List<String> worldList) {
        this.worldList = worldList;
    }

    @Override
    public boolean test(Placeholder placeholder) {
        if (worldList != null && !worldList.isEmpty()) {
            WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
            if (data != null) {
                WaystoneLocation waystoneLocation = data.getLocation();
                return worldList.contains(waystoneLocation.getWorldName());
            }
        }
        return true;
    }

    public List<String> getWorldList() {
        return worldList;
    }
}
