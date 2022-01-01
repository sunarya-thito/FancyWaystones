package thito.fancywaystones.task;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.event.DeathBookPostTeleportEvent;
import thito.fancywaystones.event.FWEvent;

public class DeathBookTeleportTask extends TeleportTask {

    private int noDamageTick;

    public DeathBookTeleportTask(Player player, Location location, int checkRadius, int checkHeight, boolean force) {
        super(player, location, checkRadius, checkHeight, force);
    }

    public void setNoDamageTicks(int noDamageTick) {
        this.noDamageTick = noDamageTick;
    }

    public int getNoDamageTicks() {
        return noDamageTick;
    }

    @Override
    protected void done() {
        if (isSuccess()) {
            confirmTeleport();
        }
    }

    private void confirmTeleport() {
        Player player = getPlayer();
        player.setNoDamageTicks(player.getNoDamageTicks() + getNoDamageTicks());
        player.sendMessage(new Placeholder()
                .putContent(Placeholder.PLAYER, player)
                .replace("{language.teleported-death}"));
        FancyWaystones.getPlugin().postTeleport("Death Book", player, null, null);
        FWEvent.call(new DeathBookPostTeleportEvent(getLocation(), getPlayer()));
    }

}
