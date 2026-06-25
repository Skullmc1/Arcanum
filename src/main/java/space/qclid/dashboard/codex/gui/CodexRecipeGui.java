package space.qclid.dashboard.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import space.qclid.dashboard.codex.CodexInventoryHolder;
import space.qclid.dashboard.codex.CodexItem;
import space.qclid.dashboard.codex.CodexRegistry;

import java.util.ArrayList;
import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexRecipeGui {

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
                stationMeta.lore(List.of(
                        MM.deserialize(C_YELLOW + "<bold>" + toSmallCaps("Required Structure"))
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
