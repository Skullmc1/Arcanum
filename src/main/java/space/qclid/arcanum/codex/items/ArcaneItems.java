package space.qclid.arcanum.codex.items;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import space.qclid.arcanum.codex.items.arcane.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcaneItems {

    private final JavaPlugin plugin;

    // Sub-modules
    private final ArcaneMachinery machinery;
    private final ArcaneRunes runes;
    private final ArcaneMaterials materials;
    private final ArcaneTrinkets trinkets;
    private final ArcaneArmor armor;
    private final ArcaneWeapons weapons;

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

        // Initialize sub-modules
        this.machinery = new ArcaneMachinery(plugin);
        this.runes = new ArcaneRunes(plugin);
        this.materials = new ArcaneMaterials(plugin);
        this.trinkets = new ArcaneTrinkets(plugin);
        this.armor = new ArcaneArmor(plugin);
        this.weapons = new ArcaneWeapons(plugin);

        // Populate cached items by delegating to sub-modules
        this.arcanaTableItem = machinery.createArcanaTableItem();
        this.upgradeTableItem = machinery.createUpgradeTableItem();
        this.bloodAltarItem = machinery.createBloodAltarItem();

        this.lifestealRuneItem = runes.createLifestealRune();
        this.speedRuneItem = runes.createSpeedRune();
        this.catchFlameRuneItem = runes.createCatchFlameRune(1);
        this.catchFlameRune2Item = runes.createCatchFlameRune(2);
        this.catchFlameRune3Item = runes.createCatchFlameRune(3);
        this.demoniumRuneItem = runes.createDemoniumRune(1);
        this.demoniumRune2Item = runes.createDemoniumRune(2);
        this.demoniumRune3Item = runes.createDemoniumRune(3);

        this.enderEssence = materials.createEnderEssence();
        this.hardenedCoal1 = materials.createHardenedCoal(1);
        this.hardenedCoal2 = materials.createHardenedCoal(2);
        this.hardenedCoalMax = materials.createHardenedCoal(3);
        this.hardenedBase1 = materials.createHardenedBase(1);
        this.hardenedBase2 = materials.createHardenedBase(2);
        this.hardenedBaseMax = materials.createHardenedBase(3);
        this.syntheticDiamond = materials.createSyntheticDiamond();
        this.syntheticEmerald = materials.createSyntheticEmerald();
        this.echoingCore = materials.createEchoingCore();
        this.potentSpiderWeb = materials.createPotentSpiderWeb();
        this.lightningEssenceItem = materials.createLightningEssence();
        this.emptyVialItem = materials.createEmptyVial();
        this.bloodItem = materials.createBlood();
        this.naturesEmbraceItem = materials.createNaturesEmbrace();
        this.sunsBrillianceItem = materials.createSunsBrilliance();
        this.blessingOfTheVoidItem = materials.createBlessingOfTheVoid();
        this.resonantPlateItem = materials.createResonantPlate();
        this.linkStoneItem = materials.createLinkStone();
        this.poisonVialItem = materials.createPoisonVial();
        this.soulOrbItem = materials.createSoulOrb();
        this.coreOfHeat = materials.createCoreOfHeat();

        this.immolationTotemItem = trinkets.createImmolationTotem();
        this.geode1 = trinkets.createVitalityGeode(1);
        this.geode2 = trinkets.createVitalityGeode(2);
        this.geode3 = trinkets.createVitalityGeode(3);

        this.speedBootsItem = armor.createSpeedBoots();
        this.shadowCloak = armor.createShadowCloak();
        this.superiorShadowCloak = armor.createSuperiorShadowCloak();
        this.galeChestplateItem = armor.createGaleChestplate();

        this.wandOfEmbersItem = weapons.createWandOfEmbers();
        this.wandOfLevitationItem = weapons.createWandOfLevitation();
        this.siphonBladeItem = weapons.createSiphonBlade();
        this.staffOfTheStormlordItem = weapons.createStaffOfTheStormlord();

        initItemMap();
    }

    public boolean isCustomItem(ItemStack item, String expectedId) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return expectedId.equals(id);
    }

    public ItemStack createArcanaTableItem() {
        return machinery.createArcanaTableItem();
    }

    public ItemStack createUpgradeTableItem() {
        return machinery.createUpgradeTableItem();
    }

    public ItemStack createBloodAltarItem() {
        return machinery.createBloodAltarItem();
    }

    public ItemStack createLifestealRune() {
        return runes.createLifestealRune();
    }

    public ItemStack createLifestealRuneOfLevel(int level) {
        return runes.createLifestealRuneOfLevel(level);
    }

    public ItemStack createSpeedRune() {
        return runes.createSpeedRune();
    }

    public ItemStack createSpeedRuneOfLevel(int level) {
        return runes.createSpeedRuneOfLevel(level);
    }

    public ItemStack createCatchFlameRune(int level) {
        return runes.createCatchFlameRune(level);
    }

    public ItemStack createDemoniumRune(int level) {
        return runes.createDemoniumRune(level);
    }

    public ItemStack createEnchantmentRune(String baseId, String effect, int level) {
        return runes.createEnchantmentRune(baseId, effect, level);
    }

    public String getRuneDisplayName(String effect) {
        return runes.getRuneDisplayName(effect);
    }

    public String getRuneType(String effect) {
        return runes.getRuneType(effect);
    }

    public String getRuneDescription(String effect) {
        return runes.getRuneDescription(effect);
    }

    public Color getRuneColor(String effect) {
        return runes.getRuneColor(effect);
    }

    public FireworkEffect.Type getRuneStarType(String effect) {
        return runes.getRuneStarType(effect);
    }

    public Material getRuneMaterial(String effect) {
        return runes.getRuneMaterial(effect);
    }

    public ItemStack createCustomRune(String id, String displayName, String effect, String type, int level, String desc, Color color, FireworkEffect.Type starType, boolean trail, boolean flicker) {
        return runes.createCustomRune(id, displayName, effect, type, level, desc, color, starType, trail, flicker);
    }

    public ItemStack createEnderEssence() {
        return materials.createEnderEssence();
    }

    public ItemStack createHardenedCoal(int tier) {
        return materials.createHardenedCoal(tier);
    }

    public ItemStack createHardenedBase(int tier) {
        return materials.createHardenedBase(tier);
    }

    public ItemStack createSyntheticDiamond() {
        return materials.createSyntheticDiamond();
    }

    public ItemStack createSyntheticEmerald() {
        return materials.createSyntheticEmerald();
    }

    public ItemStack createEchoingCore() {
        return materials.createEchoingCore();
    }

    public ItemStack createPotentSpiderWeb() {
        return materials.createPotentSpiderWeb();
    }

    public ItemStack createLightningEssence() {
        return materials.createLightningEssence();
    }

    public ItemStack createEmptyVial() {
        return materials.createEmptyVial();
    }

    public ItemStack createBlood() {
        return materials.createBlood();
    }

    public ItemStack createNaturesEmbrace() {
        return materials.createNaturesEmbrace();
    }

    public ItemStack createSunsBrilliance() {
        return materials.createSunsBrilliance();
    }

    public ItemStack createBlessingOfTheVoid() {
        return materials.createBlessingOfTheVoid();
    }

    public ItemStack createResonantPlate() {
        return materials.createResonantPlate();
    }

    public ItemStack createLinkStone() {
        return materials.createLinkStone();
    }

    public ItemStack createPoisonVial() {
        return materials.createPoisonVial();
    }

    public ItemStack createSoulOrb() {
        return materials.createSoulOrb();
    }

    public ItemStack createCoreOfHeat() {
        return materials.createCoreOfHeat();
    }

    public ItemStack createImmolationTotem() {
        return trinkets.createImmolationTotem();
    }

    public ItemStack createVitalityGeode(int tier) {
        return trinkets.createVitalityGeode(tier);
    }

    public ItemStack createAmuletOfThePhoenix() {
        return trinkets.createAmuletOfThePhoenix();
    }

    public ItemStack createTotemOfFallacy() {
        return trinkets.createTotemOfFallacy();
    }

    public ItemStack createFrostbiteRing() {
        return trinkets.createFrostbiteRing();
    }

    public ItemStack createStormcallerMedallion() {
        return trinkets.createStormcallerMedallion();
    }

    public ItemStack createSpeedBoots() {
        return armor.createSpeedBoots();
    }

    public ItemStack createShadowCloak() {
        return armor.createShadowCloak();
    }

    public ItemStack createSuperiorShadowCloak() {
        return armor.createSuperiorShadowCloak();
    }

    public ItemStack createGaleChestplate() {
        return armor.createGaleChestplate();
    }

    public ItemStack createWandOfEmbers() {
        return weapons.createWandOfEmbers();
    }

    public ItemStack createWandOfLevitation() {
        return weapons.createWandOfLevitation();
    }

    public ItemStack createStaffOfSupplant() {
        return weapons.createStaffOfSupplant();
    }

    public ItemStack createStaffOfTheStormlord() {
        return weapons.createStaffOfTheStormlord();
    }

    public ItemStack createSiphonBlade() {
        return weapons.createSiphonBlade();
    }

    public ItemStack createCorrosiveScythe() {
        return weapons.createCorrosiveScythe();
    }

    public ItemStack createWandOfTransmutation() {
        return weapons.createWandOfTransmutation();
    }

    // Skulls are created directly or delegated
    public ItemStack createCustomSkull(String skinUrl, String itemId, String displayName, List<String> loreLines) {
        return materials.createCustomSkull(skinUrl, itemId, displayName, loreLines);
    }

    public ItemStack getCustomItem(String id) {
        if (id == null) return null;
        ItemStack item = itemMap.get(id);
        if (item == null) {
            item = createDynamicCustomItem(id);
        }
        if (item != null) {
            ItemStack cloned = item.clone();
            space.qclid.arcanum.util.TextUtil.wrapItemLore(cloned);
            return cloned;
        }
        return null;
    }

    private final Map<String, ItemStack> itemMap = new HashMap<>();

    private void initItemMap() {
        itemMap.put("machinery.arcana_table", arcanaTableItem);
        itemMap.put("machinery.upgrade_table", upgradeTableItem);
        itemMap.put("arcane.lifesteal_rune", lifestealRuneItem);
        itemMap.put("arcane.speed_rune", speedRuneItem);
        itemMap.put("arcane.speed_boots", speedBootsItem);
        itemMap.put("arcane.wand_of_embers", wandOfEmbersItem);
        itemMap.put("arcane.materials.immolation_totem", immolationTotemItem);
        itemMap.put("arcane.runes.catch_flame", catchFlameRuneItem);
        itemMap.put("arcane.runes.catch_flame_2", catchFlameRune2Item);
        itemMap.put("arcane.runes.catch_flame_3", catchFlameRune3Item);
        itemMap.put("arcane.materials.ender_essence", enderEssence);
        itemMap.put("arcane.materials.hardened_coal_1", hardenedCoal1);
        itemMap.put("arcane.materials.hardened_coal_2", hardenedCoal2);
        itemMap.put("arcane.materials.hardened_coal_max", hardenedCoalMax);
        itemMap.put("arcane.materials.hardened_base_1", hardenedBase1);
        itemMap.put("arcane.materials.hardened_base_2", hardenedBase2);
        itemMap.put("arcane.materials.hardened_base_max", hardenedBaseMax);
        itemMap.put("arcane.materials.synthetic_diamond", syntheticDiamond);
        itemMap.put("arcane.materials.synthetic_emerald", syntheticEmerald);
        itemMap.put("arcane.materials.echoing_core", echoingCore);
        itemMap.put("arcane.materials.potent_spider_web", potentSpiderWeb);
        itemMap.put("arcane.armor.shadow_cloak", shadowCloak);
        itemMap.put("arcane.armor.superior_shadow_cloak", superiorShadowCloak);
        itemMap.put("arcane.trinkets.vitality_geode_1", geode1);
        itemMap.put("arcane.trinkets.vitality_geode_2", geode2);
        itemMap.put("arcane.trinkets.vitality_geode_3", geode3);
        itemMap.put("arcane.ranged.wand_of_levitation", wandOfLevitationItem);
        itemMap.put("arcane.armor.gale_chestplate", galeChestplateItem);
        itemMap.put("arcane.melee.siphon_blade", siphonBladeItem);
        itemMap.put("arcane.materials.lightning_essence", lightningEssenceItem);
        itemMap.put("arcane.ranged.staff_of_the_stormlord", staffOfTheStormlordItem);
        itemMap.put("arcane.materials.empty_vial", emptyVialItem);
        itemMap.put("arcane.materials.blood", bloodItem);
        itemMap.put("arcane.runes.demonium", demoniumRuneItem);
        itemMap.put("arcane.runes.demonium_2", demoniumRune2Item);
        itemMap.put("arcane.runes.demonium_3", demoniumRune3Item);
        itemMap.put("machinery.blood_altar", bloodAltarItem);
        itemMap.put("arcane.melee.natures_embrace", naturesEmbraceItem);
        itemMap.put("arcane.melee.suns_brilliance", sunsBrillianceItem);
        itemMap.put("arcane.trinkets.link_stone", linkStoneItem);
        itemMap.put("arcane.materials.poison_vial", poisonVialItem);
        itemMap.put("arcane.materials.soul_orb", soulOrbItem);
        itemMap.put("arcane.trinkets.blessing_of_the_void", blessingOfTheVoidItem);
        itemMap.put("arcane.materials.resonant_plate", resonantPlateItem);
        itemMap.put("arcane.materials.core_of_heat", coreOfHeat);
    }

    public ItemStack createDynamicCustomItem(String id) {
        if (id == null) return null;
        
        ItemStack item = runes.createDynamicCustomItem(id);
        if (item != null) return item;

        item = materials.createDynamicCustomItem(id);
        if (item != null) return item;

        item = machinery.createDynamicCustomItem(id);
        if (item != null) return item;

        // Custom tomes (Tome of Glintblade Phalanx)
        if (id.equals("arcane.tomes.glintblade_phalanx")) {
            Material material = Material.GLOBE_BANNER_PATTERN;
            String name = "Tome of Glintblade Phalanx";
            String loreDesc = "Summons 4 floating phantom daggers that autonomously target hostiles entering a 6-block radius.";
            
            ItemStack tome = new ItemStack(material);
            ItemMeta meta = tome.getItemMeta();
            if (meta != null) {
                NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
                meta.displayName(space.qclid.arcanum.util.TextUtil.parse(space.qclid.arcanum.util.TextUtil.C_PURPLE + "<bold>" + space.qclid.arcanum.util.TextUtil.toSmallCaps(name)));
                meta.lore(List.of(
                        space.qclid.arcanum.util.TextUtil.MM.deserialize(space.qclid.arcanum.util.TextUtil.C_GRAY + space.qclid.arcanum.util.TextUtil.toSmallCaps(loreDesc))
                ));
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                tome.setItemMeta(meta);
            }
            return tome;
        }

        return null;
    }
}
