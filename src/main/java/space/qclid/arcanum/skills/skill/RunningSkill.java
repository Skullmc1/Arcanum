package space.qclid.arcanum.skills.skill;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;
import space.qclid.arcanum.skills.SkillsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Running: sprinting earns XP per block and, at higher levels, grants Speed after a streak of
 * continuous sprinting. Sampled from the plugin's 5-tick scheduler.
 */
public final class RunningSkill implements Skill {

    /** Largest horizontal move accepted in one sample; anything bigger is a teleport. */
    private static final double MAX_STEP = 4.0;
    private static final int EFFECT_TICKS = 30;

    private final SkillManager manager;
    private final SkillsConfig config;
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final Map<UUID, Double> streak = new HashMap<>();

    public RunningSkill(SkillManager manager, SkillsConfig config) {
        this.manager = manager;
        this.config = config;
    }

    @Override
    public SkillType type() {
        return SkillType.RUNNING;
    }

    @Override
    public String nextPerk(int level) {
        return config.running.nextPerk(level);
    }

    @Override
    public void tick(Player player) {
        UUID id = player.getUniqueId();
        Location now = player.getLocation();
        Location prev = lastLocation.put(id, now.clone());

        boolean sprinting = manager.canEarn(player)
            && player.isSprinting()
            && !player.isInsideVehicle()
            && !player.isGliding()
            && !player.isFlying()
            && !player.isInWater();
        if (!sprinting || prev == null || !now.getWorld().equals(prev.getWorld())) {
            streak.remove(id);
            return;
        }

        double dx = now.getX() - prev.getX();
        double dz = now.getZ() - prev.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > MAX_STEP) {
            streak.remove(id);
            return;
        }

        double run = streak.merge(id, dist, Double::sum);
        manager.addXp(player, SkillType.RUNNING, dist * config.runningXpPerBlock);

        int level = manager.level(id, SkillType.RUNNING);
        config.running.tierFor(level).ifPresent(tier -> {
            if (run >= tier.blocks()) applySpeed(player, tier.amplifier());
        });
    }

    private void applySpeed(Player player, int amplifier) {
        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        if (current != null && current.getAmplifier() > amplifier) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_TICKS, amplifier, true, false, true));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        lastLocation.remove(id);
        streak.remove(id);
    }
}
