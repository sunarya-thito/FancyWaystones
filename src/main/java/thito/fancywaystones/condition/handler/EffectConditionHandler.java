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
    public String test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            PotionEffect effect = player.getPotionEffect(potionEffectType);
            return effect != null && effect.getAmplifier() >= level ? null : placeholder.clone()
                    .put("potion-type", ph -> potionEffectType.getName())
                    .put("amplifier", ph -> level).replace("{language.condition.effect}");
        }
        return placeholder.clone()
                .put("potion-type", ph -> potionEffectType.getName())
                .put("amplifier", ph -> level).replace("{language.condition.effect}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            PotionEffect effect = player.getPotionEffect(potionEffectType);
            return effect != null && effect.getAmplifier() >= level ? placeholder.clone()
                    .put("potion-type", ph -> potionEffectType.getName())
                    .put("amplifier", ph -> level).replace("{language.condition.not-effect}") : null;
        }
        return null;
    }
}
