package thito.fancywaystones.ui;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.*;
import java.util.function.*;

public class PlayerInput implements Listener {
    private static Map<Player, Consumer<String>> inputMap = new HashMap<>();

    public static void requestInput(Player player, Consumer<String> stringConsumer) {
        inputMap.put(player, stringConsumer);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(AsyncPlayerChatEvent e) {
        Consumer<String> string = inputMap.remove(e.getPlayer());
        if (string != null) {
            string.accept(e.getMessage());
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        inputMap.remove(e.getPlayer());
    }
}
