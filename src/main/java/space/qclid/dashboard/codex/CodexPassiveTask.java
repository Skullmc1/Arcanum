package space.qclid.dashboard.codex;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexPassiveTask {

    private final JavaPlugin plugin;

    // Track virtual furnaces and smelters for portable tools
    public final Map<UUID, Inventory> activeFurnaces = new HashMap<>();
    public final Map<UUID, Inventory> activeSmelters = new HashMap<>();

    public CodexPassiveTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void runTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateSpeedRunes(player);
            updateVitalityGeode(player);
            updateShadowCloaks(player);
            updateFlippersAndHelmet(player);
        }
        tickCatchFlameSpread();
    }

    public void runVirtualFurnaceTick() {
        tickVirtualFurnaces();
    }

    private void updateSpeedRunes(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            ItemMeta meta = boots.getItemMeta();
            if (meta != null) {
                NamespacedKey applyKey = new NamespacedKey(plugin, "rune_speed");
                Integer lvl = meta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);
                if (lvl != null) {
                    int amplifier = Math.max(0, lvl - 1);
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SPEED,
                            25,
                            amplifier,
                            true,
                            false,
                            true
                    ));
                }
            }
        }
    }

    private void updateVitalityGeode(Player player) {
        int highestBoost = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(plugin, "item_id");
                    String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (id != null) {
                        if (id.equals("arcane.trinkets.vitality_geode_1")) {
                            highestBoost = Math.max(highestBoost, 4);
                        } else if (id.equals("arcane.trinkets.vitality_geode_2")) {
                            highestBoost = Math.max(highestBoost, 10);
                        } else if (id.equals("arcane.trinkets.vitality_geode_3")) {
                            highestBoost = Math.max(highestBoost, 20);
                        }
                    }
                }
            }
        }

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("vitality_geode")) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) {
                attr.removeModifier(existing);
            }

            if (highestBoost > 0) {
                AttributeModifier mod = new AttributeModifier(
                        UUID.nameUUIDFromBytes("vitality_geode".getBytes()),
                        "vitality_geode",
                        highestBoost,
                        AttributeModifier.Operation.ADD_NUMBER
                );
                attr.addModifier(mod);
            }
        }
    }

    private void updateShadowCloaks(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() == Material.AIR) return;

        if (isCustomItem(helmet, "arcane.armor.shadow_cloak")) {
            int light = player.getLocation().getBlock().getLightLevel();
            if (light < 7) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 25, 0, true, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 0, true, false, true));
            }
        } else if (isCustomItem(helmet, "arcane.armor.superior_shadow_cloak")) {
            if (player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 25, 0, true, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 1, true, false, true)); // Speed II
            }
        }
    }

    private void updateFlippersAndHelmet(Player player) {
        // Spelunker Helmet
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && isCustomItem(helmet, "explorer.armor.spelunkers_helmet")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 25, 0, true, false, true));
            for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                if (entity instanceof org.bukkit.entity.Monster monster) {
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25, 0, true, false, false));
                }
            }
        }

        // Depth Strider Flippers
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && isCustomItem(boots, "explorer.gadgets.depth_strider_flippers")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 25, 0, true, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 25, 0, true, false, true));

            if (player.getLocation().getBlock().getType() != Material.WATER &&
                    player.getEyeLocation().getBlock().getType() != Material.WATER) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 1, true, false, true)); // Slowness II
            }
        }
    }

    private void tickCatchFlameSpread() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity source)) continue;
                if (source.getFireTicks() <= 0) continue;
                if (!source.hasMetadata("catch_flame_level")) continue;

                int level = source.getMetadata("catch_flame_level").get(0).asInt();
                double radius = 1.5;
                for (Entity nearby : source.getNearbyEntities(radius, radius, radius)) {
                    if (!(nearby instanceof LivingEntity target)) continue;
                    if (target.equals(source)) continue;
                    if (target.getFireTicks() > 0) continue;

                    double chance = 0.33;
                    if (level == 2) chance = 0.66;
                    else if (level == 3) chance = 1.0;

                    if (Math.random() <= chance) {
                        target.setFireTicks(100);
                        target.setMetadata("catch_flame_level", new org.bukkit.metadata.FixedMetadataValue(plugin, level));
                        target.getWorld().spawnParticle(org.bukkit.Particle.FLAME, target.getLocation().add(0, 0.5, 0), 10, 0.2, 0.3, 0.2, 0.05);
                        target.getWorld().playSound(target.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.2f);
                    }
                }
            }
        }
    }

    private void tickVirtualFurnaces() {
        tickFurnaceMap(activeFurnaces, false);
        tickFurnaceMap(activeSmelters, true);
    }

    private void tickFurnaceMap(Map<UUID, Inventory> map, boolean isBlastFurnace) {
        for (Map.Entry<UUID, Inventory> entry : map.entrySet()) {
            Inventory inv = entry.getValue();
            ItemStack ingredient = inv.getItem(0);
            ItemStack fuel = inv.getItem(1);
            ItemStack result = inv.getItem(2);

            if (ingredient == null || ingredient.getType() == Material.AIR) continue;

            Material smeltedType = getSmeltResult(ingredient.getType());
            if (smeltedType == null) continue;
            if (isBlastFurnace && !isBlastSmeltable(ingredient.getType())) continue;

            if (result != null && result.getType() != Material.AIR) {
                if (result.getType() != smeltedType || result.getAmount() >= result.getMaxStackSize()) {
                    continue;
                }
            }

            if (fuel != null && fuel.getType() != Material.AIR && isFuel(fuel.getType())) {
                if (result == null || result.getType() == Material.AIR) {
                    inv.setItem(2, new ItemStack(smeltedType, 1));
                } else {
                    result.setAmount(result.getAmount() + 1);
                    inv.setItem(2, result);
                }
                ingredient.setAmount(ingredient.getAmount() - 1);
                inv.setItem(0, ingredient.getAmount() <= 0 ? null : ingredient);

                if (fuel.getType() == Material.LAVA_BUCKET) {
                    inv.setItem(1, new ItemStack(Material.BUCKET));
                } else {
                    fuel.setAmount(fuel.getAmount() - 1);
                    inv.setItem(1, fuel.getAmount() <= 0 ? null : fuel);
                }

                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    p.playSound(p.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1f);
                }
            }
        }
    }

    private boolean isFuel(Material m) {
        return m == Material.COAL || m == Material.CHARCOAL || m == Material.COAL_BLOCK ||
                m == Material.LAVA_BUCKET || m == Material.BLAZE_ROD || m.name().contains("WOOD") ||
                m.name().contains("LOG") || m.name().contains("PLANKS") || m == Material.STICK;
    }

    private boolean isBlastSmeltable(Material m) {
        String name = m.name();
        return name.contains("ORE") || name.contains("RAW_");
    }

    private Material getSmeltResult(Material m) {
        switch (m) {
            case RAW_IRON: case IRON_ORE: case DEEPSLATE_IRON_ORE: return Material.IRON_INGOT;
            case RAW_GOLD: case GOLD_ORE: case DEEPSLATE_GOLD_ORE: return Material.GOLD_INGOT;
            case RAW_COPPER: case COPPER_ORE: case DEEPSLATE_COPPER_ORE: return Material.COPPER_INGOT;
            case COBBLESTONE: return Material.STONE;
            case STONE: return Material.SMOOTH_STONE;
            case SAND: case RED_SAND: return Material.GLASS;
            case KELP: return Material.DRIED_KELP;
            case PORKCHOP: return Material.COOKED_PORKCHOP;
            case BEEF: return Material.COOKED_BEEF;
            case CHICKEN: return Material.COOKED_CHICKEN;
            case MUTTON: return Material.COOKED_MUTTON;
            case SALMON: return Material.COOKED_SALMON;
            case COD: return Material.COOKED_COD;
            case POTATO: return Material.BAKED_POTATO;
            case CLAY_BALL: return Material.BRICK;
            case NETHERRACK: return Material.NETHER_BRICK;
            default: return null;
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
