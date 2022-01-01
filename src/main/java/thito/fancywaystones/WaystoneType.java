package thito.fancywaystones;

import org.bukkit.entity.Player;
import thito.fancywaystones.economy.Cost;

import java.util.List;

public interface WaystoneType {
    String name();

    boolean isAlwaysLoaded();

    boolean isAlwaysListed();

    boolean isActivationRequired();

    boolean shouldPurgeInactive();

    String getUniqueNamesContext();

    String getDisplayName(Placeholder placeholder);

    List<Cost> calculateCost(WaystoneLocation source, WaystoneData target);

    boolean isVisible(WaystoneType type);
    String[] canBeVisited(Player player, WaystoneData source, WaystoneData waystoneData);
    boolean canBeListed(Player player, WaystoneData source, WaystoneData waystoneData);
    String[] canRedirectCompass(Player player, WaystoneData waystoneData);
    boolean shouldDrop(Player player, WaystoneData waystoneData);
    boolean shouldDropPurge(WaystoneData waystoneData);
    String[] hasAccess(Player player, WaystoneData waystoneData);
    String[] hasActivationAccess(Player player, WaystoneData waystoneData);
    String[] isBreakable(Player player, WaystoneData waystoneData);
    boolean isBreakableByExplosion(WaystoneData waystoneData);
}
