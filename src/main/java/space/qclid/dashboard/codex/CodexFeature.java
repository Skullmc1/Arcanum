package space.qclid.dashboard.codex;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexFeature {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;

    // Modular handlers
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;
    private final CodexCombatListener codexCombatListener;
    private final CodexInteractionListener codexInteractionListener;
    private final CodexPlayerListener codexPlayerListener;
    private final CodexRuneListener codexRuneListener;
    private final java.util.Map<java.util.UUID, String> activeMachine = new java.util.HashMap<>();

    public CodexFeature(JavaPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = new CodexManager(plugin);
        this.registry = new CodexRegistry();

        // Initialize sub-components
        this.arcaneItems = new ArcaneItems(plugin);
        this.explorerItems = new ExplorerItems(plugin);
        this.codexCrafting = new CodexCrafting(plugin, manager, registry, arcaneItems);
        this.codexPassiveTask = new CodexPassiveTask(plugin, arcaneItems);
        
        this.codexCombatListener = new CodexCombatListener(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask);
        this.codexInteractionListener = new CodexInteractionListener(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask, activeMachine);
        this.codexPlayerListener = new CodexPlayerListener(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask, activeMachine);
        this.codexRuneListener = new CodexRuneListener(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask, activeMachine);

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

        CodexCategory arcaneRunes = new CodexCategory("arcane.runes", "arcane", "Enchantment Runes", new ItemStack(Material.FIREWORK_STAR));
        CodexCategory arcaneArmor = new CodexCategory("arcane.armor", "arcane", "Armor", new ItemStack(Material.DIAMOND_CHESTPLATE));
        CodexCategory arcaneMelee = new CodexCategory("arcane.melee", "arcane", "Melee Weapons", new ItemStack(Material.DIAMOND_SWORD));
        CodexCategory arcaneRanged = new CodexCategory("arcane.ranged", "arcane", "Ranged Weapons", new ItemStack(Material.BLAZE_ROD));
        CodexCategory arcaneBoosts = new CodexCategory("arcane.boosts", "arcane", "Arcana Boosts", new ItemStack(Material.POTION));
        CodexCategory arcaneIngredients = new CodexCategory("arcane.ingredients", "arcane", "Ingredients", new ItemStack(Material.NETHER_WART));
        CodexCategory arcaneMaterials = new CodexCategory("arcane.materials", "arcane", "Materials", new ItemStack(Material.ENDER_PEARL));
        CodexCategory arcaneTrinkets = new CodexCategory("arcane.trinkets", "arcane", "Trinkets", new ItemStack(Material.TOTEM_OF_UNDYING));
        registry.registerCategory(arcaneRunes);
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
        CodexCategory explorerRunes = new CodexCategory("explorer.runes", "explorer", "Enchantment Runes", new ItemStack(Material.FIREWORK_STAR));
        registry.registerCategory(explorerNavigation);
        registry.registerCategory(explorerExploration);
        registry.registerCategory(explorerGadgets);
        registry.registerCategory(explorerTools);
        registry.registerCategory(explorerArmor);
        registry.registerCategory(explorerRunes);

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
        arcaneRunes.addItem(lifestealRuneCodex);

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
        arcaneRunes.addItem(lifestealRune2Codex);

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
        arcaneRunes.addItem(lifestealRune3Codex);

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
        arcaneRunes.addItem(speedRuneCodex);

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
        arcaneRunes.addItem(speedRune2Codex);

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
        arcaneRunes.addItem(speedRune3Codex);

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
        arcaneRunes.addItem(catchFlameRuneCodex);

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
        arcaneRunes.addItem(catchFlameRune2Codex);

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
        arcaneRunes.addItem(catchFlameRune3Codex);

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

        // 28. Venomous Scythe
        CodexItem scythe = new CodexItem.Builder("arcane.melee.venomous_scythe")
                .displayName("Armor Scythe")
                .displayName("Venomous Scythe")
                .displayItem(arcaneItems.createVenomousScythe())
                .xpCost(18)
                .requires(new ItemStack(Material.SPIDER_EYE, 4))
                .description("Iron hoe scythe applying stacking poison on hit (+extra damage).")
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
                .description("A crackling trace of pure lightning.")
                .craftingStation(new ItemStack(Material.ZOMBIE_HEAD))
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
        arcaneRunes.addItem(demoniumI);

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
        arcaneRunes.addItem(demoniumII);

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
        arcaneRunes.addItem(demoniumIII);

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

        registerRecipes();
    }

    private void registerRecipes() {
        CodexCategory arcaneIngredients = registry.getCategory("arcane.ingredients");
        CodexCategory arcaneMaterials = registry.getCategory("arcane.materials");
        CodexCategory arcaneRunes = registry.getCategory("arcane.runes");
        CodexCategory explorerRunes = registry.getCategory("explorer.runes");

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

        // 9. Artemis's Blessing (Bows - Max 1)
        registerMultiLevelRune(arcaneRunes, "arcane.runes.artemis_blessing", "Artemis's Blessing", "artemis_blessing", "WEAPON_BOW_ONLY", 1, 
                "Silences bow shots and adds +5 levels of Punch. No levels.", 
                org.bukkit.Color.fromRGB(154, 205, 50), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.naturesEmbraceItem, enchBook,
                        null, new ItemStack(Material.BOW), null,
                        enchBook, null, enchBook
                }, arcaneItems.arcanaTableItem);

        // 10. Static Charge (Chestplate - Max 7)
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

        // 23. Void Walker (Boots - Max 1)
        registerMultiLevelRune(explorerRunes, "explorer.runes.void_walker", "Void Walker", "void_walker", "ARMOR_BOOTS", 1, 
                "Freefalling 20 blocks in the void teleports you safely back to ground. No levels.", 
                org.bukkit.Color.fromRGB(186, 85, 211), org.bukkit.FireworkEffect.Type.BALL, 10,
                new ItemStack[]{
                        enchBook, arcaneItems.blessingOfTheVoidItem, enchBook,
                        null, new ItemStack(Material.NETHERITE_BOOTS), null,
                        enchBook, arcaneItems.blessingOfTheVoidItem, enchBook
                }, explorerItems.heavyForgeItem);
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
        category.addItem(lvl1Codex);

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
            category.addItem(lvlCodex);
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
                }).build(), "Receive the Codex guide book", List.of());

        commands.register(Commands.literal("codexitem")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    if (!player.isOp() && !player.hasPermission("dashboard.admin")) {
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have permission to use this command!")));
                        return 1;
                    }
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item>")));
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
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item>")));
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
                                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Usage: /codexitem <category> <subcategory> <item>")));
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
                                )
                        )
                ).build(), "Give a custom codex item", List.of());
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
}
