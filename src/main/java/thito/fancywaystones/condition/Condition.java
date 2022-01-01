package thito.fancywaystones.condition;

import org.bukkit.enchantments.*;
import org.bukkit.potion.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.handler.*;
import thito.fancywaystones.config.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class Condition {
    public static final Map<String, Function<MapSection, ConditionHandler>> HANDLER_FACTORY_MAP = new HashMap<>();
    public static final Map<String, Supplier<ConditionHandler>> NO_PARAM_HANDLER_FACTORY_MAP = new HashMap<>();

    static {
        HANDLER_FACTORY_MAP.put("RANDOM", map ->
                new RandomConditionHandler(map.getInteger("Chance").orElse(0)));
        HANDLER_FACTORY_MAP.put("ENCHANTED", map -> {
            Enchantment enchantment = map.getString("Type").map(Enchantment::getByName).orElse(null);
            if (enchantment != null) {
                return new EnchantedConditionHandler(enchantment, map.getInteger("Level").orElse(1));
            }
            return null;
        });
        HANDLER_FACTORY_MAP.put("EFFECT", map -> {
            PotionEffectType potionEffectType = map.getString("Type").map(PotionEffectType::getByName).orElse(null);
            if (potionEffectType != null) {
                return new EffectConditionHandler(potionEffectType, map.getInteger("Level").orElse(0));
            }
            return null;
        });
        HANDLER_FACTORY_MAP.put("LAND_ACCESS", map -> new LandAccessConditionHandler());
        HANDLER_FACTORY_MAP.put("IS_MEMBER", map -> new MemberConditionHandler());
        HANDLER_FACTORY_MAP.put("HAS_PERMISSION", map -> new PermissionConditionHandler(map.getString("Permission").orElse(null)));
        HANDLER_FACTORY_MAP.put("IS_OWNER", map -> new OwnerConditionHandler());
        HANDLER_FACTORY_MAP.put("IS_EXPLOSION", map -> new ExplosionConditionHandler());
        HANDLER_FACTORY_MAP.put("WORLD_WHITELIST", map -> new WorldConditionHandler(map.getList("Worlds").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList())));
        HANDLER_FACTORY_MAP.put("TYPE_WHITELIST", map -> new TypeConditionHandler(map.getList("Types").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList())));

        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_EXPLOSION", ExplosionConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("ALWAYS", AlwaysConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("NEVER", NeverConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("LAND_ACCESS", LandAccessConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("RANDOM", () -> new RandomConditionHandler(50));
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_MEMBER", MemberConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_OWNER", OwnerConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_SAME_DIMENSION", SameDimensionConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_SAME_SERVER", SameServerConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_SAME_WORLD", SameWorldConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_SAME_TYPE", SameTypeConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_WAYSTONE_ACTIVATED", ActiveWaystoneConditionHandler::new);
        NO_PARAM_HANDLER_FACTORY_MAP.put("IS_NATURAL", NaturalWaystoneConditionHandler::new);
    }

    public static Condition fromConfig(ListSection listSection) {
        Condition condition = new Condition();
        if (listSection != null) {
            for (int i = 0; i < listSection.size(); i++) {
                listSection.getString(i).ifPresent(str -> {
                    Supplier<ConditionHandler> handler = NO_PARAM_HANDLER_FACTORY_MAP.get(str);
                    if (handler != null) {
                        condition.getRules().add(new ConditionRule(handler.get()));
                    }
                });
                listSection.getMap(i).ifPresent(mapSection -> {
                    for (String key : mapSection.getKeys()) {
                        Function<MapSection, ConditionHandler> factory = HANDLER_FACTORY_MAP.get(key);
                        if (factory != null) {
                            mapSection.getMap(key).ifPresent(map -> {
                                ConditionHandler handler = factory.apply(map);
                                if (handler != null) {
                                    ConditionRule rule = new ConditionRule(handler);
                                    rule.setNegate(map.getBoolean("Negate").orElse(false));
                                    map.getList("And").ifPresent(list -> {
                                        rule.setSubCondition(fromConfig(list));
                                    });
                                    condition.getRules().add(rule);
                                }
                            });
                        }
                    }
                });
            }
        }
        return condition;
    }

    private List<ConditionRule> rules = new ArrayList<>();

    public List<ConditionRule> getRules() {
        return rules;
    }

    public String[] getFormattedReason(Placeholder placeholder) {
        List<String> test = test(placeholder);
        if (test.isEmpty()) return null;
        if (test.size() == 1) {
            return placeholder.clone().put("reason", ph -> test.get(0)).replaceWithNewLines("{language.condition.format}").toArray(new String[0]);
        }
        return placeholder.clone().put("reasons", ph -> String.join("{language.condition.format-plural-delimiter}", test))
                .replaceWithNewLines("{language.condition.format-plural}").toArray(new String[0]);
    }

    public List<String> test(Placeholder placeholder) {
        List<String> reasons = new ArrayList<>();
        for (ConditionRule rule : rules) {
            List<String> x = rule.test(placeholder);
            if (x == null || x.isEmpty()) return Collections.emptyList();
            reasons.add(x.get(0));
        }
        return reasons;
    }
}
