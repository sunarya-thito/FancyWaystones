package thito.fancywaystones.proxy;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.proxy.message.*;

import java.util.*;

public class ProxyWaystone {

    public void introduceServer(Player player) {
        sendMessage(player, new ServerIntroductionMessage(FancyWaystones.getPlugin().getServerName()));
    }

    public void transportPlayer(Player player, ProxyLocation location) {
        Location loc = player.getLocation();
        sendMessage(player, new TeleportMessage(player.getUniqueId(), true,
                new SerializableLocation(FancyWaystones.getPlugin().getServerName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()),
                new SerializableLocation(location.getServerName(), location.getWorldName(), location.getX(), location.getY(), location.getZ())));
    }

    public void dispatchWaystoneReload(UUID uuid) {
        sendMessage(new WaystoneReloadMessage(uuid));
    }

    public void dispatchWaystoneDestroy(UUID uuid, String reason) {
        sendMessage(new WaystoneDestroyMessage(uuid, reason));
    }

    public void dispatchWaystoneUnload(UUID id) {
        sendMessage(new WaystoneUnloadRequestMessage(id));
    }

    public void dispatchWaystoneLoad(UUID id) {
        sendMessage(new WaystoneLoadRequestMessage(id));
    }

    private void sendPluginMessage(Player player, byte[] data) {
        player.sendPluginMessage(FancyWaystones.getPlugin(), "fancywaystones:waystone", data);
    }

    private void sendMessage(Message message) {
        Bukkit.getOnlinePlayers().stream().findAny().ifPresent(player -> {
            sendMessage(player, message);
        });
    }

    private void sendMessage(Player player, Message message) {
        sendPluginMessage(player, message.write());
    }

}
