package space.qclid.dashboard.codex.items.arcane;

import org.bukkit.Bukkit;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static space.qclid.dashboard.util.TextUtil.*;
import static space.qclid.dashboard.util.CodexUtil.setSkullTexture;

public class ArcaneMaterials {

    private final JavaPlugin plugin;

    public ArcaneMaterials(JavaPlugin plugin) {
        this.plugin = plugin;
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

    public ItemStack createDynamicCustomItem(String id) {
        if (id == null) return null;
        
        Material material;
        String name;
        String loreDesc;
        
        if (id.equals("arcane.materials.blue_gold")) {
            material = Material.GOLD_INGOT;
            name = "Blue Gold";
            loreDesc = "A strong gold-iron alloy forged in the Heavy Alloy Forge.";
        } else if (id.equals("arcane.materials.rose_gold")) {
            material = Material.GOLD_INGOT;
            name = "Rose Gold";
            loreDesc = "A beautiful copper-gold alloy forged in the Heavy Alloy Forge.";
        } else if (id.equals("arcane.materials.bronzed_steel")) {
            material = Material.IRON_INGOT;
            name = "Bronzed Steel";
            loreDesc = "A hardened copper-iron steel forged in the Heavy Alloy Forge.";
        } else if (id.equals("arcane.materials.abyssal_alloy")) {
            material = Material.NETHERITE_SCRAP;
            name = "Abyssal Alloy";
            loreDesc = "A dark, blood-infused nether alloy forged in the Heavy Alloy Forge.";
        } else if (id.equals("arcane.materials.crushed_ender_dust")) {
            material = Material.SUGAR;
            name = "Crushed Ender Dust";
            loreDesc = "Fine powder of crushed ender pearls produced in a Kinetic Crusher.";
        } else if (id.equals("arcane.materials.fractured_geode")) {
            material = Material.AMETHYST_SHARD;
            name = "Fractured Geode";
            loreDesc = "A cracked shell sifted from the Trommel.";
        } else if (id.equals("arcane.materials.resonant_crystal")) {
            material = Material.AMETHYST_SHARD;
            name = "Resonant Crystal";
            loreDesc = "A crystal vibrating at high frequency.";
        } else if (id.equals("arcane.materials.abyssal_sponge")) {
            material = Material.WET_SPONGE;
            name = "Abyssal Sponge";
            loreDesc = "A sponge saturated with deep ocean energy.";
        } else if (id.equals("arcane.materials.winged_insignia")) {
            material = Material.FEATHER;
            name = "Winged Insignia";
            loreDesc = "A holy feather emblem.";
        } else if (id.equals("arcane.materials.blazing_chakram")) {
            material = Material.BLAZE_ROD;
            name = "Blazing Chakram";
            loreDesc = "A forged circular ring of flame.";
        } else if (id.equals("arcane.materials.radiant_core")) {
            material = Material.GOLDEN_CARROT;
            name = "Radiant Core";
            loreDesc = "A core of pure light.";
        } else if (id.equals("arcane.materials.sun_kissed_feather")) {
            material = Material.FEATHER;
            name = "Sun-Kissed Feather";
            loreDesc = "A feather permanently warmed by solar fire.";
        } else if (id.equals("arcane.materials.radiant_geode")) {
            material = Material.AMETHYST_SHARD;
            name = "Radiant Geode";
            loreDesc = "A vital geode infused with solar energy.";
        } else if (id.equals("arcane.materials.purified_core")) {
            material = Material.AMETHYST_CLUSTER;
            name = "Purified Core";
            loreDesc = "A pristine core purged of all decay.";
        } else if (id.equals("arcane.materials.stormcaller_medallion")) {
            material = Material.COPPER_INGOT;
            name = "Stormcaller Medallion";
            loreDesc = "A copper medallion crackling with storm energy.";
        } else if (id.equals("arcane.materials.cinder_core")) {
            material = Material.MAGMA_CREAM;
            name = "Cinder Core";
            loreDesc = "A core burning with eternal cinders.";
        } else if (id.equals("arcane.materials.vial_of_demonic_blood")) {
            material = Material.POTION;
            name = "Vial of Demonic Blood";
            loreDesc = "Vial containing dark, distilled blood.";
        } else if (id.equals("arcane.materials.sulfur_clump")) {
            material = Material.GUNPOWDER;
            name = "Sulfur Clump";
            loreDesc = "A smelly clump of highly combustible sulfur.";
        } else if (id.equals("arcane.materials.withered_wrap")) {
            material = Material.PHANTOM_MEMBRANE;
            name = "Withered Wrap";
            loreDesc = "Dark wrap infused with wither energy.";
        } else if (id.equals("arcane.materials.underworld_coin")) {
            material = Material.GOLD_NUGGET;
            name = "Underworld Coin";
            loreDesc = "A coin paid to cross the river Styx.";
        } else if (id.equals("arcane.materials.surge_spark")) {
            material = Material.AMETHYST_SHARD;
            name = "Surge Spark";
            loreDesc = "A tiny spark of surging electricity.";
        } else if (id.equals("arcane.materials.gorgon_scale")) {
            material = Material.TURTLE_SCUTE;
            name = "Gorgon Scale";
            loreDesc = "A hardened scale that blocks incoming attacks.";
        } else if (id.equals("arcane.materials.kinetic_battery")) {
            material = Material.COPPER_INGOT;
            name = "Kinetic Battery";
            loreDesc = "A battery that stores kinetic force.";
        } else if (id.equals("arcane.materials.jackal_idol")) {
            material = Material.TOTEM_OF_UNDYING;
            name = "Jackal Idol";
            loreDesc = "An idol representing the judgment of Anubis.";
        } else if (id.equals("arcane.materials.beast_fang")) {
            material = Material.FLINT;
            name = "Beast Fang";
            loreDesc = "A razor-sharp fang of a mythical beast.";
        } else if (id.equals("arcane.materials.corrupted_spore")) {
            material = Material.BROWN_MUSHROOM;
            name = "Corrupted Spore";
            loreDesc = "Spore emitting noxious miasma gases.";
        } else if (id.equals("arcane.materials.serpent_scale")) {
            material = Material.TURTLE_SCUTE;
            name = "Serpent's Scale";
            loreDesc = "A glittering green scale shed by a mythical serpent.";
        } else if (id.equals("arcane.materials.cursed_diamond")) {
            material = Material.DIAMOND;
            name = "Cursed Diamond";
            loreDesc = "A synthetic diamond corrupted with wither energy.";
        } else if (id.equals("arcane.materials.blood_orb")) {
            material = Material.APPLE;
            name = "Blood Orb";
            loreDesc = "A pulsing orb of life energy. Consuming it restores hunger and saturation.";
        } else if (id.equals("arcane.materials.transmutation_core")) {
            material = Material.CONDUIT;
            name = "Transmutation Core";
            loreDesc = "A pulsing core capable of changing matter from one form to another.";
        } else {
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps(name)));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps(loreDesc))
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createCustomSkull(String skinUrl, String itemId, String displayName, List<String> loreLines) {
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

            setSkullTexture(meta, skinUrl, plugin);

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
}
