package space.qclid.arcanum.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import space.qclid.arcanum.codex.core.CodexInventoryHolder;
import space.qclid.arcanum.codex.core.CodexItem;
import space.qclid.arcanum.codex.core.CodexRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static space.qclid.arcanum.util.TextUtil.*;

public class CodexRecipeGui {

    private static final java.util.Map<String, String> STATION_DESCRIPTIONS = java.util.Map.ofEntries(
        java.util.Map.entry("machinery.arcana_table",       "Crafting Table above Dropper\nwith 2+ adjacent Bookshelves"),
        java.util.Map.entry("machinery.heavy_forge",         "Crafting Table above Blast\nFurnace above Dropper"),
        java.util.Map.entry("machinery.upgrade_table",       "Anvil above Dropper above\nBookshelf"),
        java.util.Map.entry("machinery.blood_altar",         "Red Carpet above Dropper\nabove Obsidian"),
        java.util.Map.entry("machinery.blessings_altar",     "Dropper beside 3x3 Gold Block\nplatform edged with Quartz"),
        java.util.Map.entry("machinery.heavy_alloy_forge",   "Blast Furnace above Dropper\nabove 3x3 Magma Blocks"),
        java.util.Map.entry("machinery.block_duplicator",    "Nether Brick Fence above Dropper\nsurrounded by Furnaces"),
        java.util.Map.entry("machinery.auto_sifter",         "Hopper above Dropper adjacent\nto a Cauldron"),
        java.util.Map.entry("machinery.auto_smelter",        "Hopper above Dropper adjacent\nto a Furnace"),
        java.util.Map.entry("machinery.enchanter",           "Enchanting Table above 3x3\nDiamond Block base"),
        java.util.Map.entry("machinery.disenchanter",        "Enchanting Table above 3x3\nIron Block base"),
        java.util.Map.entry("machinery.kinetic_crusher",     "Piston above Hopper above\n3x3 Iron Block base"),
        java.util.Map.entry("machinery.sifting_trommel",     "Iron Bars with 4 Copper Blocks\nabove a Hopper")
    );

    private static String getStationDescription(ItemStack station) {
        if (station == null || station.getType() == Material.AIR) return null;
        ItemMeta meta = station.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return null;
        return STATION_DESCRIPTIONS.get(id);
    }

    public static void open(Player player, CodexRegistry registry, String itemId, String parentCategoryId, int parentPage) {
        CodexItem item = registry.getItem(itemId);
        if (item == null) return;

        CodexInventoryHolder holder = new CodexInventoryHolder(
                CodexInventoryHolder.Type.RECIPE,
                itemId,
                parentCategoryId,
                parentPage
        );

        String titleStr = "<dark_gray>» " + G_GOLD + toSmallCaps("Recipe: " + item.getDisplayName());
        Inventory inv = Bukkit.createInventory(holder, 54, parse(titleStr));
        holder.setInventory(inv);

        // Fill background with gray glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(parse(" "));
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }

        // Place Crafting Station (Slot 10)
        ItemStack station;
        if (item.getId().startsWith("machinery.")) {
            station = new ItemStack(Material.BRICKS);
            ItemMeta stationMeta = station.getItemMeta();
            if (stationMeta != null) {
                stationMeta.displayName(parse(C_GOLD + toSmallCaps("Multiblock")));
                List<Component> lore = new ArrayList<>();
                lore.add(MM.deserialize(C_YELLOW + "<bold>" + toSmallCaps("Required Structure")));
                String desc = STATION_DESCRIPTIONS.get(item.getId());
                if (desc != null) {
                    for (String line : desc.split("\n")) {
                        lore.add(MM.deserialize(C_GRAY + toSmallCaps(line)));
                    }
                }
                stationMeta.lore(lore);
                station.setItemMeta(stationMeta);
            }
        } else if (item.getCraftingStation() != null && (item.getCraftingStation().getType() == Material.ZOMBIE_HEAD || item.getCraftingStation().getType() == Material.CREEPER_HEAD)) {
            station = new ItemStack(item.getCraftingStation().getType());
            ItemMeta stationMeta = station.getItemMeta();
            if (stationMeta != null) {
                stationMeta.displayName(parse(C_GOLD + toSmallCaps("Mob Drop")));
                String lore1 = item.getCraftingStation().getType() == Material.CREEPER_HEAD
                    ? toSmallCaps("Rare drop from Creepers struck")
                    : toSmallCaps("Requires killing a mob");
                String lore2 = item.getCraftingStation().getType() == Material.CREEPER_HEAD
                    ? toSmallCaps("by lightning.")
                    : toSmallCaps("with lightning.");
                stationMeta.lore(List.of(
                        MM.deserialize(C_YELLOW + "<bold>" + lore1),
                        MM.deserialize(C_YELLOW + "<bold>" + lore2)
                ));
                station.setItemMeta(stationMeta);
            }
        } else {
            station = item.getCraftingStation();
            if (station == null) {
                station = new ItemStack(Material.CRAFTING_TABLE);
            } else {
                station = station.clone();
            }
            ItemMeta stationMeta = station.getItemMeta();
            if (stationMeta != null) {
                // Keep the custom name if set, else use default Crafting Station
                if (!stationMeta.hasDisplayName()) {
                    stationMeta.displayName(parse(C_GOLD + toSmallCaps("Crafting Station")));
                }
                List<Component> lore = new ArrayList<>();
                lore.add(MM.deserialize(C_YELLOW + "<bold>" + toSmallCaps("Required Station")));
                // Add structure description if available
                String desc = getStationDescription(station);
                if (desc != null) {
                    for (String line : desc.split("\n")) {
                        lore.add(MM.deserialize(C_GRAY + toSmallCaps(line)));
                    }
                    lore.add(Component.empty());
                }
                if (stationMeta.lore() != null) {
                    lore.addAll(stationMeta.lore());
                }
                stationMeta.lore(lore);
                station.setItemMeta(stationMeta);
            }
        }
        inv.setItem(10, station);

        // Place Recipe 3x3 Grid
        ItemStack[] recipe = item.getRecipe();
        int[] invSlots = {
                14, 15, 16,
                23, 24, 25,
                32, 33, 34
        };

        for (int i = 0; i < 9; i++) {
            int slot = invSlots[i];
            ItemStack ingredient = recipe[i];
            if (ingredient != null) {
                inv.setItem(slot, ingredient.clone());
            } else {
                ItemStack emptySlot = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta emptyMeta = emptySlot.getItemMeta();
                if (emptyMeta != null) {
                    emptyMeta.displayName(parse(C_GRAY + toSmallCaps("Empty Slot")));
                    emptySlot.setItemMeta(emptyMeta);
                }
                inv.setItem(slot, emptySlot);
            }
        }

        // Place back button (Slot 45)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(parse(C_RED + toSmallCaps("Back to Category")));
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        player.openInventory(inv);
    }
}
