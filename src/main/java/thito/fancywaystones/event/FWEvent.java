package thito.fancywaystones.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FWEvent extends Event implements Cancellable {
    public static <T extends FWEvent> T call(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private boolean cancelled;

    public FWEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
