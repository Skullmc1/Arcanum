package space.qclid.dashboard.codex.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.qclid.dashboard.codex.CodexInventoryHolder;
import space.qclid.dashboard.codex.CodexRegistry;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexMainGui {

    public static void open(Player player, CodexRegistry registry) {
        CodexInventoryHolder holder = new CodexInventoryHolder(
                CodexInventoryHolder.Type.MAIN,
                null,
                null,
                0
        );

        Inventory inv = Bukkit.createInventory(holder, 27, parse("<dark_gray>» " + G_GOLD + toSmallCaps("The Codex")));
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

        // Place Main Category Icons: Machinery (10), Arcane (13), Exploration (16)
        
        // 1. General Machinery
        ItemStack machineryIcon = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta machineryMeta = machineryIcon.getItemMeta();
        if (machineryMeta != null) {
            machineryMeta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("General Machinery")));
            machineryMeta.lore(List.of(
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Unlock placed machines and tools")),
                    MM.deserialize(C_GRAY + toSmallCaps("used to craft advanced items.")),
                    MM.deserialize(""),
                    MM.deserialize(C_GREEN + toSmallCaps("Click to open"))
            ));
            machineryIcon.setItemMeta(machineryMeta);
        }
        inv.setItem(10, machineryIcon);
        holder.getSlotMap().put(10, "machinery");

        // 2. Arcane Category
        ItemStack arcaneIcon = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta arcaneMeta = arcaneIcon.getItemMeta();
        if (arcaneMeta != null) {
            arcaneMeta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Arcane Codex")));
            arcaneMeta.lore(List.of(
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Unlock mystical runes, wands,")),
                    MM.deserialize(C_GRAY + toSmallCaps("and enchanted equipment.")),
                    MM.deserialize(""),
                    MM.deserialize(C_GREEN + toSmallCaps("Click to open"))
            ));
            arcaneIcon.setItemMeta(arcaneMeta);
        }
        inv.setItem(13, arcaneIcon);
        holder.getSlotMap().put(13, "arcane");

        // 3. Exploration Category
        ItemStack explorerIcon = new ItemStack(Material.COMPASS);
        ItemMeta explorerMeta = explorerIcon.getItemMeta();
        if (explorerMeta != null) {
            explorerMeta.displayName(parse(C_GREEN + "<bold>" + toSmallCaps("Explorer Codex")));
            explorerMeta.lore(List.of(
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Unlock traversal gadgets,")),
                    MM.deserialize(C_GRAY + toSmallCaps("navigation tools, and structure guides.")),
                    MM.deserialize(""),
                    MM.deserialize(C_GREEN + toSmallCaps("Click to open"))
            ));
            explorerIcon.setItemMeta(explorerMeta);
        }
        inv.setItem(16, explorerIcon);
        holder.getSlotMap().put(16, "explorer");

        player.openInventory(inv);
    }
}
