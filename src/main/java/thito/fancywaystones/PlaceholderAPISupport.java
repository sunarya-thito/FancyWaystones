package thito.fancywaystones;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderAPISupport {
    public static boolean enableSupport;
    public static String attemptReplace(Placeholder placeholder, String string) {
        try {
            Player player = placeholder.get(Placeholder.PLAYER);
            string = PlaceholderAPI.setPlaceholders(player, string);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return string;
    }
}
