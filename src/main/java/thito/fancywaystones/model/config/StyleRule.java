package thito.fancywaystones.model.config;

import thito.fancywaystones.*;

public interface StyleRule {
    StyleRuleState[] getPossibleValues();
    StyleRuleState getValue(WaystoneData data, WaystoneState waystoneState);
}
