package thito.fancywaystones.condition.handler;

import org.bukkit.entity.*;
import org.bukkit.potion.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class EffectConditionHandler implements ConditionHandler {
    private PotionEffectType potionEffectType;
    private int level;

    public EffectConditionHandler(PotionEffectType potionEffectType, int level) {
        this.potionEffectType = potionEffectType;
        this.level = level;
    }

    @Override
    public boolean test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            PotionEffect effect = player.getPotionEffect(potionEffectType);
            return effect != null && effect.getAmplifier() >= level;
        }
        return false;
    }
}
