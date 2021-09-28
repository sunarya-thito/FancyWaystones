package thito.fancywaystones.model.config.rule;

import thito.fancywaystones.*;
import thito.fancywaystones.model.config.*;

public class WaystoneTypeRule implements StyleRule {
    @Override
    public StyleRuleState[] getPossibleValues() {
        return WaystoneManager.getManager().getTypes().stream().map(x -> new StyleRuleState(x.name()))
                .toArray(StyleRuleState[]::new);
    }

    @Override
    public StyleRuleState getValue(WaystoneData data, WaystoneState waystoneState) {
        return new StyleRuleState(data.getType().name());
    }
}
