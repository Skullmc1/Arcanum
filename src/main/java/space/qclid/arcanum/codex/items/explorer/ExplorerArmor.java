package space.qclid.arcanum.codex.items.explorer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.arcanum.util.TextUtil.*;

public class ExplorerArmor {

    private final JavaPlugin plugin;

    public ExplorerArmor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSpelunkersHelmet() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.armor.spelunkers_helmet");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Spelunker's Helmet")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants permanent Night Vision.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Highlights nearby hostile mobs."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createDepthStriderFlippers() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.depth_strider_flippers");

            if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta lam) {
                lam.setColor(Color.BLUE);
            }

            meta.displayName(parse("<#55FFFF><bold>" + toSmallCaps("Depth Strider Flippers")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Gives swim speed & water breathing.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Applies heavy slowness on land."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSlimeBoots() {
        ItemStack item = new ItemStack(Material.SLIME_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.armor.slime_boots");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Slime Boots")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Bouncy footgear designed to bypass gravity's limits.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Negates all fall damage and bounces wearer."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSteamJetpack() {
        ItemStack item = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.armor.steam_jetpack");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Steam Jetpack")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable flight thruster chestplate.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Hold Shift in mid-air to fly.")),
                    MM.deserialize(C_RED + toSmallCaps("Consumes Coal/Charcoal/Hardened Coal from inventory."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
