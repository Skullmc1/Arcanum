package space.qclid.dashboard.codex;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.codex.gui.CodexGuiListener;
import space.qclid.dashboard.codex.gui.CodexMainGui;
import space.qclid.dashboard.data.DataManager;
import space.qclid.dashboard.feature.NewGadgetsFeature;
import static space.qclid.dashboard.util.CodexUtil.setMetadata;

import space.qclid.dashboard.codex.core.*;
import space.qclid.dashboard.codex.items.*;
import space.qclid.dashboard.codex.crafting.*;
import space.qclid.dashboard.codex.listeners.*;
import space.qclid.dashboard.codex.tasks.*;

import java.util.List;
import java.util.Map;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexFeature {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;

    // Modular handlers
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final NewGadgetsFeature newGadgets;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;
    private final CodexCombatListener codexCombatListener;
    private final CodexInteractionListener codexInteractionListener;
    private final CodexPlayerListener codexPlayerListener;
    private final CodexRuneListener codexRuneListener;
    private final java.util.Map<java.util.UUID, String> activeMachine = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, org.bukkit.inventory.ItemStack[]> lastVoidedItems = new java.util.HashMap<>();

    public CodexFeature(JavaPlugin plugin, DataManager dataManager, NewGadgetsFeature newGadgets) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = new CodexManager(plugin);
        this.registry = new CodexRegistry();

        // Initialize sub-components
        this.arcaneItems = new ArcaneItems(plugin);
        this.explorerItems = new ExplorerItems(plugin);
        this.newGadgets = newGadgets;
        this.codexCrafting = new CodexCrafting(plugin, manager, registry, arcaneItems);
        this.codexPassiveTask = new CodexPassiveTask(plugin, arcaneItems);
        
        // Register new gadget items with ExplorerItems for getCustomItem lookup
        explorerItems.registerExternalItem("explorer.gadgets.echo_locator", newGadgets.echoLocator);
        explorerItems.registerExternalItem("explorer.gadgets.void_chisel", newGadgets.voidChisel);
        explorerItems.registerExternalItem("explorer.gadgets.gravity_well", newGadgets.gravityWell);
        explorerItems.registerExternalItem("explorer.gadgets.seismograph", newGadgets.seismograph);
        explorerItems.registerExternalItem("explorer.gadgets.spectral_crossing", newGadgets.spectralCrossing);
        explorerItems.registerExternalItem("explorer.gadgets.holographic_decoy", newGadgets.holographicDecoy);
        explorerItems.registerExternalItem("explorer.gadgets.null_spear", newGadgets.nullSpear);
        explorerItems.registerExternalItem("explorer.gadgets.wormhole", newGadgets.wormhole);

        CodexContext ctx = new CodexContext(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask, activeMachine, lastVoidedItems);
        
        this.codexCombatListener = new CodexCombatListener(ctx);
        this.codexInteractionListener = new CodexInteractionListener(ctx);
        this.codexPlayerListener = new CodexPlayerListener(ctx);
        this.codexRuneListener = new CodexRuneListener(ctx);

        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(codexCombatListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(codexInteractionListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(codexPlayerListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(codexRuneListener, plugin);

        // Initialize GUIs listener
        new CodexGuiListener(plugin, registry, manager);

        initRegistry();

        // Run passive Speed Boots, Geode, Cloak, Strider, and Spelunker check every 10 ticks (0.5s)
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            codexPassiveTask.runTick();
        }, 1L, 10L);

        // Run virtual furnace ticking every 20 ticks (1s)
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            codexPassiveTask.runVirtualFurnaceTick();
        }, 1L, 20L);
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

        CodexCategory arcaneRunesNormal = new CodexCategory("arcane.runes.normal", "arcane", "Normal Runes", new ItemStack(Material.PRIZE_POTTERY_SHERD));
        CodexCategory arcaneRunesDemonic = new CodexCategory("arcane.runes.demonic", "arcane", "Demonic Runes", new ItemStack(Material.GUSTER_BANNER_PATTERN));
        CodexCategory arcaneRunesHoly = new CodexCategory("arcane.runes.holy", "arcane", "Holy Runes", new ItemStack(Material.PRISMARINE_SHARD));
        
        CodexCategory arcaneArmor = new CodexCategory("arcane.armor", "arcane", "Armor", new ItemStack(Material.DIAMOND_CHESTPLATE));
        CodexCategory arcaneMelee = new CodexCategory("arcane.melee", "arcane", "Melee Weapons", new ItemStack(Material.DIAMOND_SWORD));
        CodexCategory arcaneRanged = new CodexCategory("arcane.ranged", "arcane", "Ranged Weapons", new ItemStack(Material.BLAZE_ROD));
        CodexCategory arcaneBoosts = new CodexCategory("arcane.boosts", "arcane", "Arcana Boosts", new ItemStack(Material.POTION));
        CodexCategory arcaneIngredients = new CodexCategory("arcane.ingredients", "arcane", "Ingredients", new ItemStack(Material.NETHER_WART));
        CodexCategory arcaneMaterials = new CodexCategory("arcane.materials", "arcane", "Materials", new ItemStack(Material.ENDER_PEARL));
        CodexCategory arcaneTrinkets = new CodexCategory("arcane.trinkets", "arcane", "Trinkets", new ItemStack(Material.TOTEM_OF_UNDYING));
        
        registry.registerCategory(arcaneRunesNormal);
        registry.registerCategory(arcaneRunesDemonic);
        registry.registerCategory(arcaneRunesHoly);
        registry.registerCategory(arcaneArmor);
        registry.registerCategory(arcaneMelee);
        registry.registerCategory(arcaneRanged);
        registry.registerCategory(arcaneBoosts);
        registry.registerCategory(arcaneIngredients);
        registry.registerCategory(arcaneMaterials);
        registry.registerCategory(arcaneTrinkets);

        CodexCategory explorerNavigation = new CodexCategory("explorer.navigation", "explorer", "Navigation", new ItemStack(Material.COMPASS));
        CodexCategory explorerExploration = new CodexCategory("explorer.exploration", "explorer", "Exploration", new ItemStack(Material.SPYGLASS));
        CodexCategory explorerGadgets = new CodexCategory("explorer.gadgets", "explorer", "Gadgets", new ItemStack(Material.LEAD));
        CodexCategory explorerTools = new CodexCategory("explorer.tools", "explorer", "Tools", new ItemStack(Material.IRON_PICKAXE));
        CodexCategory explorerArmor = new CodexCategory("explorer.armor", "explorer", "Armor", new ItemStack(Material.IRON_CHESTPLATE));
        
        CodexCategory explorerRunesNormal = new CodexCategory("explorer.runes.normal", "explorer", "Normal Runes", new ItemStack(Material.PRIZE_POTTERY_SHERD));
        CodexCategory explorerRunesDemonic = new CodexCategory("explorer.runes.demonic", "explorer", "Demonic Runes", new ItemStack(Material.GUSTER_BANNER_PATTERN));
        CodexCategory explorerRunesHoly = new CodexCategory("explorer.runes.holy", "explorer", "Holy Runes", new ItemStack(Material.PRISMARINE_SHARD));
        
        registry.registerCategory(explorerNavigation);
        registry.registerCategory(explorerExploration);
        registry.registerCategory(explorerGadgets);
        registry.registerCategory(explorerTools);
        registry.registerCategory(explorerArmor);
        registry.registerCategory(explorerRunesNormal);
        registry.registerCategory(explorerRunesDemonic);
        registry.registerCategory(explorerRunesHoly);

        initItemsPart1(machineryStations, arcaneRunesNormal, arcaneArmor, arcaneRanged, explorerNavigation, explorerGadgets, arcaneMaterials, arcaneTrinkets, arcaneRunesDemonic, arcaneMelee, explorerArmor, explorerTools, explorerExploration, arcaneBoosts, arcaneIngredients, arcaneRunesHoly, explorerRunesNormal, explorerRunesDemonic, explorerRunesHoly);
        initItemsPart2(machineryStations, arcaneRunesNormal, arcaneArmor, arcaneRanged, explorerNavigation, explorerGadgets, arcaneMaterials, arcaneTrinkets, arcaneRunesDemonic, arcaneMelee, explorerArmor, explorerTools, explorerExploration, arcaneBoosts, arcaneIngredients, arcaneRunesHoly, explorerRunesNormal, explorerRunesDemonic, explorerRunesHoly);
        initItemsPart3(machineryStations, arcaneRunesNormal, arcaneArmor, arcaneRanged, explorerNavigation, explorerGadgets, arcaneMaterials, arcaneTrinkets, arcaneRunesDemonic, arcaneMelee, explorerArmor, explorerTools, explorerExploration, arcaneBoosts, arcaneIngredients, arcaneRunesHoly, explorerRunesNormal, explorerRunesDemonic, explorerRunesHoly);
        initItemsPart4(machineryStations, arcaneRunesNormal, arcaneArmor, arcaneRanged, explorerNavigation, explorerGadgets, arcaneMaterials, arcaneTrinkets, arcaneRunesDemonic, arcaneMelee, explorerArmor, explorerTools, explorerExploration, arcaneBoosts, arcaneIngredients, arcaneRunesHoly, explorerRunesNormal, explorerRunesDemonic, explorerRunesHoly);

        newGadgets.registerInCodex(explorerGadgets, registry);

        registerRecipes();
    }

    private void initItemsPart1(CodexCategory machineryStations, CodexCategory arcaneRunesNormal, CodexCategory arcaneArmor, CodexCategory arcaneRanged, CodexCategory explorerNavigation, CodexCategory explorerGadgets, CodexCategory arcaneMaterials, CodexCategory arcaneTrinkets, CodexCategory arcaneRunesDemonic, CodexCategory arcaneMelee, CodexCategory explorerArmor, CodexCategory explorerTools, CodexCategory explorerExploration, CodexCategory arcaneBoosts, CodexCategory arcaneIngredients, CodexCategory arcaneRunesHoly, CodexCategory explorerRunesNormal, CodexCategory explorerRunesDemonic, CodexCategory explorerRunesHoly) {
        // 1. Arcana Table
        CodexItem arcanaTableCodex = new CodexItem.Builder("machinery.arcana_table")
                .displayName("Arcana Table")
                .displayItem(arcaneItems.arcanaTableItem)
                .xpCost(3)
                .requires(new ItemStack(Material.CRAFTING_TABLE, 1))
                .requires(new ItemStack(Material.BOOKSHELF, 2))
                .description("Structure: A Crafting Table directly on top of a Dropper with Bookshelves on the sides of the Dropper.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.CRAFTING_TABLE), null,
                        new ItemStack(Material.BOOKSHELF), new ItemStack(Material.DROPPER), new ItemStack(Material.BOOKSHELF),
                        null, null, null
                )
                .build();
        machineryStations.addItem(arcanaTableCodex);

        // 2. Heavy Forge
        CodexItem heavyForgeCodex = new CodexItem.Builder("machinery.heavy_forge")
                .displayName("Heavy Forge")
                .displayItem(explorerItems.heavyForgeItem)
                .xpCost(3)
                .requires(new ItemStack(Material.CRAFTING_TABLE, 1))
                .requires(new ItemStack(Material.BLAST_FURNACE, 1))
                .description("Structure: A Crafting Table on top of a Blast Furnace on top of a Dropper.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.CRAFTING_TABLE), null,
                        null, new ItemStack(Material.BLAST_FURNACE), null,
                        null, new ItemStack(Material.DROPPER), null
                )
                .build();
        machineryStations.addItem(heavyForgeCodex);

        // 3. Upgrade Table
        CodexItem upgradeTableCodex = new CodexItem.Builder("machinery.upgrade_table")
                .displayName("Upgrade Table")
                .displayItem(arcaneItems.upgradeTableItem)
                .xpCost(3)
                .requires(new ItemStack(Material.ANVIL, 1))
                .requires(new ItemStack(Material.BOOKSHELF, 1))
                .description("Structure: An Anvil on top of a Dropper on top of a Bookshelf.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.ANVIL), null,
                        null, new ItemStack(Material.DROPPER), null,
                        null, new ItemStack(Material.BOOKSHELF), null
                )
                .build();
        machineryStations.addItem(upgradeTableCodex);

        // Block Duplicator
        CodexItem blockDuplicatorCodex = new CodexItem.Builder("machinery.block_duplicator")
                .displayName("Block Duplicator")
                .displayItem(explorerItems.blockDuplicatorItem)
                .xpCost(15)
                .requires(new ItemStack(Material.NETHER_BRICK_FENCE, 1))
                .requires(new ItemStack(Material.DROPPER, 1))
                .requires(new ItemStack(Material.FURNACE, 2))
                .requires(new ItemStack(Material.BLAST_FURNACE, 1))
                .description("Structure: Nether Brick Fence on top of a Dropper surrounded horizontally by Furnaces/Blast Furnaces. Duplicates building blocks placed in the dropper using Lava Buckets placed in the furnaces/blast furnaces (1 bucket per 2 stacks).")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.NETHER_BRICK_FENCE), null,
                        new ItemStack(Material.FURNACE), new ItemStack(Material.DROPPER), new ItemStack(Material.BLAST_FURNACE),
                        null, new ItemStack(Material.FURNACE), null
                )
                .build();
        machineryStations.addItem(blockDuplicatorCodex);

        // Blessings Altar
        CodexItem blessingsAltar = new CodexItem.Builder("machinery.blessings_altar")
                .displayName("Blessings Altar")
                .displayItem(arcaneItems.getCustomItem("machinery.blessings_altar"))
                .xpCost(5)
                .requires(new ItemStack(Material.GOLD_BLOCK, 9))
                .description("Structure: 3x3 Gold Block platform surrounded by a ring of Quartz/Endstone, with a Dropper and Fence on one side.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.DROPPER), new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.OAK_FENCE), new ItemStack(Material.GOLD_BLOCK)
                )
                .build();
        machineryStations.addItem(blessingsAltar);

        // Heavy Alloy Forge
        CodexItem heavyAlloyForge = new CodexItem.Builder("machinery.heavy_alloy_forge")
                .displayName("Heavy Alloy Forge")
                .displayItem(arcaneItems.getCustomItem("machinery.heavy_alloy_forge"))
                .xpCost(5)
                .requires(new ItemStack(Material.BLAST_FURNACE, 1))
                .description("Structure: Blast Furnace on top of a Dropper on top of a 3x3 Magma Block base.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.BLAST_FURNACE), null,
                        null, new ItemStack(Material.DROPPER), null,
                        new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK)
                )
                .build();
        machineryStations.addItem(heavyAlloyForge);

        // Enchanter
        CodexItem enchanter = new CodexItem.Builder("machinery.enchanter")
                .displayName("Enchanter")
                .displayItem(arcaneItems.getCustomItem("machinery.enchanter"))
                .xpCost(5)
                .requires(new ItemStack(Material.ENCHANTING_TABLE, 1))
                .description("Structure: Enchanting Table on top of a 3x3 grid of Diamond Blocks.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.ENCHANTING_TABLE), null,
                        new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.DIAMOND_BLOCK),
                        new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.DIAMOND_BLOCK)
                )
                .build();
        machineryStations.addItem(enchanter);

        // Disenchanter
        CodexItem disenchanter = new CodexItem.Builder("machinery.disenchanter")
                .displayName("Disenchanter")
                .displayItem(arcaneItems.getCustomItem("machinery.disenchanter"))
                .xpCost(5)
                .requires(new ItemStack(Material.ENCHANTING_TABLE, 1))
                .description("Structure: Enchanting Table on top of a 3x3 grid of Iron Blocks.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.ENCHANTING_TABLE), null,
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK),
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK)
                )
                .build();
        machineryStations.addItem(disenchanter);

        // Kinetic Crusher
        CodexItem kineticCrusher = new CodexItem.Builder("machinery.kinetic_crusher")
                .displayName("Kinetic Crusher")
                .displayItem(arcaneItems.getCustomItem("machinery.kinetic_crusher"))
                .xpCost(5)
                .requires(new ItemStack(Material.PISTON, 1))
                .description("Structure: Piston on top of Hopper on top of a 3x3 grid of Iron Blocks.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.PISTON), null,
                        null, new ItemStack(Material.HOPPER), null,
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_BLOCK)
                )
                .build();
        machineryStations.addItem(kineticCrusher);

        // Sifting Trommel
        CodexItem siftingTrommel = new CodexItem.Builder("machinery.sifting_trommel")
                .displayName("Sifting Trommel")
                .displayItem(arcaneItems.getCustomItem("machinery.sifting_trommel"))
                .xpCost(5)
                .requires(new ItemStack(Material.IRON_BARS, 1))
                .description("Structure: Iron Bars surrounded by 4 Copper Blocks on top of a Hopper.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        new ItemStack(Material.COPPER_BLOCK), new ItemStack(Material.IRON_BARS), new ItemStack(Material.COPPER_BLOCK),
                        new ItemStack(Material.COPPER_BLOCK), new ItemStack(Material.HOPPER), new ItemStack(Material.COPPER_BLOCK),
                        null, null, null
                )
                .build();
        machineryStations.addItem(siftingTrommel);

        // 4. Vampiric Bleed Rune I
        CodexItem lifestealRuneCodex = new CodexItem.Builder("arcane.lifesteal_rune")
                .displayName("Vampiric Bleed Rune I")
                .displayItem(arcaneItems.lifestealRuneItem)
                .xpCost(8)
                .requires(new ItemStack(Material.FIREWORK_STAR, 1))
                .requires(new ItemStack(Material.DIAMOND, 1))
                .description("Apply to sword. Heals you on hit.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK),
                        new ItemStack(Material.IRON_SWORD), arcaneItems.bloodItem, new ItemStack(Material.DIAMOND_SWORD),
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(lifestealRuneCodex);

        // Vampiric Bleed Rune II
        ItemStack lifestealRune2Item = arcaneItems.createLifestealRuneOfLevel(2);
        CodexItem lifestealRune2Codex = new CodexItem.Builder("arcane.lifesteal_rune_2")
                .displayName("Vampiric Bleed Rune II")
                .displayItem(lifestealRune2Item)
                .xpCost(12)
                .requires(arcaneItems.lifestealRuneItem)
                .description("Upgraded Vampiric Bleed Rune. Heals you on hit (increased effect).")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK),
                        new ItemStack(Material.IRON_SWORD), arcaneItems.bloodItem, new ItemStack(Material.DIAMOND_SWORD),
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(lifestealRune2Codex);

        // Vampiric Bleed Rune III
        ItemStack lifestealRune3Item = arcaneItems.createLifestealRuneOfLevel(3);
        CodexItem lifestealRune3Codex = new CodexItem.Builder("arcane.lifesteal_rune_3")
                .displayName("Vampiric Bleed Rune III")
                .displayItem(lifestealRune3Item)
                .xpCost(16)
                .requires(lifestealRune2Item)
                .description("Maximum level Vampiric Bleed Rune.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK),
                        new ItemStack(Material.IRON_SWORD), arcaneItems.bloodItem, new ItemStack(Material.DIAMOND_SWORD),
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(lifestealRune3Codex);

        // 5. Speed Rune I
        CodexItem speedRuneCodex = new CodexItem.Builder("arcane.speed_rune")
                .displayName("Speed Rune I")
                .displayItem(arcaneItems.speedRuneItem)
                .xpCost(8)
                .requires(new ItemStack(Material.FIREWORK_STAR, 1))
                .requires(new ItemStack(Material.SUGAR, 4))
                .description("Apply to boots. Grants Speed I when worn.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK),
                        new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.RABBIT_FOOT), new ItemStack(Material.DIAMOND_BOOTS),
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(speedRuneCodex);

        // Speed Rune II
        ItemStack speedRune2Item = arcaneItems.createSpeedRuneOfLevel(2);
        CodexItem speedRune2Codex = new CodexItem.Builder("arcane.speed_rune_2")
                .displayName("Speed Rune II")
                .displayItem(speedRune2Item)
                .xpCost(12)
                .requires(arcaneItems.speedRuneItem)
                .description("Upgraded Speed Rune. Grants Speed II when worn.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK),
                        new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.RABBIT_FOOT), new ItemStack(Material.DIAMOND_BOOTS),
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(speedRune2Codex);

        // Speed Rune III
        ItemStack speedRune3Item = arcaneItems.createSpeedRuneOfLevel(3);
        CodexItem speedRune3Codex = new CodexItem.Builder("arcane.speed_rune_3")
                .displayName("Speed Rune III")
                .displayItem(speedRune3Item)
                .xpCost(16)
                .requires(speedRune2Item)
                .description("Maximum level Speed Rune. Grants Speed III when worn.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK),
                        new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.RABBIT_FOOT), new ItemStack(Material.DIAMOND_BOOTS),
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(speedRune3Codex);

        // 6. Speed Boots
        CodexItem speedBootsCodex = new CodexItem.Builder("arcane.speed_boots")
                .displayName("Speed Boots")
                .displayItem(arcaneItems.speedBootsItem)
                .xpCost(12)
                .requires(new ItemStack(Material.DIAMOND_BOOTS, 1))
                .requires(arcaneItems.speedRuneItem)
                .description("Diamond boots pre-infused with the Speed Rune.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        arcaneItems.speedRuneItem, null, arcaneItems.speedRuneItem,
                        null, new ItemStack(Material.DIAMOND_BOOTS), null,
                        null, null, null
                )
                .build();
        arcaneArmor.addItem(speedBootsCodex);

        // 7. Wand of Embers
        CodexItem wandItem = new CodexItem.Builder("arcane.wand_of_embers")
                .displayName("Wand of Embers")
                .displayItem(arcaneItems.wandOfEmbersItem)
                .xpCost(5)
                .requires(new ItemStack(Material.BLAZE_ROD, 1))
                .description("A magical wand that shoots fireballs when right-clicked.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.FIRE_CHARGE), new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.BLAZE_ROD),    new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.BLAZE_ROD),    new ItemStack(Material.BLAZE_POWDER)
                )
                .build();
        arcaneRanged.addItem(wandItem);

        // 8. Waypoint Compass
        CodexItem compassItem = new CodexItem.Builder("explorer.waypoint_compass")
                .displayName("Waypoint Compass")
                .displayItem(explorerItems.waypointCompassItem)
                .xpCost(10)
                .requires(new ItemStack(Material.COMPASS, 1))
                .requires(new ItemStack(Material.ENDER_PEARL, 8))
                .description("A celestial compass that teleports you to its linked waypoint.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_EYE), new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.COMPASS),      new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL),  new ItemStack(Material.ENDER_PEARL)
                )
                .build();
        explorerNavigation.addItem(compassItem);

        // 9. Iron Grappling Hook
        CodexItem ironGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.iron")
                .displayName("Iron Grappling Hook")
                .displayItem(explorerItems.ironGrappleItem)
                .xpCost(6)
                .requires(new ItemStack(Material.LEAD, 1))
                .requires(new ItemStack(Material.TRIPWIRE_HOOK, 2))
                .description("Grappling hook with a 10-block range.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.LEAD), new ItemStack(Material.LEAD), new ItemStack(Material.LEAD),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.LEAD), new ItemStack(Material.IRON_INGOT)
                )
                .build();
        explorerGadgets.addItem(ironGrappleCodex);

        // 10. Diamond Grappling Hook
        CodexItem diamondGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.diamond")
                .displayName("Diamond Grappling Hook")
                .displayItem(explorerItems.diamondGrappleItem)
                .xpCost(12)
                .requires(explorerItems.ironGrappleItem)
                .requires(new ItemStack(Material.DIAMOND, 4))
                .description("Grappling hook with a 25-block range.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.DIAMOND), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.DIAMOND),
                        new ItemStack(Material.LEAD), explorerItems.ironGrappleItem, new ItemStack(Material.LEAD),
                        new ItemStack(Material.DIAMOND), new ItemStack(Material.LEAD), new ItemStack(Material.DIAMOND)
                )
                .build();
        explorerGadgets.addItem(diamondGrappleCodex);

        // 11. Netherite Grappling Hook
        CodexItem netheriteGrappleCodex = new CodexItem.Builder("explorer.grappling_hook.netherite")
                .displayName("Netherite Grappling Hook")
                .displayItem(explorerItems.netheriteGrappleItem)
                .xpCost(20)
                .requires(explorerItems.diamondGrappleItem)
                .requires(new ItemStack(Material.NETHERITE_INGOT, 4))
                .description("Grappling hook with a 50-block range.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.TRIPWIRE_HOOK), new ItemStack(Material.NETHERITE_INGOT),
                        new ItemStack(Material.LEAD), explorerItems.diamondGrappleItem, new ItemStack(Material.LEAD),
                        new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.LEAD), new ItemStack(Material.NETHERITE_INGOT)
                )
                .build();
        explorerGadgets.addItem(netheriteGrappleCodex);

        // 12. Ender Essence
        CodexItem enderEssenceCodex = new CodexItem.Builder("arcane.materials.ender_essence")
                .displayName("Ender Essence")
                .displayItem(arcaneItems.enderEssence)
                .xpCost(5)
                .requires(new ItemStack(Material.ENDER_PEARL, 9))
                .description("Condensed ender magic used for advanced arcane crafting.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL)
                )
                .build();
        arcaneMaterials.addItem(enderEssenceCodex);

        // 13. Hardened Coal I
        CodexItem hc1 = new CodexItem.Builder("arcane.materials.hardened_coal_1")
                .displayName("Hardened Coal I")
                .displayItem(arcaneItems.hardenedCoal1)
                .xpCost(2)
                .requires(new ItemStack(Material.COAL, 9))
                .description("Pressed coal reagent.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COAL), new ItemStack(Material.COAL), new ItemStack(Material.COAL),
                        new ItemStack(Material.COAL), new ItemStack(Material.COAL), new ItemStack(Material.COAL),
                        new ItemStack(Material.COAL), new ItemStack(Material.COAL), new ItemStack(Material.COAL)
                )
                .build();
        arcaneMaterials.addItem(hc1);

        // 14. Hardened Coal II
        CodexItem hc2 = new CodexItem.Builder("arcane.materials.hardened_coal_2")
                .displayName("Hardened Coal II")
                .displayItem(arcaneItems.hardenedCoal2)
                .xpCost(4)
                .requires(arcaneItems.hardenedCoal1)
                .description("Highly pressed coal reagent.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1,
                        arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1,
                        arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1, arcaneItems.hardenedCoal1
                )
                .build();
        arcaneMaterials.addItem(hc2);

        // 15. Hardened Coal Max
        CodexItem hcMax = new CodexItem.Builder("arcane.materials.hardened_coal_max")
                .displayName("Hardened Coal Max")
                .displayItem(arcaneItems.hardenedCoalMax)
                .xpCost(6)
                .requires(arcaneItems.hardenedCoal2)
                .description("Fully crystallized coal reagent ready for diamond forging.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2,
                        arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2,
                        arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2, arcaneItems.hardenedCoal2
                )
                .build();
        arcaneMaterials.addItem(hcMax);

    }

    private void initItemsPart2(CodexCategory machineryStations, CodexCategory arcaneRunesNormal, CodexCategory arcaneArmor, CodexCategory arcaneRanged, CodexCategory explorerNavigation, CodexCategory explorerGadgets, CodexCategory arcaneMaterials, CodexCategory arcaneTrinkets, CodexCategory arcaneRunesDemonic, CodexCategory arcaneMelee, CodexCategory explorerArmor, CodexCategory explorerTools, CodexCategory explorerExploration, CodexCategory arcaneBoosts, CodexCategory arcaneIngredients, CodexCategory arcaneRunesHoly, CodexCategory explorerRunesNormal, CodexCategory explorerRunesDemonic, CodexCategory explorerRunesHoly) {

        // 16. Hardened Base I
        CodexItem hb1 = new CodexItem.Builder("arcane.materials.hardened_base_1")
                .displayName("Hardened Base I")
                .displayItem(arcaneItems.hardenedBase1)
                .xpCost(2)
                .requires(new ItemStack(Material.CLAY_BALL, 5))
                .description("Pressed clay-copper base.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.CLAY_BALL),     new ItemStack(Material.COPPER_INGOT),
                        new ItemStack(Material.CLAY_BALL),     new ItemStack(Material.CLAY_BALL),     new ItemStack(Material.CLAY_BALL),
                        new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.CLAY_BALL),     new ItemStack(Material.COPPER_INGOT)
                )
                .build();
        arcaneMaterials.addItem(hb1);

        // 17. Hardened Base II
        CodexItem hb2 = new CodexItem.Builder("arcane.materials.hardened_base_2")
                .displayName("Hardened Base II")
                .displayItem(arcaneItems.hardenedBase2)
                .xpCost(4)
                .requires(arcaneItems.hardenedBase1)
                .description("Highly pressed clay-copper base.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedBase1, arcaneItems.hardenedBase1, arcaneItems.hardenedBase1,
                        arcaneItems.hardenedBase1, arcaneItems.hardenedBase1, arcaneItems.hardenedBase1,
                        arcaneItems.hardenedBase1, arcaneItems.hardenedBase1, arcaneItems.hardenedBase1
                )
                .build();
        arcaneMaterials.addItem(hb2);

        // 18. Hardened Base Max
        CodexItem hbMax = new CodexItem.Builder("arcane.materials.hardened_base_max")
                .displayName("Hardened Base Max")
                .displayItem(arcaneItems.hardenedBaseMax)
                .xpCost(6)
                .requires(arcaneItems.hardenedBase2)
                .description("Fully crystallized metal-clay compound for emerald forging.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedBase2, arcaneItems.hardenedBase2, arcaneItems.hardenedBase2,
                        arcaneItems.hardenedBase2, arcaneItems.hardenedBase2, arcaneItems.hardenedBase2,
                        arcaneItems.hardenedBase2, arcaneItems.hardenedBase2, arcaneItems.hardenedBase2
                )
                .build();
        arcaneMaterials.addItem(hbMax);

        // 19. Synthetic Diamond
        CodexItem synDiamond = new CodexItem.Builder("arcane.materials.synthetic_diamond")
                .displayName("Synthetic Diamond")
                .displayItem(arcaneItems.syntheticDiamond)
                .xpCost(15)
                .requires(new ItemStack(Material.COAL_BLOCK, 1))
                .description("A dense, artificially forged diamond.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedCoalMax, arcaneItems.hardenedCoalMax, arcaneItems.hardenedCoalMax,
                        arcaneItems.hardenedCoalMax, new ItemStack(Material.LAVA_BUCKET), arcaneItems.hardenedCoalMax,
                        arcaneItems.hardenedCoalMax, arcaneItems.hardenedCoalMax, arcaneItems.hardenedCoalMax
                )
                .build();
        arcaneMaterials.addItem(synDiamond);

        // 20. Synthetic Emerald
        CodexItem synEmerald = new CodexItem.Builder("arcane.materials.synthetic_emerald")
                .displayName("Synthetic Emerald")
                .displayItem(arcaneItems.syntheticEmerald)
                .xpCost(15)
                .requires(new ItemStack(Material.EMERALD_ORE, 1))
                .description("A highly pressurized, artificial emerald.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.hardenedBaseMax, arcaneItems.hardenedBaseMax, arcaneItems.hardenedBaseMax,
                        arcaneItems.hardenedBaseMax, new ItemStack(Material.SLIME_BLOCK), arcaneItems.hardenedBaseMax,
                        arcaneItems.hardenedBaseMax, arcaneItems.hardenedBaseMax, arcaneItems.hardenedBaseMax
                )
                .build();
        arcaneMaterials.addItem(synEmerald);

        // 21. Echoing Core
        CodexItem echoCore = new CodexItem.Builder("arcane.materials.echoing_core")
                .displayName("Echoing Core")
                .displayItem(arcaneItems.echoingCore)
                .xpCost(10)
                .requires(new ItemStack(Material.ECHO_SHARD, 1))
                .description("Resonating core used for advanced armor upgrades.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ECHO_SHARD), new ItemStack(Material.ECHO_SHARD),      new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.ECHO_SHARD), new ItemStack(Material.HEART_OF_THE_SEA), new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.ECHO_SHARD), new ItemStack(Material.ECHO_SHARD),      new ItemStack(Material.ECHO_SHARD)
                )
                .build();
        arcaneMaterials.addItem(echoCore);

        // Immolation Totem
        CodexItem immolationTotemCodex = new CodexItem.Builder("arcane.materials.immolation_totem")
                .displayName("Immolation Totem")
                .displayItem(arcaneItems.immolationTotemItem)
                .xpCost(8)
                .requires(new ItemStack(Material.GOLD_BLOCK, 1))
                .requires(new ItemStack(Material.LAVA_BUCKET, 1))
                .description("A volatile fire reagent made with gold and lava.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET),
                        new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.GOLD_BLOCK),  new ItemStack(Material.LAVA_BUCKET),
                        new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET)
                )
                .build();
        arcaneMaterials.addItem(immolationTotemCodex);

        // Catch Flame Rune I
        CodexItem catchFlameRuneCodex = new CodexItem.Builder("arcane.runes.catch_flame")
                .displayName("Catch Flame Rune I")
                .displayItem(arcaneItems.catchFlameRuneItem)
                .xpCost(8)
                .requires(arcaneItems.immolationTotemItem)
                .description("Apply to weapon. Infuses Fire Aspect II / Flame I; spreads fire to nearby mobs on hit (33% chance).")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK),
                        new ItemStack(Material.DIAMOND_SWORD), arcaneItems.immolationTotemItem, new ItemStack(Material.BOW),
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(catchFlameRuneCodex);

        // Catch Flame Rune II
        CodexItem catchFlameRune2Codex = new CodexItem.Builder("arcane.runes.catch_flame_2")
                .displayName("Catch Flame Rune II")
                .displayItem(arcaneItems.catchFlameRune2Item)
                .xpCost(12)
                .requires(arcaneItems.catchFlameRuneItem)
                .description("Upgraded Catch Flame Rune. Fire spreads to nearby mobs with 66% chance.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK),
                        new ItemStack(Material.DIAMOND_SWORD), arcaneItems.immolationTotemItem, new ItemStack(Material.BOW),
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(catchFlameRune2Codex);

        // Catch Flame Rune III
        CodexItem catchFlameRune3Codex = new CodexItem.Builder("arcane.runes.catch_flame_3")
                .displayName("Catch Flame Rune III")
                .displayItem(arcaneItems.catchFlameRune3Item)
                .xpCost(16)
                .requires(arcaneItems.catchFlameRune2Item)
                .description("Maximum level Catch Flame Rune. Fire spreads to nearby mobs with 100% chance.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK),
                        new ItemStack(Material.DIAMOND_SWORD), arcaneItems.immolationTotemItem, new ItemStack(Material.BOW),
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK)
                )
                .build();
        arcaneRunesNormal.addItem(catchFlameRune3Codex);

        // 22. Staff of Supplant
        CodexItem staffSupplant = new CodexItem.Builder("arcane.ranged.staff_of_supplant")
                .displayName("Staff of Supplant")
                .displayItem(arcaneItems.createStaffOfSupplant())
                .xpCost(20)
                .requires(arcaneItems.enderEssence)
                .description("Shoots a projectile that swaps your position with the target entity.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BLAZE_POWDER), arcaneItems.enderEssence,                              new ItemStack(Material.BLAZE_POWDER),
                        arcaneItems.enderEssence,                         new ItemStack(Material.IRON_HOE),          arcaneItems.enderEssence,
                        new ItemStack(Material.BLAZE_POWDER), arcaneItems.enderEssence,                              new ItemStack(Material.BLAZE_POWDER)
                )
                .build();
        arcaneRanged.addItem(staffSupplant);

        // 23. Amulet of the Phoenix
        CodexItem amuletPhoenix = new CodexItem.Builder("arcane.trinkets.amulet_of_the_phoenix")
                .displayName("Amulet of the Phoenix")
                .displayItem(arcaneItems.createAmuletOfThePhoenix())
                .xpCost(30)
                .requires(new ItemStack(Material.TOTEM_OF_UNDYING, 1))
                .description("Grants totem recovery and fiery explosion knockback. Has 3 uses.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.MAGMA_CREAM), new ItemStack(Material.BLAZE_ROD),       new ItemStack(Material.MAGMA_CREAM),
                        new ItemStack(Material.GOLD_BLOCK),  new ItemStack(Material.TOTEM_OF_UNDYING), new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.MAGMA_CREAM), new ItemStack(Material.BLAZE_ROD),       new ItemStack(Material.MAGMA_CREAM)
                )
                .build();
        arcaneTrinkets.addItem(amuletPhoenix);

        // 24. Totem of Fallacy
        CodexItem totemFallacy = new CodexItem.Builder("arcane.trinkets.totem_of_fallacy")
                .displayName("Totem of Fallacy")
                .displayItem(arcaneItems.createTotemOfFallacy())
                .xpCost(40)
                .requires(arcaneItems.syntheticDiamond)
                .description("Teleports you to spawn. Reduces inventory items' durability to 10%.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ENDER_EYE),   arcaneItems.syntheticDiamond,                         new ItemStack(Material.ENDER_EYE),
                        arcaneItems.syntheticEmerald,                     new ItemStack(Material.TOTEM_OF_UNDYING), arcaneItems.syntheticEmerald,
                        new ItemStack(Material.OBSIDIAN),     new ItemStack(Material.OBSIDIAN),         new ItemStack(Material.OBSIDIAN)
                )
                .build();
        arcaneTrinkets.addItem(totemFallacy);

        // 25. Frostbite Ring
        CodexItem frostRing = new CodexItem.Builder("arcane.trinkets.frostbite_ring")
                .displayName("Frostbite Ring")
                .displayItem(arcaneItems.createFrostbiteRing())
                .xpCost(12)
                .requires(new ItemStack(Material.BLUE_ICE, 1))
                .description("Freezes water beneath your feet; extinguishes hit burning entities.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.BLUE_ICE), new ItemStack(Material.GOLD_NUGGET),
                        new ItemStack(Material.BLUE_ICE),   null,                              new ItemStack(Material.BLUE_ICE),
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.BLUE_ICE), new ItemStack(Material.GOLD_NUGGET)
                )
                .build();
        arcaneTrinkets.addItem(frostRing);

        // 26. Shadow Cloak
        CodexItem cloak = new CodexItem.Builder("arcane.armor.shadow_cloak")
                .displayName("Shadow Cloak")
                .displayItem(arcaneItems.shadowCloak)
                .xpCost(15)
                .requires(new ItemStack(Material.BLACK_DYE, 2))
                .description("Wearable cloak. Invisibility + Speed I when light level < 7.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BLACK_WOOL), new ItemStack(Material.STRING),     new ItemStack(Material.BLACK_WOOL),
                        new ItemStack(Material.BLACK_WOOL), new ItemStack(Material.BLACK_WOOL), new ItemStack(Material.BLACK_WOOL),
                        new ItemStack(Material.BLACK_WOOL), null,                               new ItemStack(Material.BLACK_WOOL)
                )
                .build();
        arcaneArmor.addItem(cloak);

        // 27. Superior Shadow Cloak
        CodexItem superiorCloak = new CodexItem.Builder("arcane.armor.superior_shadow_cloak")
                .displayName("Superior Shadow Cloak")
                .displayItem(arcaneItems.superiorShadowCloak)
                .xpCost(25)
                .requires(arcaneItems.shadowCloak)
                .description("Upgraded cloak. Invisibility + Speed II + Swift Sneak while crouched.")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        new ItemStack(Material.PHANTOM_MEMBRANE), new ItemStack(Material.ECHO_SHARD), new ItemStack(Material.PHANTOM_MEMBRANE),
                        new ItemStack(Material.ECHO_SHARD),       arcaneItems.shadowCloak,                        new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.PHANTOM_MEMBRANE), new ItemStack(Material.NETHER_STAR), new ItemStack(Material.PHANTOM_MEMBRANE)
                )
                .build();
        arcaneArmor.addItem(superiorCloak);

        // 28. Corrosive Scythe
        CodexItem scythe = new CodexItem.Builder("arcane.melee.venomous_scythe")
                .displayName("Corrosive Scythe")
                .displayItem(arcaneItems.createCorrosiveScythe())
                .xpCost(18)
                .requires(new ItemStack(Material.SPIDER_EYE, 4))
                .description("Iron hoe scythe that degrades enemy armor on each hit.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.FERMENTED_SPIDER_EYE),
                        null,                               new ItemStack(Material.STICK),      null,
                        null,                               new ItemStack(Material.STICK),      null
                )
                .build();
        arcaneMelee.addItem(scythe);

        // 29. Stormcaller Medallion
        CodexItem stormMedallion = new CodexItem.Builder("arcane.trinkets.stormcaller_medallion")
                .displayName("Stormcaller Medallion")
                .displayItem(arcaneItems.createStormcallerMedallion())
                .xpCost(20)
                .requires(new ItemStack(Material.LIGHTNING_ROD, 1))
                .description("Grants 15% lightning strike chance during storms.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.GOLD_INGOT),     new ItemStack(Material.LIGHTNING_ROD), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.LIGHTNING_ROD), new ItemStack(Material.DIAMOND),       new ItemStack(Material.LIGHTNING_ROD),
                        new ItemStack(Material.GOLD_INGOT),     new ItemStack(Material.LIGHTNING_ROD), new ItemStack(Material.GOLD_INGOT)
                )
                .build();
        arcaneTrinkets.addItem(stormMedallion);

        // 30. Wand of Transmutation
        CodexItem wandTrans = new CodexItem.Builder("arcane.ranged.wand_of_transmutation")
                .displayName("Wand of Transmutation")
                .displayItem(arcaneItems.createWandOfTransmutation())
                .xpCost(22)
                .requires(new ItemStack(Material.GOLDEN_APPLE, 1))
                .description("Beam fires turning hostiles into passive animals for 15s.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.LAPIS_LAZULI), new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.LAPIS_LAZULI),
                        null,                                 new ItemStack(Material.BONE),         null,
                        null,                                 new ItemStack(Material.BONE),         null
                )
                .build();
        arcaneRanged.addItem(wandTrans);

        // 31. Vitality Geode I
        CodexItem vg1 = new CodexItem.Builder("arcane.trinkets.vitality_geode_1")
                .displayName("Vitality Geode I")
                .displayItem(arcaneItems.geode1)
                .xpCost(15)
                .requires(new ItemStack(Material.AMETHYST_SHARD, 4))
                .description("Increases max health by +4 (2 hearts) while in inventory.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.AMETHYST_CLUSTER), new ItemStack(Material.REDSTONE_BLOCK),
                        new ItemStack(Material.AMETHYST_CLUSTER), new ItemStack(Material.GHAST_TEAR),      new ItemStack(Material.AMETHYST_CLUSTER),
                        new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.AMETHYST_CLUSTER), new ItemStack(Material.REDSTONE_BLOCK)
                )
                .build();
        arcaneTrinkets.addItem(vg1);

        // 32. Vitality Geode II
        CodexItem vg2 = new CodexItem.Builder("arcane.trinkets.vitality_geode_2")
                .displayName("Vitality Geode II")
                .displayItem(arcaneItems.geode2)
                .xpCost(18)
                .requires(arcaneItems.geode1)
                .description("Upgraded geode. Increases max health by +10 (5 hearts).")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.REDSTONE_BLOCK),
                        new ItemStack(Material.REDSTONE_BLOCK), arcaneItems.geode1,                                 new ItemStack(Material.REDSTONE_BLOCK),
                        new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.REDSTONE_BLOCK)
                )
                .build();
        arcaneTrinkets.addItem(vg2);

        // 33. Vitality Geode III
        CodexItem vg3 = new CodexItem.Builder("arcane.trinkets.vitality_geode_3")
                .displayName("Vitality Geode III")
                .displayItem(arcaneItems.geode3)
                .xpCost(25)
                .requires(arcaneItems.geode2)
                .description("Fully upgraded geode. Increases max health by +20 (10 hearts).")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        new ItemStack(Material.NETHER_STAR), new ItemStack(Material.NETHER_STAR), new ItemStack(Material.NETHER_STAR),
                        new ItemStack(Material.NETHER_STAR), arcaneItems.geode2,                              new ItemStack(Material.NETHER_STAR),
                        new ItemStack(Material.NETHER_STAR), new ItemStack(Material.NETHER_STAR), new ItemStack(Material.NETHER_STAR)
                )
                .build();
        arcaneTrinkets.addItem(vg3);

        // 34. Spelunker's Helmet
        CodexItem spelunkerHelmet = new CodexItem.Builder("explorer.armor.spelunkers_helmet")
                .displayName("Spelunker's Helmet")
                .displayItem(explorerItems.spelunkersHelmetItem)
                .xpCost(14)
                .requires(new ItemStack(Material.GLOWSTONE_DUST, 4))
                .description("Golden helmet giving Night Vision and highlights hostile mobs.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.GLOWSTONE),     new ItemStack(Material.GOLDEN_HELMET), new ItemStack(Material.GLOWSTONE),
                        new ItemStack(Material.REDSTONE),      new ItemStack(Material.REDSTONE_LAMP),  new ItemStack(Material.REDSTONE),
                        null,                                  null,                                  null
                )
                .build();
        explorerArmor.addItem(spelunkerHelmet);

        // 35. Portable Smelter
        CodexItem smelterItem = new CodexItem.Builder("explorer.tools.portable_smelter")
                .displayName("Portable Smelter")
                .displayItem(explorerItems.createPortableUtility(Material.BLAST_FURNACE, "explorer.tools.portable_smelter", "Portable Smelter"))
                .xpCost(10)
                .requires(new ItemStack(Material.BLAST_FURNACE, 1))
                .description("Right-click to open blast furnace; unsmelted items return on close.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.BLAST_FURNACE), new ItemStack(Material.IRON_BLOCK),
                        new ItemStack(Material.REDSTONE),   new ItemStack(Material.LAVA_BUCKET),   new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.OBSIDIAN),   new ItemStack(Material.OBSIDIAN),      new ItemStack(Material.OBSIDIAN)
                )
                .build();
        explorerTools.addItem(smelterItem);

        // 36. Portable Furnace
        CodexItem furnaceItem = new CodexItem.Builder("explorer.tools.portable_furnace")
                .displayName("Portable Furnace")
                .displayItem(explorerItems.createPortableUtility(Material.FURNACE, "explorer.tools.portable_furnace", "Portable Furnace"))
                .xpCost(5)
                .requires(new ItemStack(Material.FURNACE, 1))
                .description("Right-click to open furnace; unsmelted items return on close.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBBLESTONE), new ItemStack(Material.FURNACE),    new ItemStack(Material.COBBLESTONE),
                        new ItemStack(Material.REDSTONE),    new ItemStack(Material.COAL_BLOCK), new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.COBBLESTONE), new ItemStack(Material.COBBLESTONE), new ItemStack(Material.COBBLESTONE)
                )
                .build();
        explorerTools.addItem(furnaceItem);

        // 37. Portable Crafting Table
        CodexItem pcTable = new CodexItem.Builder("explorer.tools.portable_crafting_table")
                .displayName("Portable Crafting Table")
                .displayItem(explorerItems.createPortableUtility(Material.CRAFTING_TABLE, "explorer.tools.portable_crafting_table", "Portable Crafting Table"))
                .xpCost(3)
                .requires(new ItemStack(Material.CRAFTING_TABLE, 1))
                .description("Right-click to open crafting grid anywhere.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.LEATHER), new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.LEATHER),
                        new ItemStack(Material.STICK),   new ItemStack(Material.STRING),         new ItemStack(Material.STICK),
                        null,                            null,                                   null
                )
                .build();
        explorerTools.addItem(pcTable);

        // 38. Portable Anvil
        CodexItem pcAnvil = new CodexItem.Builder("explorer.tools.portable_anvil")
                .displayName("Portable Anvil")
                .displayItem(explorerItems.createPortableUtility(Material.ANVIL, "explorer.tools.portable_anvil", "Portable Anvil"))
                .xpCost(12)
                .requires(new ItemStack(Material.ANVIL, 1))
                .description("Right-click to open anvil repair screen.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.ANVIL),    new ItemStack(Material.IRON_BLOCK),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.OBSIDIAN),   new ItemStack(Material.OBSIDIAN), new ItemStack(Material.OBSIDIAN)
                )
                .build();
        explorerTools.addItem(pcAnvil);

        // 39. Portable Smithing Table
        CodexItem pcSmithing = new CodexItem.Builder("explorer.tools.portable_smithing_table")
                .displayName("Portable Smithing Table")
                .displayItem(explorerItems.createPortableUtility(Material.SMITHING_TABLE, "explorer.tools.portable_smithing_table", "Portable Smithing Table"))
                .xpCost(8)
                .requires(new ItemStack(Material.SMITHING_TABLE, 1))
                .description("Right-click to open smithing upgrades screen.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT),     new ItemStack(Material.SMITHING_TABLE), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.REDSTONE),       new ItemStack(Material.OAK_PLANKS),
                        null,                                  null,                                   null
                )
                .build();
        explorerTools.addItem(pcSmithing);

    }

    private void initItemsPart3(CodexCategory machineryStations, CodexCategory arcaneRunesNormal, CodexCategory arcaneArmor, CodexCategory arcaneRanged, CodexCategory explorerNavigation, CodexCategory explorerGadgets, CodexCategory arcaneMaterials, CodexCategory arcaneTrinkets, CodexCategory arcaneRunesDemonic, CodexCategory arcaneMelee, CodexCategory explorerArmor, CodexCategory explorerTools, CodexCategory explorerExploration, CodexCategory arcaneBoosts, CodexCategory arcaneIngredients, CodexCategory arcaneRunesHoly, CodexCategory explorerRunesNormal, CodexCategory explorerRunesDemonic, CodexCategory explorerRunesHoly) {

        // 40. Portable Grindstone
        CodexItem pcGrind = new CodexItem.Builder("explorer.tools.portable_grindstone")
                .displayName("Portable Grindstone")
                .displayItem(explorerItems.createPortableUtility(Material.GRINDSTONE, "explorer.tools.portable_grindstone", "Portable Grindstone"))
                .xpCost(6)
                .requires(new ItemStack(Material.GRINDSTONE, 1))
                .description("Right-click to open grindstone interface.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.STONE_SLAB), new ItemStack(Material.GRINDSTONE), new ItemStack(Material.STONE_SLAB),
                        new ItemStack(Material.STICK),      new ItemStack(Material.REDSTONE),   new ItemStack(Material.STICK),
                        null,                               null,                               null
                )
                .build();
        explorerTools.addItem(pcGrind);

        // 41. Portable Stonecutter
        CodexItem pcStone = new CodexItem.Builder("explorer.tools.portable_stonecutter")
                .displayName("Portable Stonecutter")
                .displayItem(explorerItems.createPortableUtility(Material.STONECUTTER, "explorer.tools.portable_stonecutter", "Portable Stonecutter"))
                .xpCost(6)
                .requires(new ItemStack(Material.STONECUTTER, 1))
                .description("Right-click to open stonecutter interface.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.STONECUTTER), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.STONE),      new ItemStack(Material.REDSTONE),    new ItemStack(Material.STONE),
                        null,                               null,                                null
                )
                .build();
        explorerTools.addItem(pcStone);

        // 42. Depth Strider Flippers
        CodexItem flippers = new CodexItem.Builder("explorer.gadgets.depth_strider_flippers")
                .displayName("Depth Strider Flippers")
                .displayItem(explorerItems.flippersItem)
                .xpCost(12)
                .requires(new ItemStack(Material.TURTLE_SCUTE, 1))
                .description("Gives swim speed & water breathing, but slowness on land.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.TURTLE_SCUTE),            null,                                 new ItemStack(Material.TURTLE_SCUTE),
                        new ItemStack(Material.PRISMARINE_SHARD), new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.PRISMARINE_SHARD),
                        new ItemStack(Material.KELP),             null,                                 new ItemStack(Material.KELP)
                )
                .build();
        explorerGadgets.addItem(flippers);

        // 43. Builder's Wand
        CodexItem bWand = new CodexItem.Builder("explorer.tools.builders_wand")
                .displayName("Builder's Wand")
                .displayItem(explorerItems.buildersWandItem)
                .xpCost(18)
                .requires(new ItemStack(Material.DIAMOND_BLOCK, 1))
                .description("Right-click face to place up to 9 matching blocks in a line.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        null, new ItemStack(Material.DIAMOND_BLOCK), null,
                        null, new ItemStack(Material.STICK),         null,
                        null, new ItemStack(Material.STICK),         null
                )
                .build();
        explorerTools.addItem(bWand);

        // 44a. Potent Spider Web
        CodexItem potentWebCodex = new CodexItem.Builder("arcane.materials.potent_spider_web")
                .displayName("Potent Spider Web")
                .displayItem(arcaneItems.potentSpiderWeb)
                .xpCost(5)
                .requires(new ItemStack(Material.COBWEB, 9))
                .description("Extremely sticky web block used for advanced binding crafts.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB)
                )
                .build();
        arcaneMaterials.addItem(potentWebCodex);

        // 44b. Webber
        CodexItem webberCodex = new CodexItem.Builder("explorer.tools.webber")
                .displayName("Webber")
                .displayItem(explorerItems.webberItem)
                .xpCost(15)
                .requires(new ItemStack(Material.COBWEB, 5))
                .description("Shoots temporary cobwebs (lasts 5s, breakable by sword).")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        arcaneItems.potentSpiderWeb, arcaneItems.potentSpiderWeb, arcaneItems.potentSpiderWeb,
                        arcaneItems.potentSpiderWeb, new ItemStack(Material.CROSSBOW), arcaneItems.potentSpiderWeb,
                        arcaneItems.potentSpiderWeb, arcaneItems.potentSpiderWeb, arcaneItems.potentSpiderWeb
                )
                .build();
        explorerTools.addItem(webberCodex);

        // 44c. Web Slingers
        CodexItem ironSlingerCodex = new CodexItem.Builder("explorer.tools.web_slinger.iron")
                .displayName("Iron Web Slinger")
                .displayItem(explorerItems.ironWebSlingerItem)
                .xpCost(10)
                .requires(explorerItems.ironGrappleItem)
                .requires(explorerItems.webberItem)
                .description("Iron Grappling Hook upgraded to place a fall-breaking cobweb where you land.")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        null, explorerItems.webberItem, null,
                        null, explorerItems.ironGrappleItem, null,
                        null, null, null
                )
                .build();
        explorerGadgets.addItem(ironSlingerCodex);

        CodexItem diamondSlingerCodex = new CodexItem.Builder("explorer.tools.web_slinger.diamond")
                .displayName("Diamond Web Slinger")
                .displayItem(explorerItems.diamondWebSlingerItem)
                .xpCost(15)
                .requires(explorerItems.diamondGrappleItem)
                .requires(explorerItems.webberItem)
                .description("Diamond Grappling Hook upgraded to place a fall-breaking cobweb where you land.")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        null, explorerItems.webberItem, null,
                        null, explorerItems.diamondGrappleItem, null,
                        null, null, null
                )
                .build();
        explorerGadgets.addItem(diamondSlingerCodex);

        CodexItem netheriteSlingerCodex = new CodexItem.Builder("explorer.tools.web_slinger.netherite")
                .displayName("Netherite Web Slinger")
                .displayItem(explorerItems.netheriteWebSlingerItem)
                .xpCost(20)
                .requires(explorerItems.netheriteGrappleItem)
                .requires(explorerItems.webberItem)
                .description("Netherite Grappling Hook upgraded to place a fall-breaking cobweb where you land.")
                .craftingStation(arcaneItems.upgradeTableItem)
                .recipe(
                        null, explorerItems.webberItem, null,
                        null, explorerItems.netheriteGrappleItem, null,
                        null, null, null
                )
                .build();
        explorerGadgets.addItem(netheriteSlingerCodex);

        // 45. Thermal Canteen
        CodexItem canteen = new CodexItem.Builder("explorer.gadgets.thermal_canteen")
                .displayName("Thermal Canteen")
                .displayItem(explorerItems.canteenItem)
                .xpCost(8)
                .requires(new ItemStack(Material.MAGMA_CREAM, 1))
                .description("Flask cures effects and restores hunger. Refill at campfire/lava.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_NUGGET), new ItemStack(Material.MAGMA_CREAM),  new ItemStack(Material.IRON_NUGGET),
                        new ItemStack(Material.GLASS),       new ItemStack(Material.GLASS_BOTTLE), new ItemStack(Material.GLASS),
                        new ItemStack(Material.IRON_NUGGET), new ItemStack(Material.IRON_NUGGET),  new ItemStack(Material.IRON_NUGGET)
                )
                .build();
        explorerGadgets.addItem(canteen);

        // 46. Beastmaster's Flute
        CodexItem flute = new CodexItem.Builder("explorer.tools.beastmasters_flute")
                .displayName("Beastmaster's Flute")
                .displayItem(explorerItems.fluteItem)
                .xpCost(20)
                .requires(new ItemStack(Material.NOTE_BLOCK, 1))
                .description("Pacifies/sleeps hostiles in a 5-block radius. Cooldown: 60s.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        null,                          new ItemStack(Material.BAMBOO),     new ItemStack(Material.STRING),
                        null,                          new ItemStack(Material.BAMBOO),     new ItemStack(Material.NOTE_BLOCK),
                        new ItemStack(Material.BAMBOO), null,                              null
                )
                .build();
        explorerTools.addItem(flute);

        // 47. Excavation Drill
        CodexItem drill = new CodexItem.Builder("explorer.tools.excavation_drill")
                .displayName("Excavation Drill")
                .displayItem(explorerItems.drillItem)
                .xpCost(25)
                .requires(new ItemStack(Material.DIAMOND_PICKAXE, 1))
                .description("Heavy 3x3 block-breaking pickaxe.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.DIAMOND),  new ItemStack(Material.IRON_BLOCK),   new ItemStack(Material.DIAMOND),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.REDSTONE),
                        null,                             new ItemStack(Material.STICK),        null
                )
                .build();
        explorerTools.addItem(drill);

        // 48. Wand of Levitation
        CodexItem wandOfLevitation = new CodexItem.Builder("arcane.ranged.wand_of_levitation")
                .displayName("Wand of Levitation")
                .displayItem(arcaneItems.wandOfLevitationItem)
                .xpCost(15)
                .requires(new ItemStack(Material.SHULKER_SHELL, 1))
                .description("Shoots a levitation bolt. Targets hit are given Levitation I for 5 seconds. Cooldown: 15 seconds.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.SHULKER_SHELL), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.SHULKER_SHELL),
                        new ItemStack(Material.FEATHER), new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.FEATHER),
                        new ItemStack(Material.FEATHER), new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.FEATHER)
                )
                .build();
        arcaneRanged.addItem(wandOfLevitation);

        // 49. Gale Chestplate
        CodexItem galeChestplate = new CodexItem.Builder("arcane.armor.gale_chestplate")
                .displayName("Gale Chestplate")
                .displayItem(arcaneItems.galeChestplateItem)
                .xpCost(18)
                .requires(new ItemStack(Material.PHANTOM_MEMBRANE, 2))
                .description("Gives permanent Slow Falling I while worn. Allows double jumping.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.PHANTOM_MEMBRANE), new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.PHANTOM_MEMBRANE),
                        new ItemStack(Material.FEATHER), new ItemStack(Material.EMERALD), new ItemStack(Material.FEATHER),
                        new ItemStack(Material.FEATHER), new ItemStack(Material.FEATHER), new ItemStack(Material.FEATHER)
                )
                .build();
        arcaneArmor.addItem(galeChestplate);

        // 50. Siphon Blade
        CodexItem siphonBlade = new CodexItem.Builder("arcane.melee.siphon_blade")
                .displayName("Siphon Blade")
                .displayItem(arcaneItems.siphonBladeItem)
                .xpCost(25)
                .requires(new ItemStack(Material.NETHER_STAR, 1))
                .description("Diamond sword that restores 15% of your max health upon killing an enemy.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.NETHER_STAR), new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.NETHER_STAR),
                        new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.SOUL_SAND), new ItemStack(Material.GHAST_TEAR),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.REDSTONE), new ItemStack(Material.REDSTONE)
                )
                .build();
        arcaneMelee.addItem(siphonBlade);

        // 51. Waypoint Teleport Plate
        CodexItem teleportationPlate = new CodexItem.Builder("explorer.navigation.teleportation_plate")
                .displayName("Waypoint Teleport Plate")
                .displayItem(explorerItems.teleportationPlateItem)
                .xpCost(14)
                .requires(new ItemStack(Material.ENDER_EYE, 1))
                .description("Right-click the placed plate with a Waypoint Compass to link. Teleports players who stand on it to linked waypoint.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE), new ItemStack(Material.ENDER_PEARL),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT)
                )
                .build();
        explorerNavigation.addItem(teleportationPlate);

        // 52. Slime Boots
        CodexItem slimeBoots = new CodexItem.Builder("explorer.gadgets.slime_boots")
                .displayName("Slime Boots")
                .displayItem(explorerItems.slimeBootsItem)
                .xpCost(10)
                .requires(new ItemStack(Material.SLIME_BALL, 1))
                .description("Negates all fall damage and bounces the wearer upward.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.SLIME_BALL), null, new ItemStack(Material.SLIME_BALL),
                        new ItemStack(Material.SLIME_BLOCK), null, new ItemStack(Material.SLIME_BLOCK),
                        new ItemStack(Material.IRON_BOOTS), null, new ItemStack(Material.IRON_BOOTS)
                )
                .build();
        explorerGadgets.addItem(slimeBoots);

        // 53. Ore Scanner
        CodexItem oreScanner = new CodexItem.Builder("explorer.exploration.ore_scanner")
                .displayName("Ore Scanner")
                .displayItem(explorerItems.oreScannerItem)
                .xpCost(12)
                .requires(new ItemStack(Material.AMETHYST_SHARD, 4))
                .description("Right-click to highlight nearby valuable ores in a 10-block radius.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.AMETHYST_SHARD), new ItemStack(Material.SPYGLASS), new ItemStack(Material.AMETHYST_SHARD),
                        new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.REDSTONE), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT)
                )
                .build();
        explorerExploration.addItem(oreScanner);

        // 54. Magnetic Ring
        CodexItem magneticRing = new CodexItem.Builder("explorer.gadgets.magnetic_ring")
                .displayName("Magnetic Ring")
                .displayItem(explorerItems.magneticRingItem)
                .xpCost(8)
                .requires(new ItemStack(Material.IRON_INGOT, 4))
                .description("Attracts dropped items within a 5-block radius when held in inventory.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_NUGGET),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_NUGGET)
                )
                .build();
        explorerGadgets.addItem(magneticRing);

        // 1. Lightning Essence
        CodexItem lightningEssence = new CodexItem.Builder("arcane.materials.lightning_essence")
                .displayName("Lightning Essence")
                .displayItem(arcaneItems.lightningEssenceItem)
                .xpCost(1)
                .defaultUnlocked(true)
                .description("A crackling trace of pure lightning. Rare drop from Creepers struck by lightning.")
                .craftingStation(new ItemStack(Material.CREEPER_HEAD))
                .recipe(
                        null, null, null,
                        null, null, null,
                        null, null, null
                )
                .build();
        arcaneMaterials.addItem(lightningEssence);

        // 2. Bottle of Lightning
        CodexItem bottleOfLightning = new CodexItem.Builder("explorer.gadgets.bottle_of_lightning")
                .displayName("Bottle of Lightning")
                .displayItem(explorerItems.bottleOfLightningItem)
                .xpCost(8)
                .requires(arcaneItems.lightningEssenceItem)
                .description("A bottle filled with raw electrical current.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        arcaneItems.lightningEssenceItem, arcaneItems.lightningEssenceItem, arcaneItems.lightningEssenceItem,
                        arcaneItems.lightningEssenceItem, new ItemStack(Material.GLASS_BOTTLE), arcaneItems.lightningEssenceItem,
                        arcaneItems.lightningEssenceItem, arcaneItems.lightningEssenceItem, arcaneItems.lightningEssenceItem
                )
                .build();
        explorerGadgets.addItem(bottleOfLightning);

        // 3. Staff of the Stormlord
        CodexItem staffOfTheStormlord = new CodexItem.Builder("arcane.ranged.staff_of_the_stormlord")
                .displayName("Staff of the Stormlord")
                .displayItem(arcaneItems.staffOfTheStormlordItem)
                .xpCost(20)
                .requires(explorerItems.bottleOfLightningItem)
                .description("Calls down a lightning bolt on target block.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        null, explorerItems.bottleOfLightningItem, null,
                        null, new ItemStack(Material.LIGHTNING_ROD), null,
                        null, new ItemStack(Material.BLAZE_ROD), null
                )
                .build();
        arcaneRanged.addItem(staffOfTheStormlord);

        // 4. Empty Vial
        CodexItem emptyVial = new CodexItem.Builder("arcane.materials.empty_vial")
                .displayName("Empty Vial")
                .displayItem(arcaneItems.emptyVialItem)
                .xpCost(5)
                .requires(new ItemStack(Material.GLASS_BOTTLE))
                .description("An empty glass container designed to hold player blood.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        null, new ItemStack(Material.REDSTONE), null,
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.GLASS_BOTTLE), new ItemStack(Material.REDSTONE),
                        null, new ItemStack(Material.REDSTONE), null
                )
                .build();
        arcaneMaterials.addItem(emptyVial);

        // 5. Blood
        CodexItem blood = new CodexItem.Builder("arcane.materials.blood")
                .displayName("Blood")
                .displayItem(arcaneItems.bloodItem)
                .xpCost(1)
                .defaultUnlocked(true)
                .description("A vial filled with player blood. Extracted from self.")
                .craftingStation(arcaneItems.emptyVialItem)
                .recipe(
                        null, null, null,
                        null, arcaneItems.emptyVialItem, null,
                        null, null, null
                )
                .build();
        arcaneMaterials.addItem(blood);

        // 6. Blood Altar
        CodexItem bloodAltar = new CodexItem.Builder("machinery.blood_altar")
                .displayName("Blood Altar")
                .displayItem(arcaneItems.bloodAltarItem)
                .xpCost(10)
                .requires(new ItemStack(Material.OBSIDIAN))
                .description("Structure: Red Carpet on top of Dropper on top of Obsidian.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.RED_CARPET), null,
                        null, new ItemStack(Material.DROPPER), null,
                        null, new ItemStack(Material.OBSIDIAN), null
                )
                .build();
        machineryStations.addItem(bloodAltar);

        // 7. Demonium I
        CodexItem demoniumI = new CodexItem.Builder("arcane.runes.demonium")
                .displayName("Demonium Rune I")
                .displayItem(arcaneItems.demoniumRuneItem)
                .xpCost(15)
                .requires(arcaneItems.bloodItem)
                .description("Apply to helmet. Burns entities in a 2-block radius (2s fire).")
                .craftingStation(arcaneItems.bloodAltarItem)
                .recipe(
                        null, arcaneItems.bloodItem, null,
                        new ItemStack(Material.WITHER_SKELETON_SKULL), arcaneItems.immolationTotemItem, new ItemStack(Material.WITHER_SKELETON_SKULL),
                        null, arcaneItems.bloodItem, null
                )
                .build();
        arcaneRunesDemonic.addItem(demoniumI);

        // 8. Demonium II
        CodexItem demoniumII = new CodexItem.Builder("arcane.runes.demonium_2")
                .displayName("Demonium Rune II")
                .displayItem(arcaneItems.demoniumRune2Item)
                .xpCost(20)
                .requires(arcaneItems.demoniumRuneItem)
                .description("Apply to helmet. Burns entities in a 2-block radius (5s fire).")
                .craftingStation(arcaneItems.bloodAltarItem)
                .recipe(
                        null, arcaneItems.bloodItem, null,
                        new ItemStack(Material.WITHER_SKELETON_SKULL), arcaneItems.demoniumRuneItem, new ItemStack(Material.WITHER_SKELETON_SKULL),
                        null, arcaneItems.bloodItem, null
                )
                .build();
        arcaneRunesDemonic.addItem(demoniumII);

    }

    private void initItemsPart4(CodexCategory machineryStations, CodexCategory arcaneRunesNormal, CodexCategory arcaneArmor, CodexCategory arcaneRanged, CodexCategory explorerNavigation, CodexCategory explorerGadgets, CodexCategory arcaneMaterials, CodexCategory arcaneTrinkets, CodexCategory arcaneRunesDemonic, CodexCategory arcaneMelee, CodexCategory explorerArmor, CodexCategory explorerTools, CodexCategory explorerExploration, CodexCategory arcaneBoosts, CodexCategory arcaneIngredients, CodexCategory arcaneRunesHoly, CodexCategory explorerRunesNormal, CodexCategory explorerRunesDemonic, CodexCategory explorerRunesHoly) {

        // 9. Demonium III
        CodexItem demoniumIII = new CodexItem.Builder("arcane.runes.demonium_3")
                .displayName("Demonium Rune III")
                .displayItem(arcaneItems.demoniumRune3Item)
                .xpCost(25)
                .requires(arcaneItems.demoniumRune2Item)
                .description("Apply to helmet. Burns entities in a 2-block radius (10s fire).")
                .craftingStation(arcaneItems.bloodAltarItem)
                .recipe(
                        null, arcaneItems.bloodItem, null,
                        new ItemStack(Material.WITHER_SKELETON_SKULL), arcaneItems.demoniumRune2Item, new ItemStack(Material.WITHER_SKELETON_SKULL),
                        null, arcaneItems.bloodItem, null
                )
                .build();
        arcaneRunesDemonic.addItem(demoniumIII);

        // 10. Ender Backpack
        CodexItem enderBackpack = new CodexItem.Builder("explorer.gadgets.ender_backpack")
                .displayName("Ender Backpack")
                .displayItem(explorerItems.enderBackpackItem)
                .xpCost(15)
                .requires(new ItemStack(Material.ENDER_CHEST))
                .description("Access ender chest remotely from anywhere.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER),
                        arcaneItems.echoingCore, new ItemStack(Material.ENDER_CHEST), arcaneItems.echoingCore,
                        new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER)
                )
                .build();
        explorerGadgets.addItem(enderBackpack);

        // 11. Safari Lasso
        CodexItem safariLasso = new CodexItem.Builder("explorer.gadgets.safari_lasso")
                .displayName("Safari Lasso")
                .displayItem(explorerItems.safariLassoItem)
                .xpCost(12)
                .requires(new ItemStack(Material.LEAD))
                .description("Captures and releases passive mobs.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.STRING), new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.STRING),
                        new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.LEAD), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.STRING), new ItemStack(Material.SLIME_BALL), new ItemStack(Material.STRING)
                )
                .build();
        explorerGadgets.addItem(safariLasso);

        // 12. Void Bag
        CodexItem voidBag = new CodexItem.Builder("explorer.gadgets.void_bag")
                .displayName("Void Bag")
                .displayItem(explorerItems.voidBagItem)
                .xpCost(10)
                .requires(new ItemStack(Material.CHEST))
                .description("A portable trash bag that voids its contents.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.LEATHER), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.LEATHER),
                        new ItemStack(Material.OBSIDIAN), new ItemStack(Material.HOPPER), new ItemStack(Material.OBSIDIAN),
                        new ItemStack(Material.LEATHER), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.LEATHER)
                )
                .build();
        explorerGadgets.addItem(voidBag);

        // 13. Steam Jetpack
        CodexItem steamJetpack = new CodexItem.Builder("explorer.armor.steam_jetpack")
                .displayName("Steam Jetpack")
                .displayItem(explorerItems.steamJetpackItem)
                .xpCost(20)
                .requires(new ItemStack(Material.FURNACE))
                .description("Sneak in mid-air to fly. Consumes coal/charcoal.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.FURNACE), new ItemStack(Material.REDSTONE),
                        arcaneItems.hardenedCoal1, new ItemStack(Material.CHAINMAIL_CHESTPLATE), arcaneItems.hardenedCoal1,
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.REDSTONE)
                )
                .build();
        explorerArmor.addItem(steamJetpack);

        // 14. Auto Sifter
        CodexItem autoSifter = new CodexItem.Builder("machinery.auto_sifter")
                .displayName("Auto Sifter")
                .displayItem(explorerItems.autoSifterItem)
                .xpCost(15)
                .requires(new ItemStack(Material.HOPPER))
                .description("Structure: Hopper on top of Dropper with adjacent Cauldron.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.REDSTONE_BLOCK), null,
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.HOPPER), new ItemStack(Material.IRON_BLOCK),
                        null, new ItemStack(Material.CAULDRON), null
                )
                .build();
        machineryStations.addItem(autoSifter);

        // 15. Auto Smelter
        CodexItem autoSmelter = new CodexItem.Builder("machinery.auto_smelter")
                .displayName("Auto Smelter")
                .displayItem(explorerItems.autoSmelterItem)
                .xpCost(15)
                .requires(new ItemStack(Material.FURNACE))
                .description("Structure: Hopper on top of Dropper with adjacent Furnace.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(
                        null, new ItemStack(Material.REDSTONE_BLOCK), null,
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.FURNACE), new ItemStack(Material.IRON_BLOCK),
                        null, new ItemStack(Material.HOPPER), null
                )
                .build();
        machineryStations.addItem(autoSmelter);

        // --- Webbed Armor Set ---
        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.webbed.helmet")
                .displayName("Webbed Hood")
                .displayItem(explorerItems.getCustomItem("explorer.armor.webbed.helmet"))
                .xpCost(6)
                .requires(new ItemStack(Material.COBWEB, 4))
                .description("Webbed Helmet. Part of the Webbed Set. Set: climb vertical walls.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.IRON_HELMET), new ItemStack(Material.COBWEB),
                        null, null, null
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.webbed.chestplate")
                .displayName("Webbed Tunic")
                .displayItem(explorerItems.getCustomItem("explorer.armor.webbed.chestplate"))
                .xpCost(8)
                .requires(new ItemStack(Material.COBWEB, 4))
                .description("Webbed Chestplate. Part of the Webbed Set. Set: climb vertical walls.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.COBWEB),
                        null, null, null
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.webbed.leggings")
                .displayName("Webbed Trousers")
                .displayItem(explorerItems.getCustomItem("explorer.armor.webbed.leggings"))
                .xpCost(7)
                .requires(new ItemStack(Material.COBWEB, 4))
                .description("Webbed Leggings. Part of the Webbed Set. Set: climb vertical walls.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.COBWEB),
                        null, null, null
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.webbed.boots")
                .displayName("Webbed Boots")
                .displayItem(explorerItems.getCustomItem("explorer.armor.webbed.boots"))
                .xpCost(5)
                .requires(new ItemStack(Material.COBWEB, 4))
                .description("Webbed Boots. Part of the Webbed Set. Set: climb vertical walls.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB), new ItemStack(Material.COBWEB),
                        new ItemStack(Material.COBWEB), new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.COBWEB),
                        null, null, null
                ).build());

        // --- Aegis Vanguard Armor Set ---
        ItemStack blueGold = arcaneItems.getCustomItem("arcane.materials.blue_gold");
        ItemStack synDiamondItem = arcaneItems.getCustomItem("arcane.materials.synthetic_diamond");

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.aegis_vanguard.helmet")
                .displayName("Aegis Vanguard Greathelm")
                .displayItem(explorerItems.getCustomItem("explorer.armor.aegis_vanguard.helmet"))
                .xpCost(15)
                .requires(blueGold)
                .description("Heavy defensive helmet. Set: permanent Slowness I, Resistance II, 10s mob taunt.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        blueGold, blueGold, blueGold,
                        blueGold, synDiamondItem, blueGold,
                        null, null, null
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.aegis_vanguard.chestplate")
                .displayName("Aegis Vanguard Platemail")
                .displayItem(explorerItems.getCustomItem("explorer.armor.aegis_vanguard.chestplate"))
                .xpCost(20)
                .requires(blueGold)
                .description("Heavy defensive chestplate. Set: permanent Slowness I, Resistance II, 10s mob taunt.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        blueGold, synDiamondItem, blueGold,
                        blueGold, blueGold, blueGold,
                        blueGold, blueGold, blueGold
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.aegis_vanguard.leggings")
                .displayName("Aegis Vanguard Greaves")
                .displayItem(explorerItems.getCustomItem("explorer.armor.aegis_vanguard.leggings"))
                .xpCost(18)
                .requires(blueGold)
                .description("Heavy defensive leggings. Set: permanent Slowness I, Resistance II, 10s mob taunt.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        blueGold, blueGold, blueGold,
                        blueGold, synDiamondItem, blueGold,
                        blueGold, null, blueGold
                ).build());

        explorerArmor.addItem(new CodexItem.Builder("explorer.armor.aegis_vanguard.boots")
                .displayName("Aegis Vanguard Sabatons")
                .displayItem(explorerItems.getCustomItem("explorer.armor.aegis_vanguard.boots"))
                .xpCost(12)
                .requires(blueGold)
                .description("Heavy defensive boots. Set: permanent Slowness I, Resistance II, 10s mob taunt.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        null, null, null,
                        blueGold, synDiamondItem, blueGold,
                        blueGold, null, blueGold
                ).build());

        // --- Storm-Weaver Armor Set ---
        ItemStack lightEss = arcaneItems.getCustomItem("arcane.materials.lightning_essence");
        ItemStack roseGold = arcaneItems.getCustomItem("arcane.materials.rose_gold");

        arcaneArmor.addItem(new CodexItem.Builder("arcane.armor.storm_weaver.helmet")
                .displayName("Storm-Weaver Hood")
                .displayItem(explorerItems.getCustomItem("arcane.armor.storm_weaver.helmet"))
                .xpCost(15)
                .requires(lightEss)
                .description("Mage hood. Set: 50% reduced spell cooldowns, regenerates magic charges.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        roseGold, lightEss, roseGold,
                        roseGold, null, roseGold,
                        null, null, null
                ).build());

        arcaneArmor.addItem(new CodexItem.Builder("arcane.armor.storm_weaver.chestplate")
                .displayName("Storm-Weaver Robe")
                .displayItem(explorerItems.getCustomItem("arcane.armor.storm_weaver.chestplate"))
                .xpCost(20)
                .requires(lightEss)
                .description("Mage robe. Set: 50% reduced spell cooldowns, regenerates magic charges.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        roseGold, null, roseGold,
                        roseGold, lightEss, roseGold,
                        roseGold, roseGold, roseGold
                ).build());

        arcaneArmor.addItem(new CodexItem.Builder("arcane.armor.storm_weaver.leggings")
                .displayName("Storm-Weaver Leggings")
                .displayItem(explorerItems.getCustomItem("arcane.armor.storm_weaver.leggings"))
                .xpCost(18)
                .requires(lightEss)
                .description("Mage leggings. Set: 50% reduced spell cooldowns, regenerates magic charges.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        roseGold, lightEss, roseGold,
                        roseGold, null, roseGold,
                        roseGold, null, roseGold
                ).build());

        arcaneArmor.addItem(new CodexItem.Builder("arcane.armor.storm_weaver.boots")
                .displayName("Storm-Weaver Sandals")
                .displayItem(explorerItems.getCustomItem("arcane.armor.storm_weaver.boots"))
                .xpCost(12)
                .requires(lightEss)
                .description("Mage sandals. Set: 50% reduced spell cooldowns, regenerates magic charges.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        null, null, null,
                        roseGold, lightEss, roseGold,
                        roseGold, null, roseGold
                ).build());

        // --- Glintblade Phalanx Tome ---
        arcaneRanged.addItem(new CodexItem.Builder("arcane.tomes.glintblade_phalanx")
                .displayName("Tome of Glintblade Phalanx")
                .displayItem(arcaneItems.getCustomItem("arcane.tomes.glintblade_phalanx"))
                .xpCost(15)
                .requires(new ItemStack(Material.BOOK, 1))
                .description("Summons 4 floating phantom daggers that autonomously target hostiles entering a 6-block radius.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.AMETHYST_SHARD), arcaneItems.echoingCore, new ItemStack(Material.AMETHYST_SHARD),
                        new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.BOOK), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.AMETHYST_SHARD), arcaneItems.echoingCore, new ItemStack(Material.AMETHYST_SHARD)
                ).build());

        // --- Structure Locators ---
        explorerExploration.addItem(new CodexItem.Builder("explorer.tools.locator.village")
                .displayName("Village Locator")
                .displayItem(explorerItems.getCustomItem("explorer.tools.locator.village"))
                .xpCost(10)
                .requires(new ItemStack(Material.BELL, 1))
                .description("Locates the nearest Village. Has 10 charges.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.BELL), new ItemStack(Material.OAK_PLANKS),
                        new ItemStack(Material.CHEST), new ItemStack(Material.COMPASS), new ItemStack(Material.WHITE_BED),
                        new ItemStack(Material.COBBLESTONE), new ItemStack(Material.COBBLESTONE), new ItemStack(Material.COBBLESTONE)
                ).build());

        explorerExploration.addItem(new CodexItem.Builder("explorer.tools.locator.stronghold")
                .displayName("Stronghold Locator")
                .displayItem(explorerItems.getCustomItem("explorer.tools.locator.stronghold"))
                .xpCost(15)
                .requires(new ItemStack(Material.STONE_BRICKS, 4))
                .description("Locates the nearest Stronghold. Has 10 charges.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.STONE_BRICKS), new ItemStack(Material.IRON_BARS), new ItemStack(Material.STONE_BRICKS),
                        new ItemStack(Material.BOOKSHELF), new ItemStack(Material.COMPASS), new ItemStack(Material.IRON_DOOR),
                        new ItemStack(Material.STONE_BRICKS), new ItemStack(Material.CHEST), new ItemStack(Material.STONE_BRICKS)
                ).build());

        explorerExploration.addItem(new CodexItem.Builder("explorer.tools.locator.end_city")
                .displayName("End City Locator")
                .displayItem(explorerItems.getCustomItem("explorer.tools.locator.end_city"))
                .xpCost(20)
                .requires(new ItemStack(Material.PURPUR_BLOCK, 4))
                .description("Locates the nearest End City. Has 10 charges.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.PURPUR_BLOCK), new ItemStack(Material.SHULKER_SHELL), new ItemStack(Material.PURPUR_BLOCK),
                        new ItemStack(Material.END_STONE_BRICKS), new ItemStack(Material.COMPASS), new ItemStack(Material.END_ROD),
                        new ItemStack(Material.PURPUR_BLOCK), new ItemStack(Material.PURPUR_PILLAR), new ItemStack(Material.PURPUR_BLOCK)
                ).build());

        // --- Omni Tool ---
        explorerExploration.addItem(new CodexItem.Builder("explorer.tools.omni_tool")
                .displayName("Omni Tool")
                .displayItem(explorerItems.getCustomItem("explorer.tools.omni_tool"))
                .xpCost(25)
                .requires(arcaneItems.getCustomItem("arcane.materials.transmutation_core"))
                .description("A tool that instantly adapts its form to harvest whatever block you look at.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_SHOVEL), new ItemStack(Material.DIAMOND_AXE),
                        null, arcaneItems.getCustomItem("arcane.materials.transmutation_core"), null,
                        null, arcaneItems.getCustomItem("arcane.materials.synthetic_diamond"), null
                ).build());

        // --- Custom Materials & Alloys ---
        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.blue_gold")
                .displayName("Blue Gold")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.blue_gold"))
                .xpCost(4)
                .requires(new ItemStack(Material.GOLD_INGOT, 3))
                .description("A strong gold-iron alloy forged in the Heavy Alloy Forge.")
                .craftingStation(arcaneItems.getCustomItem("machinery.heavy_alloy_forge"))
                .recipe(
                        new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.IRON_INGOT), null, null,
                        null, null, null
                ).build());

        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.rose_gold")
                .displayName("Rose Gold")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.rose_gold"))
                .xpCost(4)
                .requires(new ItemStack(Material.COPPER_INGOT, 1))
                .description("A beautiful copper-gold alloy forged in the Heavy Alloy Forge.")
                .craftingStation(arcaneItems.getCustomItem("machinery.heavy_alloy_forge"))
                .recipe(
                        new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.COPPER_INGOT), null,
                        null, null, null,
                        null, null, null
                ).build());

        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.bronzed_steel")
                .displayName("Bronzed Steel")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.bronzed_steel"))
                .xpCost(4)
                .requires(new ItemStack(Material.IRON_INGOT, 1))
                .description("A hardened copper-iron steel forged in the Heavy Alloy Forge.")
                .craftingStation(arcaneItems.getCustomItem("machinery.heavy_alloy_forge"))
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.COPPER_INGOT), arcaneItems.hardenedCoal1,
                        null, null, null,
                        null, null, null
                ).build());

        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.abyssal_alloy")
                .displayName("Abyssal Alloy")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.abyssal_alloy"))
                .xpCost(8)
                .requires(new ItemStack(Material.NETHERITE_SCRAP, 1))
                .description("A dark, blood-infused nether alloy forged in the Heavy Alloy Forge.")
                .craftingStation(arcaneItems.getCustomItem("machinery.heavy_alloy_forge"))
                .recipe(
                        new ItemStack(Material.NETHERITE_SCRAP), arcaneItems.bloodItem, arcaneItems.getCustomItem("arcane.materials.synthetic_emerald"),
                        null, null, null,
                        null, null, null
                ).build());

        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.crushed_ender_dust")
                .displayName("Crushed Ender Dust")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.crushed_ender_dust"))
                .xpCost(2)
                .requires(new ItemStack(Material.ENDER_PEARL, 1))
                .description("Fine powder of crushed ender pearls produced in a Kinetic Crusher.")
                .craftingStation(arcaneItems.getCustomItem("machinery.kinetic_crusher"))
                .recipe(
                        null, new ItemStack(Material.ENDER_PEARL), null,
                        null, null, null,
                        null, null, null
                ).build());

        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.fractured_geode")
                .displayName("Fractured Geode")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.fractured_geode"))
                .xpCost(2)
                .requires(new ItemStack(Material.SAND, 1))
                .description("A cracked shell sifted from the Trommel.")
                .craftingStation(arcaneItems.getCustomItem("machinery.sifting_trommel"))
                .recipe(
                        null, new ItemStack(Material.SAND), null,
                        null, null, null,
                        null, null, null
                ).build());

        // --- Transmutation Core ---
        arcaneMaterials.addItem(new CodexItem.Builder("arcane.materials.transmutation_core")
                .displayName("Transmutation Core")
                .displayItem(arcaneItems.getCustomItem("arcane.materials.transmutation_core"))
                .xpCost(15)
                .requires(new ItemStack(Material.CONDUIT, 1))
                .description("A pulsing core capable of changing matter from one form to another.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.AMETHYST_SHARD), arcaneItems.getCustomItem("arcane.materials.ender_essence"), new ItemStack(Material.AMETHYST_SHARD),
                        arcaneItems.getCustomItem("arcane.materials.ender_essence"), new ItemStack(Material.CONDUIT), arcaneItems.getCustomItem("arcane.materials.ender_essence"),
                        new ItemStack(Material.AMETHYST_SHARD), arcaneItems.getCustomItem("arcane.materials.ender_essence"), new ItemStack(Material.AMETHYST_SHARD)
                ).build());


    }

    private void registerRecipes() {
        CodexCategory arcaneIngredients = registry.getCategory("arcane.ingredients");
        CodexCategory arcaneMaterials = registry.getCategory("arcane.materials");
        CodexCategory arcaneRunes = new CodexCategory("arcane.runes", "arcane", "Enchantment Runes", new ItemStack(Material.FIREWORK_STAR));
        CodexCategory explorerRunes = new CodexCategory("explorer.runes", "explorer", "Enchantment Runes", new ItemStack(Material.FIREWORK_STAR));

        // 4. Ender Essence Shaped Recipe (Vanilla Crafting Table)
        NamespacedKey enderEssKey = new NamespacedKey(plugin, "recipe_ender_essence");
        if (Bukkit.getRecipe(enderEssKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(enderEssKey, arcaneItems.enderEssence);
            recipe.shape("PPP", "PPP", "PPP");
            recipe.setIngredient('P', Material.ENDER_PEARL);
            Bukkit.addRecipe(recipe);
        }

        // 5. Reverse Ender Essence Shapeless Recipe (Vanilla Crafting Table)
        NamespacedKey revEnderKey = new NamespacedKey(plugin, "recipe_reverse_ender_essence");
        if (Bukkit.getRecipe(revEnderKey) == null) {
            ShapelessRecipe recipe = new ShapelessRecipe(revEnderKey, new ItemStack(Material.ENDER_PEARL, 9));
            recipe.addIngredient(new RecipeChoice.ExactChoice(arcaneItems.enderEssence));
            Bukkit.addRecipe(recipe);
        }

        // 6. Cobweb Shapeless Recipe (Vanilla Crafting Table)
        NamespacedKey cobwebKey = new NamespacedKey(plugin, "recipe_cobweb");
        if (Bukkit.getRecipe(cobwebKey) == null) {
            ShapelessRecipe recipe = new ShapelessRecipe(cobwebKey, new ItemStack(Material.COBWEB));
            recipe.addIngredient(Material.STRING);
            recipe.addIngredient(Material.STRING);
            recipe.addIngredient(Material.STRING);
            recipe.addIngredient(Material.STRING);
            Bukkit.addRecipe(recipe);
        }

        // Nature's Embrace
        CodexItem naturesEmbrace = new CodexItem.Builder("arcane.materials.natures_embrace")
                .displayName("Nature's Embrace")
                .displayItem(arcaneItems.naturesEmbraceItem)
                .xpCost(15)
                .requires(new ItemStack(Material.HEART_OF_THE_SEA, 1))
                .description("A pulsating green orb humming with the raw power of the forest.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.OAK_LEAVES), new ItemStack(Material.SPRUCE_LEAVES), new ItemStack(Material.BIRCH_LEAVES),
                        new ItemStack(Material.JUNGLE_LEAVES), new ItemStack(Material.HEART_OF_THE_SEA), new ItemStack(Material.ACACIA_LEAVES),
                        new ItemStack(Material.DARK_OAK_LEAVES), new ItemStack(Material.MANGROVE_LEAVES), new ItemStack(Material.CHERRY_LEAVES)
                )
                .build();
        arcaneIngredients.addItem(naturesEmbrace);

        // Sun's Brilliance
        CodexItem sunsBrilliance = new CodexItem.Builder("arcane.materials.suns_brilliance")
                .displayName("Sun's Brilliance")
                .displayItem(arcaneItems.sunsBrillianceItem)
                .xpCost(10)
                .requires(arcaneItems.immolationTotemItem)
                .description("Solar-charged flower. Obtained by exposing a dropped Immolation Totem to direct sunlight for 10 seconds.")
                .craftingStation(new ItemStack(Material.SUNFLOWER))
                .recipe(
                        null, null, null,
                        null, arcaneItems.immolationTotemItem, null,
                        null, null, null
                )
                .build();
        arcaneIngredients.addItem(sunsBrilliance);

        // Blessing of the Void
        CodexItem blessingVoid = new CodexItem.Builder("explorer.materials.blessing_of_the_void")
                .displayName("Blessing of the Void")
                .displayItem(arcaneItems.blessingOfTheVoidItem)
                .xpCost(25)
                .requires(new ItemStack(Material.ELYTRA, 1))
                .description("An ender-encased elytra infused with void warding capabilities.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        arcaneItems.enderEssence, arcaneItems.enderEssence, arcaneItems.enderEssence,
                        arcaneItems.enderEssence, new ItemStack(Material.ELYTRA), arcaneItems.enderEssence,
                        arcaneItems.enderEssence, arcaneItems.enderEssence, arcaneItems.enderEssence
                )
                .build();
        arcaneMaterials.addItem(blessingVoid);

        // Resonant Plate
        CodexItem resonantPlate = new CodexItem.Builder("explorer.materials.resonant_plate")
                .displayName("Resonant Plate")
                .displayItem(arcaneItems.resonantPlateItem)
                .xpCost(15)
                .requires(new ItemStack(Material.SHIELD, 1))
                .description("A highly elastic metal plate designed to reflect forces.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.SHIELD), new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.REDSTONE), new ItemStack(Material.IRON_INGOT)
                )
                .build();
        arcaneMaterials.addItem(resonantPlate);

        // Link Stone
        CodexItem linkStone = new CodexItem.Builder("arcane.materials.link_stone")
                .displayName("Link Stone")
                .displayItem(arcaneItems.linkStoneItem)
                .xpCost(15)
                .requires(new ItemStack(Material.LEAD, 1))
                .description("A mystical crystal that binds and redirects connections.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.AMETHYST_SHARD), new ItemStack(Material.GOLD_NUGGET),
                        new ItemStack(Material.AMETHYST_SHARD), new ItemStack(Material.LEAD), new ItemStack(Material.AMETHYST_SHARD),
                        new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.AMETHYST_SHARD), new ItemStack(Material.GOLD_NUGGET)
                )
                .build();
        arcaneIngredients.addItem(linkStone);

        // Poison Vial
        CodexItem poisonVial = new CodexItem.Builder("arcane.materials.poison_vial")
                .displayName("Poison Vial")
                .displayItem(arcaneItems.poisonVialItem)
                .xpCost(5)
                .requires(arcaneItems.emptyVialItem)
                .description("Concentrated venom. Extract from Spiders, Cave Spiders, Bees, or Pufferfish with an Empty Vial.")
                .craftingStation(arcaneItems.emptyVialItem)
                .recipe(
                        null, null, null,
                        null, arcaneItems.emptyVialItem, null,
                        null, null, null
                )
                .build();
        arcaneIngredients.addItem(poisonVial);

        // Soul Orb
        CodexItem soulOrb = new CodexItem.Builder("arcane.materials.soul_orb")
                .displayName("Soul Orb")
                .displayItem(arcaneItems.soulOrbItem)
                .xpCost(10)
                .requires(new ItemStack(Material.GHAST_TEAR, 1))
                .description("A swirling orb containing a lost soul. Rare drop from hostile mobs.")
                .craftingStation(new ItemStack(Material.GHAST_TEAR))
                .recipe(
                        null, null, null,
                        null, new ItemStack(Material.GHAST_TEAR), null,
                        null, null, null
                )
                .build();
        arcaneIngredients.addItem(soulOrb);

        // Core of Heat
        CodexItem coreHeat = new CodexItem.Builder("explorer.materials.core_of_heat")
                .displayName("Core of Heat")
                .displayItem(arcaneItems.coreOfHeat)
                .xpCost(10)
                .requires(new ItemStack(Material.MAGMA_CREAM, 1))
                .description("An intensely hot orb forged by compressing Magma Blocks and Blaze Powder.")
                .craftingStation(explorerItems.heavyForgeItem)
                .recipe(
                        new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK),
                        new ItemStack(Material.BLAZE_POWDER), new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.MAGMA_BLOCK)
                )
                .build();
        arcaneMaterials.addItem(coreHeat);

        ItemStack book = new ItemStack(Material.BOOK);
        ItemStack enchBook = new ItemStack(Material.ENCHANTED_BOOK);

        // 1. Corrosive Slash (Axe - Max 4)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.corrosive_slash", "Corrosive Slash", "corrosive_slash", "WEAPON_AXE", 4, 
                "Reduces enemy armor toughness rating on axe hits. Level scales duration up to 10s at Level 4 (5 max stacks).", 
                org.bukkit.Color.fromRGB(85, 107, 47), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, new ItemStack(Material.SPIDER_EYE), book,
                        new ItemStack(Material.PUFFERFISH), new ItemStack(Material.WITHER_SKELETON_SKULL), new ItemStack(Material.PUFFERFISH),
                        book, new ItemStack(Material.SPIDER_EYE), book
                }, arcaneItems.arcanaTableItem);

        // 2. Scorch (Sword - Max 1)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.scorch", "Scorch", "scorch", "WEAPON_SWORD", 1, 
                "Reduces enemy armor on sword hits (stacks up to 10, max 90%). Deals 1 tick of fire damage on hit. No levels.", 
                org.bukkit.Color.fromRGB(255, 69, 0), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.MAGMA_CREAM), enchBook,
                        new ItemStack(Material.SPIDER_EYE), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.SPIDER_EYE),
                        enchBook, new ItemStack(Material.MAGMA_CREAM), enchBook
                }, arcaneItems.arcanaTableItem);

        // 3. Dwarf's Blessing (Tools - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.dwarfs_blessing", "Dwarf's Blessing", "dwarfs_blessing", "TOOLS", 1, 
                "Automatically smelts all mined blocks. Breaking logs drops Charcoal instead of wood.", 
                org.bukkit.Color.fromRGB(139, 69, 19), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.BLAST_FURNACE), enchBook,
                        null, new ItemStack(Material.BLAST_FURNACE), null,
                        enchBook, new ItemStack(Material.BLAST_FURNACE), enchBook
                }, explorerItems.heavyForgeItem);

        // 4. Glacial Thorns (Armor - Max 5)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.glacial_thorns", "Glacial Thorns", "glacial_thorns", "ARMOR", 5, 
                "Enemies hitting you have a chance to be afflicted with Weakness. Level 5 guarantees Weakness II.", 
                org.bukkit.Color.fromRGB(0, 191, 255), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, null, book,
                        new ItemStack(Material.BLUE_ICE, 4), new ItemStack(Material.PUFFERFISH), new ItemStack(Material.BLUE_ICE, 4),
                        book, null, book
                }, arcaneItems.arcanaTableItem);

        // 5. Tidal Sweep (Swords - Max 3)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.tidal_sweep", "Tidal Sweep", "tidal_sweep", "WEAPON_SWORD", 3, 
                "Unleashes a larger, longer-reaching sweep with a narrower angle. Incompatible with Sweeping Edge.", 
                org.bukkit.Color.fromRGB(30, 144, 255), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, new ItemStack(Material.PRISMARINE_SHARD), book,
                        new ItemStack(Material.PRISMARINE_SHARD), new ItemStack(Material.HEART_OF_THE_SEA), new ItemStack(Material.PRISMARINE_SHARD),
                        book, new ItemStack(Material.KELP), book
                }, arcaneItems.arcanaTableItem);

        // 6. Seismic Landing (Boots - Max 4)
        registerMultiLevelRune(explorerRunes, "explorer.runes.seismic_landing", "Seismic Landing", "seismic_landing", "ARMOR_BOOTS", 4, 
                "Negates up to 5 hearts of fall damage, releasing a massive damage/launch shockwave. Incompatible with Feather Falling.", 
                org.bukkit.Color.fromRGB(112, 128, 144), org.bukkit.FireworkEffect.Type.BALL, 12,
                new ItemStack[]{
                        book, new ItemStack(Material.POINTED_DRIPSTONE), book,
                        new ItemStack(Material.POINTED_DRIPSTONE), arcaneItems.naturesEmbraceItem, new ItemStack(Material.POINTED_DRIPSTONE),
                        book, new ItemStack(Material.POINTED_DRIPSTONE), book
                }, explorerItems.heavyForgeItem);

        // 7. Photosynthesis (Durability - Max 1)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.photosynthesis", "Photosynthesis", "photosynthesis", "DURABILITY", 1, 
                "Slowly restores item durability while standing on dirt/grass under direct sunlight. No levels.", 
                org.bukkit.Color.fromRGB(50, 205, 50), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.sunsBrillianceItem, enchBook,
                        null, arcaneItems.naturesEmbraceItem, null,
                        enchBook, arcaneItems.sunsBrillianceItem, enchBook
                }, arcaneItems.arcanaTableItem);

        // 8. Zephyr (Bows/Crossbows - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.zephyr", "Zephyr", "zephyr", "WEAPON_BOW", 1, 
                "Gravity has no effect on fired arrows. No levels.", 
                org.bukkit.Color.fromRGB(224, 255, 255), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.PHANTOM_MEMBRANE), enchBook,
                        null, new ItemStack(Material.FEATHER), null,
                        enchBook, new ItemStack(Material.PHANTOM_MEMBRANE), enchBook
                }, explorerItems.heavyForgeItem);

        // 9. Static Charge (Chestplate - Max 7)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.static_charge", "Static Charge", "static_charge", "ARMOR_CHESTPLATE", 7, 
                "Generates charge while moving. Fully charged adds spark and next hit deals +25% bonus damage.", 
                org.bukkit.Color.fromRGB(255, 255, 0), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        book, arcaneItems.lightningEssenceItem, book,
                        null, new ItemStack(Material.DIAMOND_CHESTPLATE), null,
                        book, arcaneItems.lightningEssenceItem, book
                }, arcaneItems.arcanaTableItem);

        // 11. Phantom Backstab (Swords - Max 4 - Blood Altar)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.phantom_backstab", "Phantom Backstab", "phantom_backstab", "WEAPON_SWORD", 4, 
                "Melee hits have 50% chance to strike target with spectral sword behind them after 1s.", 
                org.bukkit.Color.fromRGB(75, 0, 130), org.bukkit.FireworkEffect.Type.BALL, 15,
                new ItemStack[]{
                        enchBook, arcaneItems.createCustomRune("arcane.runes.bleed", "Bleed", "bleed", "WEAPON_SWORD_AXE", 1, "Applies ticking bleed stacks.", org.bukkit.Color.fromRGB(150, 0, 0), org.bukkit.FireworkEffect.Type.BALL, false, false), enchBook,
                        arcaneItems.enderEssence, arcaneItems.echoingCore, arcaneItems.createTotemOfFallacy(),
                        enchBook, arcaneItems.createLifestealRuneOfLevel(3), enchBook
                }, arcaneItems.bloodAltarItem);

        // 12. Telekinesis (Tools - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.telekinesis", "Telekinesis", "telekinesis", "TOOLS_HOE", 1, 
                "Teleports mined blocks and experience orbs directly to inventory. No levels.", 
                org.bukkit.Color.fromRGB(238, 130, 238), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, null, enchBook,
                        null, explorerItems.magneticRingItem, null,
                        enchBook, null, enchBook
                }, explorerItems.heavyForgeItem);

        // 13. Kinetic Rebound (Shields - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.kinetic_rebound", "Kinetic Rebound", "kinetic_rebound", "SHIELD", 1, 
                "Blocking projectiles has a high chance to reflect them back to source. No levels.", 
                org.bukkit.Color.fromRGB(211, 211, 211), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.resonantPlateItem, enchBook,
                        null, new ItemStack(Material.SHIELD), null,
                        enchBook, arcaneItems.resonantPlateItem, enchBook
                }, explorerItems.heavyForgeItem);

        // 14. Redirection (Armor - Max 6)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.redirection", "Redirection", "redirection", "ARMOR", 6, 
                "Redirects a percentage of incoming damage (up to 40% at level 6) to nearby pets.", 
                org.bukkit.Color.fromRGB(244, 164, 96), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, arcaneItems.linkStoneItem, book,
                        null, new ItemStack(Material.LEAD), null,
                        book, arcaneItems.linkStoneItem, book
                }, arcaneItems.arcanaTableItem);

        // 15. Bleed (Swords/Axes - Max 8 - Blood Altar)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.bleed", "Bleed", "bleed", "WEAPON_SWORD_AXE", 8, 
                "Applies ticking bleeding stacks on melee hit that continuously drain target health.", 
                org.bukkit.Color.fromRGB(150, 0, 0), org.bukkit.FireworkEffect.Type.BALL, 12,
                new ItemStack[]{
                        arcaneItems.bloodItem, arcaneItems.bloodItem, arcaneItems.bloodItem,
                        book, new ItemStack(Material.WITHER_SKELETON_SKULL), book,
                        arcaneItems.bloodItem, arcaneItems.bloodItem, arcaneItems.bloodItem
                }, arcaneItems.bloodAltarItem);

        // 16. Poison Spores (Swords/Axes - Max 5)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.poison_spores", "Poison Spores", "poison_spores", "WEAPON_SWORD_AXE", 5, 
                "Applies standard Poison effect on hit. Scales duration and proc chance.", 
                org.bukkit.Color.fromRGB(0, 255, 127), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, arcaneItems.poisonVialItem, book,
                        new ItemStack(Material.FERMENTED_SPIDER_EYE), new ItemStack(Material.PUFFERFISH), new ItemStack(Material.FERMENTED_SPIDER_EYE),
                        book, arcaneItems.poisonVialItem, book
                }, arcaneItems.arcanaTableItem);

        // 17. Gas Cloud (Armor - Max 4)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.gas_cloud", "Gas Cloud", "gas_cloud", "ARMOR", 4, 
                "Dealing/taking damage has a chance to release decay toxic gas cloud on block.", 
                org.bukkit.Color.fromRGB(128, 128, 0), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, new ItemStack(Material.GUNPOWDER), book,
                        arcaneItems.poisonVialItem, new ItemStack(Material.GUNPOWDER), arcaneItems.poisonVialItem,
                        book, new ItemStack(Material.GUNPOWDER), book
                }, arcaneItems.arcanaTableItem);

        // 18. Heavy Draw (Bows - Max 5)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.heavy_draw", "Heavy Draw", "heavy_draw", "WEAPON_BOW_ONLY", 5, 
                "Increases arrow draw time by 50% but raises arrow damage by up to +50%.", 
                org.bukkit.Color.fromRGB(105, 105, 105), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        book, new ItemStack(Material.ANVIL), book,
                        new ItemStack(Material.CHIPPED_ANVIL), new ItemStack(Material.BOW), new ItemStack(Material.CHIPPED_ANVIL),
                        book, new ItemStack(Material.ANVIL), book
                }, arcaneItems.arcanaTableItem);

        // 19. Timber (Axes - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.timber", "Timber", "timber", "WEAPON_AXE", 1, 
                "Instantly chops entire trees of connected logs on break. No levels.", 
                org.bukkit.Color.fromRGB(205, 133, 63), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.OAK_LOG), enchBook,
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.OAK_LOG), new ItemStack(Material.IRON_INGOT),
                        enchBook, new ItemStack(Material.IRON_INGOT), enchBook
                }, explorerItems.heavyForgeItem);

        // 20. Soul Harvester (Hoes - Max 1)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.soul_harvester", "Soul Harvester", "soul_harvester", "TOOL_HOE", 1, 
                "Hoe kills have small chance to drop Soul Orbs which give massive XP. No levels.", 
                org.bukkit.Color.fromRGB(72, 61, 139), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.soulOrbItem, enchBook,
                        null, new ItemStack(Material.NETHER_STAR), null,
                        enchBook, arcaneItems.soulOrbItem, enchBook
                }, arcaneItems.arcanaTableItem);

        // 21. Crude Sharpness (Tools/Sticks - Max 7)
        registerMultiLevelRune(explorerRunes, "explorer.runes.crude_sharpness", "Crude Sharpness", "crude_sharpness", "MELEE_OR_STICK", 7, 
                "Adds flat melee damage boost. Applies to pickaxes, shovels, axes, hoes, and sticks.", 
                org.bukkit.Color.fromRGB(192, 192, 192), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        book, new ItemStack(Material.IRON_INGOT), book,
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.STICK), new ItemStack(Material.IRON_INGOT),
                        book, new ItemStack(Material.IRON_INGOT), book
                }, explorerItems.heavyForgeItem);

        // 22. Basalt Trail (Boots - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.basalt_trail", "Basalt Trail", "basalt_trail", "ARMOR_BOOTS", 1, 
                "Cools lava blocks directly beneath your feet into temporary Basalt blocks. No levels.", 
                org.bukkit.Color.fromRGB(255, 127, 80), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.coreOfHeat, enchBook,
                        null, new ItemStack(Material.NETHERITE_BOOTS), null,
                        enchBook, arcaneItems.coreOfHeat, enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.void_walker", "Void Walker", "void_walker", "ARMOR_BOOTS", 1, 
                "Freefalling 20 blocks in the void teleports you safely back to ground. No levels.", 
                org.bukkit.Color.fromRGB(186, 85, 211), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.blessingOfTheVoidItem, enchBook,
                        null, new ItemStack(Material.NETHERITE_BOOTS), null,
                        enchBook, arcaneItems.blessingOfTheVoidItem, enchBook
                }, explorerItems.heavyForgeItem);

        // --- v1.10 New Runes ---

        // Normal Runes
        registerMultiLevelRune(arcaneRunes, "arcane.runes.breach_surge", "Breach Surge", "breach_surge", "WEAPON_SWORD_AXE", 1,
                "Striking an enemy has a 20% chance to release a homing energy projectile to a nearby target.",
                org.bukkit.Color.fromRGB(230, 230, 250), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.REDSTONE), enchBook,
                        new ItemStack(Material.REDSTONE), arcaneItems.getCustomItem("arcane.materials.surge_spark"), new ItemStack(Material.REDSTONE),
                        enchBook, new ItemStack(Material.REDSTONE), enchBook
                }, arcaneItems.arcanaTableItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.aegis_guard", "Aegis Guard", "aegis_guard", "SHIELD", 1,
                "Successfully blocking an attack grants the wielder 4 seconds of Resistance II and Regeneration I.",
                org.bukkit.Color.fromRGB(0, 128, 128), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.OBSIDIAN), enchBook,
                        new ItemStack(Material.OBSIDIAN), arcaneItems.getCustomItem("arcane.materials.gorgon_scale"), new ItemStack(Material.OBSIDIAN),
                        enchBook, new ItemStack(Material.OBSIDIAN), enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.mach_rush", "Mach Rush", "mach_rush", "ARMOR_BOOTS", 1,
                "Sprinting continuously builds up a speed multiplier. Resets on stop/jump/swing.",
                org.bukkit.Color.fromRGB(255, 215, 0), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.COPPER_INGOT), enchBook,
                        new ItemStack(Material.COPPER_INGOT), arcaneItems.getCustomItem("arcane.materials.kinetic_battery"), new ItemStack(Material.COPPER_INGOT),
                        enchBook, new ItemStack(Material.COPPER_INGOT), enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(arcaneRunes, "arcane.runes.rift_walk", "Rift Walk", "rift_walk", "ARMOR", 1,
                "Double-tapping sneak shifts you into parallel rift for 5 seconds (immune to all damage, cannot attack).",
                org.bukkit.Color.fromRGB(75, 0, 130), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        null, arcaneItems.getCustomItem("arcane.materials.ender_essence"), null,
                        arcaneItems.getCustomItem("arcane.materials.ender_essence"), new ItemStack(Material.DIAMOND_LEGGINGS), arcaneItems.getCustomItem("arcane.materials.ender_essence"),
                        null, arcaneItems.getCustomItem("arcane.materials.ender_essence"), null
                }, arcaneItems.arcanaTableItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.daedalus_touch", "Daedalus' Touch", "daedalus_touch", "TOOLS", 1,
                "Sneak-mining an ore block vein-mines up to 16 connected blocks of the same type.",
                org.bukkit.Color.fromRGB(255, 250, 250), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.DIAMOND), enchBook,
                        new ItemStack(Material.DIAMOND), arcaneItems.getCustomItem("arcane.materials.synthetic_diamond"), new ItemStack(Material.DIAMOND),
                        enchBook, new ItemStack(Material.DIAMOND), enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.ouroboros", "Ouroboros", "ouroboros", "SHIELD", 1,
                "The shield never breaks, consuming 1x Ender Essence to repair itself at 0 durability.",
                org.bukkit.Color.fromRGB(50, 205, 50), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.SLIME_BALL), enchBook,
                        new ItemStack(Material.SLIME_BALL), arcaneItems.getCustomItem("arcane.materials.serpent_scale"), new ItemStack(Material.SLIME_BALL),
                        enchBook, new ItemStack(Material.SLIME_BALL), enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(arcaneRunes, "arcane.runes.vortex", "Vortex", "vortex", "WEAPON_SWORD", 3,
                "Attacks have a chance to pull all mobs in a 10-block area in front of you. Level increases chance.",
                org.bukkit.Color.fromRGB(64, 224, 208), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.PRISMARINE_SHARD), enchBook,
                        new ItemStack(Material.PRISMARINE_SHARD), arcaneItems.getCustomItem("arcane.materials.resonant_crystal"), new ItemStack(Material.PRISMARINE_SHARD),
                        enchBook, new ItemStack(Material.PRISMARINE_SHARD), enchBook
                }, arcaneItems.arcanaTableItem);

        registerMultiLevelRune(arcaneRunes, "arcane.runes.shrapnel_shot", "Shrapnel Shot", "shrapnel_shot", "WEAPON_BOW", 3,
                "Shoots a shotgun blast of arrows (4-10) with 2 punch through. Incompatible with Multishot.",
                org.bukkit.Color.fromRGB(244, 164, 96), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.ARROW), enchBook,
                        new ItemStack(Material.ARROW), arcaneItems.getCustomItem("arcane.materials.radiant_core"), new ItemStack(Material.ARROW),
                        enchBook, new ItemStack(Material.ARROW), enchBook
                }, arcaneItems.arcanaTableItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.resonance_ping", "Resonance Ping", "resonance_ping", "TOOLS", 1,
                "Sneaking while holding pickaxe outlines valuable ores within 10 blocks for 2 seconds.",
                org.bukkit.Color.fromRGB(135, 206, 250), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.NOTE_BLOCK), enchBook,
                        new ItemStack(Material.NOTE_BLOCK), arcaneItems.getCustomItem("arcane.materials.resonant_crystal"), new ItemStack(Material.NOTE_BLOCK),
                        enchBook, new ItemStack(Material.NOTE_BLOCK), enchBook
                }, explorerItems.heavyForgeItem);

        registerMultiLevelRune(explorerRunes, "explorer.runes.naiads_repel", "Naiad's Repel", "naiads_repel", "ARMOR", 1,
                "Pushes water away, creating a 3x3 pocket of breathable air around you when submerged.",
                org.bukkit.Color.fromRGB(30, 144, 255), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.PRISMARINE_CRYSTALS), enchBook,
                        new ItemStack(Material.PRISMARINE_CRYSTALS), arcaneItems.getCustomItem("arcane.materials.abyssal_sponge"), new ItemStack(Material.PRISMARINE_CRYSTALS),
                        enchBook, new ItemStack(Material.PRISMARINE_CRYSTALS), enchBook
                }, explorerItems.heavyForgeItem);

        // Demonic Runes
        registerMultiLevelRune(arcaneRunes, "arcane.runes.miasma", "Miasma", "miasma", "WEAPON_BOW", 1,
                "Arrows detonate on impact, releasing a 4x4 cloud of spores that damages entities.",
                org.bukkit.Color.fromRGB(34, 139, 34), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.FERMENTED_SPIDER_EYE), enchBook,
                        new ItemStack(Material.FERMENTED_SPIDER_EYE), arcaneItems.getCustomItem("arcane.materials.corrupted_spore"), new ItemStack(Material.FERMENTED_SPIDER_EYE),
                        enchBook, new ItemStack(Material.FERMENTED_SPIDER_EYE), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.fenrirs_bite", "Fenrir's Bite", "fenrirs_bite", "WEAPON_AXE", 1,
                "Deals 100% bonus damage if the target is at maximum health.",
                org.bukkit.Color.fromRGB(128, 0, 0), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.FLINT), enchBook,
                        new ItemStack(Material.FLINT), arcaneItems.getCustomItem("arcane.materials.beast_fang"), new ItemStack(Material.FLINT),
                        enchBook, new ItemStack(Material.FLINT), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.anubis_judgment", "Anubis' Judgment", "anubis_judgment", "WEAPON_SWORD", 1,
                "Executing strikes on enemies below 20% health instantly kills them, dropping double loot/XP.",
                org.bukkit.Color.fromRGB(218, 165, 32), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GOLD_INGOT), enchBook,
                        new ItemStack(Material.GOLD_INGOT), arcaneItems.getCustomItem("arcane.materials.jackal_idol"), new ItemStack(Material.GOLD_INGOT),
                        enchBook, new ItemStack(Material.GOLD_INGOT), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.gloom", "Gloom", "gloom", "ARMOR_CHESTPLATE", 1,
                "Slows all nearby hostile mobs in a 5-block dark aura, passively repairing armor durability.",
                org.bukkit.Color.fromRGB(47, 79, 79), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.SOUL_SAND), enchBook,
                        new ItemStack(Material.SOUL_SAND), arcaneItems.getCustomItem("arcane.materials.cursed_diamond"), new ItemStack(Material.SOUL_SAND),
                        enchBook, new ItemStack(Material.SOUL_SAND), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.cerberus_maw", "Cerberus' Maw", "cerberus_maw", "WEAPON_AXE", 1,
                "Cleaving an enemy inflicts Bleed. If they die while bleeding, drops a blood orb.",
                org.bukkit.Color.fromRGB(139, 0, 0), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.COAL), enchBook,
                        new ItemStack(Material.COAL), arcaneItems.getCustomItem("arcane.materials.beast_fang"), new ItemStack(Material.COAL),
                        enchBook, new ItemStack(Material.COAL), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.styxs_toll", "Styx's Toll", "styxs_toll", "WEAPON_BOW", 1,
                "Roots hit targets to the ground for 3 seconds, disabling movement and teleportation.",
                org.bukkit.Color.fromRGB(105, 105, 105), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GOLD_NUGGET), enchBook,
                        new ItemStack(Material.GOLD_NUGGET), arcaneItems.getCustomItem("arcane.materials.underworld_coin"), new ItemStack(Material.GOLD_NUGGET),
                        enchBook, new ItemStack(Material.GOLD_NUGGET), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.infernalis", "Infernalis", "infernalis", "ARMOR_CHESTPLATE", 2,
                "Permanently burns wearer (makes immune to fire/lava). Melee attacks gain +20% fire damage.",
                org.bukkit.Color.fromRGB(255, 69, 0), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.BLAZE_POWDER), enchBook,
                        new ItemStack(Material.BLAZE_POWDER), arcaneItems.getCustomItem("arcane.materials.cinder_core"), new ItemStack(Material.BLAZE_POWDER),
                        enchBook, new ItemStack(Material.BLAZE_POWDER), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.remedium", "Remedium", "remedium", "WEAPON_SWORD", 1,
                "Sneak right-click consumes 50% health to purge negative effects and grant 10s Strength II.",
                org.bukkit.Color.fromRGB(148, 0, 211), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GHAST_TEAR), enchBook,
                        new ItemStack(Material.GHAST_TEAR), arcaneItems.getCustomItem("arcane.materials.vial_of_demonic_blood"), new ItemStack(Material.GHAST_TEAR),
                        enchBook, new ItemStack(Material.GHAST_TEAR), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.brimstone", "Brimstone", "brimstone", "WEAPON_BOW", 2,
                "Projectiles explode into burning ash (inflicts Wither I and slowness on non-demonic targets).",
                org.bukkit.Color.fromRGB(255, 99, 71), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GUNPOWDER), enchBook,
                        new ItemStack(Material.GUNPOWDER), arcaneItems.getCustomItem("arcane.materials.sulfur_clump"), new ItemStack(Material.GUNPOWDER),
                        enchBook, new ItemStack(Material.GUNPOWDER), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.ashen_veil", "Ashen Veil", "ashen_veil", "ARMOR", 1,
                "Crouching leaves a trail of black smoke and ash clouds that blind/suffocate enemies.",
                org.bukkit.Color.fromRGB(54, 54, 54), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.COAL), enchBook,
                        new ItemStack(Material.COAL), arcaneItems.getCustomItem("arcane.materials.withered_wrap"), new ItemStack(Material.COAL),
                        enchBook, new ItemStack(Material.COAL), enchBook
                }, arcaneItems.getCustomItem("machinery.blood_altar"));

        // Holy Runes
        registerMultiLevelRune(explorerRunes, "explorer.runes.hermes_tread", "Hermes' Tread", "hermes_tread", "ARMOR_BOOTS", 1,
                "Grants permanent Speed II and allows automatic 1-block step up elevation.",
                org.bukkit.Color.fromRGB(240, 248, 255), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.FEATHER), enchBook,
                        new ItemStack(Material.FEATHER), arcaneItems.getCustomItem("arcane.materials.winged_insignia"), new ItemStack(Material.FEATHER),
                        enchBook, new ItemStack(Material.FEATHER), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.warding_halo", "Warding Halo", "warding_halo", "ARMOR_CHESTPLATE", 1,
                "Grants protective rings that nullify the damage of the next 3 incoming attacks.",
                org.bukkit.Color.fromRGB(255, 255, 240), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.BLAZE_ROD), enchBook,
                        new ItemStack(Material.BLAZE_ROD), arcaneItems.getCustomItem("arcane.materials.blazing_chakram"), new ItemStack(Material.BLAZE_ROD),
                        enchBook, new ItemStack(Material.BLAZE_ROD), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(explorerRunes, "explorer.runes.radial_blind", "Radial Blind", "radial_blind", "SHIELD", 1,
                "Blocking a heavy attack emits a blinding flash, blinding and slowing hostile mobs.",
                org.bukkit.Color.fromRGB(255, 255, 224), org.bukkit.FireworkEffect.Type.BALL, 8,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook,
                        new ItemStack(Material.GLOWSTONE_DUST), arcaneItems.getCustomItem("arcane.materials.radiant_core"), new ItemStack(Material.GLOWSTONE_DUST),
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.apollos_ray", "Apollo's Ray", "apollos_ray", "WEAPON_BOW_ONLY", 1,
                "Fully drawn arrows transform into hitscan beams of holy fire.",
                org.bukkit.Color.fromRGB(255, 140, 0), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook,
                        new ItemStack(Material.GLOWSTONE_DUST), arcaneItems.getCustomItem("arcane.materials.sun_kissed_feather"), new ItemStack(Material.GLOWSTONE_DUST),
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.trinitys_well", "Trinity's Well", "trinitys_well", "WEAPON_BOW", 1,
                "Shooting allies heals them. Shooting enemies marks them with light to leech health.",
                org.bukkit.Color.fromRGB(152, 251, 152), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook,
                        new ItemStack(Material.GLOWSTONE_DUST), arcaneItems.getCustomItem("arcane.materials.radiant_geode"), new ItemStack(Material.GLOWSTONE_DUST),
                        enchBook, new ItemStack(Material.GLOWSTONE_DUST), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.valkyries_grace", "Valkyrie's Grace", "valkyries_grace", "ARMOR", 1,
                "Fatal damage triggers blinding knockback and Regeneration III/Resistance II.",
                org.bukkit.Color.fromRGB(245, 245, 220), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.EMERALD), enchBook,
                        new ItemStack(Material.EMERALD), arcaneItems.getCustomItem("arcane.materials.sun_kissed_feather"), new ItemStack(Material.EMERALD),
                        enchBook, new ItemStack(Material.EMERALD), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(explorerRunes, "explorer.runes.hallowed_ground", "Hallowed Ground", "hallowed_ground", "ARMOR_BOOTS", 1,
                "Walking creates a holy fire trail that damages Undead and cures poison/wither.",
                org.bukkit.Color.fromRGB(127, 255, 212), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.AMETHYST_SHARD), enchBook,
                        new ItemStack(Material.AMETHYST_SHARD), arcaneItems.getCustomItem("arcane.materials.purified_core"), new ItemStack(Material.AMETHYST_SHARD),
                        enchBook, new ItemStack(Material.AMETHYST_SHARD), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));

        registerMultiLevelRune(arcaneRunes, "arcane.runes.smite_of_jupiter", "Smite of Jupiter", "smite_of_jupiter", "WEAPON_SWORD", 1,
                "Fully charged sweep attacks call down localized silent lightning on Undead.",
                org.bukkit.Color.fromRGB(224, 255, 255), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, new ItemStack(Material.COPPER_INGOT), enchBook,
                        new ItemStack(Material.COPPER_INGOT), arcaneItems.getCustomItem("arcane.materials.stormcaller_medallion"), new ItemStack(Material.COPPER_INGOT),
                        enchBook, new ItemStack(Material.COPPER_INGOT), enchBook
                }, arcaneItems.getCustomItem("machinery.blessings_altar"));
    }

    private void registerMultiLevelRune(
            CodexCategory category,
            String baseId,
            String displayName,
            String effect,
            String type,
            int maxLevel,
            String desc,
            org.bukkit.Color color,
            org.bukkit.FireworkEffect.Type starType,
            int xpBase,
            ItemStack[] lvl1Recipe,
            ItemStack recipeStation
    ) {
        Material mat = arcaneItems.getRuneMaterial(effect);
        String subCatId;
        if (category.getId().startsWith("arcane")) {
            if (mat == Material.GUSTER_BANNER_PATTERN) {
                subCatId = "arcane.runes.demonic";
            } else if (mat == Material.PRISMARINE_SHARD) {
                subCatId = "arcane.runes.holy";
            } else {
                subCatId = "arcane.runes.normal";
            }
        } else {
            if (mat == Material.GUSTER_BANNER_PATTERN) {
                subCatId = "explorer.runes.demonic";
            } else if (mat == Material.PRISMARINE_SHARD) {
                subCatId = "explorer.runes.holy";
            } else {
                subCatId = "explorer.runes.normal";
            }
        }
        CodexCategory targetCategory = registry.getCategory(subCatId);
        if (targetCategory == null) {
            targetCategory = category;
        }

        // Register Level 1
        String l1Name = maxLevel == 1 ? displayName : displayName + " I";
        ItemStack lvl1Rune = arcaneItems.createCustomRune(baseId, displayName, effect, type, 1, desc, color, starType, false, false);
        CodexItem lvl1Codex = new CodexItem.Builder(baseId)
                .displayName(l1Name)
                .displayItem(lvl1Rune)
                .xpCost(xpBase)
                .requires(lvl1Rune)
                .description(desc)
                .craftingStation(recipeStation)
                .recipe(lvl1Recipe)
                .build();
        targetCategory.addItem(lvl1Codex);

        // Register Levels 2 to maxLevel
        for (int lvl = 2; lvl <= maxLevel; lvl++) {
            String id = baseId + "_" + lvl;
            String roman = getRomanNum(lvl);
            ItemStack prevRune = arcaneItems.createCustomRune(baseId, displayName, effect, type, lvl - 1, desc, color, getStarTypeForLevel(lvl - 1, starType), hasTrail(lvl - 1), hasFlicker(lvl - 1));
            ItemStack currentRune = arcaneItems.createCustomRune(baseId, displayName, effect, type, lvl, desc, color, getStarTypeForLevel(lvl, starType), hasTrail(lvl), hasFlicker(lvl));

            CodexItem lvlCodex = new CodexItem.Builder(id)
                    .displayName(displayName + " " + roman)
                    .displayItem(currentRune)
                    .xpCost(xpBase + (lvl - 1) * 4)
                    .requires(currentRune)
                    .description("Upgraded " + displayName + " Rune.")
                    .craftingStation(arcaneItems.upgradeTableItem)
                    .recipe(
                            null, prevRune.clone(), null,
                            null, prevRune.clone(), null,
                            null, null, null
                    )
                    .build();
            targetCategory.addItem(lvlCodex);
        }
    }

    private String getRomanNum(int level) {
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

    private org.bukkit.FireworkEffect.Type getStarTypeForLevel(int level, org.bukkit.FireworkEffect.Type baseType) {
        if (level >= 4) return org.bukkit.FireworkEffect.Type.STAR;
        if (level >= 2) return org.bukkit.FireworkEffect.Type.BALL_LARGE;
        return baseType;
    }

    private boolean hasTrail(int level) {
        return level >= 2;
    }

    private boolean hasFlicker(int level) {
        return level >= 3;
    }

    public void registerCommands(Commands commands) {
        commands.register(Commands.literal("codex")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    giveCodex(player);
                    return 1;
                })
                .then(Commands.literal("wiki")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    showWikiCategories(player);
                    return 1;
                })
                .then(Commands.argument("parent", StringArgumentType.word())
                    .suggests((ctx, b) -> {
                        String remaining = b.getRemaining().toLowerCase();
                        registry.getCategories().stream()
                            .map(CodexCategory::getParentCategoryId)
                            .distinct()
                            .filter(c -> c.toLowerCase().startsWith(remaining))
                            .forEach(b::suggest);
                        return b.buildFuture();
                    })
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String parent = ctx.getArgument("parent", String.class);
                        showWikiSubcategories(player, parent);
                        return 1;
                    })
                    .then(Commands.argument("subcategory", StringArgumentType.word())
                        .suggests((ctx, b) -> {
                            String parent = ctx.getArgument("parent", String.class);
                            String remaining = b.getRemaining().toLowerCase();
                            registry.getCategories().stream()
                                .filter(c -> c.getParentCategoryId().equalsIgnoreCase(parent))
                                .map(CodexCategory::getId)
                                .filter(id -> id.toLowerCase().startsWith(remaining))
                                .forEach(b::suggest);
                            return b.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                            String subId = ctx.getArgument("subcategory", String.class);
                            showWikiItems(player, subId);
                            return 1;
                        })
                        .then(Commands.argument("item", StringArgumentType.word())
                            .suggests((ctx, b) -> {
                                String subId = ctx.getArgument("subcategory", String.class);
                                String remaining = b.getRemaining().toLowerCase();
                                CodexCategory cat = registry.getCategory(subId);
                                if (cat != null) {
                                    cat.getItems().stream()
                                        .map(CodexItem::getId)
                                        .filter(id -> id.toLowerCase().startsWith(remaining))
                                        .forEach(b::suggest);
                                }
                                return b.buildFuture();
                            })
                            .executes(ctx -> {
                                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                String itemId = ctx.getArgument("item", String.class);
                                showWikiItemDetail(player, itemId);
                                return 1;
                            })
                        )
                    )
                )
            ).build(), "Receive the Codex guide book", List.of("wiki"));

        commands.register(Commands.literal("codexitem")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                        return 1;
                    }
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item> [amount]")));
                    return 1;
                })
                .then(Commands.argument("category", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();
                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) return builder.buildFuture();

                            String remaining = builder.getRemaining().toLowerCase();
                            registry.getCategories().stream()
                                    .map(CodexCategory::getParentCategoryId)
                                    .distinct()
                                    .filter(c -> c.toLowerCase().startsWith(remaining))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                return 1;
                            }
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item> [amount]")));
                            return 1;
                        })
                        .then(Commands.argument("subcategory", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();
                                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) return builder.buildFuture();

                                    String category = ctx.getArgument("category", String.class);
                                    String remaining = builder.getRemaining().toLowerCase();

                                    registry.getCategories().stream()
                                            .filter(c -> c.getParentCategoryId().equalsIgnoreCase(category))
                                            .map(CodexCategory::getId)
                                            .filter(id -> id.toLowerCase().startsWith(remaining))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                        return 1;
                                    }
                                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item> [amount]")));
                                    return 1;
                                })
                                .then(Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();
                                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) return builder.buildFuture();

                                            String subcategory = ctx.getArgument("subcategory", String.class);
                                            String remaining = builder.getRemaining().toLowerCase();

                                            CodexCategory cat = registry.getCategory(subcategory);
                                            if (cat != null) {
                                                cat.getItems().stream()
                                                        .map(CodexItem::getId)
                                                        .filter(id -> id.toLowerCase().startsWith(remaining))
                                                        .forEach(builder::suggest);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                                return 1;
                                            }

                                            String itemId = ctx.getArgument("item", String.class);
                                            CodexItem item = registry.getItem(itemId);
                                            if (item == null) {
                                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Item not found: ") + itemId));
                                                return 1;
                                            }

                                            ItemStack stack = item.getDisplayItem().clone();
                                            if (player.getInventory().firstEmpty() == -1) {
                                                player.getWorld().dropItemNaturally(player.getLocation(), stack);
                                            } else {
                                                player.getInventory().addItem(stack);
                                            }
                                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                                            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Gave you: ") + item.getDisplayName()));
                                            return 1;
                                        })
                                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                                        return 1;
                                                    }

                                                    String itemId = ctx.getArgument("item", String.class);
                                                    int amount = ctx.getArgument("amount", Integer.class);
                                                    CodexItem item = registry.getItem(itemId);
                                                    if (item == null) {
                                                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Item not found: ") + itemId));
                                                        return 1;
                                                    }

                                                    ItemStack stack = item.getDisplayItem().clone();
                                                    stack.setAmount(amount);
                                                    if (player.getInventory().firstEmpty() == -1) {
                                                        player.getWorld().dropItemNaturally(player.getLocation(), stack);
                                                    } else {
                                                        player.getInventory().addItem(stack);
                                                    }
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                                                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Gave you: ") + amount + "x " + item.getDisplayName()));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                ).build(), "Give a custom codex item", List.of());

        commands.register(Commands.literal("codexenchant")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                        return 1;
                    }
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexenchant <enchantment> <level>")));
                    return 1;
                })
                .then(Commands.argument("enchantment", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();
                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) return builder.buildFuture();

                            ItemStack held = player.getInventory().getItemInMainHand();
                            if (held == null || held.getType() == org.bukkit.Material.AIR) return builder.buildFuture();

                            String remaining = builder.getRemaining().toLowerCase();
                            registry.getCategories().stream()
                                    .filter(c -> c.getId().contains(".runes"))
                                    .flatMap(c -> c.getItems().stream())
                                    .filter(cItem -> {
                                        ItemMeta m = cItem.getDisplayItem().getItemMeta();
                                        if (m == null) return false;
                                        String rType = m.getPersistentDataContainer().get(new NamespacedKey(plugin, "rune_type"), PersistentDataType.STRING);
                                        return isRuneTypeValidForItem(rType, held.getType());
                                    })
                                    .map(item -> {
                                        ItemMeta m = item.getDisplayItem().getItemMeta();
                                        if (m == null) return null;
                                        return m.getPersistentDataContainer().get(new NamespacedKey(plugin, "rune_effect"), PersistentDataType.STRING);
                                    })
                                    .filter(java.util.Objects::nonNull)
                                    .distinct()
                                    .filter(eff -> eff.toLowerCase().startsWith(remaining))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                            if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                return 1;
                            }
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexenchant <enchantment> <level>")));
                            return 1;
                        })
                        .then(Commands.argument("level", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 10))
                                .suggests((ctx, builder) -> {
                                    if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();
                                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) return builder.buildFuture();
                                    try {
                                        String effect = ctx.getArgument("enchantment", String.class);
                                        int maxLvl = codexCrafting.getRuneMaxLevel(effect);
                                        if (maxLvl <= 0) maxLvl = 5;
                                        for (int i = 1; i <= maxLvl; i++) {
                                            builder.suggest(String.valueOf(i));
                                        }
                                    } catch (Exception e) {
                                        for (int i = 1; i <= 5; i++) {
                                            builder.suggest(String.valueOf(i));
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                                        return 1;
                                    }

                                    ItemStack held = player.getInventory().getItemInMainHand();
                                    if (held == null || held.getType() == org.bukkit.Material.AIR) {
                                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item in your main hand!")));
                                        return 1;
                                    }

                                    String effect = ctx.getArgument("enchantment", String.class);
                                    int level = ctx.getArgument("level", Integer.class);

                                    ItemMeta meta = held.getItemMeta();
                                    if (meta == null) return 1;

                                    NamespacedKey applyKey = new NamespacedKey(plugin, "rune_" + effect);
                                    meta.getPersistentDataContainer().set(applyKey, PersistentDataType.INTEGER, level);

                                    if (effect.equalsIgnoreCase("catch_flame")) {
                                        if (held.getType().name().contains("SWORD")) {
                                            meta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
                                        } else if (held.getType().name().contains("BOW")) {
                                            meta.addEnchant(org.bukkit.enchantments.Enchantment.FLAME, 1, true);
                                        }
                                    }

                                    held.setItemMeta(meta);
                                    space.qclid.dashboard.util.TextUtil.refreshItemLore(held, plugin);

                                    String roman = getRomanNum(level);
                                    String nameBase = getEffectDisplayName(effect);
                                    String displayEffectName = nameBase + " " + roman;

                                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully applied ") + C_GRAY + toSmallCaps(displayEffectName) + C_GREEN + toSmallCaps(" to your held item!")));
                                    return 1;
                                })
                        )
                ).build(), "Enchant held item with a custom codex rune", List.of());

        commands.register(Commands.literal("enchantment")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /enchantment <enchantment>")));
                    return 1;
                })
                .then(Commands.argument("enchantment", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (!(ctx.getSource().getSender() instanceof Player)) return builder.buildFuture();
                            String remaining = builder.getRemaining().toLowerCase();
                            for (CodexCategory cat : registry.getCategories()) {
                                if (!cat.getId().contains(".runes")) continue;
                                for (CodexItem item : cat.getItems()) {
                                    ItemMeta m = item.getDisplayItem().getItemMeta();
                                    if (m == null) continue;
                                    String eff = m.getPersistentDataContainer().get(new NamespacedKey(plugin, "rune_effect"), PersistentDataType.STRING);
                                    if (eff != null && eff.toLowerCase().startsWith(remaining)) {
                                        builder.suggest(eff);
                                    }
                                }
                            }
                            for (org.bukkit.enchantments.Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
                                String name = ench.getKey().getKey();
                                if (name.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(name);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                            String effect = ctx.getArgument("enchantment", String.class);

                            String displayName = getEffectDisplayName(effect);
                            String desc = getEnchantmentDescription(effect);
                            if (desc == null) {
                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Unknown enchantment: ") + effect));
                                return 1;
                            }
                            int maxLvl = getEnchantmentMaxLevel(effect);
                            String obtain = getEnchantmentObtainMethod(effect);

                            player.sendMessage(MM.deserialize(C_PURPLE + "<bold>" + toSmallCaps("=== " + displayName + " ===")));
                            player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Description") + ": " + C_GRAY + toSmallCaps(desc)));
                            player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Max Level") + ": " + C_ORANGE + toSmallCaps(String.valueOf(maxLvl))));
                            if (obtain != null) {
                                player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("How to Obtain") + ": " + C_GRAY + toSmallCaps(obtain)));
                            }
                            return 1;
                        })
                ).build(), "View information about an enchantment", List.of());

        commands.register(Commands.literal("codexdummy")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                        return 1;
                    }

                    Location loc = player.getLocation();
                    Zombie zombie = player.getWorld().spawn(loc, Zombie.class, z -> {
                        z.setAI(false);
                        z.setInvulnerable(false);
                        z.setCustomNameVisible(true);
                        z.customName(MM.deserialize(C_GRAY + "[<red>Dummy" + C_GRAY + "]"));
                        z.setCanPickupItems(false);
                        z.getEquipment().setHelmet(new ItemStack(org.bukkit.Material.IRON_HELMET));
                        z.getEquipment().setChestplate(new ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
                        z.getEquipment().setLeggings(new ItemStack(org.bukkit.Material.IRON_LEGGINGS));
                        z.getEquipment().setBoots(new ItemStack(org.bukkit.Material.IRON_BOOTS));
                    });
                    setMetadata(zombie, plugin, "codex_dummy", true);

                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Spawned a damage test dummy!")));
                    return 1;
                }).build(), "Spawn a damage test dummy", List.of());
    }

    public String getEnchantmentDescription(String effect) {
        if (effect == null) return "";
        String lower = effect.toLowerCase().replace(" ", "_");

        // Check custom rune descriptions first
        String runeDesc = arcaneItems.getRuneDescription(lower);
        if (runeDesc != null && !runeDesc.isEmpty()) return runeDesc;

        // Vanilla enchantments
        String vanillaKey = "minecraft:" + lower;
        for (org.bukkit.enchantments.Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
            if (ench.getKey().toString().equalsIgnoreCase(vanillaKey) ||
                ench.getKey().getKey().equalsIgnoreCase(lower)) {
                String name = getEffectDisplayName(ench.getKey().getKey());
                return name + " (Vanilla) - Max Level " + ench.getMaxLevel() + ". " + getVanillaEnchantDesc(ench);
            }
        }
        return null;
    }

    private String getVanillaEnchantDesc(org.bukkit.enchantments.Enchantment ench) {
        String key = ench.getKey().getKey();
        switch (key) {
            case "sharpness": return "Increases melee damage.";
            case "protection": return "Reduces most types of damage.";
            case "fire_protection": return "Reduces fire damage.";
            case "feather_falling": return "Reduces fall damage.";
            case "blast_protection": return "Reduces explosion damage.";
            case "projectile_protection": return "Reduces projectile damage.";
            case "respiration": return "Extends underwater breathing time.";
            case "aqua_affinity": return "Speeds up underwater mining.";
            case "thorns": return "Damages attackers.";
            case "depth_strider": return "Increases underwater movement speed.";
            case "frost_walker": return "Freezes water into ice.";
            case "soul_speed": return "Increases movement speed on soul sand/soil.";
            case "swift_sneak": return "Increases movement speed while sneaking.";
            case "efficiency": return "Increases mining speed.";
            case "unbreaking": return "Increases item durability.";
            case "fortune": return "Increases block drop quantity.";
            case "silk_touch": return "Mined blocks drop themselves.";
            case "power": return "Increases arrow damage.";
            case "punch": return "Increases arrow knockback.";
            case "flame": return "Sets arrows on fire.";
            case "infinity": return "Shooting consumes no arrows.";
            case "knockback": return "Increases sword knockback.";
            case "fire_aspect": return "Sets targets on fire.";
            case "looting": return "Increases mob loot.";
            case "sweeping": return "Increases sweep attack damage.";
            case "smite": return "Increases damage to undead.";
            case "bane_of_arthropods": return "Increases damage to arthropods.";
            case "loyalty": return "Returns trident after throw.";
            case "impaling": return "Increases damage to aquatic mobs.";
            case "riptide": return "Launches with trident in rain/water.";
            case "channeling": return "Summons lightning during storms.";
            case "multishot": return "Fires 3 arrows from crossbow.";
            case "quick_charge": return "Speeds up crossbow loading.";
            case "piercing": return "Arrows pierce through entities.";
            case "density": return "Increases damage based on fall distance.";
            case "breach": return "Reduces target armor effectiveness.";
            case "wind_burst": return "Creates wind burst on hit.";
            default: return "A vanilla Minecraft enchantment.";
        }
    }

    public String getEnchantmentObtainMethod(String effect) {
        if (effect == null) return "Unknown";
        String lower = effect.toLowerCase().replace(" ", "_");

        // Check registry for custom rune
        for (CodexCategory cat : registry.getCategories()) {
            if (!cat.getId().contains(".runes")) continue;
            for (CodexItem item : cat.getItems()) {
                ItemMeta m = item.getDisplayItem().getItemMeta();
                if (m == null) continue;
                String runeEffect = m.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "rune_effect"), PersistentDataType.STRING);
                if (runeEffect != null && runeEffect.equalsIgnoreCase(lower)) {
                    String stationName = "Arcana Table";
                    if (cat.getId().contains("demonic")) stationName = "Blood Altar";
                    else if (cat.getId().contains("holy")) stationName = "Blessings Altar";
                    String catName = cat.getParentCategoryId().equals("arcane") ? "Arcane" : "Explorer";
                    return "Craftable at the " + stationName + " (" + catName + " Runes). Level I recipe available at XP cost " + item.getXpCost() + ".";
                }
            }
        }

        // Vanilla
        String vanillaKey = "minecraft:" + lower;
        for (org.bukkit.enchantments.Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
            if (ench.getKey().toString().equalsIgnoreCase(vanillaKey) ||
                ench.getKey().getKey().equalsIgnoreCase(lower)) {
                return "Obtainable from an Enchanting Table or by trading with Librarian Villagers.";
            }
        }
        return null;
    }

    public int getEnchantmentMaxLevel(String effect) {
        if (effect == null) return 1;
        String lower = effect.toLowerCase().replace(" ", "_");
        int maxLvl = codexCrafting.getRuneMaxLevel(lower);
        if (maxLvl > 0) return maxLvl;
        for (org.bukkit.enchantments.Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
            if (ench.getKey().getKey().equalsIgnoreCase(lower)) {
                return ench.getMaxLevel();
            }
        }
        return 1;
    }

    private void giveCodex(Player player) {
        ItemStack codex = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = codex.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "codex");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("The Codex")));
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

    // ── /codex wiki helpers ───────────────────────────────────────────────────

    private void showWikiCategories(Player player) {
        player.sendMessage(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps("Codex Wiki — Categories")));
        player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("Click a category to browse its subcategories.")));
        player.sendMessage(Component.empty());

        registry.getCategories().stream()
            .map(CodexCategory::getParentCategoryId)
            .distinct()
            .forEach(parent -> {
                String cmd = "/codex wiki " + parent;
                player.sendMessage(MM.deserialize(
                    C_GREEN + "  • " +
                    "<click:run_command:'" + cmd + "'>" +
                    "<hover:show_text:'" + toSmallCaps("Browse ") + parent + "'>" +
                    C_ORANGE + toSmallCaps(parent) +
                    "</hover></click>"
                ));
            });
    }

    private void showWikiSubcategories(Player player, String parent) {
        player.sendMessage(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps("Codex Wiki — ") + toSmallCaps(parent)));
        player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("Click a subcategory to see its items.")));
        player.sendMessage(Component.empty());

        registry.getCategories().stream()
            .filter(c -> c.getParentCategoryId().equalsIgnoreCase(parent))
            .forEach(cat -> {
                String cmd = "/codex wiki " + parent + " " + cat.getId();
                int itemCount = cat.getItems().size();
                player.sendMessage(MM.deserialize(
                    C_GREEN + "  • " +
                    "<click:run_command:'" + cmd + "'>" +
                    "<hover:show_text:'" + itemCount + " " + toSmallCaps("items") + "'>" +
                    C_ORANGE + toSmallCaps(cat.getDisplayName()) + C_GRAY + " (" + itemCount + ")" +
                    "</hover></click>"
                ));
            });

        // Back button
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<click:run_command:'/codex wiki'><hover:show_text:'" + toSmallCaps("Back to categories") + "'>" +
            C_GRAY + "<bold>← " + toSmallCaps("Back") + "</bold></hover></click>"
        ));
    }

    private void showWikiItems(Player player, String subcategoryId) {
        CodexCategory cat = registry.getCategory(subcategoryId);
        if (cat == null) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Subcategory not found!")));
            return;
        }

        player.sendMessage(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps("Codex Wiki — ") + toSmallCaps(cat.getDisplayName())));
        player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("Click an item to see its details.")));
        player.sendMessage(Component.empty());

        for (CodexItem item : cat.getItems()) {
            String cmd = "/codex wiki " + cat.getParentCategoryId() + " " + subcategoryId + " " + item.getId();
            boolean unlocked = manager.isUnlocked(player.getUniqueId(), item.getId());
            String color = unlocked ? C_GREEN : C_RED;
            String status = unlocked ? toSmallCaps("Unlocked") : toSmallCaps("Locked");
            String desc = item.getDescription() != null && !item.getDescription().isEmpty()
                ? item.getDescription() : toSmallCaps("No description");
            player.sendMessage(MM.deserialize(
                "  " + color + "• " +
                "<click:run_command:'" + cmd + "'>" +
                "<hover:show_text:'" + status + "\n" + C_GRAY + desc + "'>" +
                C_ORANGE + toSmallCaps(item.getDisplayName()) +
                "</hover></click>"
            ));
        }

        // Back button
        player.sendMessage(Component.empty());
        String parentCmd = "/codex wiki " + cat.getParentCategoryId();
        player.sendMessage(MM.deserialize(
            "<click:run_command:'" + parentCmd + "'><hover:show_text:'" + toSmallCaps("Back to subcategories") + "'>" +
            C_GRAY + "<bold>← " + toSmallCaps("Back") + "</bold></hover></click>"
        ));
    }

    private void showWikiItemDetail(Player player, String itemId) {
        CodexItem item = registry.getItem(itemId);
        if (item == null) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Item not found!")));
            return;
        }

        CodexCategory cat = null;
        for (CodexCategory c : registry.getCategories()) {
            if (c.getItems().contains(item)) {
                cat = c;
                break;
            }
        }

        boolean unlocked = manager.isUnlocked(player.getUniqueId(), itemId);
        String unlockColor = unlocked ? C_GREEN : C_RED;
        String unlockStatus = unlocked ? toSmallCaps("Unlocked") : toSmallCaps("Locked — unlock in the Codex GUI");

        player.sendMessage(MM.deserialize(C_GOLD + "<bold>" + toSmallCaps(item.getDisplayName())));
        player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("ID") + ": " + C_ORANGE + itemId));
        player.sendMessage(Component.empty());

        // Description
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Description") + ":"));
            player.sendMessage(MM.deserialize(C_GRAY + item.getDescription()));
            player.sendMessage(Component.empty());
        }

        // Unlock status
        player.sendMessage(MM.deserialize(unlockColor + toSmallCaps("Status") + ": " + unlockStatus));
        player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("XP Cost") + ": " + C_ORANGE + item.getXpCost()));

        // Requirements
        if (!item.getItemRequirements().isEmpty()) {
            player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Requirements") + ":"));
            for (ItemStack req : item.getItemRequirements()) {
                String reqName = req.getItemMeta() != null && req.getItemMeta().hasDisplayName()
                    ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(req.getItemMeta().displayName())
                    : toSmallCaps(req.getType().name().replace("_", " "));
                player.sendMessage(MM.deserialize(C_GRAY + "  • " + reqName + " x" + req.getAmount()));
            }
        }

        // Crafting station
        String stationName = toSmallCaps("Crafting Table");
        if (item.getCraftingStation() != null) {
            ItemStack station = item.getCraftingStation();
            ItemMeta sm = station.getItemMeta();
            if (sm != null && sm.hasDisplayName()) {
                stationName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(sm.displayName());
            } else {
                stationName = toSmallCaps(station.getType().name().replace("_", " "));
            }
        }
        player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Crafting Station") + ": " + C_ORANGE + stationName));

        // Recipe grid
        player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Recipe") + ":"));
        ItemStack[] recipe = item.getRecipe();
        StringBuilder recipeStr = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            if (i > 0 && i % 3 == 0) recipeStr.append("\n");
            ItemStack ingredient = recipe[i];
            if (ingredient == null || ingredient.getType() == Material.AIR) {
                recipeStr.append(C_GRAY + "▢ ");
            } else {
                recipeStr.append(C_ORANGE + "▣ ");
            }
        }
        player.sendMessage(MM.deserialize(recipeStr.toString().trim()));

        // Back button
        if (cat != null) {
            player.sendMessage(Component.empty());
            String backCmd = "/codex wiki " + cat.getParentCategoryId() + " " + cat.getId();
            player.sendMessage(MM.deserialize(
                "<click:run_command:'" + backCmd + "'><hover:show_text:'" + toSmallCaps("Back to items") + "'>" +
                C_GRAY + "<bold>← " + toSmallCaps("Back") + "</bold></hover></click>"
            ));
        }
    }

    public static boolean isRuneTypeValidForItem(String runeType, org.bukkit.Material type) {
        if (runeType == null || type == null) return false;
        String name = type.name();
        switch (runeType) {
            case "WEAPON_SWORD": return name.contains("SWORD");
            case "WEAPON_AXE": return name.contains("AXE");
            case "WEAPON_SWORD_AXE": return name.contains("SWORD") || name.contains("AXE");
            case "ARMOR_BOOTS": return name.contains("BOOTS");
            case "ARMOR_CHESTPLATE": return name.contains("CHESTPLATE");
            case "ARMOR": return name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS");
            case "TOOLS": return name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("AXE");
            case "TOOLS_HOE": return name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("AXE") || name.contains("HOE");
            case "TOOL_HOE": return name.contains("HOE");
            case "WEAPON_BOW": return type == org.bukkit.Material.BOW || type == org.bukkit.Material.CROSSBOW;
            case "WEAPON_BOW_ONLY": return type == org.bukkit.Material.BOW;
            case "SHIELD": return type == org.bukkit.Material.SHIELD;
            case "DURABILITY": return type.getMaxDurability() > 0;
            case "MELEE_OR_STICK": return name.contains("SWORD") || name.contains("AXE") || name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("HOE") || type == org.bukkit.Material.STICK;
            case "WEAPON_BOW_SWORD":
            case "WEAPON": return name.contains("SWORD") || name.contains("BOW") || name.contains("CROSSBOW") || name.contains("AXE");
            default: return false;
        }
    }
}
