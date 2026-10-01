package space.qclid.arcanum.codex.items.arcane;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.arcanum.util.TextUtil.*;

public class ArcaneTrinkets {

    private final JavaPlugin plugin;

    public ArcaneTrinkets(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createAmuletOfThePhoenix() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.amulet_of_the_phoenix");
            NamespacedKey usesKey = space.qclid.arcanum.compat.Compat.key("uses");
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, 3);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Amulet of the Phoenix")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants totem recovery and fiery explosion knockback.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Uses remaining") + ": " + C_ORANGE + "3")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTotemOfFallacy() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.totem_of_fallacy");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Totem of Fallacy")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to teleport to spawn.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Reduces all inventory items' durability to 10%."))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createFrostbiteRing() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.frostbite_ring");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Frostbite Ring")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Freezes water under your feet.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Extinguishes target entities on hit."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createVitalityGeode(int tier) {
        ItemStack item = new ItemStack(Material.AMETHYST_CLUSTER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.vitality_geode_" + tier);

            String nameStr = tier == 3 ? "III" : (tier == 2 ? "II" : "I");
            int heartsStr = tier == 3 ? 10 : (tier == 2 ? 5 : 2);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Vitality Geode " + nameStr)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Increases max health by +" + (heartsStr * 2) + " (" + heartsStr + " hearts)")),
                    MM.deserialize(C_GRAY + toSmallCaps("while sitting in your inventory."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createImmolationTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.immolation_totem");
            meta.displayName(parse("<#FF3300><bold>" + toSmallCaps("Immolation Totem")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A glowing totem forged in pure lava.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Reagent for fire enchantments."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            totem.setItemMeta(meta);
        }
        return totem;
    }

    public ItemStack createStormcallerMedallion() {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.stormcaller_medallion");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Stormcaller Medallion")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants 15% lightning strike chance")),
                    MM.deserialize(C_GRAY + toSmallCaps("on hit during rain or storms."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
