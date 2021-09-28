package thito.fancywaystones.model.config;

import thito.fancywaystones.config.*;

public class ComponentData {
    private StyleRuleCompound styleRuleCompound;
    private Section config;

    public ComponentData(ComponentData other) {
        styleRuleCompound = other.styleRuleCompound;
        config = other.config;
    }

    public ComponentData(StyleRuleCompound styleRuleCompound, Section config) {
        this.styleRuleCompound = styleRuleCompound;
        this.config = config;
    }

    public StyleRuleCompound getRule() {
        return styleRuleCompound;
    }

    public Section getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "ComponentData{" +
                "styleRuleCompound=" + styleRuleCompound +
                ", config=" + config +
                '}';
    }
}
