package space.qclid.arcanum.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedPosTest {

    @Test
    void sameChunkLocalPositionPacksEqually() {
        assertEquals(PackedPos.pack(3, 64, 5), PackedPos.pack(3 + 16, 64, 5 + 32));
    }

    @Test
    void negativeCoordinatesWrapIntoTheChunk() {
        assertEquals(PackedPos.pack(15, 10, 14), PackedPos.pack(-1, 10, -2));
    }

    @Test
    void differentPositionsPackDifferently() {
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(2, 64, 1));
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(1, 64, 2));
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(1, 65, 1));
    }

    @Test
    void deepAndHighYValuesStayDistinctAndNonNegative() {
        long deep = PackedPos.pack(0, -64, 0);
        long high = PackedPos.pack(0, 319, 0);
        assertTrue(deep >= 0);
        assertTrue(high >= 0);
        assertNotEquals(deep, high);
    }
}
