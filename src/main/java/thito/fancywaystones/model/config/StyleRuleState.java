package thito.fancywaystones.model.config;

import java.util.*;

public class StyleRuleState {
    private String id;

    public StyleRuleState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StyleRuleState)) return false;
        StyleRuleState that = (StyleRuleState) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StyleRuleState{" +
                "id='" + id + '\'' +
                '}';
    }
}
