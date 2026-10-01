package space.qclid.arcanum.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/** One skill: awards XP, applies perks, and describes its next unlock for the GUI. */
public interface Skill extends Listener {

    SkillType type();

    /** Text describing the next perk above {@code level}, or {@code null} when all are unlocked. */
    String nextPerk(int level);

    /** Called every 5 ticks for each online player (only while the skill is enabled). */
    default void tick(Player player) {}
}
