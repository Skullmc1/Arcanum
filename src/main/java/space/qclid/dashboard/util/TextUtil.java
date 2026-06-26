package space.qclid.dashboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared text utilities: MiniMessage singleton, colour constants, and toSmallCaps().
 * Import statically in feature classes: import static space.qclid.dashboard.util.TextUtil.*;
 */
public final class TextUtil {

    private TextUtil() {}

    public static final MiniMessage MM = MiniMessage.miniMessage();

    // Solid colours
    public static final String C_GOLD   = "<#FFD700>";
    public static final String C_ORANGE = "<#FFA500>";
    public static final String C_YELLOW = "<#FFFF00>";
    public static final String C_RED    = "<#FF4500>";
    public static final String C_GREEN  = "<#55FF55>";
    public static final String C_GRAY   = "<#AAAAAA>";
    public static final String C_PURPLE = "<#DA70D6>";

    // Gradients
    public static final String G_GOLD = "<gradient:#FFD700:#FFA500>";

    /** Deserializes a MiniMessage string and explicitly disables italics. */
    public static Component parse(String input) {
        if (input == null) return Component.empty();
        return MM.deserialize(input).decoration(TextDecoration.ITALIC, false);
    }

    /** Converts a string to Unicode small-caps characters. */
    public static String toSmallCaps(String input) {
        if (input == null) return "";
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small  = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ";
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = normal.indexOf(c);
            result.append(index != -1 ? small.charAt(index) : c);
        }
        return result.toString();
    }

    public static String getEffectDisplayName(String effect) {
        if (effect == null) return "";
        if (effect.equalsIgnoreCase("lifesteal")) return "Vampiric Bleed";
        String[] parts = effect.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1).toLowerCase())
              .append(" ");
        }
        return sb.toString().trim();
    }

    public static String getRomanNum(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(level);
        }
    }

    /** Wraps a Kyori Component into multiple Components of max character limit, preserving style. */
    public static List<Component> wrapComponent(Component component, int limit) {
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component).trim();
        if (plain.length() <= limit) {
            return List.of(component);
        }

        List<Component> wrapped = new ArrayList<>();
        String[] words = plain.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > limit) {
                if (currentLine.length() > 0) {
                    wrapped.add(Component.text(currentLine.toString().trim()).style(component.style()));
                    currentLine = new StringBuilder();
                }
            }
            currentLine.append(word).append(" ");
        }
        if (currentLine.length() > 0) {
            wrapped.add(Component.text(currentLine.toString().trim()).style(component.style()));
        }
        return wrapped;
    }

    /** Automatically wraps the lore of the given item to keep the tooltip width tidy. */
    public static void wrapItemLore(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) return;

        List<Component> newLore = new ArrayList<>();
        for (Component line : lore) {
            newLore.addAll(wrapComponent(line, 35));
        }
        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    /** Refreshes custom enchantment/rune lore display on an item, reflecting current display names. */
    public static final Set<String> ALL_RUNES = Set.of(
        "lifesteal", "speed", "catch_flame", "demonium", "corrosive_slash", "scorch",
        "dwarfs_blessing", "glacial_thorns", "tidal_sweep", "seismic_landing",
        "photosynthesis", "zephyr", "static_charge",
        "phantom_backstab", "telekinesis", "kinetic_rebound", "redirection",
        "bleed", "poison_spores", "gas_cloud", "heavy_draw", "timber",
        "soul_harvester", "crude_sharpness", "basalt_trail", "void_walker",
        "breach_surge", "aegis_guard", "mach_rush", "rift_walk", "daedalus_touch",
        "ouroboros", "vortex", "shrapnel_shot", "resonance_ping", "naiads_repel",
        "miasma", "fenrirs_bite", "anubis_judgment", "cerberus_maw",
        "smite_of_jupiter", "warding_halo", "radial_blind", "infernalis",
        "gloom", "styxs_toll", "trinitys_well",
        "remedium", "brimstone", "ashen_veil", "hermes_tread",
        "apollos_ray", "valkyries_grace", "hallowed_ground"
    );

    public static boolean isCustomEnchantmentLine(String plain) {
        String trimmed = plain.trim();
        if (trimmed.isEmpty()) return false;
        
        // Remove trailing Roman numeral if present (e.g. "Vampiric Bleed III" -> "Vampiric Bleed")
        String baseName = trimmed;
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace != -1) {
            String lastWord = trimmed.substring(lastSpace + 1);
            if (isRomanNumeral(lastWord)) {
                baseName = trimmed.substring(0, lastSpace).trim();
            }
        }
        
        for (String rune : ALL_RUNES) {
            String dispName = getEffectDisplayName(rune);
            String smallCapsDispName = toSmallCaps(dispName);
            if (baseName.equalsIgnoreCase(dispName) ||
                baseName.equalsIgnoreCase(smallCapsDispName) ||
                baseName.contains("(Applied)") ||
                baseName.contains("(applied)")) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isRomanNumeral(String s) {
        return s.matches("^(IX|IV|V?I{0,3}|X)$");
    }

    public static void refreshItemLore(ItemStack item, Plugin plugin) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> effects = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();

        String pluginNamespace = plugin.getName().toLowerCase();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (key.getNamespace().equals(pluginNamespace) && key.getKey().startsWith("rune_")) {
                String sub = key.getKey().substring(5); // remove "rune_"
                if (sub.equals("type") || sub.equals("effect") || sub.equals("level")) {
                    continue;
                }
                Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                if (level != null) {
                    effects.add(sub);
                    levels.add(level);
                }
            }
        }

        List<Component> lore = meta.lore();
        List<Component> newLore = new ArrayList<>();
        if (lore != null) {
            for (Component line : lore) {
                String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line).trim();
                if (plain.isEmpty()) {
                    continue;
                }
                if (!isCustomEnchantmentLine(plain)) {
                    newLore.addAll(wrapComponent(line, 35));
                }
            }
        }

        // Clean trailing empty lines
        while (!newLore.isEmpty()) {
            String lastPlain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(newLore.get(newLore.size() - 1)).trim();
            if (lastPlain.isEmpty()) {
                newLore.remove(newLore.size() - 1);
            } else {
                break;
            }
        }

        // Add a single spacing line if there is description lore above and we have effects to append
        if (!newLore.isEmpty() && !effects.isEmpty()) {
            newLore.add(Component.empty());
        }

        // Append enchantments in clean, non-italicized gray
        for (int i = 0; i < effects.size(); i++) {
            String effect = effects.get(i);
            int level = levels.get(i);
            String roman = getRomanNum(level);
            String displayName = getEffectDisplayName(effect);

            Component line = parse(C_GRAY + displayName + " " + roman);
            newLore.add(line);
        }

        List<Component> oldLore = meta.lore();
        boolean changed = true;
        if (oldLore != null && oldLore.equals(newLore)) {
            changed = false;
        } else if (oldLore == null && newLore.isEmpty()) {
            changed = false;
        }

        if (changed) {
            meta.lore(newLore);
            item.setItemMeta(meta);
        }
    }
}
