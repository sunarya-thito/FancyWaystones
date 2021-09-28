package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.stream.*;

public class ModernWaystoneListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().discoverRecipes(FancyWaystones.getPlugin().getRecipeManager().getRecipes().stream().map(r -> ((Keyed) r).getKey()).collect(Collectors.toList()));
    }
}
