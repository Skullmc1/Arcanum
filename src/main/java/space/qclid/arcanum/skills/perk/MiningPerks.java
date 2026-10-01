package space.qclid.arcanum.skills.perk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Virtual Efficiency, Fortune and instant smelting for the Mining skill. */
public final class MiningPerks {

    private final int efficiencyEvery;
    private final int maxEfficiency;
    private final int[] fortuneLevels;
    private final int smeltLevel;
    private final TreeMap<Integer, List<String>> milestones = new TreeMap<>();

    public MiningPerks(int efficiencyEvery, int maxEfficiency, int[] fortuneLevels, int smeltLevel) {
        this.efficiencyEvery = Math.max(1, efficiencyEvery);
        this.maxEfficiency = Math.max(0, maxEfficiency);
        this.fortuneLevels = fortuneLevels.clone();
        this.smeltLevel = smeltLevel;
        for (int k = 1; k <= this.maxEfficiency; k++) {
            add(k * this.efficiencyEvery, "Efficiency +" + k);
        }
        for (int i = 0; i < this.fortuneLevels.length; i++) {
            add(this.fortuneLevels[i], "Fortune " + Roman.roman(i + 1));
        }
        add(this.smeltLevel, "Instant ore smelting");
    }

    public static MiningPerks defaults() {
        return new MiningPerks(25, 6, new int[] {50, 100, 150}, 150);
    }

    private void add(int level, String text) {
        milestones.computeIfAbsent(level, k -> new ArrayList<>()).add(text);
    }

    /** Virtual Efficiency levels at {@code level}. */
    public int efficiencyBonus(int level) {
        return Math.min(maxEfficiency, Math.max(0, level) / efficiencyEvery);
    }

    /** Virtual Fortune levels at {@code level}. */
    public int fortuneBonus(int level) {
        int n = 0;
        for (int l : fortuneLevels) {
            if (l <= level) n++;
        }
        return n;
    }

    public boolean instantSmelt(int level) {
        return level >= smeltLevel;
    }

    /** Fortune to use for drops: the tool's own level plus the virtual bonus. */
    public int effectiveFortune(int toolFortune, int level) {
        return Math.max(0, toolFortune) + fortuneBonus(level);
    }

    /** Amount added (as a fraction of base) to the block-break-speed attribute. */
    public double breakSpeedBonus(int level) {
        return 0.2 * efficiencyBonus(level);
    }

    /** Text for the GUI, or {@code null} when everything is unlocked. */
    public String nextPerk(int level) {
        Map.Entry<Integer, List<String>> e = milestones.higherEntry(level);
        if (e == null) return null;
        return "Level " + e.getKey() + ": " + String.join(", ", e.getValue());
    }
}
