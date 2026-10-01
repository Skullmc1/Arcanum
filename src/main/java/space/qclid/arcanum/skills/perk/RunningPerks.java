package space.qclid.arcanum.skills.perk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Speed tiers unlocked by the Running skill. */
public final class RunningPerks {

    /** @param level   skill level that unlocks the tier
     *  @param amplifier Speed potion amplifier (0 = Speed I)
     *  @param blocks  blocks sprinted continuously before the effect starts (0 = immediately) */
    public record Tier(int level, int amplifier, int blocks) {}

    public static final List<Tier> DEFAULT_TIERS = List.of(
        new Tier(25, 0, 10),
        new Tier(50, 1, 5),
        new Tier(100, 1, 0),
        new Tier(150, 2, 0));

    private final List<Tier> tiers;

    public RunningPerks(List<Tier> tiers) {
        List<Tier> sorted = new ArrayList<>(tiers);
        sorted.sort(Comparator.comparingInt(Tier::level));
        this.tiers = List.copyOf(sorted);
    }

    public static RunningPerks defaults() {
        return new RunningPerks(DEFAULT_TIERS);
    }

    /** The highest tier whose level is at or below {@code level}. */
    public Optional<Tier> tierFor(int level) {
        Tier best = null;
        for (Tier t : tiers) {
            if (t.level() <= level) best = t;
        }
        return Optional.ofNullable(best);
    }

    /** The first tier strictly above {@code level}. */
    public Optional<Tier> nextTier(int level) {
        for (Tier t : tiers) {
            if (t.level() > level) return Optional.of(t);
        }
        return Optional.empty();
    }

    public static String describe(Tier t) {
        String speed = "Speed " + Roman.roman(t.amplifier() + 1);
        return t.blocks() <= 0
            ? speed + " as soon as you sprint"
            : speed + " after sprinting " + t.blocks() + " blocks";
    }

    /** Text for the GUI, or {@code null} when every tier is unlocked. */
    public String nextPerk(int level) {
        return nextTier(level).map(t -> "Level " + t.level() + ": " + describe(t)).orElse(null);
    }
}
