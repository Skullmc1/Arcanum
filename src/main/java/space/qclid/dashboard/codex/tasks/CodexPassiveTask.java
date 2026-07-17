package space.qclid.dashboard.codex.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.BlockFace;

import space.qclid.dashboard.codex.items.ArcaneItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.CodexUtil.*;
import static space.qclid.dashboard.util.TextUtil.*;

public class CodexPassiveTask {

    private static final int HARDENED_COAL_1_BURN = 3200;
    private static final int HARDENED_COAL_2_BURN = 6400;
    private static final int HARDENED_COAL_MAX_BURN = 12800;
    private static final int COAL_BLOCK_BURN = 16000;
    private static final int LAVA_BUCKET_BURN = 20000;
    private static final int BLAZE_ROD_BURN = 2400;
    private static final int COAL_BURN = 1600;
    private static final int STICK_BURN = 100;
    private static final int DEFAULT_BURN = 300;

    private final JavaPlugin plugin;
    private final ArcaneItems arcaneItems;

    // Track virtual furnaces and smelters for portable tools
    public final Map<UUID, Inventory> activeFurnaces = new HashMap<>();
    public final Map<UUID, Inventory> activeSmelters = new HashMap<>();

    private final Map<UUID, Integer> jetpackFuelTicks = new HashMap<>();
    private int scanCounter = 0;

    public CodexPassiveTask(JavaPlugin plugin, ArcaneItems arcaneItems) {
        this.plugin = plugin;
        this.arcaneItems = arcaneItems;
    }

