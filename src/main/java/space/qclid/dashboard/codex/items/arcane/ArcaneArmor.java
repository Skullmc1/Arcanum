package space.qclid.dashboard.codex.items.arcane;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.dashboard.util.TextUtil.*;

public class ArcaneArmor {

    private final JavaPlugin plugin;

    public ArcaneArmor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSpeedBoots() {
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta meta = boots.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.speed_boots");
            NamespacedKey speedKey = new NamespacedKey(plugin, "rune_speed");
            meta.getPersistentDataContainer().set(speedKey, PersistentDataType.INTEGER, 1);

            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Speed Boots")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Diamond boots infused with swiftness.")),
                    MM.deserialize(""),
                    MM.deserialize(C_PURPLE + toSmallCaps("Speed I") + " " + C_GRAY + toSmallCaps("(Applied)"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            boots.setItemMeta(meta);
        }
        return boots;
    }

    public ItemStack createShadowCloak() {
        ItemStack item = new ItemStack(Material.BLACK_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.shadow_cloak");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Shadow Cloak")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Invisibility + Speed I when light level < 7.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable on head."))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSuperiorShadowCloak() {
        ItemStack item = new ItemStack(Material.BLACK_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.superior_shadow_cloak");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Superior Shadow Cloak")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Invisibility + Speed II + Swift Sneak while crouched.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable on head."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createGaleChestplate() {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.gale_chestplate");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Gale Chestplate")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Mystical infused armor allowing weightless flight.")),
                    MM.deserialize(""),
                    MM.deserialize(C_PURPLE + toSmallCaps("Slow Falling I") + " " + C_GRAY + toSmallCaps("(Applied)")),
                    MM.deserialize(C_YELLOW + toSmallCaps("Double Jump") + " " + C_GRAY + toSmallCaps("(10s Cooldown)"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
