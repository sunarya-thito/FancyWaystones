package thito.fancywaystones.types;

import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;
import thito.fancywaystones.condition.handler.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.economy.*;
import thito.fancywaystones.location.*;

import java.util.*;

public class ConfigWaystoneType implements WaystoneType {
    private final String id;
    private final String name;
    private final boolean alwaysLoaded, alwaysListed, requiresActivation, purge;
    private final Condition accessCondition, activationCondition, purgeDropsCondition, breakCondition, dropCondition;
    private final String uniqueKey;

    private final boolean multiWorldCostEnabled, multiDimensionalCostEnabled, multiServerCostEnabled, distanceCostEnabled, basicFeeEnabled;
    private final Map<EconomyService, Integer>
    multiWorldCostMap,
    multiDimensionalCostMap,
    multiServerCostMap,
    distanceCostMap,
    basicFeeCostMap;
    private final double distanceCostDivision;
    private final boolean distanceCostFloorDivision;

    public ConfigWaystoneType(ConfigurationSection section) {
        id = section.getName();
        name = section.getString("Name");
        alwaysLoaded = section.getBoolean("Always Loaded");
        alwaysListed = section.getBoolean("Always Listed");
        requiresActivation = section.getBoolean("Requires Activation");
        purge = section.getBoolean("Purge Inactive");
        accessCondition = Condition.fromConfig(new ListSection(section.getList("Access")));
        activationCondition = Condition.fromConfig(new ListSection(section.getList("Activation")));
        purgeDropsCondition = Condition.fromConfig(new ListSection(section.getList("Purge Drops")));
        breakCondition = Condition.fromConfig(new ListSection(section.getList("Breakable")));
        dropCondition = Condition.fromConfig(new ListSection(section.getList("Drops")));
        uniqueKey = section.getString("Unique Names");
        multiWorldCostEnabled = section.getBoolean("Price.Multiworld.Enable");
        multiDimensionalCostEnabled = section.getBoolean("Price.Multidimensional.Enable");
        multiServerCostEnabled = section.getBoolean("Price.Multiserver.Enable");
        distanceCostEnabled = section.getBoolean("Price.Distance.Enable");
        basicFeeEnabled = section.getBoolean("Price.Basic Fee.Enable");
        distanceCostDivision = section.getDouble("Price.Distance.Divide");
        distanceCostFloorDivision = section.getBoolean("Price.Distance.Floor Divide");
        multiWorldCostMap = parseCost(section.getConfigurationSection("Price.Multiworld.Cost"));
        multiDimensionalCostMap = parseCost(section.getConfigurationSection("Price.Multidimensional.Cost"));
        multiServerCostMap = parseCost(section.getConfigurationSection("Price.Multiserver.Cost"));
        distanceCostMap = parseCost(section.getConfigurationSection("Price.Distance.Cost"));
        basicFeeCostMap = parseCost(section.getConfigurationSection("Price.Basic Fee.Cost"));
    }

