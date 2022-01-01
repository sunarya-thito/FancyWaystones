package thito.fancywaystones.condition.handler;

import org.bukkit.entity.Player;
import thito.fancywaystones.Placeholder;
import thito.fancywaystones.condition.ConditionHandler;

public class PermissionConditionHandler implements ConditionHandler {
    private String permission;

    public PermissionConditionHandler(String permission) {
        this.permission = permission;
    }

    @Override
    public String test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (permission == null) return null;
        if (player != null) {
            return player.hasPermission(placeholder.replace(permission)) ? null :
                    placeholder.clone().put("permission", ph -> placeholder.replace(permission)).replace("{language.condition.permission}");
        }
        return placeholder.clone().put("permission", ph -> placeholder.replace(permission)).replace("{language.condition.permission}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (permission == null) return null;
        if (player != null) {
            return player.hasPermission(placeholder.replace(permission)) ?
                    placeholder.clone().put("permission", ph -> placeholder.replace(permission)).replace("{language.condition.not-permission}") : null;
        }
        return null;
    }
}
