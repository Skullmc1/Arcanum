package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.data.DataManager;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Handles /linkchest (link a single chest you're standing on)
 * and /chest (remotely open your linked chest from anywhere).
 */
public class LinkedChestFeature {

    private final DataManager data;

    public LinkedChestFeature(DataManager data) {
        this.data = data;
    }

    public void registerCommands(Commands commands) {
        var linkChestBuilder = Commands.literal("linkchest")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;

                org.bukkit.block.Block b1 = player.getLocation().getBlock();
                org.bukkit.block.Block b2 = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();

                org.bukkit.block.Block block = null;
                if      (b1.getType() == Material.CHEST || b1.getType() == Material.TRAPPED_CHEST) block = b1;
                else if (b2.getType() == Material.CHEST || b2.getType() == Material.TRAPPED_CHEST) block = b2;

                if (block == null) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be standing on a chest!")));
                    return 1;
                }

                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                if (chest.getInventory() instanceof org.bukkit.inventory.DoubleChestInventory) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Only single chests can be linked!")));
                    return 1;
                }

                PlayerSettings settings = data.getOrCreate(player.getUniqueId());
                settings.linkedChest = block.getLocation();
                data.save(player.getUniqueId());
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Chest linked successfully!")));
                return 1;
            });

        commands.register(linkChestBuilder.build(), "Link a chest you are standing on", List.of());

        var chestBuilder = Commands.literal("chest")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                PlayerSettings settings = data.get(player.getUniqueId());

                if (settings == null || settings.linkedChest == null) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You have no linked chest! Use /linkchest while standing on one.")));
                    return 1;
                }

                org.bukkit.block.Block block = settings.linkedChest.getBlock();
                if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The linked chest no longer exists!")));
                    settings.linkedChest = null;
                    data.save();
                    return 1;
                }

                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                if (chest.getInventory() instanceof org.bukkit.inventory.DoubleChestInventory) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The linked chest has become a double chest and is now invalid.")));
                    settings.linkedChest = null;
                    data.save();
                    return 1;
                }

                player.openInventory(chest.getInventory());
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                return 1;
            });

        commands.register(chestBuilder.build(), "Open your linked chest", List.of());
    }
}
