package space.qclid.dashboard.codex.items.arcane;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.dashboard.util.TextUtil.*;

public class ArcaneMachinery {

    private final JavaPlugin plugin;

    public ArcaneMachinery(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createArcanaTableItem() {
        ItemStack table = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.arcana_table");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Arcana Table")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A placed Crafting Table with a Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("below and bookshelves on the sides."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            table.setItemMeta(meta);
        }
        return table;
    }

    public ItemStack createUpgradeTableItem() {
        ItemStack table = new ItemStack(Material.ANVIL);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.upgrade_table");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Upgrade Table")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An Anvil on top of a Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("on top of a Bookshelf."))
            ));
            table.setItemMeta(meta);
        }
        return table;
    }

    public ItemStack createBloodAltarItem() {
        ItemStack table = new ItemStack(Material.RED_CARPET);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.blood_altar");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Blood Altar")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Structure: Red Carpet on top of")),
                    MM.deserialize(C_GRAY + toSmallCaps("a Dropper on top of Obsidian.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Used for demonic enchantments."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            table.setItemMeta(meta);
        }
        return table;
    }

    public ItemStack createDynamicCustomItem(String id) {
        if (id == null) return null;
        
        Material material;
        String name;
        String loreDesc;
        
        if (id.equals("machinery.blessings_altar")) {
            material = Material.GOLD_BLOCK;
            name = "Blessings Altar";
            loreDesc = "A 3x3 Gold Block platform surrounded by a ring of Quartz or Endstone, with a Dropper and Fence.";
        } else if (id.equals("machinery.heavy_alloy_forge")) {
            material = Material.BLAST_FURNACE;
            name = "Heavy Alloy Forge";
            loreDesc = "Structure: Blast Furnace on top of a Dropper on top of a 3x3 Magma Block base.";
        } else if (id.equals("machinery.enchanter")) {
            material = Material.ENCHANTING_TABLE;
            name = "Enchanter";
            loreDesc = "Structure: Enchanting Table on top of a 3x3 grid of Diamond Blocks.";
        } else if (id.equals("machinery.disenchanter")) {
            material = Material.ENCHANTING_TABLE;
            name = "Disenchanter";
            loreDesc = "Structure: Enchanting Table on top of a 3x3 grid of Iron Blocks.";
        } else if (id.equals("machinery.kinetic_crusher")) {
            material = Material.PISTON;
            name = "Kinetic Crusher";
            loreDesc = "Structure: Piston on top of Hopper on top of a 3x3 grid of Iron Blocks.";
        } else if (id.equals("machinery.sifting_trommel")) {
            material = Material.IRON_BARS;
            name = "Sifting Trommel";
            loreDesc = "Structure: Iron Bars surrounded by 4 Copper Blocks on top of a Hopper.";
        } else {
            return null;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps(name)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps(loreDesc))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
