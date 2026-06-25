package space.qclid.dashboard.codex;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
}
