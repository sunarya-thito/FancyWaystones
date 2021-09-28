package thito.fancywaystones.task;

import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.effect.*;

public abstract class WaystoneWarmUpTask extends WarmUpTask {
    public WaystoneWarmUpTask(Player player, WaystoneData sourceWaystone, WaystoneData targetWaystone) {
        super(player, sourceWaystone, targetWaystone,
                Effect.deserializeEffects(FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Waystone.Effects")),
                (int) Util.parseTime(FancyWaystones.getPlugin().getEffectsYml().getConfig().getString("Warm Up Waystone.Time")));
        ConfigurationSection section = FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Waystone.Overlay");
        title = section.getString("Title");
        subtitle = section.getString("Subtitle");
        cancelledTitle = section.getString("Cancelled Title");
        cancelledSubtitle = section.getString("Cancelled Subtitle");
    }
}
