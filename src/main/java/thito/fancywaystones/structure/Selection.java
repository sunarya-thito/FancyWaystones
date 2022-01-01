package thito.fancywaystones.structure;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class Selection {
    private static final Map<Player, Selection> playerPos = new WeakHashMap<>();

    public static Selection getSelection(Player player) {
        return playerPos.computeIfAbsent(player, x -> new Selection());
    }

    private Location pos1, pos2;

    public Selection generalize() {
        Selection selection = new Selection();
        selection.pos1 = new Location(pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ()));
        selection.pos2 = new Location(pos1.getWorld(),
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ()));
        return selection;
    }

    public int getWidth() {
        return Math.abs(pos1.getBlockX() - pos2.getBlockX());
    }

    public int getHeight() {
        return Math.abs(pos1.getBlockY() - pos2.getBlockY());
    }

    public int getLength() {
        return Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
        if (this.pos2 != null && this.pos2.getWorld() != pos1.getWorld()) {
            this.pos2 = null;
        }
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
        if (this.pos1 != null && this.pos1.getWorld() != pos2.getWorld()) {
            this.pos1 = null;
        }
    }
}
