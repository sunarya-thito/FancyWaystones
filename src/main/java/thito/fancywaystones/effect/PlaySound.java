package thito.fancywaystones.effect;

import com.cryptomorin.xseries.XSound;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.location.*;

public class PlaySound implements Effect {
    private XSound sound;
    private boolean playAtWaystoneSource, playAtPlayer, playAtWaystoneTarget, once, global;
    private float volume, pitch;
    private int tick;

    public PlaySound(ConfigurationSection section) {
        try {
            this.sound = XSound.valueOf(section.getString("Sound"));
        } catch (Throwable t) {
            t.printStackTrace();
            this.sound = XSound.BLOCK_ANVIL_BREAK;
        }
        playAtPlayer = section.getBoolean("Play At Player");
        playAtWaystoneSource = section.getBoolean("Play At Waystone Source");
        playAtWaystoneTarget = section.getBoolean("Play At Waystone Target");
        once = section.getBoolean("Once");
        global = section.getBoolean("Global");
        tick = Math.max(1, section.getInt("Tick"));
        volume = (float) section.getDouble("Volume", 1d);
        pitch = (float) section.getDouble("Pitch", 1d);
    }

    @Override
    public EffectHandler createHandler(Player player, WaystoneData waystoneData, WaystoneData target) {
        return tickTime -> {
            if (sound.isSupported()) {
                if (once ? tickTime == tick : tickTime % tick == 0) {
                    if (playAtPlayer) {
                        if (global) {
                            player.getWorld()
                                    .playSound(player.getLocation(), sound.parseSound(), volume, pitch);
                        } else {
                            player.playSound(player.getLocation(), sound.parseSound(), volume, pitch);
                        }
                    }
                    if (playAtWaystoneSource && waystoneData != null && waystoneData.getLocation() instanceof LocalLocation && ((LocalLocation) waystoneData.getLocation()).getLocation().getWorld() != null) {
                        if (global) {
                            ((LocalLocation) waystoneData.getLocation()).getLocation().getWorld()
                                    .playSound(((LocalLocation) waystoneData.getLocation()).getLocation(), sound.parseSound(), volume, pitch);
                        } else {
                            player.playSound(((LocalLocation) waystoneData.getLocation()).getLocation(), sound.parseSound(), volume, pitch);
                        }
                    }
                    if (playAtWaystoneTarget && target != null && target.getLocation() instanceof LocalLocation && ((LocalLocation) target.getLocation()).getLocation().getWorld() != null) {
                        if (global) {
                            ((LocalLocation) target.getLocation()).getLocation().getWorld()
                                    .playSound(((LocalLocation) target.getLocation()).getLocation(), sound.parseSound(), volume, pitch);
                        } else {
                            player.playSound(((LocalLocation) target.getLocation()).getLocation(), sound.parseSound(), volume, pitch);
                        }
                    }
                }
            }
        };
    }
}
