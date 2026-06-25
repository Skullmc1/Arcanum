package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Provides the /enchant and /disenchant commands with custom GUI workflows.
 * Enchanting costs diamonds; disenchanting refunds them.
 */
public class EnchantFeature implements Listener {

    private static final String GUI_ENCHANT    = "Enchanter";
    private static final String GUI_DISENCHANT = "Disenchanter";

    public EnchantFeature(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerCommands(Commands commands) {
        commands.register(Commands.literal("enchant")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                openEnchanterGUI(player);
                return 1;
            }).build(), "Open the enchanter GUI", List.of());

        commands.register(Commands.literal("disenchant")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                openDisenchanterGUI(player);
                return 1;
            }).build(), "Open the disenchanter GUI", List.of());
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component titleComp = event.getView().title();
        String plainTitle   = PlainTextComponentSerializer.plainText().serialize(titleComp);

        boolean isDisenchant = plainTitle.contains(toSmallCaps(GUI_DISENCHANT));
        boolean isEnchant    = !isDisenchant && plainTitle.contains(toSmallCaps(GUI_ENCHANT));
        if (!isEnchant && !isDisenchant) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.ENCHANTED_BOOK) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) return;

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) clicked.getItemMeta();
        if (!bookMeta.hasStoredEnchants()) return;

        Map.Entry<Enchantment, Integer> entry = bookMeta.getStoredEnchants().entrySet().iterator().next();
        Enchantment ench = entry.getKey();
        int level        = entry.getValue();

        if (isEnchant) {
            int cost = (ench.getMaxLevel() == 1) ? 3 : level;
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
                player.closeInventory();
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You need " + cost + " diamonds!")));
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));

            if (held.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) held.getItemMeta();
                meta.addStoredEnchant(ench, level, true);
                held.setItemMeta(meta);
            } else {
                held.addUnsafeEnchantment(ench, level);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
            openEnchanterGUI(player);

        } else {
            int refund = (ench.getMaxLevel() == 1) ? 3 : level;

            if (held.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) held.getItemMeta();
                meta.removeStoredEnchant(ench);
                held.setItemMeta(meta);
            } else {
                held.removeEnchantment(ench);
            }
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, refund));
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);

            Map<Enchantment, Integer> remaining = held.getType() == Material.ENCHANTED_BOOK
                    ? ((EnchantmentStorageMeta) held.getItemMeta()).getStoredEnchants()
                    : held.getEnchantments();
            if (remaining.isEmpty()) player.closeInventory();
            else openDisenchanterGUI(player);
        }
    }

    // ── GUIs ─────────────────────────────────────────────────────────────────

    private void openEnchanterGUI(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item!")));
            return;
        }

        List<Enchantment> possible = new ArrayList<>();
        for (Enchantment ench : Registry.ENCHANTMENT) {
            if (ench.canEnchantItem(held) || held.getType() == Material.ENCHANTED_BOOK) possible.add(ench);
        }
        if (possible.isEmpty()) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item cannot be enchanted!")));
            return;
        }

        int size = Math.min(54, Math.max(9, ((possible.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, parse(G_GOLD + toSmallCaps(GUI_ENCHANT)));

        for (Enchantment ench : possible) {
            int currentLevel = held.getType() == Material.ENCHANTED_BOOK
                    ? ((EnchantmentStorageMeta) held.getItemMeta()).getStoredEnchantLevel(ench)
                    : held.getEnchantmentLevel(ench);
            if (currentLevel >= ench.getMaxLevel()) continue;

            int nextLevel = currentLevel + 1;
            int cost      = (ench.getMaxLevel() == 1) ? 3 : nextLevel;

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(ench, nextLevel, true);
            meta.displayName(parse(G_GOLD + toSmallCaps(ench.key().value().replace("_", " ")) + " " + nextLevel));
            meta.lore(List.of(MM.deserialize(C_YELLOW + toSmallCaps("Cost") + ": " + C_ORANGE + cost + " " + toSmallCaps("diamonds"))));
            book.setItemMeta(meta);
            inv.addItem(book);
        }
        player.openInventory(inv);
    }

    private void openDisenchanterGUI(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item!")));
            return;
        }

        Map<Enchantment, Integer> enchants = held.getType() == Material.ENCHANTED_BOOK
                ? ((EnchantmentStorageMeta) held.getItemMeta()).getStoredEnchants()
                : held.getEnchantments();

        if (enchants.isEmpty()) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item has no enchantments!")));
            return;
        }

        int size = Math.min(54, Math.max(9, ((enchants.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, parse(G_GOLD + toSmallCaps(GUI_DISENCHANT)));

        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            int refund = (e.getKey().getMaxLevel() == 1) ? 3 : e.getValue();
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(e.getKey(), e.getValue(), true);
            meta.displayName(parse(G_GOLD + toSmallCaps(e.getKey().key().value().replace("_", " ")) + " " + e.getValue()));
            meta.lore(List.of(MM.deserialize(C_GREEN + toSmallCaps("Refund") + ": " + C_ORANGE + refund + " " + toSmallCaps("diamonds"))));
            book.setItemMeta(meta);
            inv.addItem(book);
        }
        player.openInventory(inv);
    }
}
