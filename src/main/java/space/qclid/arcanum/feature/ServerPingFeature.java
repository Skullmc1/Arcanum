package space.qclid.arcanum.feature;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static space.qclid.arcanum.util.TextUtil.*;

/**
 * Sets the server MOTD to display the current in-game day number.
 */
public class ServerPingFeature implements Listener {

    public ServerPingFeature(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        World world = Bukkit.getWorlds().get(0);
        if (world == null) return;
        long day = world.getFullTime() / 24000L;
        event.motd(MM.deserialize(C_GOLD + toSmallCaps("Day : ") + C_ORANGE + day));
    }
}
