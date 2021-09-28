package thito.fancywaystones.economy;

import org.bukkit.entity.*;
import thito.fancywaystones.*;

public class LevelEconomyService implements EconomyService {

    public boolean isEnabled() {
        return FancyWaystones.getPlugin().getConfig().getBoolean("Economy.Level.Enable");
    }

    @Override
    public String getDisplayName(Placeholder placeholder) {
        return placeholder.replace("{language.economy-names.level}");
    }

    @Override
    public String getId() {
        return "Level";
    }

    @Override
    public String formattedCurrency(int amount) {
        return FancyWaystones.getPlugin().getConfig().getString("Economy.Level.Currency Format").replace("{amount}", String.valueOf(amount));
    }

    @Override
    public boolean has(Player player, int amount) {
        return player.getLevel() >= amount;
    }

    @Override
    public void withdraw(Player player, int amount) {
        player.setLevel(player.getLevel() - amount);
    }

    @Override
    public void deposit(Player player, int amount) {
        player.setLevel(player.getLevel() + amount);
    }

    @Override
    public double balance(Player player) {
        return player.getLevel();
    }
}
