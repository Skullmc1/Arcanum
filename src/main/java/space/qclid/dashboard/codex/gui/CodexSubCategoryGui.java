package space.qclid.dashboard.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.qclid.dashboard.codex.core.CodexCategory;
import space.qclid.dashboard.codex.core.CodexInventoryHolder;
import space.qclid.dashboard.codex.core.CodexRegistry;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexSubCategoryGui {

    public static void open(Player player, CodexRegistry registry, String parentCategoryId) {
        // Get sub-categories
        List<CodexCategory> subCategories = registry.getCategoriesForParent(parentCategoryId);

        int size = 27;
        if (subCategories.size() > 7) {
            size = 45;
        }

        CodexInventoryHolder holder = new CodexInventoryHolder(
                CodexInventoryHolder.Type.SUB_CATEGORY,
                parentCategoryId,
                null,
                0
        );

        String categoryTitleName = switch (parentCategoryId.toLowerCase()) {
            case "machinery" -> "General Machinery";
            case "arcane" -> "Arcane Codex";
            case "explorer" -> "Explorer Codex";
            default -> parentCategoryId;
        };

        String titleStr = "<dark_gray>» " + G_GOLD + toSmallCaps(categoryTitleName);
        Inventory inv = Bukkit.createInventory(holder, size, parse(titleStr));
        holder.setInventory(inv);

        // Fill background with gray glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(parse(" "));
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < size; i++) {
            inv.setItem(i, glass);
        }

        // Layout subcategories
        if (subCategories.size() <= 7) {
            int startSlot = 13 - (subCategories.size() / 2);
            for (int i = 0; i < subCategories.size(); i++) {
                int slot = startSlot + i;
                setupCategoryIcon(inv, holder, slot, subCategories.get(i));
            }
        } else {
            int index = 0;
            for (CodexCategory category : subCategories) {
                int slot = 10 + (index / 7) * 9 + (index % 7);
                if (slot >= size - 9) break; // stay above the last row
                setupCategoryIcon(inv, holder, slot, category);
                index++;
            }
        }

        // Place back button (Slot size - 5)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(parse(C_RED + toSmallCaps("Back to Main Menu")));
            back.setItemMeta(backMeta);
        }
        inv.setItem(size - 5, back);

        player.openInventory(inv);
    }

    private static void setupCategoryIcon(Inventory inv, CodexInventoryHolder holder, int slot, CodexCategory category) {
        ItemStack icon = category.getIcon();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps(category.getDisplayName())));
            meta.lore(List.of(
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Click to view recipes in")),
                    MM.deserialize(C_GRAY + toSmallCaps(category.getDisplayName() + ".")),
                    MM.deserialize(""),
                    MM.deserialize(C_GREEN + toSmallCaps("Click to open"))
            ));
            icon.setItemMeta(meta);
        }
        inv.setItem(slot, icon);
        holder.getSlotMap().put(slot, category.getId());
    }
}
