package thito.fancywaystones;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.HashSet;
import java.util.Set;

public class WaystoneModernListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockExplodeEvent e) {
        Set<WaystoneData> destroyed = new HashSet<>();
        String reason = "{language-reason-unknown-explosion}";
        WaystoneListener.handleExplosion(destroyed, reason, e.blockList());
    }
}
