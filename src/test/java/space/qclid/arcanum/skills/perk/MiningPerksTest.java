package space.qclid.arcanum.skills.perk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiningPerksTest {

    private final MiningPerks perks = MiningPerks.defaults();

    @Test
    void efficiencyGrowsEvery25LevelsAndCapsAtSix() {
        assertEquals(0, perks.efficiencyBonus(1));
        assertEquals(0, perks.efficiencyBonus(24));
        assertEquals(1, perks.efficiencyBonus(25));
        assertEquals(2, perks.efficiencyBonus(50));
        assertEquals(6, perks.efficiencyBonus(150));
        assertEquals(6, perks.efficiencyBonus(400));
    }

    @Test
    void fortuneUnlocksAt50100150() {
        assertEquals(0, perks.fortuneBonus(49));
        assertEquals(1, perks.fortuneBonus(50));
        assertEquals(1, perks.fortuneBonus(99));
        assertEquals(2, perks.fortuneBonus(100));
        assertEquals(3, perks.fortuneBonus(150));
    }

    @Test
    void instantSmeltOnlyAtMaxLevel() {
        assertFalse(perks.instantSmelt(149));
        assertTrue(perks.instantSmelt(150));
    }

    @Test
    void virtualFortuneStacksWithTheTool() {
        assertEquals(3, perks.effectiveFortune(3, 1));
        assertEquals(4, perks.effectiveFortune(3, 50));
        assertEquals(6, perks.effectiveFortune(3, 150));
        assertEquals(0, perks.effectiveFortune(0, 10));
    }

    @Test
    void breakSpeedBonusIsTwentyPercentPerEfficiencyLevel() {
        assertEquals(0.0, perks.breakSpeedBonus(10), 1e-9);
        assertEquals(0.2, perks.breakSpeedBonus(25), 1e-9);
        assertEquals(1.2, perks.breakSpeedBonus(150), 1e-9);
    }

    @Test
    void nextPerkJoinsPerksThatShareALevel() {
        assertEquals("Level 25: Efficiency +1", perks.nextPerk(1));
        assertEquals("Level 50: Efficiency +2, Fortune I", perks.nextPerk(49));
        assertEquals("Level 150: Efficiency +6, Fortune III, Instant ore smelting", perks.nextPerk(149));
        assertNull(perks.nextPerk(150));
    }
}
