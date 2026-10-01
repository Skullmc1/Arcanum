package space.qclid.arcanum.skills;

/**
 * XP ↔ level maths. XP needed to go from level L to L+1 is {@code round(a * L^p)}.
 * Level 1 corresponds to 0 total XP; {@code maxLevel} is the cap.
 */
public final class SkillCurve {

    private final double a;
    private final double p;
    private final int maxLevel;
    /** cumulative[L] = total XP required to reach level L (index 0 unused). */
    private final long[] cumulative;

    public SkillCurve(double a, double p, int maxLevel) {
        if (maxLevel < 1) throw new IllegalArgumentException("maxLevel must be >= 1");
        this.a = a;
        this.p = p;
        this.maxLevel = maxLevel;
        this.cumulative = new long[maxLevel + 1];
        for (int l = 2; l <= maxLevel; l++) {
            cumulative[l] = cumulative[l - 1] + xpToNext(l - 1);
        }
    }

    public int maxLevel() { return maxLevel; }

    /** XP needed to advance from {@code level} to {@code level + 1}; 0 at or beyond the cap. */
    public long xpToNext(int level) {
        if (level < 1 || level >= maxLevel) return 0;
        return Math.max(1, Math.round(a * Math.pow(level, p)));
    }

    /** Total XP at which {@code level} is reached (clamped to 1..maxLevel). */
    public long totalXpForLevel(int level) {
        if (level < 1) return 0;
        return cumulative[Math.min(level, maxLevel)];
    }

    /** Level for a total XP value, clamped to 1..maxLevel. */
    public int levelForXp(long totalXp) {
        if (totalXp <= 0) return 1;
        int lo = 1;
        int hi = maxLevel;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (cumulative[mid] <= totalXp) lo = mid; else hi = mid - 1;
        }
        return lo;
    }

    /** XP earned inside the current level. */
    public long xpIntoLevel(long totalXp) {
        int l = levelForXp(totalXp);
        return Math.max(0, totalXp - cumulative[l]);
    }

    /** Fraction (0..1) of the way to the next level; 1.0 at the cap. */
    public double progress(long totalXp) {
        int l = levelForXp(totalXp);
        if (l >= maxLevel) return 1.0;
        return (double) (totalXp - cumulative[l]) / xpToNext(l);
    }

    public long maxTotalXp() { return cumulative[maxLevel]; }
}
