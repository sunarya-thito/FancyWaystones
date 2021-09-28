package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.effect.Effect;
import thito.fancywaystones.effect.*;

import java.util.*;
import java.util.stream.*;

public class PostTeleportTask extends Task {

    private int targetTick;
    private int tick;
    private List<EffectHandler> effects = new ArrayList<>();
    private Location location;
    private Player player;
    private WaystoneData target;
    private String title, subtitle;
    private Placeholder placeholder = new Placeholder();

    public PostTeleportTask(List<Effect> effects, int targetTick, Player player, WaystoneData source, WaystoneData target, String title, String subtitle) {
        this.effects.addAll(effects.stream().map(x -> x.createHandler(player, source, target)).collect(Collectors.toList()));
        this.targetTick = targetTick;
        this.player = player;
        this.target = target;
        this.location = player.getLocation();
        this.title = title;
        this.subtitle = subtitle;
    }

    public Placeholder getPlaceholder() {
        return placeholder;
    }

    public List<EffectHandler> getEffects() {
        return effects;
    }

    @Override
    public void run() {
        if (tick >= targetTick) {
            cancel();
            return;
        }
        for (int i = 0; i < effects.size(); i++) {
            try {
                effects.get(i).tick(tick);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (title != null || subtitle != null) {
            if (player.isOnline()) {
                Placeholder placeholder = new Placeholder().putContent(Placeholder.PLAYER, player)
                        .putContent(Placeholder.WAYSTONE, target)
                        .combine(getPlaceholder())
                        .put("countdown", ph -> (targetTick - tick) / 20);
                Util.sendTitle(player, placeholder.replace(title), placeholder.replace(subtitle), 0, 35, 20);
            }
        }
        tick++;
    }

}
