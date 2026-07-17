package space.qclid.dashboard.feature;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.data.DataManager;

/**
 * Spawns flame particle trail pointing toward the player's active destination or tracked player.
 * Called every scheduler tick from the main plugin.
 */
public class NavigationFeature {

    private final DataManager data;

    public NavigationFeature(DataManager data) {
        this.data = data;
    }

    public void spawnParticles(Player player) {
        PlayerSettings settings = data.get(player.getUniqueId());
        if (settings == null) return;

        Location target = null;

        if (settings.destination != null && settings.destination.getWorld() != null
                && settings.destination.getWorld().equals(player.getWorld())) {
            target = settings.destination;
        } else if (settings.trackingPlayer != null) {
            Player tracked = org.bukkit.Bukkit.getPlayer(settings.trackingPlayer);
            if (tracked != null && tracked.isOnline() && tracked.getWorld().equals(player.getWorld())) {
                target = tracked.getLocation();
            }
        }

        if (target != null) {
            Location playerLoc = player.getEyeLocation().subtract(0, 0.5, 0);
            Vector direction = target.toVector().subtract(playerLoc.toVector()).normalize();
            for (double i = 2.0; i <= 4.0; i += 0.5) {
                Location particleLoc = playerLoc.clone().add(direction.clone().multiply(i));
                player.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
            }
        }
    }
}
