package thito.fancywaystones.hook;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import thito.fancywaystones.FancyWaystones;
import thito.fancywaystones.WaystoneManager;
import thito.fancywaystones.model.config.component.ItemsAdderCustomBlockComponent;
import thito.fancywaystones.model.config.component.ModelEngineComponent;
import thito.fancywaystones.model.config.component.OraxenCustomBlockComponent;

import java.util.logging.Level;

public class LateHookLoader implements Listener {

    public void checkStatus() {
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            hookItemsAdder();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            hookOraxen();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {

        }
    }

    @EventHandler
    public void onPluginLoad(PluginEnableEvent e) {
        if (e.getPlugin().getName().equals("ItemsAdder")) {
            hookItemsAdder();
        }
        if (e.getPlugin().getName().equals("Oraxen")) {
            hookOraxen();
        }
        if (e.getPlugin().getName().equals("ModelEngine")) {
            hookModelEngine();
        }
    }

    private void hookModelEngine() {
        if (WaystoneManager.getManager().getComponentTypeMap().containsKey("model-engine")) return;
        try {
            WaystoneManager.getManager().getComponentTypeMap().put("model-engine", new ModelEngineComponent());
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Hooked with ModelEngine");
        } catch (Throwable t) {
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Not hooked with ModelEngine", t);
        }
    }

    private void hookItemsAdder() {
        if (WaystoneManager.getManager().getComponentTypeMap().containsKey("items-adder")) return;
        try {
            WaystoneManager.getManager().getComponentTypeMap().put("items-adder", new ItemsAdderCustomBlockComponent());
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Hooked with ItemsAdder");
        } catch (Throwable t) {
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Not hooked with ItemsAdder", t);
        }
    }

    private void hookOraxen() {
        if (WaystoneManager.getManager().getComponentTypeMap().containsKey("oraxen")) return;
        try {
            WaystoneManager.getManager().getComponentTypeMap().put("oraxen", new OraxenCustomBlockComponent());
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Hooked with Oraxen");
        } catch (Throwable t) {
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Not hooked with Oraxen", t);
        }
    }

}
