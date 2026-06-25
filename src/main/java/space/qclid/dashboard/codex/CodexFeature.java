package space.qclid.dashboard.codex;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.codex.gui.CodexGuiListener;
import space.qclid.dashboard.codex.gui.CodexMainGui;
import space.qclid.dashboard.codex.gui.CodexSubCategoryGui;
import space.qclid.dashboard.data.DataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexFeature implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;

    // Track active block structures players are right-clicking
    private final Map<UUID, String> activeMachine = new HashMap<>();

    // Custom items cache
    private ItemStack arcanaTableItem;
    private ItemStack heavyForgeItem;
    private ItemStack lifestealRuneItem;
    private ItemStack speedRuneItem;
    private ItemStack speedBootsItem;
    private ItemStack wandOfEmbersItem;
    private ItemStack waypointCompassItem;
    private ItemStack ironGrappleItem;
    private ItemStack diamondGrappleItem;
    private ItemStack netheriteGrappleItem;

    public CodexFeature(JavaPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = new CodexManager(plugin);
        this.registry = new CodexRegistry();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize GUIs listener
        new CodexGuiListener(plugin, registry, manager);

        initRegistry();

        // Run passive Speed Boots check every 10 ticks (0.5s)
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateSpeedRunes(player);
            }
        }, 1L, 10L);
    }

    public CodexManager getManager() {
        return manager;
    }

    public CodexRegistry getRegistry() {
        return registry;
    }

    private void initRegistry() {
        // Create Sub-Categories
        CodexCategory machineryStations = new CodexCategory("machinery.stations", "machinery", "Crafting Stations", new ItemStack(Material.CRAFTING_TABLE));
        registry.registerCategory(machineryStations);

        CodexCategory arcaneRunes = new CodexCategory("arcane.runes", "arcane", "Enchantment Runes", new ItemStack(Material.FIREWORK_STAR));
        CodexCategory arcaneArmor = new CodexCategory("arcane.armor", "arcane", "Armor", new ItemStack(Material.DIAMOND_CHESTPLATE));
        CodexCategory arcaneMelee = new CodexCategory("arcane.melee", "arcane", "Melee Weapons", new ItemStack(Material.DIAMOND_SWORD));
        CodexCategory arcaneRanged = new CodexCategory("arcane.ranged", "arcane", "Ranged Weapons", new ItemStack(Material.BLAZE_ROD));
        CodexCategory arcaneBoosts = new CodexCategory("arcane.boosts", "arcane", "Arcana Boosts", new ItemStack(Material.POTION));
        CodexCategory arcaneIngredients = new CodexCategory("arcane.ingredients", "arcane", "Ingredients", new ItemStack(Material.NETHER_WART));
        registry.registerCategory(arcaneRunes);
        registry.registerCategory(arcaneArmor);
        registry.registerCategory(arcaneMelee);
        registry.registerCategory(arcaneRanged);
        registry.registerCategory(arcaneBoosts);
        registry.registerCategory(arcaneIngredients);

        CodexCategory explorerNavigation = new CodexCategory("explorer.navigation", "explorer", "Navigation", new ItemStack(Material.COMPASS));
        CodexCategory explorerExploration = new CodexCategory("explorer.exploration", "explorer", "Exploration", new ItemStack(Material.SPYGLASS));
        CodexCategory explorerGadgets = new CodexCategory("explorer.gadgets", "explorer", "Gadgets", new ItemStack(Material.LEAD));
        registry.registerCategory(explorerNavigation);
        registry.registerCategory(explorerExploration);
        registry.registerCategory(explorerGadgets);

        // Build items
        this.arcanaTableItem = createArcanaTableItem();
        this.heavyForgeItem = createHeavyForgeItem();
        this.lifestealRuneItem = createLifestealRune();
        this.speedRuneItem = createSpeedRune();
        this.speedBootsItem = createSpeedBoots();
        this.wandOfEmbersItem = createWandOfEmbers();
        this.waypointCompassItem = createWaypointCompass();
        this.ironGrappleItem = createGrapplingHook("Iron", C_GRAY, 10.0, "explorer.grappling_hook.iron");
        this.diamondGrappleItem = createGrapplingHook("Diamond", "<#55FFFF>", 25.0, "explorer.grappling_hook.diamond");
        this.netheriteGrappleItem = createGrapplingHook("Netherite", C_PURPLE, 50.0, "explorer.grappling_hook.netherite");

        // 1. Arcana Table
        CodexItem arcanaTableCodex = new CodexItem.Builder("machinery.arcana_table")
                .displayName("Arcana Table")
                .displayItem(arcanaTableItem)
                .xpCost(3)
                .requires(new ItemStack(Material.CRAFTING_TABLE, 1))
                .requires(new ItemStack(Material.BOOKSHELF, 2))
                .description("Crafting station with 2 adjacent Bookshelves horizontally.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.BOOK), null,
                        new ItemStack(Material.BOOKSHELF), new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.BOOKSHELF),
                        null, new ItemStack(Material.BOOK), null
                )
                .build();
        machineryStations.addItem(arcanaTableCodex);

        // 2. Heavy Forge
        CodexItem heavyForgeCodex = new CodexItem.Builder("machinery.heavy_forge")
                .displayName("Heavy Forge")
                .displayItem(heavyForgeItem)
                .xpCost(3)
                .requires(new ItemStack(Material.CRAFTING_TABLE, 1))
                .requires(new ItemStack(Material.BLAST_FURNACE, 1))
                .description("Crafting station placed directly on top of a Blast Furnace.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.CRAFTING_TABLE), null,
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.BLAST_FURNACE), new ItemStack(Material.IRON_INGOT),
                        null, new ItemStack(Material.IRON_INGOT), null
                )
                .build();
        machineryStations.addItem(heavyForgeCodex);

        // 3. Lifesteal Rune
        CodexItem lifestealRuneCodex = new CodexItem.Builder("arcane.lifesteal_rune")
                .displayName("Lifesteal Rune I")
                .displayItem(lifestealRuneItem)
                .xpCost(8)
                .requires(new ItemStack(Material.FIREWORK_STAR, 1))
                .requires(new ItemStack(Material.DIAMOND, 1))
                .description("Apply to sword. Heals you on hit.")
                .craftingStation(arcanaTableItem)
                .recipe(
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.DIAMOND), new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.FIREWORK_STAR), new ItemStack(Material.IRON_SWORD), new ItemStack(Material.FIREWORK_STAR),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.NETHER_WART), new ItemStack(Material.REDSTONE)
                )
                .build();
        arcaneRunes.addItem(lifestealRuneCodex);

        // 4. Speed Rune
        CodexItem speedRuneCodex = new CodexItem.Builder("arcane.speed_rune")
                .displayName("Speed Rune I")
                .displayItem(speedRuneItem)
                .xpCost(8)
                .requires(new ItemStack(Material.FIREWORK_STAR, 1))
                .requires(new ItemStack(Material.SUGAR, 4))
                .description("Apply to boots. Grants Speed I when worn.")
                .craftingStation(arcanaTableItem)
                .recipe(
                        new ItemStack(Material.SUGAR), new ItemStack(Material.DIAMOND), new ItemStack(Material.SUGAR),
                        new ItemStack(Material.FIREWORK_STAR), new ItemStack(Material.SUGAR), new ItemStack(Material.FIREWORK_STAR),
                        new ItemStack(Material.SUGAR), new ItemStack(Material.SUGAR), new ItemStack(Material.SUGAR)
                )
                .build();
        arcaneRunes.addItem(speedRuneCodex);

        // 5. Speed Boots
        CodexItem speedBootsCodex = new CodexItem.Builder("arcane.speed_boots")
                .displayName("Speed Boots")
                .displayItem(speedBootsItem)
                .xpCost(12)
                .requires(new ItemStack(Material.DIAMOND_BOOTS, 1))
                .requires(speedRuneItem)
                .description("Diamond boots pre-infused with the Speed Rune.")
                .craftingStation(arcanaTableItem)
                .recipe(
                        speedRuneItem, null, speedRuneItem,
                        null, new ItemStack(Material.DIAMOND_BOOTS), null,
                        null, null, null
                )
                .build();
        arcaneArmor.addItem(speedBootsCodex);

        // 6. Wand of Embers
        CodexItem wandItem = new CodexItem.Builder("arcane.wand_of_embers")
                .displayName("Wand of Embers")
                .displayItem(wandOfEmbersItem)
                .xpCost(5)
                .requires(new ItemStack(Material.BLAZE_ROD, 1))
                .description("A magical wand that shoots fireballs when right-clicked.")
                .craftingStation(arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.FIRE_CHARGE), new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.BLAZE_ROD),    new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.BLAZE_ROD),    new ItemStack(Material.BLAZE_POWDER)
                )
                .build();
        arcaneRanged.addItem(wandItem);

        // 7. Waypoint Compass
        CodexItem compassItem = new CodexItem.Builder("explorer.waypoint_compass")
                .displayName("Waypoint Compass")
                .displayItem(waypointCompassItem)
                .xpCost(10)
                .requires(new ItemStack(Material.COMPASS, 1))
                .requires(new ItemStack(Material.ENDER_PEARL, 8))
                .description("A celestial compass that teleports you to its linked waypoint.")
                .craftingStation(heavyForgeItem)
                .recipe(
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_EYE), new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.COMPASS),      new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL),  new ItemStack(Material.ENDER_PEARL)
                )
                .build();
        explorerNavigation.addItem(compassItem);

        // 8. Iron Grappling Hook
        CodexItem ironGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.iron")
                .displayName("Iron Grappling Hook")
                .displayItem(ironGrappleItem)
                .xpCost(6)
                .requires(new ItemStack(Material.LEAD, 1))
                .requires(new ItemStack(Material.TRIPWIRE_HOOK, 2))
                .description("Grappling hook with a 10-block range.")
                .craftingStation(heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.LEAD), new ItemStack(Material.LEAD), new ItemStack(Material.LEAD),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.LEAD), new ItemStack(Material.IRON_INGOT)
                )
                .build();
        explorerGadgets.addItem(ironGrappleCodex);

        // 9. Diamond Grappling Hook
        CodexItem diamondGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.diamond")
                .displayName("Diamond Grappling Hook")
                .displayItem(diamondGrappleItem)
                .xpCost(12)
                .requires(ironGrappleItem)
                .requires(new ItemStack(Material.DIAMOND, 4))
                .description("Grappling hook with a 25-block range.")
                .craftingStation(heavyForgeItem)
                .recipe(
                        new ItemStack(Material.DIAMOND), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.DIAMOND),
                        new ItemStack(Material.LEAD), ironGrappleItem, new ItemStack(Material.LEAD),
                        new ItemStack(Material.DIAMOND), new ItemStack(Material.LEAD), new ItemStack(Material.DIAMOND)
                )
                .build();
        explorerGadgets.addItem(diamondGrappleCodex);

        // 10. Netherite Grappling Hook
        CodexItem netheriteGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.netherite")
                .displayName("Netherite Grappling Hook")
                .displayItem(netheriteGrappleItem)
                .xpCost(20)
                .requires(diamondGrappleItem)
                .requires(new ItemStack(Material.NETHERITE_INGOT, 4))
                .description("Grappling hook with a 50-block range.")
                .craftingStation(heavyForgeItem)
                .recipe(
                        new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.NETHERITE_INGOT),
                        new ItemStack(Material.LEAD), diamondGrappleItem, new ItemStack(Material.LEAD),
                        new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.LEAD), new ItemStack(Material.NETHERITE_INGOT)
                )
                .build();
        explorerGadgets.addItem(netheriteGrappleCodex);

        registerRecipes();
    }

    private ItemStack createArcanaTableItem() {
        ItemStack table = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = table.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.arcana_table");
            meta.displayName(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps("Arcana Table")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A placed Crafting Table with at least")),
                    MM.deserialize(C_GRAY + toSmallCaps("2 Bookshelves horizontally adjacent to it."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            table.setItemMeta(meta);
        }
        return table;
    }

    private ItemStack createHeavyForgeItem() {
        ItemStack forge = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta meta = forge.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machinery.heavy_forge");
            meta.displayName(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps("Heavy Forge")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A placed Crafting Table directly on top")),
                    MM.deserialize(C_GRAY + toSmallCaps("of a Blast Furnace."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            forge.setItemMeta(meta);
        }
        return forge;
    }

    private ItemStack createLifestealRune() {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.lifesteal_rune");
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "WEAPON_SWORD");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "lifesteal");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, 1);

            meta.displayName(MM.deserialize(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + C_RED + toSmallCaps("Lifesteal I") + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Weapon-Sword")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("sword in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Lifesteal I") + ": " + C_GRAY + toSmallCaps("Heals on attack."))
            ));

            // Custom burst color (red & purple)
            meta.setEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.PURPLE)
                    .with(FireworkEffect.Type.BURST)
                    .build());

            star.setItemMeta(meta);
        }
        return star;
    }

    private ItemStack createSpeedRune() {
        ItemStack star = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.speed_rune");
            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "ARMOR_BOOTS");
            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "speed");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, 1);

            meta.displayName(MM.deserialize(C_PURPLE + toSmallCaps("Enchantment Rune") + C_GRAY + " (" + "<#55FFFF>" + toSmallCaps("Speed I") + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Type") + ": " + C_PURPLE + toSmallCaps("Armor-Boots")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Hold in main hand and swap with")),
                    MM.deserialize(C_GRAY + toSmallCaps("boots in off-hand to apply.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Speed I") + ": " + C_GRAY + toSmallCaps("Grants Speed I when worn."))
            ));

            // Custom ball color (aqua & yellow)
            meta.setEffect(FireworkEffect.builder()
                    .withColor(Color.AQUA, Color.YELLOW)
                    .with(FireworkEffect.Type.BALL)
                    .build());

            star.setItemMeta(meta);
        }
        return star;
    }

    private ItemStack createSpeedBoots() {
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta meta = boots.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.speed_boots");
            NamespacedKey speedKey = new NamespacedKey(plugin, "rune_speed");
            meta.getPersistentDataContainer().set(speedKey, PersistentDataType.INTEGER, 1);

            meta.displayName(MM.deserialize(C_PURPLE + "<bold>" + toSmallCaps("Speed Boots")));
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

    private ItemStack createWandOfEmbers() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "arcane.wand_of_embers");
            meta.displayName(MM.deserialize(C_PURPLE + "<bold>" + toSmallCaps("Wand of Embers")));
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

    private ItemStack createWaypointCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "explorer.waypoint_compass");
            meta.displayName(MM.deserialize(G_GOLD + "<bold>" + toSmallCaps("Waypoint Compass")));
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

    private ItemStack createGrapplingHook(String tier, String color, double range, String itemId) {
        ItemStack lead = new ItemStack(Material.LEAD);
        ItemMeta meta = lead.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
            meta.displayName(MM.deserialize(color + "<bold>" + toSmallCaps(tier + " Grappling Hook")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("A visual grappling hook that pulls you.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Range") + ": " + C_ORANGE + (int) range + " " + toSmallCaps("blocks")),
                    MM.deserialize(""),
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to launch."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            lead.setItemMeta(meta);
        }
        return lead;
    }

    private void registerRecipes() {
        // 1. Arcana Table Shaped Recipe
        NamespacedKey tableKey = new NamespacedKey(plugin, "recipe_arcana_table");
        if (Bukkit.getRecipe(tableKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(tableKey, arcanaTableItem);
            recipe.shape(" B ", "LTL", " B ");
            recipe.setIngredient('B', Material.BOOK);
            recipe.setIngredient('L', Material.BOOKSHELF);
            recipe.setIngredient('T', Material.CRAFTING_TABLE);
            Bukkit.addRecipe(recipe);
        }

        // 2. Heavy Forge Shaped Recipe
        NamespacedKey forgeKey = new NamespacedKey(plugin, "recipe_heavy_forge");
        if (Bukkit.getRecipe(forgeKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(forgeKey, heavyForgeItem);
            recipe.shape(" T ", "IFI", " I ");
            recipe.setIngredient('T', Material.CRAFTING_TABLE);
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('F', Material.BLAST_FURNACE);
            Bukkit.addRecipe(recipe);
        }

        // 3. Lifesteal Rune Recipe
        NamespacedKey lifestealKey = new NamespacedKey(plugin, "recipe_lifesteal_rune");
        if (Bukkit.getRecipe(lifestealKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(lifestealKey, lifestealRuneItem);
            recipe.shape("RDR", "SWS", "RNR");
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('S', Material.FIREWORK_STAR);
            recipe.setIngredient('W', Material.IRON_SWORD);
            recipe.setIngredient('N', Material.NETHER_WART);
            Bukkit.addRecipe(recipe);
        }

        // 4. Speed Rune Recipe
        NamespacedKey speedRuneKey = new NamespacedKey(plugin, "recipe_speed_rune");
        if (Bukkit.getRecipe(speedRuneKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(speedRuneKey, speedRuneItem);
            recipe.shape("SDS", "SGS", "SSS");
            recipe.setIngredient('S', Material.SUGAR);
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('G', Material.FIREWORK_STAR);
            Bukkit.addRecipe(recipe);
        }

        // 5. Speed Boots Recipe
        NamespacedKey speedBootsKey = new NamespacedKey(plugin, "recipe_speed_boots");
        if (Bukkit.getRecipe(speedBootsKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(speedBootsKey, speedBootsItem);
            recipe.shape("R R", " B ", "   ");
            recipe.setIngredient('R', Material.FIREWORK_STAR); // Validate in PrepareItemCraft Event
            recipe.setIngredient('B', Material.DIAMOND_BOOTS);
            Bukkit.addRecipe(recipe);
        }

        // 6. Wand of Embers Shaped Recipe
        NamespacedKey wandKey = new NamespacedKey(plugin, "recipe_wand_of_embers");
        if (Bukkit.getRecipe(wandKey) == null) {
            ShapedRecipe wandRecipe = new ShapedRecipe(wandKey, wandOfEmbersItem);
            wandRecipe.shape("PFP", "PRP", "PRP");
            wandRecipe.setIngredient('P', Material.BLAZE_POWDER);
            wandRecipe.setIngredient('F', Material.FIRE_CHARGE);
            wandRecipe.setIngredient('R', Material.BLAZE_ROD);
            Bukkit.addRecipe(wandRecipe);
        }

        // 7. Waypoint Compass Shaped Recipe
        NamespacedKey compassKey = new NamespacedKey(plugin, "recipe_waypoint_compass");
        if (Bukkit.getRecipe(compassKey) == null) {
            ShapedRecipe compassRecipe = new ShapedRecipe(compassKey, waypointCompassItem);
            compassRecipe.shape("PEP", "PCP", "PPP");
            compassRecipe.setIngredient('P', Material.ENDER_PEARL);
            compassRecipe.setIngredient('E', Material.ENDER_EYE);
            compassRecipe.setIngredient('C', Material.COMPASS);
            Bukkit.addRecipe(compassRecipe);
        }

        // 8. Iron Grappling Hook Recipe
        NamespacedKey ironGrappleKey = new NamespacedKey(plugin, "recipe_iron_grapple");
        if (Bukkit.getRecipe(ironGrappleKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(ironGrappleKey, ironGrappleItem);
            recipe.shape("IHI", "LLL", "ILI");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('H', Material.TRIPWIRE_HOOK);
            recipe.setIngredient('L', Material.LEAD);
            Bukkit.addRecipe(recipe);
        }

        // 9. Diamond Grappling Hook Recipe
        NamespacedKey diamondGrappleKey = new NamespacedKey(plugin, "recipe_diamond_grapple");
        if (Bukkit.getRecipe(diamondGrappleKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(diamondGrappleKey, diamondGrappleItem);
            recipe.shape("DHD", "LGL", "DLD");
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('H', Material.TRIPWIRE_HOOK);
            recipe.setIngredient('L', Material.LEAD);
            recipe.setIngredient('G', Material.LEAD); // Enforced as Iron Grapple in PrepareItemCraft Event
            Bukkit.addRecipe(recipe);
        }

        // 10. Netherite Grappling Hook Recipe
        NamespacedKey netheriteGrappleKey = new NamespacedKey(plugin, "recipe_netherite_grapple");
        if (Bukkit.getRecipe(netheriteGrappleKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(netheriteGrappleKey, netheriteGrappleItem);
            recipe.shape("NHN", "LGL", "NLN");
            recipe.setIngredient('N', Material.NETHERITE_INGOT);
            recipe.setIngredient('H', Material.TRIPWIRE_HOOK);
            recipe.setIngredient('L', Material.LEAD);
            recipe.setIngredient('G', Material.LEAD); // Enforced as Diamond Grapple in PrepareItemCraft Event
            Bukkit.addRecipe(recipe);
        }
    }

    public void registerCommands(Commands commands) {
        commands.register(Commands.literal("codex")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    giveCodex(player);
                    return 1;
                }).build(), "Receive the Codex guide book", List.of());
    }

    private void giveCodex(Player player) {
        ItemStack codex = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = codex.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "codex");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.displayName(MM.deserialize(G_GOLD + "<bold>" + toSmallCaps("The Codex")));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Right-click to open your guide book.")),
                    MM.deserialize(""),
                    MM.deserialize(C_YELLOW + toSmallCaps("Contains recipes and unlockable utilities."))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            codex.setItemMeta(meta);
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), codex);
        } else {
            player.getInventory().addItem(codex);
        }
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Given the Codex!")));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // ── Structure detection: right-clicking a crafting table block ──────────
        // This is handled in the same handler to avoid double-firing issues.
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            org.bukkit.block.Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.CRAFTING_TABLE) {
                // If holding a custom grappling hook lead, allow the block interaction
                // to proceed normally — but don't treat it as a machine open.
                ItemStack heldItem = event.getItem();
                boolean holdingCustomLead = false;
                if (heldItem != null && heldItem.getType() == Material.LEAD) {
                    ItemMeta heldMeta = heldItem.getItemMeta();
                    if (heldMeta != null) {
                        NamespacedKey hk = new NamespacedKey(plugin, "item_id");
                        holdingCustomLead = heldMeta.getPersistentDataContainer().has(hk, PersistentDataType.STRING);
                    }
                }
                if (!holdingCustomLead) {
                    String structure = detectStructure(clickedBlock);
                    activeMachine.put(player.getUniqueId(), structure);
                    if (structure.equals("arcane_table")) {
                        player.sendActionBar(MM.deserialize(C_PURPLE + toSmallCaps("Using: Arcana Table")));
                        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);
                    } else if (structure.equals("heavy_forge")) {
                        player.sendActionBar(MM.deserialize(C_GOLD + toSmallCaps("Using: Heavy Forge")));
                        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 0.8f, 1.2f);
                    }
                }
            }
        }

        // ── Custom item handling ───────────────────────────────────────────────
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 1. Right-click Codex Book
        NamespacedKey codexKey = new NamespacedKey(plugin, "codex");
        if (meta.getPersistentDataContainer().has(codexKey, PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (event.getAction().name().contains("RIGHT")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
                CodexMainGui.open(player, registry);
            }
            return;
        }

        // 2. Custom Codex Items — only fire on right-click
        NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
        String itemId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        if (itemId == null) return;

        if (!event.getAction().name().contains("RIGHT")) return;

        // Grappling Hook — cancel if targeting a fence post or animal to preserve vanilla lead behaviour
        if (itemId.startsWith("explorer.grappling_hook.")) {
            org.bukkit.block.Block target = event.getClickedBlock();
            if (target != null) {
                String targetName = target.getType().name();
                if (targetName.contains("FENCE") || targetName.contains("WALL")) {
                    // Let vanilla handle leashing to fence posts
                    return;
                }
            }
            // Also abort if the player right-clicked an entity (handled by EntityInteract, not here)

            event.setCancelled(true);
            if (player.hasCooldown(Material.LEAD)) return;

            double range = 10.0;
            if (itemId.endsWith("diamond")) range = 25.0;
            else if (itemId.endsWith("netherite")) range = 50.0;

            // Shoot projectile
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("grapple_range", new FixedMetadataValue(plugin, range));
            snowball.setMetadata("grapple_owner", new FixedMetadataValue(plugin, player.getUniqueId()));

            // Spawn invisible leash ArmorStand as the visual wire anchor
            ArmorStand stand = player.getWorld().spawn(player.getEyeLocation(), ArmorStand.class, s -> {
                s.setInvisible(true);
                s.setMarker(true);
                s.setGravity(false);
                s.setSilent(true);
                s.setPersistent(false);
            });
            stand.setLeashHolder(player);

            player.setCooldown(Material.LEAD, 80); // 4 s cooldown
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.2f);

            final Snowball finalSnowball = snowball;
            final ArmorStand finalStand = stand;
            final double finalRange = range;

            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (!finalSnowball.isValid() || finalSnowball.isDead() || !finalStand.isValid() || !player.isOnline()) {
                    t.cancel();
                    if (finalStand.isValid()) finalStand.remove();
                    return;
                }
                finalStand.teleport(finalSnowball.getLocation());
                if (player.getLocation().distance(finalSnowball.getLocation()) > finalRange) {
                    t.cancel();
                    finalSnowball.remove();
                    if (finalStand.isValid()) finalStand.remove();
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.5f);
                }
            }, 1L, 1L);
            return;
        }

        if (itemId.equals("arcane.wand_of_embers")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.BLAZE_ROD)) return;
            player.launchProjectile(org.bukkit.entity.SmallFireball.class);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            player.setCooldown(Material.BLAZE_ROD, 30); // 1.5 s cooldown
        } else if (itemId.equals("explorer.waypoint_compass")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.COMPASS)) return;
            NamespacedKey linkKey = new NamespacedKey(plugin, "linked_waypoint");
            String linkedWp = meta.getPersistentDataContainer().get(linkKey, PersistentDataType.STRING);
            if (player.isSneaking() || linkedWp == null) {
                openWaypointLinkGui(player);
            } else {
                teleportToWaypoint(player, linkedWp, item);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Only clear the active machine state when the player closes a crafting
        // inventory — NOT when they close a chest, the Codex GUI, etc.
        if (!(event.getPlayer() instanceof Player player)) return;
        org.bukkit.event.inventory.InventoryType type = event.getInventory().getType();
        if (type == org.bukkit.event.inventory.InventoryType.WORKBENCH
                || type == org.bukkit.event.inventory.InventoryType.CRAFTING) {
            activeMachine.remove(player.getUniqueId());
        }
    }

    private String detectStructure(org.bukkit.block.Block craftingTable) {
        // 1. Heavy Forge: Crafting Table on top of Blast Furnace
        org.bukkit.block.Block below = craftingTable.getRelative(org.bukkit.block.BlockFace.DOWN);
        if (below.getType() == Material.BLAST_FURNACE) {
            return "heavy_forge";
        }

        // 2. Arcana Table: Crafting Table horizontally adjacent to at least 2 Bookshelves
        int bookshelves = 0;
        org.bukkit.block.BlockFace[] faces = {
                org.bukkit.block.BlockFace.NORTH,
                org.bukkit.block.BlockFace.SOUTH,
                org.bukkit.block.BlockFace.EAST,
                org.bukkit.block.BlockFace.WEST
        };
        for (org.bukkit.block.BlockFace face : faces) {
            if (craftingTable.getRelative(face).getType() == Material.BOOKSHELF) {
                bookshelves++;
            }
        }
        if (bookshelves >= 2) {
            return "arcane_table";
        }

        return "none";
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!snowball.hasMetadata("grapple_range")) return;

        Player player = (Player) snowball.getShooter();
        if (player == null || !player.isOnline()) return;

        // Visual pull
        Location hitLoc = event.getHitBlock() != null
                ? event.getHitBlock().getLocation().add(0.5, 0.5, 0.5)
                : (event.getHitEntity() != null ? event.getHitEntity().getLocation() : null);

        if (hitLoc != null) {
            // Apply pull
            org.bukkit.util.Vector vector = hitLoc.toVector().subtract(player.getLocation().toVector());
            double distance = vector.length();
            if (distance > 1) {
                org.bukkit.util.Vector velocity = vector.normalize().multiply(1.5);
                velocity.setY(Math.min(0.8, vector.getY() * 0.1 + 0.55));
                player.setVelocity(velocity);
                player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 0.8f, 1.3f);
            }
        }
        snowball.remove();
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();

        if (mainHand == null || mainHand.getType() != Material.FIREWORK_STAR) return;
        if (offHand == null || offHand.getType() == Material.AIR) return;

        ItemMeta mainMeta = mainHand.getItemMeta();
        if (mainMeta == null) return;

        NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
        String itemId = mainMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        if (itemId == null) return;

        if (itemId.equals("arcane.lifesteal_rune") || itemId.equals("arcane.speed_rune")) {
            event.setCancelled(true);

            NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");
            String runeType = mainMeta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            if (runeType == null) return;

            boolean valid = false;
            if (runeType.equals("WEAPON_SWORD")) {
                valid = offHand.getType().name().contains("SWORD");
            } else if (runeType.equals("ARMOR_BOOTS")) {
                valid = offHand.getType().name().contains("BOOTS");
            }

            if (!valid) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This rune cannot be applied to this item!")));
                return;
            }

            ItemMeta offMeta = offHand.getItemMeta();
            if (offMeta == null) return;

            NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
            String effect = mainMeta.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
            NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
            Integer lvl = mainMeta.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
            if (effect == null || lvl == null) return;

            NamespacedKey applyKey = new NamespacedKey(plugin, "rune_" + effect);
            offMeta.getPersistentDataContainer().set(applyKey, PersistentDataType.INTEGER, lvl);

            List<net.kyori.adventure.text.Component> lore = offMeta.lore();
            if (lore == null) lore = new java.util.ArrayList<>();

            String displayEffectName = effect.equalsIgnoreCase("lifesteal") ? "Lifesteal I" : "Speed I";
            net.kyori.adventure.text.Component runeLoreComponent = MM.deserialize(C_PURPLE + toSmallCaps(displayEffectName) + " " + C_GRAY + toSmallCaps("(Applied)"));

            boolean found = false;
            for (int i = 0; i < lore.size(); i++) {
                String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(lore.get(i));
                if (plain.contains(toSmallCaps(displayEffectName))) {
                    lore.set(i, runeLoreComponent);
                    found = true;
                    break;
                }
            }
            if (!found) {
                lore.add(MM.deserialize(""));
                lore.add(runeLoreComponent);
            }
            offMeta.lore(lore);
            offHand.setItemMeta(offMeta);

            // Consume 1 rune
            mainHand.setAmount(mainHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(mainHand);

            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
            player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 30, 0.5, 1, 0.5);
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully applied ") + C_PURPLE + toSmallCaps(displayEffectName) + C_GREEN + toSmallCaps(" to your item!")));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack sword = player.getInventory().getItemInMainHand();
        if (sword == null || sword.getType() == Material.AIR) return;

        ItemMeta meta = sword.getItemMeta();
        if (meta == null) return;

        NamespacedKey applyKey = new NamespacedKey(plugin, "rune_lifesteal");
        Integer lvl = meta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);
        if (lvl == null) return;

        double damage = event.getFinalDamage();
        double healAmount = damage * 0.15 * lvl;

        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));

        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1.2, 0), 4, 0.2, 0.2, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f);
    }

    private void updateSpeedRunes(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            ItemMeta meta = boots.getItemMeta();
            if (meta != null) {
                NamespacedKey applyKey = new NamespacedKey(plugin, "rune_speed");
                if (meta.getPersistentDataContainer().has(applyKey, PersistentDataType.INTEGER)) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SPEED,
                            25,
                            0,
                            true,
                            false,
                            true
                    ));
                }
            }
        }
    }

    private void openWaypointLinkGui(Player player) {
        PlayerSettings settings = dataManager.get(player.getUniqueId());
        if (settings == null || settings.waypoints.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You have no waypoints! Create one first using /wp create <name>")));
            return;
        }

        WaypointLinkInventoryHolder linkHolder = new WaypointLinkInventoryHolder();
        int size = Math.min(54, Math.max(9, ((settings.waypoints.size() - 1) / 9 + 1) * 9));
        Inventory inv = Bukkit.createInventory(linkHolder, size, MM.deserialize("<dark_gray>» " + G_GOLD + toSmallCaps("Link Waypoint")));
        linkHolder.setInventory(inv);

        ItemStack glass = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(MM.deserialize(" "));
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < size; i++) {
            inv.setItem(i, glass);
        }

        int slot = 0;
        for (String wpName : settings.waypoints.keySet()) {
            ItemStack wpItem = new ItemStack(Material.MAP);
            ItemMeta wpMeta = wpItem.getItemMeta();
            if (wpMeta != null) {
                wpMeta.displayName(MM.deserialize(C_ORANGE + toSmallCaps(wpName)));
                wpMeta.lore(List.of(
                        MM.deserialize(""),
                        MM.deserialize(C_GRAY + toSmallCaps("Click to link this waypoint")),
                        MM.deserialize(C_GRAY + toSmallCaps("to your compass."))
                ));
                wpItem.setItemMeta(wpMeta);
            }
            inv.setItem(slot, wpItem);
            linkHolder.getSlotMap().put(slot, wpName);
            slot++;
            if (slot >= size) break;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
        player.openInventory(inv);
    }

    private void teleportToWaypoint(Player player, String waypointName, ItemStack compass) {
        PlayerSettings settings = dataManager.get(player.getUniqueId());
        Location loc = null;
        if (settings != null) {
            loc = settings.waypoints.get(waypointName);
        }

        if (loc == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Linked waypoint '") + waypointName + toSmallCaps("' no longer exists!")));

            ItemMeta meta = compass.getItemMeta();
            if (meta != null) {
                NamespacedKey linkKey = new NamespacedKey(plugin, "linked_waypoint");
                meta.getPersistentDataContainer().remove(linkKey);
                meta.displayName(MM.deserialize(G_GOLD + toSmallCaps("Waypoint Compass")));
                meta.lore(List.of(
                        MM.deserialize(C_GRAY + toSmallCaps("A celestial compass that aligns with waypoints.")),
                        MM.deserialize(""),
                        MM.deserialize(C_YELLOW + toSmallCaps("Right-click to teleport to the linked waypoint.")),
                        MM.deserialize(C_GRAY + toSmallCaps("Shift-Right-click to link a waypoint."))
                ));
                compass.setItemMeta(meta);
            }
            return;
        }

        Location before = player.getLocation();
        player.teleport(loc);

        before.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, before, 50, 0.5, 1, 0.5);
        loc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, loc, 50, 0.5, 1, 0.5);

        before.getWorld().playSound(before, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        player.setCooldown(Material.COMPASS, 200);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Teleported to waypoint: ") + C_ORANGE + waypointName));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (itemId == null) return;

        if (event.getView().getPlayer() instanceof Player player) {
            // 1. Verify Player Unlocks
            if (!manager.isUnlocked(player.getUniqueId(), itemId)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }

            // 2. Verify Placed Machine Structure requirements
            String structure = activeMachine.getOrDefault(player.getUniqueId(), "none");
            boolean tableRequired = itemId.equals("arcane.wand_of_embers") || 
                                    itemId.equals("arcane.lifesteal_rune") || 
                                    itemId.equals("arcane.speed_rune") || 
                                    itemId.equals("arcane.speed_boots");
            boolean forgeRequired = itemId.equals("explorer.waypoint_compass") || 
                                    itemId.startsWith("explorer.grappling_hook.");

            if (tableRequired && !structure.equals("arcane_table")) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                player.sendActionBar(MM.deserialize(C_RED + toSmallCaps("Requires: Arcana Table")));
                return;
            }

            if (forgeRequired && !structure.equals("heavy_forge")) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                player.sendActionBar(MM.deserialize(C_RED + toSmallCaps("Requires: Heavy Forge")));
                return;
            }

            // 3. Verify Custom Ingredients (blocking vanilla equivalents)
            if (itemId.equals("arcane.speed_boots")) {
                ItemStack[] matrix = event.getInventory().getMatrix();
                if (!isCustomItem(matrix[0], "arcane.speed_rune") || !isCustomItem(matrix[2], "arcane.speed_rune")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            } else if (itemId.equals("explorer.grappling_hook.diamond")) {
                ItemStack[] matrix = event.getInventory().getMatrix();
                // slot 4 is the middle slot in a 3x3 grid
                if (!isCustomItem(matrix[4], "explorer.grappling_hook.iron")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            } else if (itemId.equals("explorer.grappling_hook.netherite")) {
                ItemStack[] matrix = event.getInventory().getMatrix();
                if (!isCustomItem(matrix[4], "explorer.grappling_hook.diamond")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }

    private boolean isCustomItem(ItemStack item, String expectedId) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return expectedId.equals(id);
    }
}
