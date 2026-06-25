package space.qclid.dashboard.codex;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import net.kyori.adventure.text.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public final ItemStack wandOfLevitationItem;
    public final ItemStack galeChestplateItem;
    public final ItemStack siphonBladeItem;
    public final ItemStack lightningEssenceItem;
    public final ItemStack staffOfTheStormlordItem;
    public final ItemStack emptyVialItem;
    public final ItemStack bloodItem;
    public final ItemStack demoniumRuneItem;
    public final ItemStack demoniumRune2Item;
    public final ItemStack demoniumRune3Item;
    public final ItemStack bloodAltarItem;
    public final ItemStack naturesEmbraceItem;
    public final ItemStack sunsBrillianceItem;
    public final ItemStack linkStoneItem;
    public final ItemStack poisonVialItem;
    public final ItemStack soulOrbItem;
    public final ItemStack blessingOfTheVoidItem;
    public final ItemStack resonantPlateItem;
    public final ItemStack coreOfHeat;

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
        this.wandOfLevitationItem = createWandOfLevitation();
        this.galeChestplateItem = createGaleChestplate();
        this.siphonBladeItem = createSiphonBlade();
        this.lightningEssenceItem = createLightningEssence();
        this.staffOfTheStormlordItem = createStaffOfTheStormlord();
        this.emptyVialItem = createEmptyVial();
        this.bloodItem = createBlood();
        this.demoniumRuneItem = createDemoniumRune(1);
        this.demoniumRune2Item = createDemoniumRune(2);
        this.demoniumRune3Item = createDemoniumRune(3);
        this.bloodAltarItem = createBloodAltarItem();
        this.naturesEmbraceItem = createNaturesEmbrace();
        this.sunsBrillianceItem = createSunsBrilliance();
        this.linkStoneItem = createLinkStone();
        this.poisonVialItem = createPoisonVial();
        this.soulOrbItem = createSoulOrb();
        this.blessingOfTheVoidItem = createBlessingOfTheVoid();
        this.resonantPlateItem = createResonantPlate();
        this.coreOfHeat = createCoreOfHeat();
    }

    public boolean isCustomItem(ItemStack item, String expectedId) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return expectedId.equals(id);
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
            case "artemis_blessing": return "Artemis's Blessing";
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
            case "artemis_blessing":
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
            case "artemis_blessing": return "Silences bow shots and adds +5 levels of Punch. No levels.";
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
            case "artemis_blessing": return Color.fromRGB(154, 205, 50);
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

    public ItemStack createLightningEssence() {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.lightning_essence");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Lightning Essence")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A crackling trace of pure lightning.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Mob Drop ") + C_GRAY + toSmallCaps("(Lightning Kill)"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStaffOfTheStormlord() {
        ItemStack item = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.staff_of_the_stormlord");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Staff of the Stormlord")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Calls down a lightning bolt on")),
                    MM.deserialize(C_GRAY + toSmallCaps("the block you are looking at.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to strike lightning.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 20 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEmptyVial() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.empty_vial");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Empty Vial")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An empty glass container designed")),
                    MM.deserialize(C_GRAY + toSmallCaps("to hold player blood.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to extract blood.")),
                    MM.deserialize(C_RED + toSmallCaps("Warning: Deals 60% current health damage!"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBlood() {
        ItemStack item = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.blood");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Blood")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A vial filled with player blood.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Used for dark demonic rituals."))
            ));
            meta.setColor(Color.fromRGB(139, 0, 0)); // Dark red
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createDemoniumRune(int level) {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            NamespacedKey levelKey = new NamespacedKey(plugin, "rune_level");

            String suffix = level == 3 ? "III" : (level == 2 ? "II" : "I");
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, "arcane.runes.demonium" + (level > 1 ? "_" + level : ""));
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

            // Visual star appearances
            Color main = Color.fromRGB(60, 60, 60); // Dark grey
            FireworkEffect.Type type = FireworkEffect.Type.BALL;
            boolean trail = false;
            boolean flicker = false;
            Color fade = null;

            if (level == 2) {
                main = Color.fromRGB(30, 30, 30); // Very dark grey
                type = FireworkEffect.Type.BALL_LARGE;
                trail = true;
            } else if (level == 3) {
                main = Color.fromRGB(0, 0, 0); // Black
                type = FireworkEffect.Type.STAR;
                trail = true;
                flicker = true;
                fade = Color.fromRGB(139, 0, 0); // Fade to Dark Red
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

    public ItemStack createBloodAltarItem() {
        ItemStack table = new ItemStack(Material.RED_CARPET);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.blood_altar");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Blood Altar")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Structure: Red Carpet on top of")),
                    MM.deserialize(C_GRAY + toSmallCaps("a Dropper on top of Obsidian.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Used for demonic enchantments."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            table.setItemMeta(meta);
        }
        return table;
    }

    public ItemStack createCustomSkull(String skinUrl, String itemId, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(displayName));
            List<Component> lore = new ArrayList<>();
            for (String l : loreLines) {
                lore.add(MM.deserialize(l));
            }
            meta.lore(lore);

            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(skinUrl));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (Exception ignored) {}

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWandOfLevitation() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.ranged.wand_of_levitation");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Wand of Levitation")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Shoots a bolt rendering targets weightless.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to shoot levitation bolt.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 15 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createGaleChestplate() {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.armor.gale_chestplate");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Gale Chestplate")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Mystical infused armor allowing weightless flight.")),
                    MM.deserialize(""),
                    MM.deserialize(C_PURPLE + toSmallCaps("Slow Falling I") + " " + C_GRAY + toSmallCaps("(Applied)")),
                    MM.deserialize(C_YELLOW + toSmallCaps("Double Jump") + " " + C_GRAY + toSmallCaps("(10s Cooldown)"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSiphonBlade() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.melee.siphon_blade");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Siphon Blade")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A cursed blade that feeds on life force.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Restores 15% max health on kill."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createNaturesEmbrace() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.natures_embrace");
            meta.displayName(parse(C_GREEN + "<bold>" + toSmallCaps("Nature's Embrace")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A pulsating green orb humming with")),
                    MM.deserialize(C_GRAY + toSmallCaps("the raw power of the forest."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSunsBrilliance() {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.suns_brilliance");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Sun's Brilliance")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A solar-charged flower radiating")),
                    MM.deserialize(C_GRAY + toSmallCaps("with intense celestial heat."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBlessingOfTheVoid() {
        ItemStack item = new ItemStack(Material.ELYTRA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.materials.blessing_of_the_void");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Blessing of the Void")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An ender-encased elytra infused")),
                    MM.deserialize(C_GRAY + toSmallCaps("with void warding capabilities."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createResonantPlate() {
        ItemStack item = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.materials.resonant_plate");
            meta.displayName(parse(C_GRAY + "<bold>" + toSmallCaps("Resonant Plate")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A highly elastic metal plate designed")),
                    MM.deserialize(C_GRAY + toSmallCaps("to reflect kinetic and magical forces."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createLinkStone() {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.link_stone");
            meta.displayName(parse(C_ORANGE + "<bold>" + toSmallCaps("Link Stone")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A mystical crystal that binds")),
                    MM.deserialize(C_GRAY + toSmallCaps("and redirects soul connections."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPoisonVial() {
        ItemStack item = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.poison_vial");
            meta.displayName(parse(C_GREEN + "<bold>" + toSmallCaps("Poison Vial")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A concentrated venom extracted")),
                    MM.deserialize(C_GRAY + toSmallCaps("from toxic wilderness creatures."))
            ));
            meta.setColor(Color.fromRGB(50, 150, 50));
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSoulOrb() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.materials.soul_orb");
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps("Soul Orb")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A swirling orb containing a lost soul.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to absorb for 500 XP."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createCustomRune(String id, String displayName, String effect, String type, int level, String desc, Color color, FireworkEffect.Type starType, boolean trail, boolean flicker) {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
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

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder()
                    .withColor(color)
                    .with(starType)
                    .trail(trail)
                    .flicker(flicker);
            meta.setEffect(effectBuilder.build());

            star.setItemMeta(meta);
        }
        return star;
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

    public ItemStack createCoreOfHeat() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.materials.core_of_heat");
            meta.displayName(parse(C_ORANGE + "<bold>" + toSmallCaps("Core of Heat")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An intensely hot orb forged by compressing")),
                    MM.deserialize(C_GRAY + toSmallCaps("Magma Blocks and Blaze Powder."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
