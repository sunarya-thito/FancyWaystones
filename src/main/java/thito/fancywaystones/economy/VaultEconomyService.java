package thito.fancywaystones.economy;

import net.milkbowl.vault.economy.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;

public class VaultEconomyService implements EconomyService {

    private Economy economy;

    public VaultEconomyService() {
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public boolean isEnabled() {
        return FancyWaystones.getPlugin().getConfig().getBoolean("Economy.Vault.Enable");
    }

    @Override
    public String getDisplayName(Placeholder placeholder) {
        return placeholder.replace("{language.economy-names.level}");
    }

    @Override
    public String getId() {
        return "Vault";
    }

    @Override
    public String formattedCurrency(int amount) {
        return FancyWaystones.getPlugin().getConfig().getString("Economy.Vault.Currency Format").replace("{amount}", String.valueOf(amount));
    }

    @Override
    public boolean has(Player player, int amount) {
        return economy.getBalance(player) >= amount;
    }

    @Override
    public void withdraw(Player player, int amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(Player player, int amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public double balance(Player player) {
        return economy.getBalance(player);
    }
}
