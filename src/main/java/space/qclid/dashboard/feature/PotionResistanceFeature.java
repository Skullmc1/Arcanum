package space.qclid.dashboard.feature;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Halves the duration of any harmful potion effect that a player received
 * within the last 60 seconds, providing natural resistance to repeated status effects.
 */
public class PotionResistanceFeature implements Listener {

    /** Tracks the last time each harmful effect was applied per player. */
    private final Map<UUID, Map<PotionEffectType, Long>> effectHistory = new HashMap<>();

    public PotionResistanceFeature(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED
                && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;

        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null || newEffect.getType().getCategory() != PotionEffectTypeCategory.HARMFUL) return;

        Map<PotionEffectType, Long> history = effectHistory.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long currentTime = System.currentTimeMillis();
        Long lastTime    = history.get(newEffect.getType());

        if (lastTime != null && (currentTime - lastTime) < 60_000L) {
            int newDuration = newEffect.getDuration() / 2;
            if (newDuration > 0) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(
                        newEffect.getType(), newDuration, newEffect.getAmplifier(),
                        newEffect.isAmbient(), newEffect.hasParticles(), newEffect.hasIcon()));
                player.sendMessage(MM.deserialize(C_RED + "⚠ " + toSmallCaps("Resistance active") + ": "
                        + C_ORANGE + toSmallCaps(newEffect.getType().key().value()
                                .replace("minecraft:", "").replace("_", " "))
                        + " " + toSmallCaps("duration halved!")));
            }
        }
        history.put(newEffect.getType(), currentTime);
    }
}
