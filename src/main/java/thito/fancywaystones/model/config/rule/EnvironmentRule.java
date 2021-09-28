package thito.fancywaystones.model.config.rule;

import org.bukkit.*;
import thito.fancywaystones.*;
import thito.fancywaystones.model.config.*;

import java.util.*;

public class EnvironmentRule implements StyleRule {
    private StyleRuleState[] values = Arrays.stream(World.Environment.values()).map(x -> new StyleRuleState(x.name().toLowerCase())).toArray(StyleRuleState[]::new);
    @Override
    public StyleRuleState[] getPossibleValues() {
        return values;
    }

    @Override
    public StyleRuleState getValue(WaystoneData data, WaystoneState waystoneState) {
        for (StyleRuleState s : values) {
            if (s.getId().equalsIgnoreCase(data.getEnvironment().name())) {
                return s;
            }
        }
        return null;
    }
}
