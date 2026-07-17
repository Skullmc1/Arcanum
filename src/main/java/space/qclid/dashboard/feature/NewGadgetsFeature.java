package space.qclid.dashboard.feature;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import space.qclid.dashboard.codex.core.CodexCategory;
import space.qclid.dashboard.codex.core.CodexItem;

import static space.qclid.dashboard.util.CodexUtil.*;
import static space.qclid.dashboard.util.TextUtil.*;

public class NewGadgetsFeature implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, List<Entity>> activeEffects = new HashMap<>();
    private final Map<Location, UUID> tempBlocks = new HashMap<>();
    private final Map<UUID, WormholeAnchor> wormholes = new HashMap<>();

    // ── Items ─────────────────────────────────────────────────────────────────
    public final ItemStack echoLocator;
    public final ItemStack voidChisel;
    public final ItemStack gravityWell;
    public final ItemStack seismograph;
    public final ItemStack spectralCrossing;
    public final ItemStack holographicDecoy;
    public final ItemStack nullSpear;
    public final ItemStack wormhole;

    public NewGadgetsFeature(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.echoLocator = buildEchoLocator();
        this.voidChisel = buildVoidChisel();
        this.gravityWell = buildGravityWell();
        this.seismograph = buildSeismograph();
        this.spectralCrossing = buildSpectralCrossing();
        this.holographicDecoy = buildHolographicDecoy();
        this.nullSpear = buildNullSpear();
        this.wormhole = buildWormhole();

        // Tick scheduler for active effects
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> tick(), 1L, 2L);
    }

    // ── Item builders ─────────────────────────────────────────────────────────

    private ItemStack createGadgetItem(Material material, String itemId, String displayName, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING, itemId);
            meta.displayName(parse(C_PURPLE + "<bold>" + toSmallCaps(displayName)));
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(MM.deserialize(line));
            }
            meta.lore(lore);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildEchoLocator() {
        return createGadgetItem(Material.SPYGLASS, "explorer.gadgets.echo_locator",
                "Echo Locator",
                C_GRAY + toSmallCaps("Emits a scanning pulse that reveals"),
                C_GRAY + toSmallCaps("nearby living entities."),
                "",
                C_YELLOW + toSmallCaps("Right-click to scan. 10s cooldown."));
    }

    private ItemStack buildVoidChisel() {
        return createGadgetItem(Material.IRON_PICKAXE, "explorer.gadgets.void_chisel",
                "Void Chisel",
                C_GRAY + toSmallCaps("Places temporary blocks at range."),
                C_GRAY + toSmallCaps("Shift-right-click to remove them."),
                "",
                C_YELLOW + toSmallCaps("Right-click surface to place. 30s duration."));
    }

    private ItemStack buildGravityWell() {
        return createGadgetItem(Material.ENDER_EYE, "explorer.gadgets.gravity_well",
                "Gravity Well",
                C_GRAY + toSmallCaps("Creates a vortex that pulls in"),
                C_GRAY + toSmallCaps("nearby items and creatures."),
                "",
                C_YELLOW + toSmallCaps("Right-click to deploy. 10s duration."));
    }

    private ItemStack buildSeismograph() {
        return createGadgetItem(Material.DEBUG_STICK, "explorer.gadgets.seismograph",
                "Seismograph",
                C_GRAY + toSmallCaps("Sends a shockwave through the ground"),
                C_GRAY + toSmallCaps("to reveal underground cavities."),
                "",
                C_YELLOW + toSmallCaps("Right-click ground. 20s cooldown."));
    }

    private ItemStack buildSpectralCrossing() {
        return createGadgetItem(Material.ENDER_PEARL, "explorer.gadgets.spectral_crossing",
                "Spectral Crossing",
                C_GRAY + toSmallCaps("Creates a temporary bridge across"),
                C_GRAY + toSmallCaps("gaps. Fades after a few seconds."),
                "",
                C_YELLOW + toSmallCaps("Right-click a distant point. 15s cooldown."));
    }

    private ItemStack buildHolographicDecoy() {
        return createGadgetItem(Material.SNOWBALL, "explorer.gadgets.holographic_decoy",
                "Holographic Decoy",
                C_GRAY + toSmallCaps("Creates a phantom decoy that lures"),
                C_GRAY + toSmallCaps("hostile mobs away from you."),
                "",
                C_YELLOW + toSmallCaps("Throw to deploy. 20s cooldown."));
    }

    private ItemStack buildNullSpear() {
        return createGadgetItem(Material.BLAZE_ROD, "explorer.gadgets.null_spear",
                "Null Spear",
                C_GRAY + toSmallCaps("Orbits a protective shield around you"),
                C_GRAY + toSmallCaps("that intercepts incoming projectiles."),
                "",
                C_YELLOW + toSmallCaps("Right-click to activate. 12s cooldown."));
    }

    private ItemStack buildWormhole() {
        return createGadgetItem(Material.END_CRYSTAL, "explorer.gadgets.wormhole",
                "Wormhole",
                C_GRAY + toSmallCaps("Links two locations with a portal."),
                C_GRAY + toSmallCaps("Right-click a block to set Anchor A,"),
                C_GRAY + toSmallCaps("then another for Anchor B."),
                "",
                C_YELLOW + toSmallCaps("Portals last 60 seconds."));
    }

    // ── Public accessors for Codex registration ───────────────────────────────

    public ItemStack getCustomItem(String id) {
        if (id == null) return null;
        return switch (id) {
            case "explorer.gadgets.echo_locator" -> echoLocator.clone();
            case "explorer.gadgets.void_chisel" -> voidChisel.clone();
            case "explorer.gadgets.gravity_well" -> gravityWell.clone();
            case "explorer.gadgets.seismograph" -> seismograph.clone();
            case "explorer.gadgets.spectral_crossing" -> spectralCrossing.clone();
            case "explorer.gadgets.holographic_decoy" -> holographicDecoy.clone();
            case "explorer.gadgets.null_spear" -> nullSpear.clone();
            case "explorer.gadgets.wormhole" -> wormhole.clone();
            default -> null;
        };
    }

    // ── Interaction handler ───────────────────────────────────────────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return;

        switch (id) {
            case "explorer.gadgets.echo_locator" -> activateEchoLocator(player);
            case "explorer.gadgets.void_chisel" -> activateVoidChisel(player, event);
            case "explorer.gadgets.gravity_well" -> activateGravityWell(player, event);
            case "explorer.gadgets.seismograph" -> activateSeismograph(player, event);
            case "explorer.gadgets.spectral_crossing" -> activateSpectralCrossing(player);
            case "explorer.gadgets.holographic_decoy" -> activateHolographicDecoy(player);
            case "explorer.gadgets.null_spear" -> activateNullSpear(player);
            case "explorer.gadgets.wormhole" -> activateWormhole(player, event);
        }
    }

    // ── Echo Locator ──────────────────────────────────────────────────────────

    private void activateEchoLocator(Player player) {
        if (player.hasCooldown(echoLocator.getType())) return;
        player.setCooldown(echoLocator.getType(), 200);

        Location center = player.getLocation().add(0, 1, 0);
        List<Entity> found = new ArrayList<>();

        // Expanding ring pulse over 1.5s (15 ticks * 3 = 45 ticks)
        for (int radius = 1; radius <= 20; radius++) {
            int r = radius;
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                for (int angle = 0; angle < 360; angle += 15) {
                    double rad = Math.toRadians(angle);
                    double x = center.getX() + r * Math.cos(rad);
                    double z = center.getZ() + r * Math.sin(rad);
                    Location ploc = new Location(center.getWorld(), x, center.getY(), z);
                    center.getWorld().spawnParticle(Particle.WAX_ON, ploc, 0, 0, 0, 0, 0.02);
                }
                // Check entities at this radius
                for (Entity e : center.getNearbyEntities(r, r, r)) {
                    if (e instanceof LivingEntity le && !le.equals(player) && !found.contains(e)) {
                        found.add(e);
                        le.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.GLOWING, 60, 0));
                        double dist = Math.round(le.getLocation().distance(center) * 10.0) / 10.0;
                        player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Echo: ")
                                + C_ORANGE + le.getName() + C_GRAY + " (" + dist + "m)"));
                    }
                }
                center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.0f + r * 0.03f);
            }, r * 2L);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        consumeItem(player);
    }

    // ── Void Chisel ───────────────────────────────────────────────────────────

    private void activateVoidChisel(Player player, PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        if (player.isSneaking()) {
            // Remove temporary blocks in a small area
            int removed = 0;
            Iterator<Map.Entry<Location, UUID>> it = tempBlocks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, UUID> entry = it.next();
                if (entry.getValue().equals(player.getUniqueId()) && entry.getKey().distance(clicked.getLocation()) < 5) {
                    Block b = entry.getKey().getBlock();
                    if (b.getType() != Material.AIR) {
                        b.setType(Material.AIR);
                        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), 10, b.getBlockData());
                    }
                    it.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Removed ") + removed + toSmallCaps(" temporary block(s).")));
            }
            return;
        }

        // Place temporary block at the face
        Block target = clicked.getRelative(event.getBlockFace());
        Location loc = target.getLocation();

        // Remove existing temp block
        if (tempBlocks.containsKey(loc)) {
            tempBlocks.remove(loc);
            target.setType(Material.AIR);
            return;
        }

        Material heldType = player.getInventory().getItemInOffHand().getType();
        if (heldType == Material.AIR || !heldType.isBlock()) {
            player.sendActionBar(MM.deserialize(C_RED + toSmallCaps("Hold a block in your off-hand to place.")));
            return;
        }

        target.setType(heldType);
        tempBlocks.put(loc, player.getUniqueId());
        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 0.5f, 1f);

        // Auto-remove after 30 seconds
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            if (tempBlocks.remove(loc) != null && target.getType() == heldType) {
                target.setType(Material.AIR);
                target.getWorld().spawnParticle(Particle.BLOCK, loc.add(0.5, 0.5, 0.5), 10, target.getBlockData());
            }
        }, 600L);

        consumeItem(player);
    }

    // ── Gravity Well ──────────────────────────────────────────────────────────

    private void activateGravityWell(Player player, PlayerInteractEvent event) {
        if (player.hasCooldown(gravityWell.getType())) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location center = clicked.getRelative(event.getBlockFace()).getLocation().add(0.5, 0.5, 0.5);
        UUID uid = UUID.randomUUID();

        // Visual: rotating ring of particles
        BlockDisplay ring = center.getWorld().spawn(center, BlockDisplay.class);
        ring.setBlock(Material.CRYING_OBSIDIAN.createBlockData());
        ring.setInterpolationDuration(2);
        ring.setTransformation(new Transformation(
                new org.joml.Vector3f(-0.5f, 0, -0.5f),
                new Quaternionf(),
                new org.joml.Vector3f(1, 0.1f, 1),
                new Quaternionf()));
        ring.setGlowing(true);

        activeEffects.put(uid, new ArrayList<>(List.of(ring)));

        // Timer
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            ring.remove();
            activeEffects.remove(uid);
            center.getWorld().spawnParticle(Particle.SMOKE, center, 30, 1, 1, 1, 0.1);
            center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
        }, 200L);

        player.setCooldown(gravityWell.getType(), 300);
        consumeItem(player);
    }

    private void tickGravityWell(Location center, Entity ring) {
        for (Entity e : center.getNearbyEntities(5, 3, 5)) {
            if (e.equals(ring)) continue;
            if (e instanceof Item item) {
                Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.3);
                item.setVelocity(item.getVelocity().add(pull));
            } else if (e instanceof LivingEntity le && !(le instanceof Player)) {
                Vector pull = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(0.15);
                le.setVelocity(le.getVelocity().add(pull));
            }
        }
    }

    // ── Seismograph ───────────────────────────────────────────────────────────

    private void activateSeismograph(Player player, PlayerInteractEvent event) {
        if (player.hasCooldown(seismograph.getType())) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location origin = clicked.getRelative(event.getBlockFace()).getLocation();
        World world = origin.getWorld();
        List<BlockDisplay> tiles = new ArrayList<>();

        // Expanding ring of display tiles on the ground surface
        for (int radius = 1; radius <= 10; radius++) {
            int r = radius;
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    int x = (int) Math.round(r * Math.cos(rad));
                    int z = (int) Math.round(r * Math.sin(rad));
                    Block surface = world.getBlockAt(origin.clone().add(x, 0, z));
                    if (surface.getType().isSolid()) continue;

                    // Check below for air pocket
                    Block below = surface.getRelative(BlockFace.DOWN);
                    if (below.getType() != Material.AIR) continue;

                    // Reveal with a glass tile
                    surface.setType(Material.GLASS);
                    tempBlocks.put(surface.getLocation(), player.getUniqueId());

                    plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                        if (surface.getType() == Material.GLASS) {
                            surface.setType(Material.AIR);
                            tempBlocks.remove(surface.getLocation());
                        }
                    }, 100L);
                }
                world.playSound(origin, Sound.BLOCK_ANCIENT_DEBRIS_HIT, 0.3f, 0.5f + r * 0.05f);
            }, r * 2L);
        }

        player.setCooldown(seismograph.getType(), 400);
        consumeItem(player);
    }

    // ── Spectral Crossing ─────────────────────────────────────────────────────

    private void activateSpectralCrossing(Player player) {
        if (player.hasCooldown(spectralCrossing.getType())) return;

        RayTraceResult result = player.rayTraceBlocks(30);
        if (result == null || result.getHitBlock() == null) {
            player.sendActionBar(MM.deserialize(C_RED + toSmallCaps("Look at a distant surface!")));
            return;
        }

        Location target = result.getHitPosition().toLocation(player.getWorld());
        Location start = player.getLocation().add(0, -0.5, 0);
        Vector dir = target.toVector().subtract(start.toVector());
        double distance = dir.length();
        dir.normalize();

        List<BlockDisplay> bridge = new ArrayList<>();
        int blocks = Math.min((int) Math.ceil(distance), 30);

        for (int i = 0; i <= blocks; i++) {
            Location bl = start.clone().add(dir.clone().multiply(i));
            Block b = bl.getBlock();
            if (b.getType().isSolid()) continue;

            BlockDisplay bd = player.getWorld().spawn(b.getLocation().add(0.5, 0, 0.5), BlockDisplay.class);
            bd.setBlock(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
            bd.setGlowing(true);
            bd.setInterpolationDuration(10);

            bd.setInterpolationDelay(0);
            bd.setTransformation(new Transformation(
                    new Vector3f(-0.5f, 0, -0.5f),
                    new Quaternionf(),
                    new Vector3f(1.02f, 0.1f, 1.02f),
                    new Quaternionf()));
            bd.setInterpolationDuration(10);

            bridge.add(bd);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 0.5f);

        // Remove after 5s with crumble
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            for (BlockDisplay bd : bridge) {
                if (!bd.isValid()) continue;
                Location bl = bd.getLocation();
                bl.getWorld().spawnParticle(Particle.BLOCK, bl, 5, bd.getBlock());
                bd.remove();
            }
        }, 100L);

        player.setCooldown(spectralCrossing.getType(), 300);
        consumeItem(player);
    }

    // ── Holographic Decoy ─────────────────────────────────────────────────────

    private void activateHolographicDecoy(Player player) {
        if (player.hasCooldown(holographicDecoy.getType())) return;

        Snowball projectile = player.launchProjectile(Snowball.class);
        setMetadata(projectile, plugin, "decoy_projectile", true);
        projectile.setVelocity(player.getLocation().getDirection().multiply(1.5));
        player.setCooldown(holographicDecoy.getType(), 400);
        consumeItem(player);
    }

    @EventHandler
    public void onDecoyHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!hasMetadata(snowball, plugin, "decoy_projectile")) return;

        Location loc = snowball.getLocation();
        if (event.getHitBlock() != null) {
            loc = event.getHitBlock().getRelative(event.getHitBlockFace()).getLocation().add(0.5, 0, 0.5);
        }

        World world = loc.getWorld();

        // Spawn an armor stand decoy dressed like a player
        ArmorStand decoy = world.spawn(loc, ArmorStand.class);
        decoy.setVisible(true);
        decoy.setSmall(false);
        decoy.setArms(true);
        decoy.setInvulnerable(true);
        decoy.setMarker(false);

        // Equip with leather armor + player head if the thrower is a player
        if (snowball.getShooter() instanceof Player thrower) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(thrower);
                head.setItemMeta(sm);
            }
            decoy.getEquipment().setHelmet(head);
            decoy.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            decoy.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            decoy.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            // Give a sword in hand
            decoy.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        }

        setMetadata(decoy, plugin, "decoy", true);

        // Flash animation before removal
        final int[] decoyTicks = {0};
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            decoyTicks[0]++;
            if (!decoy.isValid() || decoyTicks[0] > 50) {
                if (decoy.isValid()) {
                    world.spawnParticle(Particle.CLOUD, decoy.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                    decoy.remove();
                }
                task.cancel();
                return;
            }
            // Attract nearby mobs
            for (Entity e : decoy.getNearbyEntities(8, 8, 8)) {
                if (e instanceof Mob mob) {
                    mob.setTarget(decoy);
                }
            }
            // Flash
            if (decoyTicks[0] > 35 && decoyTicks[0] % 5 == 0) {
                decoy.setVisible(!decoy.isVisible());
            }
        }, 1L, 2L);
    }

    // ── Null Spear ────────────────────────────────────────────────────────────

    private void activateNullSpear(Player player) {
        if (player.hasCooldown(nullSpear.getType())) return;

        UUID uid = player.getUniqueId();
        // Remove existing shield
        if (activeEffects.containsKey(uid)) {
            for (Entity e : activeEffects.get(uid)) e.remove();
            activeEffects.remove(uid);
        }

        // Create orbiting shield items
        List<Entity> orbs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemDisplay orb = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);
            orb.setItemStack(new ItemStack(Material.SHIELD));
            orb.setGlowing(true);
            orb.setBillboard(Display.Billboard.CENTER);
            orb.setInterpolationDuration(2);
            orb.setTransformation(new Transformation(
                    new Vector3f(-0.3f, -0.3f, -0.3f),
                    new Quaternionf(),
                    new Vector3f(0.6f, 0.6f, 0.6f),
                    new Quaternionf()));
            orbs.add(orb);
        }

        activeEffects.put(uid, orbs);

        // Auto-remove after 8 seconds
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            List<Entity> existing = activeEffects.remove(uid);
            if (existing != null) {
                for (Entity e : existing) e.remove();
            }
        }, 160L);

        player.setCooldown(nullSpear.getType(), 240);
        consumeItem(player);
    }

    // ── Wormhole ──────────────────────────────────────────────────────────────

    private void activateWormhole(Player player, PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location target = clicked.getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
        UUID uid = player.getUniqueId();

        WormholeAnchor existing = wormholes.get(uid);
        if (existing == null || existing.anchorB != null) {
            // Create new wormhole pair starting with Anchor A
            if (existing != null) cleanupWormhole(existing);
            wormholes.put(uid, new WormholeAnchor(target, null, new ArrayList<>(), uid));
            player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Anchor A set! Place Anchor B.")));
            player.playSound(target, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 1f);
        } else {
            // Set Anchor B
            existing.anchorB = target;
            spawnPortalRings(existing);
            player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Wormhole linked! Portals active for 60s.")));
            player.playSound(target, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

            // Auto-expire
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                if (wormholes.get(uid) == existing) {
                    cleanupWormhole(existing);
                    wormholes.remove(uid);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Your Wormhole has expired.")));
                }
            }, 1200L);
        }

        consumeItem(player);
    }

    private void spawnPortalRings(WormholeAnchor wh) {
        for (Location loc : new Location[]{wh.anchorA, wh.anchorB}) {
            BlockDisplay ring = loc.getWorld().spawn(loc, BlockDisplay.class);
            ring.setBlock(Material.PURPLE_STAINED_GLASS.createBlockData());
            ring.setGlowing(true);
            ring.setBillboard(Display.Billboard.CENTER);
            ring.setInterpolationDuration(2);
            ring.setTransformation(new Transformation(
                    new Vector3f(-1f, -2f, -1f),
                    new Quaternionf(),
                    new Vector3f(2f, 4f, 2f),
                    new Quaternionf()));
            wh.entities.add(ring);
        }
    }

    private void cleanupWormhole(WormholeAnchor wh) {
        for (Entity e : wh.entities) e.remove();
    }

    private static class WormholeAnchor {
        Location anchorA;
        Location anchorB;
        List<Entity> entities;
        UUID owner;
        WormholeAnchor(Location a, Location b, List<Entity> e, UUID o) { anchorA = a; anchorB = b; entities = e; owner = o; }
    }

    // ── Projectile interception for Null Spear ────────────────────────────────

    @EventHandler
    public void onProjectileHitNullSpear(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if (proj == null) return;
        Location ploc = proj.getLocation();

        for (Map.Entry<UUID, List<Entity>> entry : activeEffects.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            Entity firstOrb = entry.getValue().get(0);
            if (!firstOrb.isValid()) continue;
            if (firstOrb.getLocation().distance(ploc) > 6) continue;

            // Intercept! Remove the projectile
            proj.remove();
            ploc.getWorld().spawnParticle(Particle.FIREWORK, ploc, 15, 0.3, 0.3, 0.3, 0.05);
            ploc.getWorld().playSound(ploc, Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.5f);
            return;
        }
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    private void tick() {
        Iterator<Map.Entry<UUID, List<Entity>>> it = activeEffects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, List<Entity>> entry = it.next();
            UUID uid = entry.getKey();
            List<Entity> entities = entry.getValue();
            if (entities.isEmpty() || !entities.get(0).isValid()) {
                it.remove();
                continue;
            }

            Player player = Bukkit.getPlayer(uid);
            if (player == null || !player.isOnline()) {
                for (Entity e : entities) e.remove();
                it.remove();
                continue;
            }

            // Null Spear: orbit shields around player
            if (entities.size() == 3 && entities.get(0) instanceof ItemDisplay) {
                Location ploc = player.getLocation().add(0, 1.5, 0);
                long tick = Bukkit.getCurrentTick();
                for (int i = 0; i < 3; i++) {
                    Entity orb = entities.get(i);
                    double angle = Math.toRadians(tick * 3 + i * 120);
                    double radius = 2.0;
                    Location targetLoc = ploc.clone().add(Math.cos(angle) * radius, Math.sin(angle * 0.5) * 0.3, Math.sin(angle) * radius);
                    orb.teleport(targetLoc);
                }
            }

            // Gravity Well: pull items/mobs
            if (entities.size() == 1 && entities.get(0) instanceof BlockDisplay ring) {
                tickGravityWell(ring.getLocation(), ring);
            }

            // Wormhole: check for players touching the rings
            for (WormholeAnchor wh : wormholes.values()) {
                if (wh.anchorA == null || wh.anchorB == null || wh.entities.size() < 2) continue;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Location ploc = p.getLocation();
                    for (int i = 0; i < 2; i++) {
                        Location portalLoc = (i == 0) ? wh.anchorA : wh.anchorB;
                        Location dest = (i == 0) ? wh.anchorB : wh.anchorA;
                        if (ploc.distance(portalLoc) < 2.0 && hasMetadata(p, plugin, "wormhole_cooldown_" + uid)) {
                            p.teleport(dest);
                            setMetadata(p, plugin, "wormhole_cooldown_" + uid, true);
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                            // Remove cooldown after 2s
                            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                                removeMetadata(p, plugin, "wormhole_cooldown_" + uid);
                            }, 40L);
                        }
                    }
                }
            }
        }
    }

    // ── Prevent breaking temp blocks ──────────────────────────────────────────

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (tempBlocks.containsKey(event.getBlock().getLocation())) {
            tempBlocks.remove(event.getBlock().getLocation());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void consumeItem(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
        }
    }

    // ── Codex Registration ────────────────────────────────────────────────────

    public void registerInCodex(CodexCategory cat, space.qclid.dashboard.codex.core.CodexRegistry registry) {
        cat.addItem(new CodexItem.Builder("explorer.gadgets.echo_locator")
                .displayName("Echo Locator").displayItem(echoLocator).xpCost(12)
                .requires(new ItemStack(Material.SPYGLASS))
                .description("Emits a scanning pulse that reveals nearby living entities. 10s cooldown.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.DAYLIGHT_DETECTOR), new ItemStack(Material.COMPASS), new ItemStack(Material.DAYLIGHT_DETECTOR),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.SPYGLASS), new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.void_chisel")
                .displayName("Void Chisel").displayItem(voidChisel).xpCost(10)
                .requires(new ItemStack(Material.IRON_PICKAXE))
                .description("Places temporary blocks at range. Shift-right-click to remove them.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.OBSIDIAN), new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.OBSIDIAN),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.REDSTONE), new ItemStack(Material.ENDER_PEARL),
                        null, new ItemStack(Material.STICK), null)
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.gravity_well")
                .displayName("Gravity Well").displayItem(gravityWell).xpCost(18)
                .requires(new ItemStack(Material.ENDER_EYE))
                .description("Creates a vortex that pulls in nearby items and creatures. 10s duration.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.OBSIDIAN), new ItemStack(Material.ENDER_EYE), new ItemStack(Material.OBSIDIAN),
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.NETHERITE_SCRAP), new ItemStack(Material.IRON_BLOCK),
                        new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.HOPPER), new ItemStack(Material.IRON_BLOCK))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.seismograph")
                .displayName("Seismograph").displayItem(seismograph).xpCost(20)
                .requires(new ItemStack(Material.DEBUG_STICK))
                .description("Sends a shockwave through the ground to reveal underground cavities. 20s cooldown.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT),
                        new ItemStack(Material.REDSTONE), new ItemStack(Material.DAYLIGHT_DETECTOR), new ItemStack(Material.REDSTONE),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.STICK), new ItemStack(Material.IRON_INGOT))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.spectral_crossing")
                .displayName("Spectral Crossing").displayItem(spectralCrossing).xpCost(25)
                .requires(new ItemStack(Material.ENDER_PEARL))
                .description("Creates a temporary bridge across gaps. Fades after a few seconds. 15s cooldown.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.GHAST_TEAR), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.GHAST_TEAR),
                        new ItemStack(Material.PHANTOM_MEMBRANE), new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS), new ItemStack(Material.PHANTOM_MEMBRANE),
                        new ItemStack(Material.PHANTOM_MEMBRANE), new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.PHANTOM_MEMBRANE))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.holographic_decoy")
                .displayName("Holographic Decoy").displayItem(holographicDecoy).xpCost(15)
                .requires(new ItemStack(Material.SNOWBALL))
                .description("Creates a phantom decoy that lures hostile mobs away from you. 20s cooldown.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.LEATHER), new ItemStack(Material.IRON_SWORD), new ItemStack(Material.LEATHER),
                        new ItemStack(Material.LEATHER), new ItemStack(Material.SNOWBALL), new ItemStack(Material.LEATHER),
                        new ItemStack(Material.LEATHER), new ItemStack(Material.REDSTONE), new ItemStack(Material.LEATHER))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.null_spear")
                .displayName("Null Spear").displayItem(nullSpear).xpCost(22)
                .requires(new ItemStack(Material.BLAZE_ROD))
                .description("Orbits a protective shield that intercepts incoming projectiles. 12s cooldown.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.SHIELD), new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.SHIELD),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.NETHER_STAR), new ItemStack(Material.IRON_INGOT),
                        new ItemStack(Material.IRON_INGOT), new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.IRON_INGOT))
                .build());

        cat.addItem(new CodexItem.Builder("explorer.gadgets.wormhole")
                .displayName("Wormhole").displayItem(wormhole).xpCost(30)
                .requires(new ItemStack(Material.END_CRYSTAL))
                .description("Links two locations with a portal. Right-click to set anchors. Lasts 60 seconds.")
                .craftingStation(new ItemStack(Material.CRAFTING_TABLE))
                .recipe(new ItemStack(Material.END_CRYSTAL), new ItemStack(Material.ENDER_EYE), new ItemStack(Material.END_CRYSTAL),
                        new ItemStack(Material.OBSIDIAN), new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.OBSIDIAN),
                        new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.ENDER_PEARL))
                .build());
    }
}
