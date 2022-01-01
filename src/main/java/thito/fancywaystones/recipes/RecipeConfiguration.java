package thito.fancywaystones.recipes;

import org.bukkit.configuration.ConfigurationSection;
import thito.fancywaystones.config.Section;

public class RecipeConfiguration {

    public static RecipeConfiguration fromConfig(ConfigurationSection section) {
        if (section == null) return new RecipeConfiguration(false, false);
        return new RecipeConfiguration(
                section.getBoolean("Auto Give.On Join"),
                section.getBoolean("Auto Give.First Join Only")
        );
    }

    public static RecipeConfiguration fromConfig(Section section) {
        if (section == null) return new RecipeConfiguration(false, false);
        return new RecipeConfiguration(
                section.getBoolean("Auto Give.On Join").orElse(false),
                section.getBoolean("Auto Give.First Join Only").orElse(false)
        );
    }

    private boolean giveOnJoin;
    private boolean firstJoinOnly;

    public RecipeConfiguration(boolean giveOnJoin, boolean firstJoinOnly) {
        this.giveOnJoin = giveOnJoin;
        this.firstJoinOnly = firstJoinOnly;
    }

    public boolean isFirstJoinOnly() {
        return firstJoinOnly;
    }

    public boolean isGiveOnJoin() {
        return giveOnJoin;
    }

    public void setFirstJoinOnly(boolean firstJoinOnly) {
        this.firstJoinOnly = firstJoinOnly;
    }

    public void setGiveOnJoin(boolean giveOnJoin) {
        this.giveOnJoin = giveOnJoin;
    }
}
