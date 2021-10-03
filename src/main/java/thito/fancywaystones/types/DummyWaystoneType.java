package thito.fancywaystones.types;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;
import thito.fancywaystones.economy.*;

import java.util.*;

public class DummyWaystoneType implements WaystoneType {
    @Override
    public String name() {
        return "dummy";
    }

    @Override
    public boolean isAlwaysLoaded() {
        return false;
    }

    @Override
    public boolean isAlwaysListed() {
        return false;
    }

    @Override
    public boolean isActivationRequired() {
        return false;
    }

    @Override
    public boolean shouldPurgeInactive() {
        return false;
    }

    @Override
    public String getUniqueNamesContext() {
        return null;
    }

    @Override
    public String getDisplayName(Placeholder placeholder) {
        return "";
    }

    @Override
    public List<Cost> calculateCost(WaystoneLocation source, WaystoneData target) {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldDrop(Player player, WaystoneData waystoneData) {
        return false;
    }

    @Override
    public boolean shouldDropPurge(WaystoneData waystoneData) {
        return false;
    }

    @Override
    public boolean hasAccess(Player player, WaystoneData waystoneData) {
        return false;
    }

    @Override
    public boolean hasActivationAccess(Player player, WaystoneData waystoneData) {
        return false;
    }

    @Override
    public boolean isBreakable(Player player, WaystoneData waystoneData) {
        return false;
    }

    @Override
    public boolean isBreakableByExplosion(WaystoneData waystoneData) {
        return false;
    }
}
