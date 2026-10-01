package space.qclid.arcanum.skills;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import space.qclid.arcanum.compat.Compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Remembers blocks players placed (only block types a skill rewards), stored in the
 * chunk's persistent data, so placing and re-breaking a block cannot farm XP.
 */
public final class PlacedBlockTracker implements Listener {

    private static final NamespacedKey KEY = Compat.key("placed_blocks");

    private final List<Predicate<Material>> rules = new ArrayList<>();

    /** Adds a rule: blocks of any type matching a rule are tracked when placed. */
    public void trackWhen(Predicate<Material> rule) {
        rules.add(rule);
    }

    private boolean isTrackedType(Material material) {
        for (Predicate<Material> rule : rules) {
            if (rule.test(material)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (!isTrackedType(block.getType())) return;
        add(block);
    }

    /** @return true (and forgets the block) if a player placed this block. */
    public boolean consumeIfPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        long[] stored = pdc.get(KEY, PersistentDataType.LONG_ARRAY);
        if (stored == null || stored.length == 0) return false;

        long packed = PackedPos.pack(block.getX(), block.getY(), block.getZ());
        int index = -1;
        for (int i = 0; i < stored.length; i++) {
            if (stored[i] == packed) { index = i; break; }
        }
        if (index < 0) return false;

        if (stored.length == 1) {
            pdc.remove(KEY);
        } else {
            long[] next = new long[stored.length - 1];
            System.arraycopy(stored, 0, next, 0, index);
            System.arraycopy(stored, index + 1, next, index, stored.length - index - 1);
            pdc.set(KEY, PersistentDataType.LONG_ARRAY, next);
        }
        return true;
    }

    private void add(Block block) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        long[] stored = pdc.get(KEY, PersistentDataType.LONG_ARRAY);
        long packed = PackedPos.pack(block.getX(), block.getY(), block.getZ());
        if (stored == null) {
            pdc.set(KEY, PersistentDataType.LONG_ARRAY, new long[] {packed});
            return;
        }
        for (long l : stored) {
            if (l == packed) return;
        }
        long[] next = new long[stored.length + 1];
        System.arraycopy(stored, 0, next, 0, stored.length);
        next[stored.length] = packed;
        pdc.set(KEY, PersistentDataType.LONG_ARRAY, next);
    }
}
