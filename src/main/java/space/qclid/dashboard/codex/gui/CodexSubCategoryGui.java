package space.qclid.dashboard.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.qclid.dashboard.codex.CodexCategory;
import space.qclid.dashboard.codex.CodexInventoryHolder;
import space.qclid.dashboard.codex.CodexRegistry;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexSubCategoryGui {

    public static void open(Player player, CodexRegistry registry, String parentCategoryId) {
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
        Inventory inv = Bukkit.createInventory(holder, 27, parse(titleStr));
        holder.setInventory(inv);

        // Fill background with gray glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(parse(" "));
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Get sub-categories
        List<CodexCategory> subCategories = registry.getCategoriesForParent(parentCategoryId);

        // Place them in the middle row (slots 10 to 16)
        int startSlot = 13 - (subCategories.size() / 2);
        if (startSlot < 9) startSlot = 9;

        for (int i = 0; i < subCategories.size(); i++) {
            int slot = startSlot + i;
            if (slot > 17) break; // stay in the middle row

            CodexCategory category = subCategories.get(i);
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

        // Place back button (Slot 22)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(parse(C_RED + toSmallCaps("Back to Main Menu")));
            back.setItemMeta(backMeta);
        }
        inv.setItem(22, back);

        player.openInventory(inv);
    }
}
