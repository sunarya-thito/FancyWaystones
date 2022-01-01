package thito.fancywaystones;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class Debug {
    public static Set<CommandSender> listener = Collections.newSetFromMap(new WeakHashMap<>());

    public static void debug(String message) {
        try {
            for (CommandSender sender : listener) {
                if (sender instanceof Player && !((Player) sender).isOnline()) {
                    continue;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cFW&8] &e") + message);
            }
        } catch (Throwable ignored) {
        }
    }
}
