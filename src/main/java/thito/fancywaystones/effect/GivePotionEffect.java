package thito.fancywaystones.effect;

import com.cryptomorin.xseries.XPotion;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import thito.fancywaystones.*;

public class GivePotionEffect implements Effect {
    private int tick, duration, amplifier;
    private boolean once, particle, force, ambient, icon;
    private XPotion potion;
    public GivePotionEffect(ConfigurationSection section) {
        tick = Math.max(1, section.getInt("Tick"));
        duration = (int) Util.parseTime(section.getString("Duration"));
        amplifier = section.getInt("Amplifier");
        once = section.getBoolean("Once");
        particle = section.getBoolean("Show Particle");
        force = section.getBoolean("Force");
        icon = section.getBoolean("Show Icon");
        ambient = section.getBoolean("Show Ambience");
        try {
            potion = XPotion.valueOf(section.getString("Potion Effect"));
        } catch (Throwable t) {
            t.printStackTrace();
            potion = XPotion.BLINDNESS;
        }
    }


    @Override
    public EffectHandler createHandler(Player player, WaystoneData waystoneData, WaystoneData target) {
        return tickTime -> {
            if (once ? tickTime == tick : tickTime % tick == 0) {
                if (FancyWaystones.getPlugin().isEnabled()) {
                    Bukkit.getScheduler().runTask(FancyWaystones.getPlugin(), () -> {
                        try {
                            player.addPotionEffect(new PotionEffect(potion.parsePotionEffectType(), duration, amplifier, ambient, particle, icon), force);
                        } catch (Throwable t) {
                            player.addPotionEffect(new PotionEffect(potion.parsePotionEffectType(), duration, amplifier, ambient, particle), force);
                        }
                    });
                }
            }
        };
    }
}
