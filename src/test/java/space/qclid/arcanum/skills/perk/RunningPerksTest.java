package space.qclid.arcanum.skills.perk;

import org.junit.jupiter.api.Test;
import space.qclid.arcanum.skills.perk.RunningPerks.Tier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RunningPerksTest {

    private final RunningPerks perks = RunningPerks.defaults();

    @Test
    void noPerkBelowLevel25() {
        assertTrue(perks.tierFor(1).isEmpty());
        assertTrue(perks.tierFor(24).isEmpty());
    }

    @Test
    void tierBoundariesMatchTheSpec() {
        assertEquals(new Tier(25, 0, 10), perks.tierFor(25).orElseThrow());
        assertEquals(new Tier(25, 0, 10), perks.tierFor(49).orElseThrow());
        assertEquals(new Tier(50, 1, 5), perks.tierFor(50).orElseThrow());
        assertEquals(new Tier(50, 1, 5), perks.tierFor(99).orElseThrow());
        assertEquals(new Tier(100, 1, 0), perks.tierFor(100).orElseThrow());
        assertEquals(new Tier(150, 2, 0), perks.tierFor(150).orElseThrow());
    }

    @Test
    void nextTierLooksAhead() {
        assertEquals(25, perks.nextTier(0).orElseThrow().level());
        assertEquals(50, perks.nextTier(25).orElseThrow().level());
        assertTrue(perks.nextTier(150).isEmpty());
    }

    @Test
    void unsortedTiersAreSorted() {
        RunningPerks p = new RunningPerks(List.of(new Tier(100, 1, 0), new Tier(25, 0, 10)));
        assertEquals(25, p.tierFor(60).orElseThrow().level());
        assertEquals(100, p.tierFor(120).orElseThrow().level());
    }

    @Test
    void describesTiersForTheGui() {
        assertEquals("Speed I after sprinting 10 blocks", RunningPerks.describe(new Tier(25, 0, 10)));
        assertEquals("Speed III as soon as you sprint", RunningPerks.describe(new Tier(150, 2, 0)));
    }

    @Test
    void nextPerkTextOrNullAtMax() {
        assertEquals("Level 25: Speed I after sprinting 10 blocks", perks.nextPerk(1));
        assertNull(perks.nextPerk(150));
    }
}
