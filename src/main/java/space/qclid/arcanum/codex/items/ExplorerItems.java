package space.qclid.arcanum.codex.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import space.qclid.arcanum.codex.items.explorer.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplorerItems {

    private final JavaPlugin plugin;

    // Sub-modules
    private final ExplorerTools tools;
    private final ExplorerArmor armor;
    private final ExplorerGadgets gadgets;

    // Cache items
    public final ItemStack heavyForgeItem;
    public final ItemStack waypointCompassItem;
    public final ItemStack ironGrappleItem;
    public final ItemStack diamondGrappleItem;
    public final ItemStack netheriteGrappleItem;
    public final ItemStack spelunkersHelmetItem;
    public final ItemStack flippersItem;
    public final ItemStack buildersWandItem;
    public final ItemStack webberItem;
    public final ItemStack ironWebSlingerItem;
    public final ItemStack diamondWebSlingerItem;
    public final ItemStack netheriteWebSlingerItem;
    public final ItemStack canteenItem;
    public final ItemStack fluteItem;
    public final ItemStack drillItem;
    public final ItemStack teleportationPlateItem;
    public final ItemStack slimeBootsItem;
    public final ItemStack oreScannerItem;
    public final ItemStack magneticRingItem;
    public final ItemStack bottleOfLightningItem;
    public final ItemStack enderBackpackItem;
    public final ItemStack autoSifterItem;
    public final ItemStack autoSmelterItem;
    public final ItemStack safariLassoItem;
    public final ItemStack voidBagItem;
    public final ItemStack voidBagRefundItem;
    public final ItemStack steamJetpackItem;
    public final ItemStack blockDuplicatorItem;

    public ExplorerItems(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize sub-modules
        this.tools = new ExplorerTools(plugin);
        this.armor = new ExplorerArmor(plugin);
        this.gadgets = new ExplorerGadgets(plugin);

        // Populate cached items by delegating to sub-modules
        this.heavyForgeItem = tools.createHeavyForgeItem();
        this.waypointCompassItem = tools.createWaypointCompass();
        this.ironGrappleItem = tools.createGrapplingHook("Iron", space.qclid.arcanum.util.TextUtil.C_GRAY, 10.0, "explorer.grappling_hook.iron");
        this.diamondGrappleItem = tools.createGrapplingHook("Diamond", "<#55FFFF>", 25.0, "explorer.grappling_hook.diamond");
        this.netheriteGrappleItem = tools.createGrapplingHook("Netherite", space.qclid.arcanum.util.TextUtil.C_PURPLE, 50.0, "explorer.grappling_hook.netherite");
        this.buildersWandItem = tools.createBuildersWand();
        this.webberItem = tools.createWebber();
        this.ironWebSlingerItem = tools.createWebSlingerItem("Iron", space.qclid.arcanum.util.TextUtil.C_GRAY, 10.0, "explorer.tools.web_slinger.iron");
        this.diamondWebSlingerItem = tools.createWebSlingerItem("Diamond", "<#55FFFF>", 25.0, "explorer.tools.web_slinger.diamond");
        this.netheriteWebSlingerItem = tools.createWebSlingerItem("Netherite", space.qclid.arcanum.util.TextUtil.C_PURPLE, 50.0, "explorer.tools.web_slinger.netherite");
        this.fluteItem = tools.createBeastmastersFlute();
        this.drillItem = tools.createExcavationDrill();
        this.autoSifterItem = tools.createAutoSifter();
        this.autoSmelterItem = tools.createAutoSmelter();
        this.blockDuplicatorItem = tools.createBlockDuplicatorItem();

        this.spelunkersHelmetItem = armor.createSpelunkersHelmet();
        this.flippersItem = armor.createDepthStriderFlippers();
        this.slimeBootsItem = armor.createSlimeBoots();
        this.steamJetpackItem = armor.createSteamJetpack();

        this.canteenItem = gadgets.createThermalCanteen();
        this.teleportationPlateItem = gadgets.createTeleportationPlate();
        this.oreScannerItem = gadgets.createOreScanner();
        this.magneticRingItem = gadgets.createMagneticRing();
        this.bottleOfLightningItem = gadgets.createBottleOfLightning();
        this.enderBackpackItem = gadgets.createEnderBackpack();
        this.safariLassoItem = gadgets.createSafariLasso();
        this.voidBagItem = gadgets.createVoidBag();
        this.voidBagRefundItem = gadgets.createVoidBagRefund();
        initItemMap();
    }

    private final Map<String, ItemStack> itemMap = new HashMap<>();

    private void initItemMap() {
        itemMap.put("machinery.heavy_forge", heavyForgeItem);
        itemMap.put("explorer.waypoint_compass", waypointCompassItem);
        itemMap.put("explorer.grappling_hook.iron", ironGrappleItem);
        itemMap.put("explorer.grappling_hook.diamond", diamondGrappleItem);
        itemMap.put("explorer.grappling_hook.netherite", netheriteGrappleItem);
        itemMap.put("explorer.armor.spelunkers_helmet", spelunkersHelmetItem);
        itemMap.put("explorer.armor.depth_strider_flippers", flippersItem);
        itemMap.put("explorer.tools.builders_wand", buildersWandItem);
        itemMap.put("explorer.tools.webber", webberItem);
        itemMap.put("explorer.tools.web_slinger.iron", ironWebSlingerItem);
        itemMap.put("explorer.tools.web_slinger.diamond", diamondWebSlingerItem);
        itemMap.put("explorer.tools.web_slinger.netherite", netheriteWebSlingerItem);
        itemMap.put("explorer.gadgets.thermal_canteen", canteenItem);
        itemMap.put("explorer.tools.beastmasters_flute", fluteItem);
        itemMap.put("explorer.tools.excavation_drill", drillItem);
        itemMap.put("explorer.gadgets.teleportation_plate", teleportationPlateItem);
        itemMap.put("explorer.armor.slime_boots", slimeBootsItem);
        itemMap.put("explorer.exploration.ore_scanner", oreScannerItem);
        itemMap.put("explorer.trinkets.magnetic_ring", magneticRingItem);
        itemMap.put("explorer.trinkets.bottle_of_lightning", bottleOfLightningItem);
        itemMap.put("explorer.gadgets.ender_backpack", enderBackpackItem);
        itemMap.put("explorer.tools.auto_sifter", autoSifterItem);
        itemMap.put("explorer.tools.auto_smelter", autoSmelterItem);
        itemMap.put("explorer.gadgets.safari_lasso", safariLassoItem);
        itemMap.put("explorer.gadgets.void_bag", voidBagItem);
        itemMap.put("explorer.gadgets.void_bag_refund", voidBagRefundItem);
        itemMap.put("explorer.gadgets.steam_jetpack", steamJetpackItem);
        itemMap.put("machinery.block_duplicator", blockDuplicatorItem);
    }

    public ItemStack createHeavyForgeItem() {
        return tools.createHeavyForgeItem();
    }

    public ItemStack createWaypointCompass() {
        return tools.createWaypointCompass();
    }

    public ItemStack createGrapplingHook(String tier, String color, double range, String itemId) {
        return tools.createGrapplingHook(tier, color, range, itemId);
    }

    public ItemStack createSpelunkersHelmet() {
        return armor.createSpelunkersHelmet();
    }

    public ItemStack createDepthStriderFlippers() {
        return armor.createDepthStriderFlippers();
    }

    public ItemStack createBuildersWand() {
        return tools.createBuildersWand();
    }

    public ItemStack createWebber() {
        return tools.createWebber();
    }

    public ItemStack createWebSlingerItem(String tier, String color, double range, String itemId) {
        return tools.createWebSlingerItem(tier, color, range, itemId);
    }

    public ItemStack createThermalCanteen() {
        return gadgets.createThermalCanteen();
    }

    public ItemStack createBeastmastersFlute() {
        return tools.createBeastmastersFlute();
    }

    public ItemStack createExcavationDrill() {
        return tools.createExcavationDrill();
    }

    public ItemStack createTeleportationPlate() {
        return gadgets.createTeleportationPlate();
    }

    public ItemStack createSlimeBoots() {
        return armor.createSlimeBoots();
    }

    public ItemStack createOreScanner() {
        return gadgets.createOreScanner();
    }

    public ItemStack createMagneticRing() {
        return gadgets.createMagneticRing();
    }

    public ItemStack createBottleOfLightning() {
        return gadgets.createBottleOfLightning();
    }

    public ItemStack createEnderBackpack() {
        return gadgets.createEnderBackpack();
    }

    public ItemStack createAutoSifter() {
        return tools.createAutoSifter();
    }

    public ItemStack createAutoSmelter() {
        return tools.createAutoSmelter();
    }

    public ItemStack createSafariLasso() {
        return gadgets.createSafariLasso();
    }

    public ItemStack createVoidBag() {
        return gadgets.createVoidBag();
    }

    public ItemStack createVoidBagRefund() {
        return gadgets.createVoidBagRefund();
    }

    public ItemStack createSteamJetpack() {
        return armor.createSteamJetpack();
    }

    public ItemStack createBlockDuplicatorItem() {
        return tools.createBlockDuplicatorItem();
    }

    public ItemStack createPortableUtility(Material material, String itemId, String displayName) {
        return tools.createPortableUtility(material, itemId, displayName);
    }

    /** Register an external item (from another feature) for lookup via getCustomItem. */
    public void registerExternalItem(String id, ItemStack item) {
        if (id != null && item != null) {
            itemMap.put(id, item);
        }
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

    public ItemStack createDynamicCustomItem(String id) {
        if (id == null) return null;
        
        Material material = Material.PAPER;
        String name = "";
        String loreDesc = "";
        
        if (id.startsWith("explorer.tools.locator.")) {
            material = Material.SPYGLASS;
            String struct = id.substring(id.lastIndexOf('.') + 1);
            String structName = Character.toUpperCase(struct.charAt(0)) + struct.substring(1).replace("_", " ");
            name = structName + " Locator";
            loreDesc = "A device containing blocks from the structure itself. Right-click to find the nearest " + structName + ". Has 10 charges.";
        }
        
        else if (id.equals("explorer.armor.webbed.helmet")) {
            material = Material.CHAINMAIL_HELMET;
            name = "Webbed Hood";
            loreDesc = "Part of the Webbed Set. Crafted using spider silk webs.";
        } else if (id.equals("explorer.armor.webbed.chestplate")) {
            material = Material.CHAINMAIL_CHESTPLATE;
            name = "Webbed Tunic";
            loreDesc = "Part of the Webbed Set. Crafted using spider silk webs.";
        } else if (id.equals("explorer.armor.webbed.leggings")) {
            material = Material.CHAINMAIL_LEGGINGS;
            name = "Webbed Trousers";
            loreDesc = "Part of the Webbed Set. Crafted using spider silk webs.";
        } else if (id.equals("explorer.armor.webbed.boots")) {
            material = Material.CHAINMAIL_BOOTS;
            name = "Webbed Boots";
            loreDesc = "Part of the Webbed Set. Crafted using spider silk webs.";
        }
        
        else if (id.equals("explorer.armor.aegis_vanguard.helmet")) {
            material = Material.DIAMOND_HELMET;
            name = "Aegis Vanguard Greathelm";
            loreDesc = "Heavy defensive helmet crafted from Blue Gold and Synthetic Diamonds.";
        } else if (id.equals("explorer.armor.aegis_vanguard.chestplate")) {
            material = Material.DIAMOND_CHESTPLATE;
            name = "Aegis Vanguard Platemail";
            loreDesc = "Heavy defensive chestplate crafted from Blue Gold and Synthetic Diamonds.";
        } else if (id.equals("explorer.armor.aegis_vanguard.leggings")) {
            material = Material.DIAMOND_LEGGINGS;
            name = "Aegis Vanguard Greaves";
            loreDesc = "Heavy defensive leggings crafted from Blue Gold and Synthetic Diamonds.";
        } else if (id.equals("explorer.armor.aegis_vanguard.boots")) {
            material = Material.DIAMOND_BOOTS;
            name = "Aegis Vanguard Sabatons";
            loreDesc = "Heavy defensive boots crafted from Blue Gold and Synthetic Diamonds.";
        }
        
        else if (id.equals("arcane.armor.storm_weaver.helmet")) {
            material = Material.GOLDEN_HELMET;
            name = "Storm-Weaver Hood";
            loreDesc = "Magical hood crafted from Lightning Essence and Rose Gold.";
        } else if (id.equals("arcane.armor.storm_weaver.chestplate")) {
            material = Material.GOLDEN_CHESTPLATE;
            name = "Storm-Weaver Robe";
            loreDesc = "Magical robe crafted from Lightning Essence and Rose Gold.";
        } else if (id.equals("arcane.armor.storm_weaver.leggings")) {
            material = Material.GOLDEN_LEGGINGS;
            name = "Storm-Weaver Leggings";
            loreDesc = "Magical leggings crafted from Lightning Essence and Rose Gold.";
        } else if (id.equals("arcane.armor.storm_weaver.boots")) {
            material = Material.GOLDEN_BOOTS;
            name = "Storm-Weaver Sandals";
            loreDesc = "Magical sandals crafted from Lightning Essence and Rose Gold.";
        } else if (id.equals("explorer.tools.omni_tool")) {
            material = Material.DIAMOND_PICKAXE;
            name = "Omni Tool";
            loreDesc = "A tool that instantly adapts its form to harvest whatever block you look at.";
        }
        
        if (name.isEmpty()) return null;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            meta.displayName(space.qclid.arcanum.util.TextUtil.parse(space.qclid.arcanum.util.TextUtil.C_GREEN + "<bold>" + space.qclid.arcanum.util.TextUtil.toSmallCaps(name)));
            meta.lore(List.of(
                    space.qclid.arcanum.util.TextUtil.MM.deserialize(space.qclid.arcanum.util.TextUtil.C_GRAY + space.qclid.arcanum.util.TextUtil.toSmallCaps(loreDesc))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
