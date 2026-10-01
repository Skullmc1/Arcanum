package space.qclid.arcanum.skills.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillCurve;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/** The /skills screen: one icon per enabled skill with level, progress bar and next perk. */
public final class SkillsGui {

    private static final int SIZE = 36;
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int BAR_WIDTH = 20;

    private SkillsGui() {}

    public static void open(Player player, SkillManager manager, List<Skill> skills) {
        SkillsInventoryHolder holder = new SkillsInventoryHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, parse(C_GOLD + "<bold>" + toSmallCaps("Skills")));
        holder.setInventory(inv);

        int slot = 0;
        for (Skill skill : skills) {
            if (!manager.isEnabled(skill.type()) || slot >= SLOTS.length) continue;
            inv.setItem(SLOTS[slot++], icon(player.getUniqueId(), skill, manager));
        }
        player.openInventory(inv);
    }

    private static ItemStack icon(UUID id, Skill skill, SkillManager manager) {
        SkillType type = skill.type();
        SkillCurve curve = manager.curve();
        long xp = manager.xp(id, type);
        int level = curve.levelForXp(xp);

        Material material = Material.matchMaterial(type.icon());
        ItemStack item = new ItemStack(material != null ? material : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps(type.displayName())));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<Component> lore = new ArrayList<>();
        lore.add(parse(C_YELLOW + toSmallCaps("Level " + level + " / " + curve.maxLevel())));
        lore.add(parse(bar(curve.progress(xp))));
        lore.add(parse(level >= curve.maxLevel()
            ? C_GREEN + toSmallCaps("Max level")
            : C_GRAY + curve.xpIntoLevel(xp) + " / " + curve.xpToNext(level) + " xp"));
        lore.add(Component.empty());
        String next = skill.nextPerk(level);
        lore.add(parse(next == null
            ? C_GREEN + toSmallCaps("All perks unlocked")
            : C_PURPLE + toSmallCaps("Next: ") + C_GRAY + next));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String bar(double progress) {
        int filled = (int) Math.round(progress * BAR_WIDTH);
        StringBuilder sb = new StringBuilder(C_GREEN);
        for (int i = 0; i < BAR_WIDTH; i++) {
            if (i == filled) sb.append(C_GRAY);
            sb.append(i < filled ? "█" : "░");
        }
        return sb.toString();
    }
}
