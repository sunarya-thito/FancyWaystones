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

    public void transportPlayer(Player player, ProxyLocation location, UUID sourceWaystoneId, UUID waystoneId) {
        Location loc = player.getLocation();
        sendMessage(player, new TeleportMessage(player.getUniqueId(), true,
                new SerializableLocation(sourceWaystoneId, FancyWaystones.getPlugin().getServerName(), loc.getWorld().getUID(), loc.getX(), loc.getY(), loc.getZ()),
                new SerializableLocation(waystoneId, location.getServerName(), location.getWorldUUID(), location.getX(), location.getY(), location.getZ())));
    }

    public void dispatchWaystoneReload(UUID uuid) {
        sendMessage(new WaystoneReloadRequestMessage(uuid));
    }

    public void dispatchWaystoneDestroy(UUID uuid, String reason) {
        sendMessage(new WaystoneDestroyMessage(uuid, reason));
    }

    public void dispatchWaystoneUnload(UUID id) {
        sendMessage(new WaystoneUnloadRequestMessage(id));
    }

    public void showInfo(Player player) {
        sendMessage(player, new RequestInfoMessage());
    }

    public void dispatchWaystoneLoad(UUID id) {
        sendMessage(new WaystoneLoadRequestMessage(id));
    }

    private void sendPluginMessage(Player player, byte[] data) {
        player.sendPluginMessage(FancyWaystones.getPlugin(), "fancywaystones:waystone", data);
    }

    public void sendMessage(Message message) {
        Bukkit.getOnlinePlayers().stream().findAny().ifPresent(player -> {
            sendMessage(player, message);
        });
    }

    public void sendMessage(Player player, Message message) {
        sendPluginMessage(player, message.write());
    }

}
