package space.qclid.dashboard.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import space.qclid.dashboard.codex.*;

import java.util.ArrayList;
import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexCategoryGui {

    private static final int ITEMS_PER_PAGE = 45;

    public static void open(Player player, CodexRegistry registry, CodexManager manager, String categoryId, int page) {
        CodexCategory category = registry.getCategory(categoryId);
        if (category == null) return;

        CodexInventoryHolder holder = new CodexInventoryHolder(
                CodexInventoryHolder.Type.CATEGORY,
                categoryId,
                category.getParentCategoryId(),
                page
        );

        String titleStr = "<dark_gray>» " + G_GOLD + toSmallCaps(category.getDisplayName());
        Inventory inv = Bukkit.createInventory(holder, 54, parse(titleStr));
        holder.setInventory(inv);

        List<CodexItem> allItems = category.getItems();
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // 1. Populate items
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < endIndex) {
                CodexItem item = allItems.get(itemIndex);
                boolean unlocked = manager.isUnlocked(player.getUniqueId(), item.getId());

                ItemStack guiItem;
                if (unlocked) {
                    guiItem = item.getDisplayItem();
                    ItemMeta meta = guiItem.getItemMeta();
                    if (meta != null) {
                        List<Component> lore = new ArrayList<>();
                        if (meta.lore() != null) {
                            lore.addAll(meta.lore());
                        } else if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            lore.add(MM.deserialize(C_GRAY + toSmallCaps(item.getDescription())));
                        }
                        lore.add(MM.deserialize(""));
                        lore.add(MM.deserialize(C_GREEN + "✔ " + toSmallCaps("Unlocked")));
                        lore.add(MM.deserialize(C_YELLOW + toSmallCaps("Click to view recipe")));
                        meta.lore(lore);
                        guiItem.setItemMeta(meta);
                    }
                } else {
                    guiItem = new ItemStack(Material.IRON_BARS);
                    ItemMeta meta = guiItem.getItemMeta();
                    if (meta != null) {
                        meta.displayName(parse(C_RED + "🔒 " + toSmallCaps(item.getDisplayName())));
                        List<Component> lore = new ArrayList<>();
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            lore.add(MM.deserialize(C_GRAY + toSmallCaps(item.getDescription())));
                            lore.add(MM.deserialize(""));
                        }
                        lore.add(MM.deserialize(C_YELLOW + toSmallCaps("Unlock Cost") + ": " + C_ORANGE + item.getXpCost() + " " + toSmallCaps("levels")));
                        
                        // Check requirements
                        if (!item.getItemRequirements().isEmpty()) {
                            lore.add(MM.deserialize(""));
                            lore.add(MM.deserialize(C_GOLD + toSmallCaps("Requirements") + ":"));
                            for (ItemStack req : item.getItemRequirements()) {
                                boolean hasReq = hasRequirement(player, req);
                                String check = hasReq ? "<green>✔</green>" : "<red>✘</red>";
                                String color = hasReq ? C_GRAY : "<red>";
                                String reqName;
                                if (req.getItemMeta() != null && req.getItemMeta().hasDisplayName()) {
                                    reqName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(req.getItemMeta().displayName());
                                } else {
                                    reqName = req.getType().name().replace("_", " ").toLowerCase();
                                }
                                lore.add(MM.deserialize("  " + check + " " + color + req.getAmount() + "x " + toSmallCaps(reqName)));
                            }
                        }
                        lore.add(MM.deserialize(""));
                        lore.add(MM.deserialize(C_GREEN + toSmallCaps("Click to unlock")));
                        meta.lore(lore);
                        guiItem.setItemMeta(meta);
                    }
                }
                inv.setItem(i, guiItem);
                holder.getSlotMap().put(i, item.getId());
            } else {
                ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta glassMeta = glass.getItemMeta();
                if (glassMeta != null) {
                    glassMeta.displayName(parse(" "));
                    glass.setItemMeta(glassMeta);
                }
                inv.setItem(i, glass);
            }
        }

        // 2. Navigation row (45 to 53)
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(parse(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // Slot 45: Back Arrow
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(parse(C_RED + toSmallCaps("Back to Categories")));
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        // Slot 51: Prev Page
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.FEATHER);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.displayName(parse(C_YELLOW + toSmallCaps("Previous Page") + " (" + page + ")"));
                prev.setItemMeta(prevMeta);
            }
            inv.setItem(51, prev);
        }

        // Slot 52: Next Page
        int totalPages = ((allItems.size() - 1) / ITEMS_PER_PAGE) + 1;
        if (page + 1 < totalPages) {
            ItemStack next = new ItemStack(Material.FEATHER);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.displayName(parse(C_YELLOW + toSmallCaps("Next Page") + " (" + (page + 2) + ")"));
                next.setItemMeta(nextMeta);
            }
            inv.setItem(52, next);
        }

        player.openInventory(inv);
    }

    private static boolean hasRequirement(Player player, ItemStack req) {
        if (req == null) return true;
        ItemMeta reqMeta = req.getItemMeta();
        String reqId = null;
        if (reqMeta != null) {
            NamespacedKey key = new NamespacedKey(org.bukkit.plugin.java.JavaPlugin.getPlugin(space.qclid.dashboard.DashboardPlugin.class), "item_id");
            reqId = reqMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }

        if (reqId == null) {
            return player.getInventory().containsAtLeast(req, req.getAmount());
        }

        int found = 0;
        NamespacedKey key = new NamespacedKey(org.bukkit.plugin.java.JavaPlugin.getPlugin(space.qclid.dashboard.DashboardPlugin.class), "item_id");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == req.getType()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (reqId.equals(id)) {
                        found += item.getAmount();
                    }
                }
            }
        }
        return found >= req.getAmount();
    }
}
