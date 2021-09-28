package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.entity.*;

public class WaystoneBlock {
    private WaystoneData data;
    private WaystoneModelHandler modelHandler;

    public WaystoneBlock(WaystoneData data) {
        this.data = data;
    }

    public WaystoneData getData() {
        return data;
    }

    public void update() {
        modelHandler.update();
    }

    public void update(Player player) {
        modelHandler.update(player);
    }

    public void spawn() {
        WaystoneModel model = data.getModel();
        if (model == null) model = WaystoneManager.getManager().getDefaultModel();
        modelHandler = model.createHandler(data);
        update();
    }

    public void destroyModel() {
        modelHandler.destroy();
    }

    public boolean isPart(Location loc) {
        return modelHandler.isPart(loc);
    }

}