    public void runTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateSpeedRunes(player);
            updateVitalityGeode(player);
            updateShadowCloaks(player);
            updateFlippersAndHelmet(player);
            updateGaleChestplate(player);
            updateMagneticRing(player);
            updateJetpack(player);
            updateDemoniumRunes(player);
            updateGloomAura(player);
            updatePhotosynthesis(player);
            updateVoidWalker(player);
            updateArmorSets(player);
            refreshPlayerEquipmentLore(player);
        }
        tickCatchFlameSpread();
        updateSunExposureItems();

        scanCounter++;
        if (scanCounter >= 10) { // every 100 ticks (5s)
            scanCounter = 0;
            runAutomations();
        }
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
                        new NamespacedKey("codex", "vitality_geode"),
                        highestBoost,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY
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
            for (LivingEntity source : world.getEntitiesByClass(LivingEntity.class)) {
                if (source.getFireTicks() <= 0) {
                    if (hasMetadata(source, plugin, "catch_flame_level")) {
                        removeMetadata(source, plugin, "catch_flame_level");
                    }
                    continue;
                }
                if (!hasMetadata(source, plugin, "catch_flame_level")) continue;

                int level = getIntMetadata(source, plugin, "catch_flame_level", 0);
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
                        setMetadata(target, plugin, "catch_flame_level", level);
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

    private void updateGaleChestplate(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && isCustomItem(chestplate, "arcane.armor.gale_chestplate")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 25, 0, true, false, true));
            if (player.isOnGround()) {
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                    if (!player.hasCooldown(Material.DIAMOND_CHESTPLATE)) {
                        player.setAllowFlight(true);
                    } else {
                        player.setAllowFlight(false);
                    }
                }
            }
        } else {
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                if (player.getAllowFlight()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }
    }

    private void updateMagneticRing(Player player) {
        boolean hasRing = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isCustomItem(item, "explorer.gadgets.magnetic_ring")) {
                hasRing = true;
                break;
            }
        }
        if (hasRing) {
            double radius = 5.0;
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof org.bukkit.entity.Item itemEntity) {
                    if (itemEntity.getPickupDelay() <= 0) {
                        org.bukkit.util.Vector target = player.getLocation().add(0, 0.5, 0).toVector();
                        org.bukkit.util.Vector dir = target.subtract(itemEntity.getLocation().toVector());
                        double distance = dir.length();
                        if (distance > 0.2) {
                            double speed = 0.35;
                            org.bukkit.util.Vector vel = dir.normalize().multiply(speed);
                            itemEntity.setVelocity(vel);
                        }
                    }
                }
            }
        }
    }

    private void updateJetpack(Player player) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        ItemStack chest = player.getInventory().getChestplate();
        if (chest != null && isCustomItem(chest, "explorer.armor.steam_jetpack")) {
            if (player.isSneaking() && !player.isOnGround()) {
                int ticks = jetpackFuelTicks.getOrDefault(player.getUniqueId(), 0) + 10;
                if (ticks >= 20) {
                    ticks = 0;
                    if (!consumeJetpackFuel(player)) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
                        player.sendActionBar(MM.deserialize(C_RED + toSmallCaps("Jetpack: Out of fuel!")));
                        jetpackFuelTicks.put(player.getUniqueId(), 0);
                        return;
                    }
                }
                jetpackFuelTicks.put(player.getUniqueId(), ticks);

                org.bukkit.util.Vector vel = player.getVelocity();
                player.setVelocity(new org.bukkit.util.Vector(vel.getX(), 0.42, vel.getZ()));

                player.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), 3, 0.1, 0.0, 0.1, 0.02);
                player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 2, 0.05, 0.0, 0.05, 0.01);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.15f, 0.8f);
            } else {
                jetpackFuelTicks.remove(player.getUniqueId());
            }
        }
    }

    private boolean consumeJetpackFuel(Player player) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return true;

        String[] fuels = {
            "arcane.materials.hardened_coal_max",
            "arcane.materials.hardened_coal_2",
            "arcane.materials.hardened_coal_1"
        };
        for (String fuelId : fuels) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && isCustomItem(item, fuelId)) {
                    item.setAmount(item.getAmount() - 1);
                    return true;
                }
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && (item.getType() == Material.COAL || item.getType() == Material.CHARCOAL)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(plugin, "item_id");
                    if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        continue;
                    }
                }
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    private void updateDemoniumRunes(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() == Material.AIR) return;

        ItemMeta meta = helmet.getItemMeta();
        if (meta == null) return;

        NamespacedKey applyKey = new NamespacedKey(plugin, "rune_demonium");
        Integer lvl = meta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);
        if (lvl != null) {
            double radius = 2.0;
            int duration = lvl == 1 ? 40 : (lvl == 2 ? 100 : 200);
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof LivingEntity target && !target.equals(player)) {
                    if (target instanceof Player pTarget) {
                        if (pTarget.getGameMode() == org.bukkit.GameMode.CREATIVE || pTarget.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                            continue;
                        }
                    }
                    target.setFireTicks(duration);
                    target.getWorld().spawnParticle(org.bukkit.Particle.FLAME, target.getLocation().add(0, 0.5, 0), 2, 0.1, 0.2, 0.1, 0.01);
                }
            }
        }
    }

    private void updateGloomAura(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() == Material.AIR) return;
        ItemMeta meta = chestplate.getItemMeta();
        if (meta == null) return;
        NamespacedKey glKey = new NamespacedKey(plugin, "rune_gloom");
        if (!meta.getPersistentDataContainer().has(glKey, PersistentDataType.INTEGER)) return;
        int radius = 5;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Monster monster && !monster.isDead()) {
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, true, false, true));
                if (Math.random() < 0.05) {
                    player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, monster.getLocation().add(0, 1, 0), 1, 0.1, 0.2, 0.1, 0.01);
                }
            }
        }
    }

    private final java.util.Map<org.bukkit.Location, Long> knownDroppers = new java.util.HashMap<>();
    private static final long DROPPER_CACHE_TTL = 600_000L; // 10min cache

    private void runAutomations() {
        long now = System.currentTimeMillis();
        java.util.Set<org.bukkit.Location> newlyFound = new java.util.HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            int px = loc.getBlockX();
            int py = loc.getBlockY();
            int pz = loc.getBlockZ();

            for (int x = px - 8; x <= px + 8; x++) {
                for (int y = py - 4; y <= py + 4; y++) {
                    for (int z = pz - 8; z <= pz + 8; z++) {
                        org.bukkit.block.Block b = world.getBlockAt(x, y, z);
                        if (b.getType() != Material.DROPPER) continue;
                        org.bukkit.Location bLoc = b.getLocation();

                        // Check cache first
                        Long lastCheck = knownDroppers.get(bLoc);
                        if (lastCheck != null && (now - lastCheck) < DROPPER_CACHE_TTL) continue;

                        newlyFound.add(bLoc);
                        knownDroppers.put(bLoc, now);

                        if (b.getState() instanceof org.bukkit.block.Dropper dropper) {
                            tickAutomationDropper(dropper);
                        }
                    }
                }
            }
        }

        // Clean stale entries every 5th run
        if (scanCounter % 5 == 0 && !newlyFound.isEmpty()) {
            int staleCutoff = 0;
            for (java.util.Iterator<java.util.Map.Entry<org.bukkit.Location, Long>> it = knownDroppers.entrySet().iterator(); it.hasNext();) {
                if ((now - it.next().getValue()) > DROPPER_CACHE_TTL * 2) {
                    it.remove();
                    staleCutoff++;
                }
            }
        }
    }

    private void tickAutomationDropper(org.bukkit.block.Dropper dropper) {
        org.bukkit.block.Block block = dropper.getBlock();

        org.bukkit.block.Block top = block.getRelative(org.bukkit.block.BlockFace.UP);
        if (top.getType() == Material.HOPPER) {
            org.bukkit.block.BlockFace[] faces = {
                org.bukkit.block.BlockFace.NORTH,
                org.bukkit.block.BlockFace.SOUTH,
                org.bukkit.block.BlockFace.EAST,
                org.bukkit.block.BlockFace.WEST
            };
            boolean hasCauldron = false;
            org.bukkit.block.Block cauldronBlock = null;
            for (org.bukkit.block.BlockFace face : faces) {
                org.bukkit.block.Block adj = block.getRelative(face);
                if (adj.getType() == Material.CAULDRON) {
                    hasCauldron = true;
                    cauldronBlock = adj;
                    break;
                }
            }
            if (hasCauldron) {
                runSifterTick(dropper, cauldronBlock);
                return;
            }

            boolean hasFurnace = false;
            for (org.bukkit.block.BlockFace face : faces) {
                org.bukkit.block.Block adj = block.getRelative(face);
                if (adj.getType() == Material.FURNACE || adj.getType() == Material.BLAST_FURNACE || adj.getType() == Material.SMOKER) {
                    hasFurnace = true;
                    break;
                }
            }
            if (hasFurnace) {
                runSmelterTick(dropper);
            }
        }
    }

    private void runSifterTick(org.bukkit.block.Dropper dropper, org.bukkit.block.Block cauldron) {
        Inventory inv = dropper.getInventory();
        ItemStack target = null;
        int targetSlot = -1;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && (item.getType() == Material.GRAVEL || item.getType() == Material.SAND)) {
                target = item;
                targetSlot = i;
                break;
            }
        }
        if (target == null) return;

        Material siftType = target.getType();
        target.setAmount(target.getAmount() - 1);
        inv.setItem(targetSlot, target.getAmount() <= 0 ? null : target);

        Material result = Material.AIR;
        double rand = Math.random();
        if (siftType == Material.GRAVEL) {
            if (rand < 0.15) result = Material.RAW_IRON;
            else if (rand < 0.20) result = Material.RAW_GOLD;
            else if (rand < 0.30) result = Material.RAW_COPPER;
            else if (rand < 0.50) result = Material.COAL;
            else if (rand < 0.70) result = Material.CLAY_BALL;
            else result = Material.FLINT;
        } else {
            if (rand < 0.25) result = Material.REDSTONE;
            else if (rand < 0.40) result = Material.GLOWSTONE_DUST;
            else if (rand < 0.55) result = Material.QUARTZ;
            else if (rand < 0.80) result = Material.CLAY_BALL;
        }

        if (result != Material.AIR) {
            ItemStack output = new ItemStack(result, 1);
            java.util.HashMap<Integer, ItemStack> leftover = inv.addItem(output);
            if (!leftover.isEmpty()) {
                Location dropLoc = cauldron.getLocation().add(0.5, 1.1, 0.5);
                dropLoc.getWorld().dropItemNaturally(dropLoc, leftover.get(0));
            }
        }

        Location loc = dropper.getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, siftType == Material.GRAVEL ? Sound.BLOCK_GRAVEL_BREAK : Sound.BLOCK_SAND_BREAK, 0.5f, 0.8f);
        loc.getWorld().spawnParticle(
                org.bukkit.Particle.BLOCK,
                loc,
                10,
                0.2, 0.2, 0.2,
                siftType.createBlockData()
        );
    }

    private void runSmelterTick(org.bukkit.block.Dropper dropper) {
        Inventory inv = dropper.getInventory();
        NamespacedKey burnKey = new NamespacedKey(plugin, "burn_ticks");
        org.bukkit.persistence.PersistentDataContainer pdc = dropper.getPersistentDataContainer();
        int burnTicks = pdc.getOrDefault(burnKey, PersistentDataType.INTEGER, 0);

        if (burnTicks > 0) {
            burnTicks = Math.max(0, burnTicks - 100);
            pdc.set(burnKey, PersistentDataType.INTEGER, burnTicks);
            dropper.update();

            smeltOneItem(dropper);
        } else {
            ItemStack fuelStack = null;
            int fuelSlot = -1;
            ItemStack smeltStack = null;
            int smeltSlot = -1;

            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    if (fuelStack == null && isAutomationFuel(item)) {
                        fuelStack = item;
                        fuelSlot = i;
                    } else if (smeltStack == null && getSmeltResult(item.getType()) != null) {
                        smeltStack = item;
                        smeltSlot = i;
                    }
                }
            }

            if (fuelStack != null && smeltStack != null) {
                int fuelVal = getFuelBurnTime(fuelStack);
                fuelStack.setAmount(fuelStack.getAmount() - 1);
                inv.setItem(fuelSlot, fuelStack.getAmount() <= 0 ? null : fuelStack);

                pdc.set(burnKey, PersistentDataType.INTEGER, fuelVal);
                dropper.update();

                smeltOneItem(dropper);

                Location loc = dropper.getLocation().add(0.5, 0.5, 0.5);
                loc.getWorld().playSound(loc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.8f, 1f);
                loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc, 5, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    private boolean isAutomationFuel(ItemStack item) {
        if (item == null) return false;
        Material m = item.getType();
        if (isCustomItem(item, "arcane.materials.hardened_coal_1") ||
            isCustomItem(item, "arcane.materials.hardened_coal_2") ||
            isCustomItem(item, "arcane.materials.hardened_coal_max")) return true;
        return m == Material.COAL || m == Material.CHARCOAL || m == Material.COAL_BLOCK ||
               m == Material.LAVA_BUCKET || m == Material.BLAZE_ROD || m.name().contains("WOOD") ||
               m.name().contains("LOG") || m.name().contains("PLANKS") || m == Material.STICK;
    }

    private int getFuelBurnTime(ItemStack item) {
        if (isCustomItem(item, "arcane.materials.hardened_coal_1")) return HARDENED_COAL_1_BURN;
        if (isCustomItem(item, "arcane.materials.hardened_coal_2")) return HARDENED_COAL_2_BURN;
        if (isCustomItem(item, "arcane.materials.hardened_coal_max")) return HARDENED_COAL_MAX_BURN;

        Material m = item.getType();
        switch (m) {
            case COAL_BLOCK: return COAL_BLOCK_BURN;
            case LAVA_BUCKET: return LAVA_BUCKET_BURN;
            case BLAZE_ROD: return BLAZE_ROD_BURN;
            case COAL: case CHARCOAL: return COAL_BURN;
            case STICK: return STICK_BURN;
            default: return DEFAULT_BURN;
        }
    }

    private void smeltOneItem(org.bukkit.block.Dropper dropper) {
        Inventory inv = dropper.getInventory();
        ItemStack smeltStack = null;
        int smeltSlot = -1;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && getSmeltResult(item.getType()) != null) {
                smeltStack = item;
                smeltSlot = i;
                break;
            }
        }

        if (smeltStack != null) {
            Material resultMat = getSmeltResult(smeltStack.getType());
            smeltStack.setAmount(smeltStack.getAmount() - 1);
            inv.setItem(smeltSlot, smeltStack.getAmount() <= 0 ? null : smeltStack);

            ItemStack output = new ItemStack(resultMat, 1);
            java.util.HashMap<Integer, ItemStack> leftover = inv.addItem(output);
            if (!leftover.isEmpty()) {
                Location dropLoc = dropper.getLocation().add(0.5, 1.1, 0.5);
                dropLoc.getWorld().dropItemNaturally(dropLoc, leftover.get(0));
            }

            Location loc = dropper.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1f);
            loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private void updateSunExposureItems() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
                org.bukkit.entity.Item itemEntity = (org.bukkit.entity.Item) entity;
                ItemStack stack = itemEntity.getItemStack();
                if (stack == null || stack.getType() == Material.AIR) continue;

                if (isCustomItem(stack, "arcane.materials.immolation_totem")) {
                    Location loc = itemEntity.getLocation();
                    long time = world.getTime();
                    boolean isDay = time < 13000;
                    boolean isRaining = world.hasStorm();

                    if (isDay && !isRaining && loc.getBlock().getLightFromSky() == 15) {
                        int ticks = getIntMetadata(itemEntity, plugin, "sun_exposure_ticks", 0);
                        ticks += 10;
                        if (ticks >= 200) {
                            int amount = stack.getAmount();
                            ItemStack newStack = arcaneItems.sunsBrillianceItem.clone();
                            newStack.setAmount(amount);
                            itemEntity.setItemStack(newStack);
                            removeMetadata(itemEntity, plugin, "sun_exposure_ticks");

                            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1f, 1.2f);
                            loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc, 20, 0.2, 0.2, 0.2, 0.05);
                        } else {
                            setMetadata(itemEntity, plugin, "sun_exposure_ticks", ticks);
                            loc.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, loc.add(0, 0.1, 0), 2, 0.1, 0.1, 0.1, 0.0);
                        }
                    } else {
                        removeMetadata(itemEntity, plugin, "sun_exposure_ticks");
                    }
                }
            }
        }
    }

    private void updatePhotosynthesis(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        long time = world.getTime();
        boolean isDay = time < 13000;
        boolean isRaining = world.hasStorm();

        if (isDay && !isRaining && loc.getBlock().getLightFromSky() == 15) {
            Material typeBelow = loc.getBlock().getRelative(BlockFace.DOWN).getType();
            if (typeBelow == Material.GRASS_BLOCK || typeBelow == Material.DIRT || typeBelow == Material.COARSE_DIRT || typeBelow == Material.ROOTED_DIRT || typeBelow == Material.MUD || typeBelow == Material.MUDDY_MANGROVE_ROOTS) {
                boolean repairedAny = false;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                        NamespacedKey applyKey = new NamespacedKey(plugin, "rune_photosynthesis");
                        if (meta.getPersistentDataContainer().has(applyKey, PersistentDataType.INTEGER)) {
                            int dmg = damageable.getDamage();
                            if (dmg > 0) {
                                damageable.setDamage(dmg - 1);
                                item.setItemMeta(meta);
                                repairedAny = true;
                            }
                        }
                    }
                }
                if (repairedAny) {
                    player.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.0);
                }
            }
        }
    }

    private void updateVoidWalker(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType() == Material.AIR) return;
        ItemMeta meta = boots.getItemMeta();
        if (meta == null) return;
        NamespacedKey applyKey = new NamespacedKey(plugin, "rune_void_walker");
        if (meta.getPersistentDataContainer().has(applyKey, PersistentDataType.INTEGER)) {
            int minHeight = player.getWorld().getMinHeight();
            if (player.getLocation().getY() < minHeight - 5 && player.getFallDistance() >= 15) {
                Location spawn = player.getRespawnLocation();
                if (spawn == null) {
                    spawn = player.getWorld().getSpawnLocation();
                }

                player.teleport(spawn);
                player.setFallDistance(0);
                player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.0f);
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Void Walker saved you from the void!")));
            }
        }
    }

    private void updateArmorSets(Player player) {
        if (isWearingFullSet(player, "webbed")) {
            if (isNextToWall(player)) {
                org.bukkit.util.Vector vel = player.getVelocity();
                if (player.isSneaking()) {
                    player.setVelocity(new org.bukkit.util.Vector(vel.getX(), 0.0, vel.getZ()));
                } else if (vel.getY() > -0.1) {
                    player.setVelocity(new org.bukkit.util.Vector(vel.getX(), 0.18, vel.getZ()));
                }
            }
        }

        if (isWearingFullSet(player, "aegis_vanguard")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 0, true, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 25, 1, true, false, true));

            if (Bukkit.getCurrentTick() % 200 == 0) {
                Location loc = player.getLocation();
                player.getWorld().spawnParticle(org.bukkit.Particle.CRIT, loc.clone().add(0, 1, 0), 20, 1.0, 0.5, 1.0, 0.1);
                player.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 0.5f, 1.5f);
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(8.0, 8.0, 8.0)) {
                    if (entity instanceof org.bukkit.entity.Monster monster) {
                        monster.setTarget(player);
                    }
                }
                player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Aegis Vanguard pulse: Mobs taunted!")));
            }
        }

        if (isWearingFullSet(player, "storm_weaver")) {
            if (player.getVelocity().lengthSquared() > 0.001) {
                if (Bukkit.getCurrentTick() % 100 == 0) {
                    regenerateMagicalCharges(player);
                }
            }
        }
    }

    public boolean isWearingFullSet(Player player, String setName) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        return isSetPiece(helmet, setName, "helmet") &&
               isSetPiece(chest, setName, "chestplate") &&
               isSetPiece(legs, setName, "leggings") &&
               isSetPiece(boots, setName, "boots");
    }

    private boolean isSetPiece(ItemStack item, String setName, String piece) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return false;
        return id.contains(".armor." + setName + "." + piece);
    }

    private boolean isNextToWall(Player player) {
        Location loc = player.getLocation();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace face : faces) {
            if (loc.getBlock().getRelative(face).getType().isSolid()) return true;
            if (loc.clone().add(0, 0.5, 0).getBlock().getRelative(face).getType().isSolid()) return true;
        }
        return false;
    }

    private void regenerateMagicalCharges(Player player) {
        boolean charged = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (id == null) continue;

            NamespacedKey chargeKey = new NamespacedKey(plugin, "charges");
            if (id.equals("explorer.gadgets.thermal_canteen")) {
                int charges = meta.getPersistentDataContainer().getOrDefault(chargeKey, PersistentDataType.INTEGER, 4);
                if (charges < 4) {
                    charges++;
                    meta.getPersistentDataContainer().set(chargeKey, PersistentDataType.INTEGER, charges);
                    meta.lore(List.of(
                            MM.deserialize(C_GRAY + toSmallCaps("Flask cures effects and restores hunger.")),
                            MM.deserialize(""),
                            MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + charges + " / 4")
                    ));
                    item.setItemMeta(meta);
                    charged = true;
                }
            } else if (id.startsWith("explorer.tools.locator.")) {
                int charges = meta.getPersistentDataContainer().getOrDefault(chargeKey, PersistentDataType.INTEGER, 10);
                if (charges < 10) {
                    charges++;
                    meta.getPersistentDataContainer().set(chargeKey, PersistentDataType.INTEGER, charges);
                    String struct = id.substring(id.lastIndexOf('.') + 1);
                    String structName = Character.toUpperCase(struct.charAt(0)) + struct.substring(1).replace("_", " ");
                    meta.lore(List.of(
                            MM.deserialize(C_GRAY + toSmallCaps("Locates the nearest " + structName + ".")),
                            MM.deserialize(""),
                            MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + charges + " / 10")
                    ));
                    item.setItemMeta(meta);
                    charged = true;
                }
            }
        }
        if (charged) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.5f);
            player.sendActionBar(MM.deserialize(C_PURPLE + toSmallCaps("Storm-Weaver: Regenerated magical charges!")));
        }
    }

    private void refreshPlayerEquipmentLore(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        refreshItemLore(main, plugin);
        ItemStack off = player.getInventory().getItemInOffHand();
        refreshItemLore(off, plugin);
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            refreshItemLore(armor, plugin);
        }
    }
}
