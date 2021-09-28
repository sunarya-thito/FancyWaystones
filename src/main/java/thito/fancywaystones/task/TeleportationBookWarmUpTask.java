package thito.fancywaystones.task;

import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.effect.Effect;

public abstract class TeleportationBookWarmUpTask extends WarmUpTask {
    public TeleportationBookWarmUpTask(Player player, WaystoneData targetWaystone) {
        super(player, null, targetWaystone,
                Effect.deserializeEffects(FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Teleportation Book.Effects")),
                (int) Util.parseTime(FancyWaystones.getPlugin().getEffectsYml().getConfig().getString("Warm Up Teleportation Book.Time")));
        ConfigurationSection section = FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Teleportation Book.Overlay");
        title = section.getString("Title");
        subtitle = section.getString("Subtitle");
        cancelledTitle = section.getString("Cancelled Title");
        cancelledSubtitle = section.getString("Cancelled Subtitle");
    }
}
