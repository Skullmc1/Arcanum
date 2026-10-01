package space.qclid.arcanum.codex.items.arcane;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.arcanum.util.TextUtil.*;

public class ArcaneWeapons {

    private final JavaPlugin plugin;

    public ArcaneWeapons(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createWandOfEmbers() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.wand_of_embers");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Embers")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A magical wand crafted from nether embers.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to cast fireballs."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public ItemStack createWandOfLevitation() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.wand_of_levitation");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Levitation")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Shoots a bolt rendering targets weightless.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to shoot levitation bolt.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 15 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStaffOfSupplant() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.staff_of_supplant");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Staff of Supplant")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Shoots a projectile that swaps your")),
                    MM.deserialize(C_GRAY + toSmallCaps("position with the target entity."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStaffOfTheStormlord() {
        ItemStack item = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.staff_of_the_stormlord");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Staff of the Stormlord")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Calls down a lightning bolt on")),
                    MM.deserialize(C_GRAY + toSmallCaps("the block you are looking at.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to strike lightning.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 20 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSiphonBlade() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.melee.siphon_blade");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Siphon Blade")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A cursed blade that feeds on life force.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Restores 15% max health on kill."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createCorrosiveScythe() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.melee.venomous_scythe");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Corrosive Scythe")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Degrades enemy armor on each hit.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Reduces their protection over time."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWandOfTransmutation() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.wand_of_transmutation");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Transmutation")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to temporarily turn hostiles")),
                    MM.deserialize(C_GRAY + toSmallCaps("into passive animals for 15s."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
