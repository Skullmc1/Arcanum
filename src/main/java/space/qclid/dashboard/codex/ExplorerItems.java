package space.qclid.dashboard.codex;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.Bukkit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

public class ExplorerItems {

    private final JavaPlugin plugin;

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
    public final ItemStack steamJetpackItem;
    public final ItemStack blockDuplicatorItem;

    public ExplorerItems(JavaPlugin plugin) {
        this.plugin = plugin;

        this.heavyForgeItem = createHeavyForgeItem();
        this.waypointCompassItem = createWaypointCompass();
        this.ironGrappleItem = createGrapplingHook("Iron", C_GRAY, 10.0, "explorer.grappling_hook.iron");
        this.diamondGrappleItem = createGrapplingHook("Diamond", "<#55FFFF>", 25.0, "explorer.grappling_hook.diamond");
        this.netheriteGrappleItem = createGrapplingHook("Netherite", C_PURPLE, 50.0, "explorer.grappling_hook.netherite");
        this.spelunkersHelmetItem = createSpelunkersHelmet();
        this.flippersItem = createDepthStriderFlippers();
        this.buildersWandItem = createBuildersWand();
        this.webberItem = createWebber();
        this.ironWebSlingerItem = createWebSlingerItem("Iron", C_GRAY, 10.0, "explorer.tools.web_slinger.iron");
        this.diamondWebSlingerItem = createWebSlingerItem("Diamond", "<#55FFFF>", 25.0, "explorer.tools.web_slinger.diamond");
        this.netheriteWebSlingerItem = createWebSlingerItem("Netherite", C_PURPLE, 50.0, "explorer.tools.web_slinger.netherite");
        this.canteenItem = createThermalCanteen();
        this.fluteItem = createBeastmastersFlute();
        this.drillItem = createExcavationDrill();
        this.teleportationPlateItem = createTeleportationPlate();
        this.slimeBootsItem = createSlimeBoots();
        this.oreScannerItem = createOreScanner();
        this.magneticRingItem = createMagneticRing();
        this.bottleOfLightningItem = createBottleOfLightning();
        this.enderBackpackItem = createEnderBackpack();
        this.autoSifterItem = createAutoSifter();
        this.autoSmelterItem = createAutoSmelter();
        this.safariLassoItem = createSafariLasso();
        this.voidBagItem = createVoidBag();
        this.steamJetpackItem = createSteamJetpack();
        this.blockDuplicatorItem = createBlockDuplicatorItem();
    }

    public ItemStack createHeavyForgeItem() {
        ItemStack forge = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta meta = forge.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.heavy_forge");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Heavy Forge")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A Crafting Table on top of a Blast Furnace")),
                    MM.deserialize(C_GRAY + toSmallCaps("on top of a Dropper."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            forge.setItemMeta(meta);
        }
        return forge;
    }

