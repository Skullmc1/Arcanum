package space.qclid.arcanum.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillCurveTest {

    private final SkillCurve curve = new SkillCurve(12.0, 1.3, 150);

    @Test
    void levelOneNeedsNoXp() {
        assertEquals(0, curve.totalXpForLevel(1));
        assertEquals(1, curve.levelForXp(0));
    }

    @Test
    void xpToNextFollowsTheFormula() {
        assertEquals(Math.round(12 * Math.pow(10, 1.3)), curve.xpToNext(10));
    }

    @Test
    void xpToNextIsZeroAtMaxLevel() {
        assertEquals(0, curve.xpToNext(150));
        assertEquals(0, curve.xpToNext(0));
    }

    @Test
    void totalXpIsStrictlyIncreasing() {
        for (int l = 2; l <= 150; l++) {
            assertTrue(curve.totalXpForLevel(l) > curve.totalXpForLevel(l - 1), "level " + l);
        }
    }

    @Test
    void levelForXpRoundTripsAtEveryBoundary() {
        for (int l = 1; l <= 150; l++) {
            long t = curve.totalXpForLevel(l);
            assertEquals(l, curve.levelForXp(t), "exactly at level " + l);
            if (l > 1) assertEquals(l - 1, curve.levelForXp(t - 1), "one xp short of level " + l);
        }
    }

    @Test
    void levelIsClampedToValidRange() {
        assertEquals(150, curve.levelForXp(Long.MAX_VALUE));
        assertEquals(1, curve.levelForXp(-5));
        assertEquals(curve.totalXpForLevel(150), curve.totalXpForLevel(999));
        assertEquals(0, curve.totalXpForLevel(-3));
    }

    @Test
    void progressIsAFractionAndFullAtMax() {
        long half = curve.totalXpForLevel(10) + curve.xpToNext(10) / 2;
        assertEquals(0.5, curve.progress(half), 0.02);
        assertEquals(0.0, curve.progress(curve.totalXpForLevel(10)), 1e-9);
        assertEquals(1.0, curve.progress(curve.maxTotalXp()), 1e-9);
        assertEquals(1.0, curve.progress(Long.MAX_VALUE), 1e-9);
    }

    @Test
    void defaultPacingMatchesTheSpec() {
        long to25 = curve.totalXpForLevel(25);
        long to150 = curve.totalXpForLevel(150);
        assertTrue(to25 >= 6_000 && to25 <= 12_000, "level 25 total was " + to25);
        assertTrue(to150 >= 400_000 && to150 <= 650_000, "level 150 total was " + to150);
        assertEquals(to150, curve.maxTotalXp());
    }

    @Test
    void rejectsInvalidMaxLevel() {
        assertThrows(IllegalArgumentException.class, () -> new SkillCurve(12.0, 1.3, 0));
    }
}
