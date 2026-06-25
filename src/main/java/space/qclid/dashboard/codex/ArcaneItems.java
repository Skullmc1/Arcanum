package space.qclid.dashboard.codex;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class ArcaneItems {

    private final JavaPlugin plugin;

    // Cache items
    public final ItemStack arcanaTableItem;
    public final ItemStack upgradeTableItem;
    public final ItemStack lifestealRuneItem;
    public final ItemStack speedRuneItem;
    public final ItemStack speedBootsItem;
    public final ItemStack wandOfEmbersItem;
    public final ItemStack immolationTotemItem;
    public final ItemStack catchFlameRuneItem;
    public final ItemStack catchFlameRune2Item;
    public final ItemStack catchFlameRune3Item;
    public final ItemStack enderEssence;
    public final ItemStack hardenedCoal1;
    public final ItemStack hardenedCoal2;
    public final ItemStack hardenedCoalMax;
    public final ItemStack hardenedBase1;
    public final ItemStack hardenedBase2;
    public final ItemStack hardenedBaseMax;
    public final ItemStack syntheticDiamond;
    public final ItemStack syntheticEmerald;
    public final ItemStack echoingCore;
    public final ItemStack shadowCloak;
    public final ItemStack superiorShadowCloak;
    public final ItemStack geode1;
    public final ItemStack geode2;
    public final ItemStack geode3;
    public final ItemStack potentSpiderWeb;

    public ArcaneItems(JavaPlugin plugin) {
        this.plugin = plugin;

        this.arcanaTableItem = createArcanaTableItem();
        this.upgradeTableItem = createUpgradeTableItem();
        this.lifestealRuneItem = createLifestealRune();
        this.speedRuneItem = createSpeedRune();
        this.speedBootsItem = createSpeedBoots();
        this.wandOfEmbersItem = createWandOfEmbers();
        this.immolationTotemItem = createImmolationTotem();
        this.catchFlameRuneItem = createCatchFlameRune(1);
        this.catchFlameRune2Item = createCatchFlameRune(2);
        this.catchFlameRune3Item = createCatchFlameRune(3);
        this.enderEssence = createEnderEssence();
        this.hardenedCoal1 = createHardenedCoal(1);
        this.hardenedCoal2 = createHardenedCoal(2);
        this.hardenedCoalMax = createHardenedCoal(3);
        this.hardenedBase1 = createHardenedBase(1);
        this.hardenedBase2 = createHardenedBase(2);
        this.hardenedBaseMax = createHardenedBase(3);
        this.syntheticDiamond = createSyntheticDiamond();
        this.syntheticEmerald = createSyntheticEmerald();
        this.echoingCore = createEchoingCore();
        this.shadowCloak = createShadowCloak();
        this.superiorShadowCloak = createSuperiorShadowCloak();
        this.geode1 = createVitalityGeode(1);
        this.geode2 = createVitalityGeode(2);
        this.geode3 = createVitalityGeode(3);
        this.potentSpiderWeb = createPotentSpiderWeb();
    }

    public ItemStack createArcanaTableItem() {
        ItemStack table = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.arcana_table");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Arcana Table")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A placed Crafting Table with a Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("below and bookshelves on the sides."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            table.setItemMeta(meta);
        }
        return table;
    }

    public ItemStack createUpgradeTableItem() {
        ItemStack table = new ItemStack(Material.ANVIL);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.upgrade_table");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Upgrade Table")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An Anvil on top of a Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("on top of a Bookshelf."))
            ));
            table.setItemMeta(meta);
        }
        return table;
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
            meta.displayName(parse(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + C_RED + toSmallCaps("Lifesteal " + roman) + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Weapon-Sword")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("sword in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Lifesteal " + roman) + ": " + C_GRAY + toSmallCaps("Heals on attack."))
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

    public ItemStack createImmolationTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.immolation_totem");
            meta.displayName(parse("<#FF3300><bold>" + toSmallCaps("Immolation Totem")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A glowing totem forged in pure lava.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Reagent for fire enchantments."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            totem.setItemMeta(meta);
        }
        return totem;
    }

    public ItemStack createSpeedBoots() {
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta meta = boots.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.speed_boots");
            NamespacedKey speedKey = new NamespacedKey(plugin, "rune_speed");
            meta.getPersistentDataContainer().set(speedKey, PersistentDataType.INTEGER, 1);

            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Speed Boots")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Diamond boots infused with swiftness.")),
                    MM.deserialize(""),
                    MM.deserialize(C_PURPLE + toSmallCaps("Speed I") + " " + C_GRAY + toSmallCaps("(Applied)"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            boots.setItemMeta(meta);
        }
        return boots;
    }

    public ItemStack createWandOfEmbers() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.wand_of_embers");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Embers")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A magical wand crafted from nether embers.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to cast fireballs."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public ItemStack createEnderEssence() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.ender_essence");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Ender Essence")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Condensed ender magic used for")),
                    MM.deserialize(C_GRAY + toSmallCaps("advanced arcane crafting."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createHardenedCoal(int tier) {
        ItemStack item = new ItemStack(Material.COAL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            String id = "arcane.materials.hardened_coal_" + (tier == 3 ? "max" : tier);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

            String nameStr = tier == 3 ? "Max" : (tier == 2 ? "II" : "I");
            meta.displayName(parse(C_PURPLE + toSmallCaps("Hardened Coal " + nameStr)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Pressed coal reagent used in metallurgy."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createHardenedBase(int tier) {
        ItemStack item = new ItemStack(Material.CLAY_BALL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            String id = "arcane.materials.hardened_base_" + (tier == 3 ? "max" : tier);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

            String nameStr = tier == 3 ? "Max" : (tier == 2 ? "II" : "I");
            meta.displayName(parse(C_PURPLE + toSmallCaps("Hardened Base " + nameStr)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Crystallized clay-metal compound."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSyntheticDiamond() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.synthetic_diamond");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Synthetic Diamond")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A dense, artificially forged diamond."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSyntheticEmerald() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.synthetic_emerald");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Synthetic Emerald")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A highly pressurized, artificial emerald."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEchoingCore() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.echoing_core");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Echoing Core")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A resonating core used for advanced upgrades."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStaffOfSupplant() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.staff_of_supplant");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Staff of Supplant")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Shoots a projectile that swaps your")),
                    MM.deserialize(C_GRAY + toSmallCaps("position with the target entity."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createAmuletOfThePhoenix() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.amulet_of_the_phoenix");
            NamespacedKey usesKey = new NamespacedKey(plugin, "uses");
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, 3);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Amulet of the Phoenix")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants totem recovery and fiery explosion knockback.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Uses remaining") + ": " + C_ORANGE + "3")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTotemOfFallacy() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.totem_of_fallacy");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Totem of Fallacy")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to teleport to spawn.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Reduces all inventory items' durability to 10%."))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createFrostbiteRing() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.frostbite_ring");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Frostbite Ring")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Freezes water under your feet.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Extinguishes target entities on hit."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createShadowCloak() {
        ItemStack item = new ItemStack(Material.BLACK_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.shadow_cloak");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Shadow Cloak")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Invisibility + Speed I when light level < 7.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable on head."))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSuperiorShadowCloak() {
        ItemStack item = new ItemStack(Material.BLACK_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.superior_shadow_cloak");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Superior Shadow Cloak")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Invisibility + Speed II + Swift Sneak while crouched.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable on head."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createVenomousScythe() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.melee.venomous_scythe");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Venomous Scythe")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Applies stacking poison on hit.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Deals bonus damage to poisoned mobs."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStormcallerMedallion() {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.stormcaller_medallion");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Stormcaller Medallion")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants 15% lightning strike chance")),
                    MM.deserialize(C_GRAY + toSmallCaps("on hit during rain or storms."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWandOfTransmutation() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.wand_of_transmutation");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Transmutation")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to temporarily turn hostiles")),
                    MM.deserialize(C_GRAY + toSmallCaps("into passive animals for 15s."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createVitalityGeode(int tier) {
        ItemStack item = new ItemStack(Material.AMETHYST_CLUSTER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.trinkets.vitality_geode_" + tier);

            String nameStr = tier == 3 ? "III" : (tier == 2 ? "II" : "I");
            int heartsStr = tier == 3 ? 10 : (tier == 2 ? 5 : 2);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Vitality Geode " + nameStr)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Increases max health by +" + (heartsStr * 2) + " (" + heartsStr + " hearts)")),
                    MM.deserialize(C_GRAY + toSmallCaps("while sitting in your inventory."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPotentSpiderWeb() {
        ItemStack item = new ItemStack(Material.COBWEB);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.potent_spider_web");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Potent Spider Web")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Extremely sticky web block used")),
                    MM.deserialize(C_GRAY + toSmallCaps("for advanced binding crafts."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
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
        }
        return null;
    }
}
