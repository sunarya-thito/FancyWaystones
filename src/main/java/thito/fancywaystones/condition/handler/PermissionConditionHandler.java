package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class PermissionConditionHandler implements ConditionHandler {
    private String permission;

    public PermissionConditionHandler(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            if (permission == null) return true;
            return player.hasPermission(permission);
        }
        return false;
    }
}
