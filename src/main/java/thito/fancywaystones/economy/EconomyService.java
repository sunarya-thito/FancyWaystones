package thito.fancywaystones.economy;

import org.bukkit.entity.*;
import thito.fancywaystones.*;

public interface EconomyService {
    boolean isEnabled();
    String getId();
    String getDisplayName(Placeholder placeholder);
    String formattedCurrency(int amount);
    boolean has(Player player, int amount);
    void withdraw(Player player, int amount);
    void deposit(Player player, int amount);
    double balance(Player player);
}
