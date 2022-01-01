package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class MemberConditionHandler implements ConditionHandler {
    @Override
    public String test(Placeholder placeholder) {
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        Player player = placeholder.get(Placeholder.PLAYER);
        if (data != null && player != null) {
            return data.getMembers().contains(new WaystoneMember(player.getUniqueId())) ? null :
                    placeholder.replace("{language.condition.member}");
        }
        return placeholder.replace("{language.condition.member}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        WaystoneData data = placeholder.get(Placeholder.WAYSTONE);
        Player player = placeholder.get(Placeholder.PLAYER);
        if (data != null && player != null) {
            return data.getMembers().contains(new WaystoneMember(player.getUniqueId())) ?
                    placeholder.replace("{language.condition.not-member}") : null;
        }
        return null;
    }
}
