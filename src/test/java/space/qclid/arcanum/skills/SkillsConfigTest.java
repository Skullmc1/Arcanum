package space.qclid.arcanum.skills;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SkillsConfigTest {

    private static final Logger LOG = Logger.getLogger("test");

    private static SkillsConfig load(MemoryConfiguration yaml) {
        return SkillsConfig.load(yaml, LOG);
    }

    @Test
    void emptyConfigGivesTheDefaults() {
        SkillsConfig c = load(new MemoryConfiguration());
        assertEquals(12.0, c.curveA);
        assertEquals(1.3, c.curveP);
        assertEquals(150, c.maxLevel);
        assertEquals(60.0, c.miningXp.get("DIAMOND_ORE"));
        assertEquals(1.0, c.miningXp.get("STONE"));
    }

    @Test
    void partialMiningSectionKeepsTheDefaultsForOtherBlocks() {
        MemoryConfiguration yaml = new MemoryConfiguration();
        yaml.set("xp.mining.DIAMOND_ORE", 100);

        SkillsConfig c = load(yaml);

        assertEquals(100.0, c.miningXp.get("DIAMOND_ORE"));
        assertEquals(1.0, c.miningXp.get("STONE"), "unlisted blocks keep their default XP");
        assertEquals(15.0, c.miningXp.get("IRON_ORE"));
    }

    @Test
    void settingABlockToZeroTurnsItsXpOff() {
        MemoryConfiguration yaml = new MemoryConfiguration();
        yaml.set("xp.mining.STONE", 0);
        assertEquals(0.0, load(yaml).miningXp.get("STONE"));
    }

    @Test
    void blockNamesAreCaseInsensitive() {
        MemoryConfiguration yaml = new MemoryConfiguration();
        yaml.set("xp.mining.diamond_ore", 7);
        assertEquals(7.0, load(yaml).miningXp.get("DIAMOND_ORE"));
    }

    private static void assertFallsBackToDefaultCurve(String path, Object badValue) {
        MemoryConfiguration yaml = new MemoryConfiguration();
        yaml.set(path, badValue);
        SkillsConfig c = load(yaml);
        assertEquals(12.0, c.curveA, path + "=" + badValue);
        assertEquals(1.3, c.curveP, path + "=" + badValue);
        assertEquals(150, c.maxLevel, path + "=" + badValue);
    }

    @Test
    void absurdCurveValuesFallBackToTheDefaults() {
        assertFallsBackToDefaultCurve("curve.max-level", 5_000_000);
        assertFallsBackToDefaultCurve("curve.max-level", 0);
        assertFallsBackToDefaultCurve("curve.a", 100_000.0);
        assertFallsBackToDefaultCurve("curve.a", -3.0);
        assertFallsBackToDefaultCurve("curve.p", 9.0);
    }

    @Test
    void sensibleCustomCurveIsKept() {
        MemoryConfiguration yaml = new MemoryConfiguration();
        yaml.set("curve.a", 20.0);
        yaml.set("curve.p", 1.5);
        yaml.set("curve.max-level", 200);
        SkillsConfig c = load(yaml);
        assertEquals(20.0, c.curveA);
        assertEquals(1.5, c.curveP);
        assertEquals(200, c.maxLevel);
    }
}
