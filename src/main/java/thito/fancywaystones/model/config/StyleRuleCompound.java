package thito.fancywaystones.model.config;

import thito.fancywaystones.*;

import java.util.*;

public class StyleRuleCompound {
    public static StyleRuleCompound parse(String string) {
        String[] split = string.split("\\s+");
        StyleRuleCompound compound = new StyleRuleCompound();
        for (String s : split) {
            String[] entry = s.split(":");
            if (entry.length == 2) {
                StyleRule styleRule = WaystoneManager.getManager().getStyleRuleMap().get(entry[0]);
                String value = entry[1];
                if (value.equals("*") || value.isEmpty()) {
                    compound.compound.computeIfAbsent(styleRule, x -> new HashSet<>());
                } else {
                    String[] values = value.split(",");
                    Set<StyleRuleState> states = compound.compound.computeIfAbsent(styleRule, x -> new HashSet<>());
                    StyleRuleState[] possibleValues = styleRule.getPossibleValues();
                    for (String val : values) {
                        for (StyleRuleState pos : possibleValues) {
                            if (pos.getId().equals(val)) {
                                states.add(pos);
                                break;
                            }
                        }
                    }
                }
            } else if (entry.length == 1) {
                StyleRule styleRule = WaystoneManager.getManager().getStyleRuleMap().get(entry[0]);
                if (styleRule != null) {
                    compound.compound.computeIfAbsent(styleRule, x -> new HashSet<>());
                }
            }
        }
        return compound;
    }

    private Map<StyleRule, Set<StyleRuleState>> compound = new HashMap<>();

    public boolean matches(WaystoneData waystoneData, WaystoneState state) {
        for (Map.Entry<StyleRule, Set<StyleRuleState>> entry : compound.entrySet()) {
            StyleRule rule = entry.getKey();
            StyleRuleState ruleState = rule.getValue(waystoneData, state);
            if (!entry.getValue().isEmpty() && !entry.getValue().contains(ruleState)) {
                return false;
            }
        }
        return true;
    }

    public Map<StyleRule, Set<StyleRuleState>> getCompound() {
        return compound;
    }

    @Override
    public String toString() {
        return "StyleRuleCompound{" +
                "compound=" + compound +
                '}';
    }
}
