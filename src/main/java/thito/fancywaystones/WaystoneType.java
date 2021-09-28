package thito.fancywaystones;

import org.bukkit.entity.*;
import thito.fancywaystones.economy.*;

import java.util.*;

public interface WaystoneType {
    String name();

    boolean isAlwaysLoaded();

    boolean isAlwaysListed();

    boolean isActivationRequired();

    boolean shouldPurgeInactive();

    String getUniqueNamesContext();

    String getDisplayName(Placeholder placeholder);

    List<Cost> calculateCost(WaystoneLocation source, WaystoneData target);

    boolean shouldDrop(Player player, WaystoneData waystoneData);
    boolean shouldDropPurge(WaystoneData waystoneData);
    boolean hasAccess(Player player, WaystoneData waystoneData);
    boolean hasActivationAccess(Player player, WaystoneData waystoneData);
    boolean isBreakable(Player player, WaystoneData waystoneData);
}