    public ItemStack createWaypointCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.waypoint_compass");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Waypoint Compass")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A celestial compass that aligns with waypoints.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to teleport to the linked waypoint.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Shift-Right-click to link a waypoint."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    public ItemStack createGrapplingHook(String tier, String color, double range, String itemId) {
        ItemStack lead = new ItemStack(Material.LEAD);
        ItemMeta meta = lead.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(color + "<bold>" + toSmallCaps(tier + " Grappling Hook")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A visual grappling hook that pulls you.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Range") + ": " + C_ORANGE + (int) range + " " + toSmallCaps("blocks")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in hand and right-click block to use."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            lead.setItemMeta(meta);
        }
        return lead;
    }

    public ItemStack createSpelunkersHelmet() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.armor.spelunkers_helmet");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Spelunker's Helmet")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Grants permanent Night Vision.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Highlights nearby hostile mobs."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createDepthStriderFlippers() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.depth_strider_flippers");

            if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta lam) {
                lam.setColor(Color.BLUE);
            }

            meta.displayName(parse("<#55FFFF><bold>" + toSmallCaps("Depth Strider Flippers")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Gives swim speed & water breathing.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Applies heavy slowness on land."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBuildersWand() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.tools.builders_wand");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Builder's Wand")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click face to place up to")),
                    MM.deserialize(C_GRAY + toSmallCaps("9 matching blocks in a line."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWebber() {
        ItemStack item = new ItemStack(Material.COBWEB);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.tools.webber");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Webber")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to shoot temporary cobwebs.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Duration") + ": " + C_ORANGE + "5 seconds"),
                    MM.deserialize(C_GRAY + toSmallCaps("Webs drop nothing when broken with a sword."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWebSlingerItem(String tier, String color, double range, String itemId) {
        ItemStack lead = new ItemStack(Material.LEAD);
        ItemMeta meta = lead.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(color + "<bold>" + toSmallCaps(tier + " Web Slinger")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A visual grappling hook that pulls you")),
                    MM.deserialize(C_GRAY + toSmallCaps("and places a cobweb when you land.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Range") + ": " + C_ORANGE + (int) range + " " + toSmallCaps("blocks")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in hand and right-click block to use."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            lead.setItemMeta(meta);
        }
        return lead;
    }

    public ItemStack createThermalCanteen() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.thermal_canteen");
            NamespacedKey chargesKey = new NamespacedKey(plugin, "charges");
            meta.getPersistentDataContainer().set(chargesKey, PersistentDataType.INTEGER, 4);
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Thermal Canteen")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Flask cures effects and restores hunger.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + "4 / 4")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBeastmastersFlute() {
        ItemStack item = new ItemStack(Material.BAMBOO);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.tools.beastmasters_flute");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Beastmaster's Flute")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Pacifies/sleeps hostiles in 5-block radius.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Cooldown") + ": " + C_ORANGE + "60s")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createExcavationDrill() {
        ItemStack item = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.tools.excavation_drill");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Excavation Drill")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Heavy 3x3 block-breaking pickaxe."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPortableUtility(Material material, String itemId, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps(displayName)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to open virtual " + displayName + " screen."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTeleportationPlate() {
        ItemStack item = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.navigation.teleportation_plate");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Waypoint Teleport Plate")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A ground-based teleportation structure.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click with Waypoint Compass to link.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Linked to: None"))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSlimeBoots() {
        ItemStack item = new ItemStack(Material.SLIME_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.slime_boots");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Slime Boots")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Bouncy footgear designed to bypass gravity's limits.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Negates all fall damage and bounces wearer."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createOreScanner() {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.exploration.ore_scanner");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Ore Scanner")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("An exploration gadget detecting nearby ore veins.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Right-click to highlight nearby ores.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Cooldown: 30 seconds."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBottleOfLightning() {
        ItemStack item = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.bottle_of_lightning");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Bottle of Lightning")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A bottle filled with raw electrical current.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Crafted from siphoning lightning essence."))
            ));
            meta.setColor(Color.WHITE);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEnderBackpack() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/ddcc189633c789cb6d5e78d13a5043b26e7b40cdb7cfc4e23aa2279574967b4",
                "explorer.gadgets.ender_backpack",
                G_GOLD + "<bold>" + toSmallCaps("Ender Backpack"),
                List.of(
                        C_GRAY + toSmallCaps("Right-click to open your ender"),
                        C_GRAY + toSmallCaps("chest inventory remotely from anywhere."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click to open.")
                )
        );
    }

    public ItemStack createAutoSifter() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.auto_sifter");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Auto Sifter")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Structure: Hopper on top of Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("with Cauldron adjacent to the Dropper.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Sifts Gravel or Sand into resources over time."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createAutoSmelter() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.auto_smelter");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Auto Smelter")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Structure: Hopper on top of Dropper")),
                    MM.deserialize(C_GRAY + toSmallCaps("with Furnace adjacent to the Dropper.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Automatically smelts dropper items over time."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSafariLasso() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/41a129ef4ccb4a36fde3d4e8c187b5a83cc4e85aa2279574967b4a6cc286c23a",
                "explorer.gadgets.safari_lasso",
                G_GOLD + "<bold>" + toSmallCaps("Safari Lasso"),
                List.of(
                        C_GRAY + toSmallCaps("Captures passive animals for transport."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click a passive mob to capture."),
                        C_YELLOW + toSmallCaps("Right-click a block to release the mob.")
                )
        );
    }

    public ItemStack createVoidBag() {
        return createCustomSkull(
                "http://textures.minecraft.net/texture/967b4a6cc286c23ae80365778848d1d81f21cc415bf3c3c789cb6d5e78d13a504",
                "explorer.gadgets.void_bag",
                G_GOLD + "<bold>" + toSmallCaps("Void Bag"),
                List.of(
                        C_GRAY + toSmallCaps("A pocket dimension void trash bag."),
                        "",
                        C_YELLOW + toSmallCaps("Right-click to open."),
                        C_RED + toSmallCaps("All items inside are deleted on close!")
                )
        );
    }

    public ItemStack createSteamJetpack() {
        ItemStack item = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.armor.steam_jetpack");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Steam Jetpack")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Wearable flight thruster chestplate.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Hold Shift in mid-air to fly.")),
                    MM.deserialize(C_RED + toSmallCaps("Consumes Coal/Charcoal/Hardened Coal from inventory."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomSkull(String skinUrl, String itemId, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(parse(displayName));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
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

    public ItemStack createMagneticRing() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.gadgets.magnetic_ring");
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Magnetic Ring")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A passive trinket that attracts nearby drops.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Attracts items within 5-block radius."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBlockDuplicatorItem() {
        ItemStack item = new ItemStack(Material.NETHER_BRICK_FENCE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.block_duplicator");
            meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps("Block Duplicator")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Structure: Nether Brick Fence on top of")),
                    MM.deserialize(C_GRAY + toSmallCaps("a Dropper surrounded by Furnaces/Blast Furnaces.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Duplicates building blocks placed inside.")),
                    MM.deserialize(C_GRAY + toSmallCaps("Requires Lava Buckets as fuel (1 bucket per 2 stacks)."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
