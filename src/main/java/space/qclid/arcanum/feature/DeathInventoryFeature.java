package space.qclid.arcanum.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.arcanum.PlayerSettings;
import space.qclid.arcanum.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/**
 * Manages the physical Wither Death Chest system.
 * - Replaces virtual recovery with an in-world Chest + Wither Skeleton Skull structure.
 * - Intercepts death events, routes items to chest, and expels old ones if double-death occurs.
 * - Restricts each player to exactly one active Death Chest structure.
 */
public class DeathInventoryFeature implements Listener {

    private final DataManager data;

    public DeathInventoryFeature(JavaPlugin plugin, DataManager data) {
        this.data = data;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerCommands(Commands commands) {
        // No /deathinv commands are registered in v1.10.
    }

    private boolean isValidDeathChestStructure(Location chestLoc) {
        if (chestLoc == null) return false;
        Block chestBlock = chestLoc.getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST) return false;
        Block above = chestBlock.getRelative(BlockFace.UP);
        return above.getType() == Material.WITHER_SKELETON_SKULL || above.getType() == Material.WITHER_SKELETON_WALL_SKULL;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());

        if (settings.deathChest != null && isValidDeathChestStructure(settings.deathChest)) {
            Block chestBlock = settings.deathChest.getBlock();
            Chest chestState = (Chest) chestBlock.getState();
            Inventory chestInv = chestState.getInventory();

            // Expel existing items if there are any
            boolean hasItems = false;
            for (ItemStack item : chestInv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    hasItems = true;
                    break;
                }
            }
            if (hasItems) {
                for (ItemStack item : chestInv.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        chestBlock.getWorld().dropItemNaturally(chestBlock.getLocation().add(0.5, 1.1, 0.5), item);
                    }
                }
                chestInv.clear();
            }

            // Transfer player inventory into the chest
            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            event.getDrops().clear();

            for (ItemStack drop : drops) {
                if (drop == null || drop.getType() == Material.AIR) continue;
                Map<Integer, ItemStack> leftover = chestInv.addItem(drop);
                for (ItemStack left : leftover.values()) {
                    chestBlock.getWorld().dropItemNaturally(chestBlock.getLocation().add(0.5, 1.1, 0.5), left);
                }
            }

            chestBlock.getWorld().playSound(chestBlock.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 0.8f);
            chestBlock.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, chestBlock.getLocation().add(0.5, 1.5, 0.5), 30, 0.5, 0.5, 0.5);

            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Your inventory has been safely stored in your Death Chest at ") 
                    + C_GOLD + settings.deathChest.getBlockX() + ", " + settings.deathChest.getBlockY() + ", " + settings.deathChest.getBlockZ()));
        } else {
            // Default drop behavior: items drop normally, but we still print coordinate details
            Location loc = player.getLocation();
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            String command = String.format("/destination %d %d %d", x, y, z);
            String deathMsg = String.format(
                "%s☠ %s 📍 <white><click:run_command:'%s'><hover:show_text:'%s'>%d, %d, %d</hover></click></white> %s %s",
                C_PURPLE, toSmallCaps("Death location"),
                command, toSmallCaps("click to set destination"),
                x, y, z,
                toSmallCaps("in"), toSmallCaps(player.getWorld().getName()));

            player.sendMessage(MM.deserialize(deathMsg));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.WITHER_SKELETON_SKULL || block.getType() == Material.WITHER_SKELETON_WALL_SKULL) {
            Block below = block.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.CHEST || below.getType() == Material.TRAPPED_CHEST) {
                Player player = event.getPlayer();
                PlayerSettings settings = data.getOrCreate(player.getUniqueId());

                if (settings.deathChest != null && isValidDeathChestStructure(settings.deathChest)) {
                    event.setCancelled(true);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You already have an active Death Chest at ") 
                            + C_GOLD + settings.deathChest.getBlockX() + ", " + settings.deathChest.getBlockY() + ", " + settings.deathChest.getBlockZ() + "!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                // Register new death chest
                settings.deathChest = below.getLocation();
                data.save(player.getUniqueId());
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Death Chest registered successfully at ") 
                        + C_GOLD + below.getX() + ", " + below.getY() + ", " + below.getZ() + "!"));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        checkAndUnregister(block, event.getPlayer());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            checkAndUnregister(block, null);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            checkAndUnregister(block, null);
        }
    }

    private void checkAndUnregister(Block block, Player breaker) {
        Location checkLoc = null;
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            checkLoc = block.getLocation();
        } else if (block.getType() == Material.WITHER_SKELETON_SKULL || block.getType() == Material.WITHER_SKELETON_WALL_SKULL) {
            checkLoc = block.getRelative(BlockFace.DOWN).getLocation();
        }

        if (checkLoc == null) return;

        for (Map.Entry<UUID, PlayerSettings> entry : data.all().entrySet()) {
            PlayerSettings s = entry.getValue();
            if (s.deathChest != null && s.deathChest.equals(checkLoc)) {
                s.deathChest = null;
                data.save(entry.getKey());

                Player owner = Bukkit.getPlayer(entry.getKey());
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage(MM.deserialize(C_RED + toSmallCaps("Your Death Chest at ") 
                            + C_GOLD + checkLoc.getBlockX() + ", " + checkLoc.getBlockY() + ", " + checkLoc.getBlockZ() 
                            + C_RED + toSmallCaps(" has been broken/unregistered.")));
                    owner.playSound(owner.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
                }
                break;
            }
        }
    }
}
