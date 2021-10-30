package thito.fancywaystones.proxy;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import thito.fancywaystones.FancyWaystones;

public class ServerIntroductionTask extends BukkitRunnable {
    @Override
    public void run() {
        ProxyWaystone proxyWaystone = FancyWaystones.getPlugin().getProxyWaystone();
        if (proxyWaystone == null) {
            cancel();
            return;
        }
        Bukkit.getOnlinePlayers().stream().findAny().ifPresent(proxyWaystone::introduceServer);
    }
}
