package thito.fancywaystones.effect;

import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;

import java.util.*;

public interface Effect {
    static List<Effect> deserializeEffects(ConfigurationSection section) {
        List<Effect> effects = new ArrayList<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection sec = section.getConfigurationSection(key);
                if (sec != null) {
                    String type = sec.getString("Type");
                    if (type != null) {
                        switch (type) {
                            case "CircleParticle":
                                effects.add(new CircleParticle(sec));
                                break;
                            case "SphereParticle":
                                effects.add(new SphereParticle(sec));
                                break;
                            case "PlaySound":
                                effects.add(new PlaySound(sec));
                                break;
                            case "GivePotionEffect":
                                effects.add(new GivePotionEffect(sec));
                                break;
                        }
                    }
                }
            }
        }
        return effects;
    }
    EffectHandler createHandler(Player player, WaystoneData waystoneData, WaystoneData target);
}
