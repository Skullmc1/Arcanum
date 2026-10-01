package space.qclid.arcanum.skills.skill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import space.qclid.arcanum.skills.PlacedBlockTracker;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;
import space.qclid.arcanum.skills.SkillsConfig;

import java.util.Collection;
import java.util.Map;

/**
 * Mining: XP for stone and ores, virtual Fortune on ores, and instant smelting at max level.
 * Virtual Efficiency is applied separately by {@code ToolSpeedService}.
 */
public final class MiningSkill implements Skill {

    private static final Map<Material, Material> SMELTED = Map.of(
        Material.RAW_IRON, Material.IRON_INGOT,
        Material.RAW_GOLD, Material.GOLD_INGOT,
        Material.RAW_COPPER, Material.COPPER_INGOT,
        Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

    private final SkillManager manager;
    private final SkillsConfig config;
    private final PlacedBlockTracker tracker;

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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!manager.isEnabled(SkillType.MINING) || !manager.canEarn(player)) return;

        Block block = event.getBlock();
        Material type = block.getType();
        double xp = config.miningXp.getOrDefault(type.name(), 0.0);
        boolean ore = isOre(type);
        if (xp <= 0 && !ore) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!isPickaxe(tool.getType())) return;

        // A block the player placed earns nothing and gets no perks.
        if (tracker.consumeIfPlaced(block)) return;

        manager.addXp(player, SkillType.MINING, xp);
        if (!ore) return;
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        int level = manager.level(player.getUniqueId(), SkillType.MINING);
        int toolFortune = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int fortune = config.mining.effectiveFortune(toolFortune, level);
        boolean smelt = config.mining.instantSmelt(level);
        if (fortune == toolFortune && !smelt) return; // vanilla drops are already right

        ItemStack boosted = tool.clone();
        if (fortune > toolFortune) boosted.addUnsafeEnchantment(Enchantment.FORTUNE, fortune);
        Collection<ItemStack> drops = block.getDrops(boosted, player);

        event.setDropItems(false);
        Location at = block.getLocation().add(0.5, 0.5, 0.5);
        for (ItemStack drop : drops) {
            ItemStack out = smelt ? smelted(drop) : drop;
            block.getWorld().dropItemNaturally(at, out);
        }
    }

    private static ItemStack smelted(ItemStack drop) {
        Material result = SMELTED.get(drop.getType());
        if (result == null) return drop;
        return new ItemStack(result, drop.getAmount());
    }
}
