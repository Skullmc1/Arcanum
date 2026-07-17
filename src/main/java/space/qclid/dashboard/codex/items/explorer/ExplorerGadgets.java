package space.qclid.dashboard.codex.items.explorer;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static space.qclid.dashboard.util.TextUtil.*;
import static space.qclid.dashboard.util.CodexUtil.setSkullTexture;

public class ExplorerGadgets {

    private final JavaPlugin plugin;

    public ExplorerGadgets(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createThermalCanteen() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.thermal_canteen");
            NamespacedKey chargesKey = new NamespacedKey(plugin, "charges");
            meta.getPersistentDataContainer().set(chargesKey, PersistentDataType.INTEGER, 4);
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Thermal Canteen")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Flask cures effects and restores hunger.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + "4 / 4")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTeleportationPlate() {
        ItemStack item = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.navigation.teleportation_plate");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Waypoint Teleport Plate")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A ground-based teleportation structure.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click with Waypoint Compass to link.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Linked to: None"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createOreScanner() {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.exploration.ore_scanner");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Ore Scanner")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An exploration gadget detecting nearby ore veins.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to highlight nearby ores.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 30 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createMagneticRing() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.magnetic_ring");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Magnetic Ring")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A passive trinket that attracts nearby drops.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Attracts items within 5-block radius."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBottleOfLightning() {
        ItemStack item = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.bottle_of_lightning");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Bottle of Lightning")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A bottle filled with raw electrical current.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Crafted from siphoning lightning essence."))
            ));
            meta.setColor(Color.WHITE);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEnderBackpack() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/ddcc189633c789cb6d5e78d13a5043b26e7b40cdb7cfc4e23aa2279574967b4",
                "explorer.gadgets.ender_backpack",
                G_GOLD + "<bold>" + toSmallCaps("Ender Backpack"),
                List.of(
                        C_GRAY + toSmallCaps("Right-click to open your ender"),
                        C_GRAY + toSmallCaps("chest inventory remotely from anywhere."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click to open.")
                )
        );
    }

    public ItemStack createSafariLasso() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/41a129ef4ccb4a36fde3d4e8c187b5a83cc4e85aa2279574967b4a6cc286c23a",
                "explorer.gadgets.safari_lasso",
                G_GOLD + "<bold>" + toSmallCaps("Safari Lasso"),
                List.of(
                        C_GRAY + toSmallCaps("Captures passive animals for transport."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click a passive mob to capture."),
                        C_YELLOW + toSmallCaps("Right-click a block to release the mob.")
                )
        );
    }

    public ItemStack createVoidBag() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/967b4a6cc286c23ae80365778848d1d81f21cc415bf3c3c789cb6d5e78d13a504",
                "explorer.gadgets.void_bag",
                G_GOLD + "<bold>" + toSmallCaps("Void Bag"),
                List.of(
                        C_GRAY + toSmallCaps("A pocket dimension void trash bag."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click to open."),
                        C_RED + toSmallCaps("All items inside are deleted on close!")
                )
        );
    }

    public ItemStack createVoidBagRefund() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.void_bag_refund");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Void Bag Refund")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Reclaims the last items voided from")),
                    MM.deserialize(C_GRAY + toSmallCaps("your Void Bag.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to restore items.")),
                    MM.deserialize(C_RED + toSmallCaps("One-time use."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomSkull(String skinUrl, String itemId, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(displayName));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String l : loreLines) {
                lore.add(MM.deserialize(l));
            }
            meta.lore(lore);

            setSkullTexture(meta, skinUrl, plugin);

            item.setItemMeta(meta);
        }
        return item;
    }
}