    private static Map<EconomyService, Integer> parseCost(ConfigurationSection section) {
        Map<EconomyService, Integer> costMap = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (section.isInt(key)) {
                    WaystoneManager.getManager().getEconomyServices().stream()
                            .filter(x -> x.getId().equals(key)).findAny()
                            .ifPresent(economyService -> costMap.put(economyService, section.getInt(key)));
                }
            }
        }
        return costMap;
    }

    @Override
    public String name() {
        return id;
    }

    @Override
    public boolean isAlwaysLoaded() {
        return alwaysLoaded;
    }

    @Override
    public boolean isAlwaysListed() {
        return alwaysListed;
    }

    @Override
    public boolean isActivationRequired() {
        return requiresActivation;
    }

    @Override
    public boolean shouldPurgeInactive() {
        return purge;
    }

    @Override
    public String getUniqueNamesContext() {
        return uniqueKey;
    }

    @Override
    public String getDisplayName(Placeholder placeholder) {
        return placeholder.replace(name);
    }

    @Override
    public List<Cost> calculateCost(WaystoneLocation source, WaystoneData target) {
        if (source == null) return Collections.emptyList();
        Map<EconomyService, Cost> costMap = new HashMap<>();
        double distance = target.getLocation().distance(source);
        if (WaystoneLocation.isCrossServer(distance) && multiServerCostEnabled) {
            multiServerCostMap.forEach((service, cost) -> {
                Cost costObj = costMap.get(service);
                if (costObj != null) {
                    costMap.put(service, new Cost(service, costObj.getAmount() + cost));
                } else {
                    costMap.put(service, new Cost(service, cost));
                }
            });
        }
        if (!Objects.equals(source.getEnvironment(), target.getEnvironment()) && multiDimensionalCostEnabled) {
            multiDimensionalCostMap.forEach((service, cost) -> {
                Cost costObj = costMap.get(service);
                if (costObj != null) {
                    costMap.put(service, new Cost(service, costObj.getAmount() + cost));
                } else {
                    costMap.put(service, new Cost(service, cost));
                }
            });
        }
        if (WaystoneLocation.isCrossWorld(distance) && multiWorldCostEnabled) {
            multiWorldCostMap.forEach((service, cost) -> {
                Cost costObj = costMap.get(service);
                if (costObj != null) {
                    costMap.put(service, new Cost(service, costObj.getAmount() + cost));
                } else {
                    costMap.put(service, new Cost(service, cost));
                }
            });
        }
        if (distanceCostEnabled) {
            double multiplier = distanceCostFloorDivision ? Math.floor(distance / distanceCostDivision) : distance / distanceCostDivision;
            distanceCostMap.forEach((service, cost) -> {
                Cost costObj = costMap.get(service);
                if (costObj != null) {
                    costMap.put(service, new Cost(service, costObj.getAmount() + (int) (cost * multiplier)));
                } else {
                    costMap.put(service, new Cost(service, (int) (cost * multiplier)));
                }
            });
        }
        if (basicFeeEnabled) {
            basicFeeCostMap.forEach((service, cost) -> {
                Cost costObj = costMap.get(service);
                if (costObj != null) {
                    costMap.put(service, new Cost(service, costObj.getAmount() + cost));
                } else {
                    costMap.put(service, new Cost(service, cost));
                }
            });
        }
        return new ArrayList<>(costMap.values());
    }

    @Override
    public boolean shouldDrop(Player player, WaystoneData waystoneData) {
        return dropCondition.test(new Placeholder().putContent(Placeholder.PLAYER, player)
        .putContent(Placeholder.WAYSTONE, waystoneData));
    }

    @Override
    public boolean shouldDropPurge(WaystoneData waystoneData) {
        return purgeDropsCondition.test(new Placeholder().putContent(Placeholder.WAYSTONE, waystoneData));
    }

    @Override
    public boolean hasAccess(Player player, WaystoneData waystoneData) {
        return accessCondition.test(new Placeholder().putContent(Placeholder.PLAYER, player)
                .putContent(Placeholder.WAYSTONE, waystoneData));
    }

    @Override
    public boolean hasActivationAccess(Player player, WaystoneData waystoneData) {
        return activationCondition.test(new Placeholder().putContent(Placeholder.PLAYER, player)
                .putContent(Placeholder.WAYSTONE, waystoneData));
    }

    @Override
    public boolean isBreakable(Player player, WaystoneData waystoneData) {
        return breakCondition.test(new Placeholder().putContent(Placeholder.PLAYER, player)
                .putContent(Placeholder.WAYSTONE, waystoneData));
    }

    @Override
    public boolean isBreakableByExplosion(WaystoneData waystoneData) {
        return breakCondition.test(new Placeholder().putContent(Placeholder.WAYSTONE, waystoneData)
        .putContent(ExplosionConditionHandler.EXPLOSION_CAUSE, true));
    }
}
