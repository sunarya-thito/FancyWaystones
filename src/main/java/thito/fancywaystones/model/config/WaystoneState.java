package thito.fancywaystones.model.config;

public enum WaystoneState {
    ACTIVE(new StyleRuleState("active")), INACTIVE(new StyleRuleState("inactive"));

    private final StyleRuleState state;

    WaystoneState(StyleRuleState state) {
        this.state = state;
    }

    public StyleRuleState getState() {
        return state;
    }
}
