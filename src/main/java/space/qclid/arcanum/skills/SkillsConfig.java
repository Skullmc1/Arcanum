package space.qclid.arcanum.skills;

import org.bukkit.configuration.ConfigurationSection;
import space.qclid.arcanum.skills.perk.MiningPerks;
import space.qclid.arcanum.skills.perk.RunningPerks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/** Values read from {@code skills.yml}; every field falls back to a safe default. */
public final class SkillsConfig {

    private static final Map<String, Double> DEFAULT_MINING_XP = defaultMiningXp();

    // Sanity limits: keep the cumulative XP table small and far from overflowing a long.
    private static final double MAX_CURVE_A = 1000.0;
    private static final double MAX_CURVE_P = 3.0;
    private static final int MAX_LEVEL_LIMIT = 1000;

    public final double curveA;
    public final double curveP;
    public final int maxLevel;
    public final double runningXpPerBlock;
    public final Map<String, Double> miningXp;
    public final RunningPerks running;
    public final MiningPerks mining;
    public final Set<String> disabledWorlds;
    public final Set<SkillType> disabledSkills;

    private SkillsConfig(double curveA, double curveP, int maxLevel, double runningXpPerBlock,
                         Map<String, Double> miningXp, RunningPerks running, MiningPerks mining,
                         Set<String> disabledWorlds, Set<SkillType> disabledSkills) {
        this.curveA = curveA;
        this.curveP = curveP;
        this.maxLevel = maxLevel;
        this.runningXpPerBlock = runningXpPerBlock;
        this.miningXp = miningXp;
        this.running = running;
        this.mining = mining;
        this.disabledWorlds = disabledWorlds;
        this.disabledSkills = disabledSkills;
    }

    public static SkillsConfig load(ConfigurationSection s, Logger log) {
        double a = s.getDouble("curve.a", 12.0);
        double p = s.getDouble("curve.p", 1.3);
        int max = s.getInt("curve.max-level", 150);
        if (a <= 0 || a > MAX_CURVE_A || p < 0 || p > MAX_CURVE_P || max < 1 || max > MAX_LEVEL_LIMIT) {
            log.warning("Invalid curve in skills.yml, using defaults.");
            a = 12.0;
            p = 1.3;
            max = 150;
        }

        // Defaults first, so a partial section only overrides the blocks it lists (set a block to 0 to turn it off).
        Map<String, Double> miningXp = new HashMap<>(DEFAULT_MINING_XP);
        ConfigurationSection mx = s.getConfigurationSection("xp.mining");
        if (mx != null) {
            for (String key : mx.getKeys(false)) {
                miningXp.put(key.toUpperCase(), Math.max(0.0, mx.getDouble(key)));
            }
        }

        Set<SkillType> disabled = new HashSet<>();
        for (String id : s.getStringList("disabled-skills")) {
            SkillType.fromId(id).ifPresentOrElse(disabled::add,
                () -> log.warning("Unknown skill id in disabled-skills: " + id));
        }

        int[] fortune = s.getIntegerList("perks.mining.fortune-levels").stream().mapToInt(Integer::intValue).toArray();
        if (fortune.length == 0) fortune = new int[] {50, 100, 150};

        return new SkillsConfig(
            a, p, max,
            Math.max(0.0, s.getDouble("xp.running-per-block", 0.3)),
            Map.copyOf(miningXp),
            new RunningPerks(readTiers(s, log)),
            new MiningPerks(
                s.getInt("perks.mining.efficiency-every", 25),
                s.getInt("perks.mining.max-efficiency", 6),
                fortune,
                s.getInt("perks.mining.smelt-level", 150)),
            new HashSet<>(s.getStringList("disabled-worlds")),
            disabled);
    }

    private static List<RunningPerks.Tier> readTiers(ConfigurationSection s, Logger log) {
        List<RunningPerks.Tier> tiers = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("perks.running.tiers")) {
            try {
                tiers.add(new RunningPerks.Tier(num(m, "level"), num(m, "amplifier"), num(m, "blocks")));
            } catch (RuntimeException e) {
                log.warning("Invalid running tier in skills.yml: " + m);
            }
        }
        return tiers.isEmpty() ? RunningPerks.DEFAULT_TIERS : tiers;
    }

    private static int num(Map<?, ?> m, String key) {
        return ((Number) m.get(key)).intValue();
    }

    private static Map<String, Double> defaultMiningXp() {
        Map<String, Double> m = new HashMap<>();
        for (String n : new String[] {"STONE", "DEEPSLATE", "GRANITE", "DIORITE", "ANDESITE", "TUFF",
                "NETHERRACK", "BASALT", "BLACKSTONE", "END_STONE"}) m.put(n, 1.0);
        m.put("COAL_ORE", 8.0);        m.put("DEEPSLATE_COAL_ORE", 8.0);
        m.put("COPPER_ORE", 10.0);     m.put("DEEPSLATE_COPPER_ORE", 10.0);
        m.put("IRON_ORE", 15.0);       m.put("DEEPSLATE_IRON_ORE", 15.0);
        m.put("REDSTONE_ORE", 12.0);   m.put("DEEPSLATE_REDSTONE_ORE", 12.0);
        m.put("LAPIS_ORE", 12.0);      m.put("DEEPSLATE_LAPIS_ORE", 12.0);
        m.put("GOLD_ORE", 25.0);       m.put("DEEPSLATE_GOLD_ORE", 25.0);
        m.put("NETHER_GOLD_ORE", 10.0); m.put("NETHER_QUARTZ_ORE", 10.0);
        m.put("EMERALD_ORE", 40.0);    m.put("DEEPSLATE_EMERALD_ORE", 40.0);
        m.put("DIAMOND_ORE", 60.0);    m.put("DEEPSLATE_DIAMOND_ORE", 60.0);
        m.put("ANCIENT_DEBRIS", 100.0);
        return m;
    }
}
