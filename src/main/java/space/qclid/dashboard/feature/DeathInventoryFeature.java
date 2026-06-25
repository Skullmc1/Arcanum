package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.data.DataManager;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Manages the death inventory system:
 * - Clears ground drops on death and saves inventory snapshot
 * - Provides /deathinv command to open the recovery GUI (one-time use)
 * - Wipes the snapshot when the GUI is closed
 */
public class DeathInventoryFeature implements Listener {

    private static final String GUI_TITLE = "ᴅᴇᴀᴛʜ ɪɴᴠᴇɴᴛᴏʀʏ";

    private final DataManager data;

    public DeathInventoryFeature(JavaPlugin plugin, DataManager data) {
        this.data = data;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("deathinv")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                PlayerSettings settings = data.get(player.getUniqueId());
                if (settings == null || settings.deathItems == null) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("No death inventory found!")));
                    return 1;
                }
                openDeathInventory(player, settings.deathItems);
                return 1;
            });
        commands.register(builder.build(), "Retrieve items from your last death", List.of());
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player   = event.getEntity();
        Location loc    = player.getLocation();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();

        // Save snapshot and suppress ground drops — /deathinv is the one-time recovery
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
        settings.deathItems = player.getInventory().getContents().clone();
        event.getDrops().clear();
        data.save();

        String command  = String.format("/destination %d %d %d", x, y, z);
        String deathMsg = String.format(
            "%s☠ %s 📍 <white><click:run_command:'%s'><hover:show_text:'%s'>%d, %d, %d</hover></click></white> %s %s",
            C_PURPLE, toSmallCaps("Death location"),
            command, toSmallCaps("click to set destination"),
            x, y, z,
            toSmallCaps("in"), toSmallCaps(player.getWorld().getName()));

        player.sendMessage(MM.deserialize(deathMsg));
        player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Inventory saved! Use /deathinv to retrieve items.")));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = MM.serialize(event.getView().title());
        if (!title.contains(toSmallCaps(GUI_TITLE))) return;

        Player player = (Player) event.getPlayer();
        PlayerSettings settings = data.get(player.getUniqueId());
        if (settings != null) {
            // One-time use: wipe the snapshot regardless of what remains in the GUI
            settings.deathItems = null;
            data.save();
        }
    }

    // ── GUI ───────────────────────────────────────────────────────────────────

    private void openDeathInventory(Player player, ItemStack[] items) {
        Inventory inv = Bukkit.createInventory(null, 45, MM.deserialize(G_GOLD + toSmallCaps(GUI_TITLE)));
        inv.setContents(items);
        player.openInventory(inv);
    }
}
