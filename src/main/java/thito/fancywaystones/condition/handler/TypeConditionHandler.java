package thito.fancywaystones.condition.handler;

import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;

import java.util.List;

public class TypeConditionHandler implements ConditionHandler {
    private List<String> ids;

    public TypeConditionHandler(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public String test(Placeholder placeholder) {
        if (ids != null && !ids.isEmpty()) {
            return ids.contains(placeholder.get(Placeholder.WAYSTONE).getType().name()) ? null :
                    placeholder.clone().put("types", ph -> String.join(", ", ids)).replace("{language.condition.types}");
        }
        return null;
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        if (ids != null && !ids.isEmpty()) {
            return ids.contains(placeholder.get(Placeholder.WAYSTONE).getType().name()) ?
                    placeholder.clone().put("types", ph -> String.join(", ", ids)).replace("{language.condition.not-types}") : null;
        }
        return placeholder.clone().put("types", ph -> String.join(", ", ids)).replace("{language.condition.not-types}");
    }
}
