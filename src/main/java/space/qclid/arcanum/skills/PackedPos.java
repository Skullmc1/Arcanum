package space.qclid.arcanum.skills;

/** Packs a block position into a chunk-local long (so it can live in a chunk's persistent data). */
public final class PackedPos {

    private static final int Y_OFFSET = 2048;

    private PackedPos() {}

    public static long pack(int x, int y, int z) {
        return ((long) (y + Y_OFFSET) << 8) | ((long) (x & 15) << 4) | (long) (z & 15);
    }
}
