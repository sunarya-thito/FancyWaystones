package thito.fancywaystones.model.config.rule;

import thito.fancywaystones.*;
import thito.fancywaystones.model.config.*;

public class WaystoneStateRule implements StyleRule {
    private static final StyleRuleState[] values = {
            WaystoneState.ACTIVE.getState(),
            WaystoneState.INACTIVE.getState()
    };
    @Override
    public StyleRuleState[] getPossibleValues() {
        return values;
    }

    @Override
    public StyleRuleState getValue(WaystoneData data, WaystoneState waystoneState) {
        return waystoneState.getState();
    }
}
