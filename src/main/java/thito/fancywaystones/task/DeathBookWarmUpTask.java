package thito.fancywaystones.task;

import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.effect.*;

public abstract class DeathBookWarmUpTask extends WarmUpTask {
    public DeathBookWarmUpTask(Player player) {
        super(player, null, null,
                Effect.deserializeEffects(FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Death Book.Effects")),
                (int) Util.parseTime(FancyWaystones.getPlugin().getEffectsYml().getConfig().getString("Warm Up Death Book.Time")));
        ConfigurationSection section = FancyWaystones.getPlugin().getEffectsYml().getConfig().getConfigurationSection("Warm Up Death Book.Overlay");
        title = section.getString("Title");
        subtitle = section.getString("Subtitle");
        cancelledTitle = section.getString("Cancelled Title");
        cancelledSubtitle = section.getString("Cancelled Subtitle");
    }
}
