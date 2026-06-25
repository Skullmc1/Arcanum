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
    private final CodexListener codexListener;

    public CodexFeature(JavaPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = new CodexManager(plugin);
        this.registry = new CodexRegistry();

        // Initialize sub-components
        this.arcaneItems = new ArcaneItems(plugin);
        this.explorerItems = new ExplorerItems(plugin);
        this.codexCrafting = new CodexCrafting(plugin, manager, registry, arcaneItems);
        this.codexPassiveTask = new CodexPassiveTask(plugin);
        this.codexListener = new CodexListener(plugin, dataManager, manager, registry, arcaneItems, explorerItems, codexCrafting, codexPassiveTask);

        // Register listener
        plugin.getServer().getPluginManager().registerEvents(codexListener, plugin);

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
        registry.registerCategory(explorerNavigation);
        registry.registerCategory(explorerExploration);
        registry.registerCategory(explorerGadgets);
        registry.registerCategory(explorerTools);
        registry.registerCategory(explorerArmor);

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

        // 4. Lifesteal Rune I
        CodexItem lifestealRuneCodex = new CodexItem.Builder("arcane.lifesteal_rune")
                .displayName("Lifesteal Rune I")
                .displayItem(arcaneItems.lifestealRuneItem)
                .xpCost(8)
                .requires(new ItemStack(Material.FIREWORK_STAR, 1))
                .requires(new ItemStack(Material.DIAMOND, 1))
                .description("Apply to sword. Heals you on hit.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK),
                        new ItemStack(Material.IRON_SWORD), new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.DIAMOND_SWORD),
                        new ItemStack(Material.BOOK), new ItemStack(Material.BOOK), new ItemStack(Material.BOOK)
                )
                .build();
        arcaneRunes.addItem(lifestealRuneCodex);

        // Lifesteal Rune II
        ItemStack lifestealRune2Item = arcaneItems.createLifestealRuneOfLevel(2);
        CodexItem lifestealRune2Codex = new CodexItem.Builder("arcane.lifesteal_rune_2")
                .displayName("Lifesteal Rune II")
                .displayItem(lifestealRune2Item)
                .xpCost(12)
                .requires(arcaneItems.lifestealRuneItem)
                .description("Upgraded Lifesteal Rune. Heals you on hit (increased effect).")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK),
                        new ItemStack(Material.IRON_SWORD), new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.DIAMOND_SWORD),
                        new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK), new ItemStack(Material.WRITABLE_BOOK)
                )
                .build();
        arcaneRunes.addItem(lifestealRune2Codex);

        // Lifesteal Rune III
        ItemStack lifestealRune3Item = arcaneItems.createLifestealRuneOfLevel(3);
        CodexItem lifestealRune3Codex = new CodexItem.Builder("arcane.lifesteal_rune_3")
                .displayName("Lifesteal Rune III")
                .displayItem(lifestealRune3Item)
                .xpCost(16)
                .requires(lifestealRune2Item)
                .description("Maximum level Lifesteal Rune.")
                .craftingStation(arcaneItems.arcanaTableItem)
                .recipe(
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.ENCHANTED_BOOK),
                        new ItemStack(Material.IRON_SWORD), new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.DIAMOND_SWORD),
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

        registerRecipes();
    }

    private void registerRecipes() {
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
