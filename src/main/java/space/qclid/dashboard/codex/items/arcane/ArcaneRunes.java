package space.qclid.dashboard.codex.items.arcane;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import static space.qclid.dashboard.util.TextUtil.*;

public class ArcaneRunes {

    private final JavaPlugin plugin;

    public ArcaneRunes(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createLifestealRune() {
        return createLifestealRuneOfLevel(1);
    }

    public ItemStack createLifestealRuneOfLevel(int level) {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
        if (meta != null) {
            String id = "arcane.lifesteal_rune" + (level == 1 ? "" : "_" + level);
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "WEAPON_SWORD");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "lifesteal");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, level);

            String roman = level == 1 ? "I" : (level == 2 ? "II" : "III");
            meta.displayName(parse(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + C_RED + toSmallCaps("Vampiric Bleed " + roman) + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Weapon-Sword")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("sword in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Vampiric Bleed " + roman) + ": " + C_GRAY + toSmallCaps("Heals on attack."))
            ));

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
            if (level == 1) {
                effectBuilder.withColor(Color.fromRGB(220, 20, 60))
                        .with(FireworkEffect.Type.BALL);
            } else if (level == 2) {
                effectBuilder.withColor(Color.fromRGB(139, 0, 0))
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true);
            } else {
                effectBuilder.withColor(Color.fromRGB(139, 0, 0))
                        .withFade(Color.fromRGB(30, 30, 30))
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true)
                        .flicker(true);
            }
            meta.setEffect(effectBuilder.build());

            star.setItemMeta(meta);
        }
        return star;
    }

    public ItemStack createSpeedRune() {
        return createSpeedRuneOfLevel(1);
    }

    public ItemStack createSpeedRuneOfLevel(int level) {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
        if (meta != null) {
            String id = "arcane.speed_rune" + (level == 1 ? "" : "_" + level);
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "ARMOR_BOOTS");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "speed");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, level);

            String roman = level == 1 ? "I" : (level == 2 ? "II" : "III");
            meta.displayName(parse(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + "<#55FFFF>" + toSmallCaps("Speed " + roman) + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Armor-Boots")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("boots in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Speed " + roman) + ": " + C_GRAY + toSmallCaps("Grants Speed " + roman + " when worn."))
            ));

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
            if (level == 1) {
                effectBuilder.withColor(Color.fromRGB(0, 255, 255))
                        .with(FireworkEffect.Type.BALL);
            } else if (level == 2) {
                effectBuilder.withColor(Color.fromRGB(0, 139, 139))
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true);
            } else {
                effectBuilder.withColor(Color.fromRGB(0, 255, 255))
                        .withFade(Color.fromRGB(255, 255, 255))
                        .with(FireworkEffect.Type.STAR)
                        .trail(true)
                        .flicker(true);
            }
            meta.setEffect(effectBuilder.build());

            star.setItemMeta(meta);
        }
        return star;
    }

    public ItemStack createCatchFlameRune(int level) {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
        if (meta != null) {
            String id = "arcane.runes.catch_flame" + (level == 1 ? "" : "_" + level);
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "WEAPON_BOW_SWORD");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "catch_flame");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, level);

            String roman = level == 1 ? "I" : (level == 2 ? "II" : "III");
            meta.displayName(parse(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + "<#FFAA00>" + toSmallCaps("Catch Flame " + roman) + C_GRAY + ")"));

            String chanceStr = level == 1 ? "33%" : (level == 2 ? "66%" : "100%");
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Weapon (Sword/Bow)")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("weapon in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Catch Flame " + roman) + ": " + C_GRAY + toSmallCaps("Applies Fire Aspect II / Flame I.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Spreads fire to nearby mobs (" + chanceStr + " chance)."))
            ));

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
            if (level == 1) {
                effectBuilder.withColor(Color.fromRGB(255, 140, 0))
                        .with(FireworkEffect.Type.BALL);
            } else if (level == 2) {
                effectBuilder.withColor(Color.fromRGB(255, 69, 0))
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true);
            } else {
                effectBuilder.withColor(Color.fromRGB(255, 215, 0))
                        .withFade(Color.fromRGB(255, 0, 0))
                        .with(FireworkEffect.Type.STAR)
                        .trail(true)
                        .flicker(true);
            }
            meta.setEffect(effectBuilder.build());

            star.setItemMeta(meta);
        }
        return star;
    }

    public ItemStack createDemoniumRune(int level) {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            NamespacedKey levelKey = new NamespacedKey(plugin, "rune_level");

            String suffix = level == 3 ? "III" : (level == 2 ? "II" : "I");
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, "arcane.runes.demonium" + (level > 1 ? "_" + level : ""));
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "ARMOR");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "demonium");
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);

            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Enchantment Rune (Demonium " + suffix + ")")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Apply to helmet. Sets fire to players,")),
                    MM.deserialize(C_GRAY + toSmallCaps("animals, and mobs in a 2-block radius.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Hold in main hand and swap with helmet in off-hand.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Fire duration") + ": " + C_ORANGE + (level == 1 ? "2s" : (level == 2 ? "5s" : "10s")))
            ));

            Color main = Color.fromRGB(60, 60, 60);
            FireworkEffect.Type type = FireworkEffect.Type.BALL;
            boolean trail = false;
            boolean flicker = false;
            Color fade = null;

            if (level == 2) {
                main = Color.fromRGB(30, 30, 30);
                type = FireworkEffect.Type.BALL_LARGE;
                trail = true;
            } else if (level == 3) {
                main = Color.fromRGB(0, 0, 0);
                type = FireworkEffect.Type.STAR;
                trail = true;
                flicker = true;
                fade = Color.fromRGB(139, 0, 0);
            }

            FireworkEffect.Builder builder = FireworkEffect.builder()
                    .withColor(main)
                    .with(type)
                    .trail(trail)
                    .flicker(flicker);
            if (fade != null) builder.withFade(fade);

            meta.setEffect(builder.build());
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEnchantmentRune(String baseId, String effect, int level) {
        if (effect.equalsIgnoreCase("lifesteal")) {
            return createLifestealRuneOfLevel(level);
        } else if (effect.equalsIgnoreCase("speed")) {
            return createSpeedRuneOfLevel(level);
        } else if (effect.equalsIgnoreCase("catch_flame")) {
            return createCatchFlameRune(level);
        } else if (effect.equalsIgnoreCase("demonium")) {
            return createDemoniumRune(level);
        }

        String displayName = getRuneDisplayName(effect);
        String type = getRuneType(effect);
        String desc = getRuneDescription(effect);
        Color color = getRuneColor(effect);
        FireworkEffect.Type starType = getRuneStarType(effect);

        if (displayName != null) {
            String fullId = baseId;
            if (level > 1) {
                if (fullId.matches(".+_\\d+")) {
                    fullId = fullId.substring(0, fullId.lastIndexOf('_'));
                }
                fullId = fullId + "_" + level;
            }
            return createCustomRune(fullId, displayName, effect, type, level, desc, color, starType, level >= 2, level >= 3);
        }

        return null;
    }

    public ItemStack createCustomRune(String id, String displayName, String effect, String type, int level, String desc, Color color, FireworkEffect.Type starType, boolean trail, boolean flicker) {
        Material mat = getRuneMaterial(effect);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type);
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, effect);
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, level);

            String roman = level == 1 && desc.toLowerCase().contains("no levels") ? "" : " " + getRoman(level);
            meta.displayName(parse(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + colorToMiniMessage(color) + toSmallCaps(displayName + roman) + C_GRAY + ")"));

            String loreType = formatTypeForLore(type);

            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps(loreType)),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps(loreType.toLowerCase() + " in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps(displayName + roman) + ": " + C_GRAY + toSmallCaps(desc))
            ));

            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }
        return item;
    }

    private String getRoman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            default: return String.valueOf(level);
        }
    }

    private String colorToMiniMessage(Color color) {
        return String.format("<#%02X%02X%02X>", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String formatTypeForLore(String type) {
        if (type.equalsIgnoreCase("WEAPON_SWORD")) return "Weapon-Sword";
        if (type.equalsIgnoreCase("WEAPON_AXE")) return "Weapon-Axe";
        if (type.equalsIgnoreCase("WEAPON_SWORD_AXE")) return "Weapon-Sword/Axe";
        if (type.equalsIgnoreCase("ARMOR_BOOTS")) return "Armor-Boots";
        if (type.equalsIgnoreCase("ARMOR_CHESTPLATE")) return "Armor-Chestplate";
        if (type.equalsIgnoreCase("ARMOR")) return "Armor";
        if (type.equalsIgnoreCase("TOOLS")) return "Tools";
        if (type.equalsIgnoreCase("TOOLS_HOE")) return "Tools/Hoe";
        if (type.equalsIgnoreCase("TOOL_HOE")) return "Tool-Hoe";
        if (type.equalsIgnoreCase("WEAPON_BOW")) return "Weapon-Bow/Crossbow";
        if (type.equalsIgnoreCase("WEAPON_BOW_ONLY")) return "Weapon-Bow";
        if (type.equalsIgnoreCase("SHIELD")) return "Shield";
        if (type.equalsIgnoreCase("DURABILITY")) return "Durability Gear";
        if (type.equalsIgnoreCase("MELEE_OR_STICK")) return "Melee Weapon/Stick";
        return type;
    }

    public String getRuneDisplayName(String effect) {
        switch (effect.toLowerCase()) {
            case "corrosive_slash": return "Corrosive Slash";
            case "scorch": return "Scorch";
            case "dwarfs_blessing": return "Dwarf's Blessing";
            case "glacial_thorns": return "Glacial Thorns";
            case "tidal_sweep": return "Tidal Sweep";
            case "seismic_landing": return "Seismic Landing";
            case "photosynthesis": return "Photosynthesis";
            case "zephyr": return "Zephyr";
            case "static_charge": return "Static Charge";
            case "phantom_backstab": return "Phantom Backstab";
            case "telekinesis": return "Telekinesis";
            case "kinetic_rebound": return "Kinetic Rebound";
            case "redirection": return "Redirection";
            case "bleed": return "Bleed";
            case "poison_spores": return "Poison Spores";
            case "gas_cloud": return "Gas Cloud";
            case "heavy_draw": return "Heavy Draw";
            case "timber": return "Timber";
            case "soul_harvester": return "Soul Harvester";
            case "crude_sharpness": return "Crude Sharpness";
            case "basalt_trail": return "Basalt Trail";
            case "void_walker": return "Void Walker";
            default: return null;
        }
    }

    public String getRuneType(String effect) {
        switch (effect.toLowerCase()) {
            case "corrosive_slash":
            case "timber":
                return "WEAPON_AXE";
            case "scorch":
            case "tidal_sweep":
            case "phantom_backstab":
                return "WEAPON_SWORD";
            case "dwarfs_blessing":
                return "TOOLS";
            case "glacial_thorns":
            case "redirection":
            case "gas_cloud":
                return "ARMOR";
            case "seismic_landing":
            case "basalt_trail":
            case "void_walker":
                return "ARMOR_BOOTS";
            case "photosynthesis":
                return "DURABILITY";
            case "zephyr":
                return "WEAPON_BOW";
            case "heavy_draw":
                return "WEAPON_BOW_ONLY";
            case "static_charge":
                return "ARMOR_CHESTPLATE";
            case "telekinesis":
                return "TOOLS_HOE";
            case "kinetic_rebound":
                return "SHIELD";
            case "bleed":
            case "poison_spores":
            case "crude_sharpness":
                return "WEAPON_SWORD_AXE";
            case "soul_harvester":
                return "TOOL_HOE";
            default: return "WEAPON";
        }
    }

    public String getRuneDescription(String effect) {
        switch (effect.toLowerCase()) {
            case "corrosive_slash": return "Reduces enemy armor toughness rating on axe hits. Level scales duration up to 10s at Level 4 (5 max stacks).";
            case "scorch": return "Reduces enemy armor on sword hits (stacks up to 10, max 90%). Deals 1 tick of fire damage on hit. No levels.";
            case "dwarfs_blessing": return "Automatically smelts all mined blocks. Breaking logs drops Charcoal instead of wood.";
            case "glacial_thorns": return "Enemies hitting you have a chance to be afflicted with Weakness. Level 5 guarantees Weakness II.";
            case "tidal_sweep": return "Unleashes a larger, longer-reaching sweep with a narrower angle. Incompatible with Sweeping Edge.";
            case "seismic_landing": return "Negates up to 5 hearts of fall damage, releasing a massive damage/launch shockwave. Incompatible with Feather Falling.";
            case "photosynthesis": return "Slowly restores item durability while standing on dirt/grass under direct sunlight. No levels.";
            case "zephyr": return "Gravity has no effect on fired arrows. No levels.";
            case "static_charge": return "Generates charge while moving. Fully charged adds spark and next hit deals +25% bonus damage.";
            case "phantom_backstab": return "Melee hits have 50% chance to strike target with spectral sword behind them after 1s.";
            case "telekinesis": return "Teleports mined blocks and experience orbs directly to inventory. No levels.";
            case "kinetic_rebound": return "Blocking projectiles has a high chance to reflect them back to source. No levels.";
            case "redirection": return "Redirects a percentage of incoming damage (up to 40% at level 6) to nearby pets.";
            case "bleed": return "Applies ticking bleeding stacks on melee hit that continuously drain target health.";
            case "poison_spores": return "Applies standard Poison effect on hit. Scales duration and proc chance.";
            case "gas_cloud": return "Dealing/taking damage has a chance to release decay toxic gas cloud on block.";
            case "heavy_draw": return "Increases arrow draw time by 50% but raises arrow damage by up to +50%.";
            case "timber": return "Instantly chops entire trees of connected logs on break. No levels.";
            case "soul_harvester": return "Hoe kills have small chance to drop Soul Orbs which give massive XP. No levels.";
            case "crude_sharpness": return "Adds flat melee damage boost. Applies to pickaxes, shovels, axes, hoes, and sticks.";
            case "basalt_trail": return "Cools lava blocks directly beneath your feet into temporary Basalt blocks. No levels.";
            case "void_walker": return "Freefalling 20 blocks in the void teleports you safely back to ground. No levels.";
            default: return "";
        }
    }

    public Color getRuneColor(String effect) {
        switch (effect.toLowerCase()) {
            case "corrosive_slash": return Color.fromRGB(85, 107, 47);
            case "scorch": return Color.fromRGB(255, 69, 0);
            case "dwarfs_blessing": return Color.fromRGB(139, 69, 19);
            case "glacial_thorns": return Color.fromRGB(0, 191, 255);
            case "tidal_sweep": return Color.fromRGB(30, 144, 255);
            case "seismic_landing": return Color.fromRGB(112, 128, 144);
            case "photosynthesis": return Color.fromRGB(50, 205, 50);
            case "zephyr": return Color.fromRGB(224, 255, 255);
            case "static_charge": return Color.fromRGB(255, 255, 0);
            case "phantom_backstab": return Color.fromRGB(75, 0, 130);
            case "telekinesis": return Color.fromRGB(238, 130, 238);
            case "kinetic_rebound": return Color.fromRGB(211, 211, 211);
            case "redirection": return Color.fromRGB(244, 164, 96);
            case "bleed": return Color.fromRGB(150, 0, 0);
            case "poison_spores": return Color.fromRGB(0, 255, 127);
            case "gas_cloud": return Color.fromRGB(128, 128, 0);
            case "heavy_draw": return Color.fromRGB(105, 105, 105);
            case "timber": return Color.fromRGB(205, 133, 63);
            case "soul_harvester": return Color.fromRGB(72, 61, 139);
            case "crude_sharpness": return Color.fromRGB(192, 192, 192);
            case "basalt_trail": return Color.fromRGB(255, 127, 80);
            case "void_walker": return Color.fromRGB(186, 85, 211);
            default: return Color.WHITE;
        }
    }

    public FireworkEffect.Type getRuneStarType(String effect) {
        switch (effect.toLowerCase()) {
            case "corrosive_slash":
            case "glacial_thorns":
            case "static_charge":
            case "phantom_backstab":
            case "redirection":
            case "bleed":
            case "poison_spores":
            case "gas_cloud":
            case "heavy_draw":
            case "crude_sharpness":
                return FireworkEffect.Type.BALL;
            default:
                return FireworkEffect.Type.BALL_LARGE;
        }
    }

    public Material getRuneMaterial(String effect) {
        if (effect == null) return Material.PRIZE_POTTERY_SHERD;
        switch (effect.toLowerCase()) {
            case "lifesteal":
            case "speed":
            case "catch_flame":
            case "breach_surge":
            case "aegis_guard":
            case "mach_rush":
            case "rift_walk":
            case "daedalus_touch":
            case "ouroboros":
            case "vortex":
            case "shrapnel_shot":
            case "resonance_ping":
            case "naiads_repel":
                return Material.PRIZE_POTTERY_SHERD;

            case "demonium":
            case "corrosive_slash":
            case "scorch":
            case "glacial_thorns":
            case "seismic_landing":
            case "gas_cloud":
            case "bleed":
            case "poison_spores":
            case "heavy_draw":
            case "timber":
            case "soul_harvester":
            case "crude_sharpness":
            case "basalt_trail":
            case "void_walker":
            case "miasma":
            case "fenrirs_bite":
            case "anubis_judgment":
            case "gloom":
            case "cerberus_maw":
            case "styxs_toll":
            case "infernalis":
            case "remedium":
            case "brimstone":
            case "ashen_veil":
                return Material.GUSTER_BANNER_PATTERN;

            case "dwarfs_blessing":
            case "photosynthesis":
            case "zephyr":
            case "static_charge":
            case "phantom_backstab":
            case "telekinesis":
            case "kinetic_rebound":
            case "redirection":
            case "hermes_tread":
            case "warding_halo":
            case "radial_blind":
            case "apollos_ray":
            case "trinitys_well":
            case "valkyries_grace":
            case "hallowed_ground":
            case "smite_of_jupiter":
                return Material.PRISMARINE_SHARD;

            default:
                return Material.PRIZE_POTTERY_SHERD;
        }
    }

    public ItemStack createDynamicCustomItem(String id) {
        if (id == null) return null;
        if (id.startsWith("arcane.runes.") || id.startsWith("explorer.runes.")) {
            String effect = id.substring(id.lastIndexOf('.') + 1);
            int level = 1;
            if (effect.contains("_")) {
                try {
                    String levelStr = effect.substring(effect.lastIndexOf('_') + 1);
                    level = Integer.parseInt(levelStr);
                    effect = effect.substring(0, effect.lastIndexOf('_'));
                } catch (NumberFormatException e) {
                    plugin.getLogger().fine("Could not parse rune level from " + effect + ": " + e.getMessage());
                }
            }
            String baseId = id;
            if (id.matches(".+_\\d+")) {
                baseId = id.substring(0, id.lastIndexOf('_'));
            }
            return createEnchantmentRune(baseId, effect, level);
        }
        return null;
    }
}
