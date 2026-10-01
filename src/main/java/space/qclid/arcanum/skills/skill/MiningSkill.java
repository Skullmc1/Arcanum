package space.qclid.arcanum.skills.skill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import space.qclid.arcanum.skills.PlacedBlockTracker;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;
import space.qclid.arcanum.skills.SkillsConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mining: XP for stone and ores, virtual Fortune on ores, and instant smelting at max level.
 * Virtual Efficiency is applied separately by {@code ToolSpeedService}.
 *
 * XP and perks are decided at MONITOR priority (after every other plugin had its say), and the boosted
 * drops are only swapped in when the break really completes ({@link BlockDropItemEvent}), so a break that
 * another plugin cancels never grants XP or drops.
 */
public final class MiningSkill implements Skill {

    private static final Map<Material, Material> SMELTED = Map.of(
        Material.RAW_IRON, Material.IRON_INGOT,
        Material.RAW_GOLD, Material.GOLD_INGOT,
        Material.RAW_COPPER, Material.COPPER_INGOT,
        Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

    /** Drops computed at break time, waiting for the break to complete. */
    private record Pending(Location block, List<ItemStack> drops) {}

    private final SkillManager manager;
    private final SkillsConfig config;
    private final PlacedBlockTracker tracker;
    private final Map<UUID, Pending> pending = new HashMap<>();

    public MiningSkill(SkillManager manager, SkillsConfig config, PlacedBlockTracker tracker) {
        this.manager = manager;
        this.config = config;
        this.tracker = tracker;
        tracker.trackWhen(m -> isOre(m) || config.miningXp.containsKey(m.name()));
    }

    public static boolean isOre(Material m) {
        return m.name().endsWith("_ORE") || m == Material.ANCIENT_DEBRIS;
    }

    public static boolean isPickaxe(Material m) {
        return m.name().endsWith("_PICKAXE");
    }

    @Override
    public SkillType type() {
        return SkillType.MINING;
    }

    @Override
    public String nextPerk(int level) {
        return config.mining.nextPerk(level);
    }

    /**
     * Awards Mining XP for {@code block} if it is a natural (not player-placed) mining block broken with a
     * pickaxe. Also used by Codex tools that break blocks themselves. Call before the block is removed.
     *
     * @return true if the block counted (so perks may apply)
     */
    public boolean awardXp(Player player, Block block, ItemStack tool) {
        if (!manager.isEnabled(SkillType.MINING) || !manager.canEarn(player)) return false;
        Material type = block.getType();
        double xp = config.miningXp.getOrDefault(type.name(), 0.0);
        if (xp <= 0 && !isOre(type)) return false;
        if (tool == null || !isPickaxe(tool.getType())) return false;
        // A block the player placed earns nothing and gets no perks.
        if (tracker.consumeIfPlaced(block)) return false;
        manager.addXp(player, SkillType.MINING, xp);
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!awardXp(player, block, tool)) return;
        if (!isOre(block.getType())) return;
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        int level = manager.level(player.getUniqueId(), SkillType.MINING);
        int toolFortune = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int fortune = config.mining.effectiveFortune(toolFortune, level);
        boolean smelt = config.mining.instantSmelt(level);
        if (fortune == toolFortune && !smelt) return; // vanilla drops are already right

        ItemStack boosted = tool.clone();
        if (fortune > toolFortune) boosted.addUnsafeEnchantment(Enchantment.FORTUNE, fortune);
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack drop : block.getDrops(boosted, player)) {
            drops.add(smelt ? smelted(drop) : drop);
        }
        pending.put(player.getUniqueId(), new Pending(block.getLocation(), drops));
    }

    /** Swaps in the boosted drops once the break has actually happened. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent event) {
        Pending p = pending.remove(event.getPlayer().getUniqueId());
        if (p == null) return;
        Location at = event.getBlock().getLocation();
        if (!at.equals(p.block())) return;

        event.getItems().clear();
        Location center = at.clone().add(0.5, 0.5, 0.5);
        for (ItemStack drop : p.drops()) {
            at.getWorld().dropItemNaturally(center, drop);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    private static ItemStack smelted(ItemStack drop) {
        Material result = SMELTED.get(drop.getType());
        if (result == null) return drop;
        return new ItemStack(result, drop.getAmount());
    }
}
