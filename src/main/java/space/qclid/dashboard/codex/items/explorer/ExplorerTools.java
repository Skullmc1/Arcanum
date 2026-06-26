package space.qclid.dashboard.codex.items.explorer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import static space.qclid.dashboard.util.TextUtil.*;

public class ExplorerTools {

    private final JavaPlugin plugin;

    public ExplorerTools(JavaPlugin plugin) {
        this.plugin = plugin;
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
