package space.qclid.arcanum.codex.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.Registry;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.arcanum.PlayerSettings;
import space.qclid.arcanum.codex.gui.CodexMainGui;
import space.qclid.arcanum.data.DataManager;

import space.qclid.arcanum.codex.core.*;
import space.qclid.arcanum.codex.items.*;
import space.qclid.arcanum.codex.crafting.*;
import space.qclid.arcanum.codex.tasks.*;
import space.qclid.arcanum.codex.gui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;
import static space.qclid.arcanum.util.CodexUtil.*;

public class CodexInteractionListener implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;
    private final Map<UUID, String> activeMachine;
    private final Map<UUID, ItemStack[]> lastVoidedItems;

    public CodexInteractionListener(CodexContext ctx) {
        this.plugin = ctx.plugin();
        this.dataManager = ctx.dataManager();
        this.manager = ctx.manager();
        this.registry = ctx.registry();
        this.arcaneItems = ctx.arcaneItems();
        this.explorerItems = ctx.explorerItems();
        this.codexCrafting = ctx.codexCrafting();
        this.codexPassiveTask = ctx.codexPassiveTask();
        this.activeMachine = ctx.activeMachine();
        this.lastVoidedItems = ctx.lastVoidedItems();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (hasMetadata(player, plugin, "rift_walk_active")) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            ItemStack held = event.getItem();
            if (held != null && held.getType() != Material.AIR) {
                ItemMeta heldMeta = held.getItemMeta();
                if (heldMeta != null) {
                    NamespacedKey idKey = space.qclid.arcanum.compat.Compat.key("item_id");
                    String id = heldMeta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
                    if (id != null && id.equals("explorer.tools.omni_tool")) {
                        Material blockType = event.getClickedBlock().getType();
                        Material targetToolMat = Material.DIAMOND_PICKAXE;
                        
                        String name = blockType.name();
                        if (name.contains("DIRT") || name.contains("SAND") || name.contains("GRAVEL") || name.contains("CLAY") || name.contains("SNOW") || name.contains("GRASS_BLOCK") || name.contains("SOUL_SAND") || name.contains("SOUL_SOIL")) {
                            targetToolMat = Material.DIAMOND_SHOVEL;
                        } else if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANKS") || name.contains("CHEST") || name.contains("FENCE") || name.contains("DOOR") || name.contains("STAIRS") || name.contains("SLAB")) {
                            if (blockType.isSolid() && (name.contains("OAK") || name.contains("SPRUCE") || name.contains("BIRCH") || name.contains("JUNGLE") || name.contains("ACACIA") || name.contains("DARK_OAK") || name.contains("MANGROVE") || name.contains("CHERRY") || name.contains("BAMBOO") || name.contains("CRIMSON") || name.contains("WARPED"))) {
                                targetToolMat = Material.DIAMOND_AXE;
                            }
                        }
                        
                        if (held.getType() != targetToolMat) {
                            held.setType(targetToolMat);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 0.5f, 1.5f);
                        }
                    }
                }
            }
        }
        if (hasMetadata(player, plugin, "interacted_entity_this_tick")) {
            return;
        }

        // Check physical action first for Waypoint Teleport Plate
        if (event.getAction() == org.bukkit.event.block.Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                String coordKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                Location targetLoc = dataManager.teleportPlates.get(coordKey);
                if (targetLoc != null) {
                    event.setCancelled(true);
                    player.teleport(targetLoc);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5);
                    return;
                }
            }
        }

        // ── Structure detection: right-clicking block trigger ──────────
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                // Enchanter check
                if (codexCrafting.isValidEnchanter(clickedBlock)) {
                    event.setCancelled(true);
                    openEnchanterGUI(player);
                    return;
                }
                // Disenchanter check
                if (codexCrafting.isValidDisenchanter(clickedBlock)) {
                    event.setCancelled(true);
                    openDisenchanterGUI(player);
                    return;
                }

                // Blessings Altar check
                if (clickedBlock.getType().name().contains("FENCE") && !clickedBlock.getType().name().contains("NETHER")) {
                    Dropper dropper = codexCrafting.getDropperForStructure(clickedBlock, "blessings_altar");
                    if (dropper != null) {
                        event.setCancelled(true);
                        Block dropperBlock = clickedBlock.getRelative(BlockFace.DOWN);
                        BlockFace goldCenterDir = null;
                        BlockFace[] horizontalFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                        for (BlockFace face : horizontalFaces) {
                            if (dropperBlock.getRelative(face, 2).getType() == Material.GOLD_BLOCK) {
                                goldCenterDir = face;
                                break;
                            }
                        }
                        if (goldCenterDir != null) {
                            Location goldCenterLoc = dropperBlock.getRelative(goldCenterDir, 2).getLocation();
                            codexCrafting.triggerBlessingsAltarSacrifice(player, clickedBlock, dropper, goldCenterLoc);
                        }
                        return;
                    }
                }

                // Blood Altar check
                if (clickedBlock.getType() == Material.RED_CARPET) {
                    Block below = clickedBlock.getRelative(BlockFace.DOWN);
                    if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                        Block bottom = below.getRelative(BlockFace.DOWN);
                        if (bottom.getType() == Material.OBSIDIAN) {
                            // Check if player is standing on or near the carpet
                            Block feetBlock = player.getLocation().getBlock();
                            Block feetBelow = feetBlock.getRelative(BlockFace.DOWN);
                            if (feetBlock.equals(clickedBlock) || feetBelow.equals(clickedBlock) || player.getLocation().distance(clickedBlock.getLocation().add(0.5, 1.0, 0.5)) < 2.0) {
                                event.setCancelled(true);
                                codexCrafting.executeBloodAltarCraft(player, dropper, clickedBlock.getLocation().add(0.5, 1.1, 0.5));
                                return;
                            }
                        }
                    }
                }

                // Block Duplicator check
                if (clickedBlock.getType() == Material.NETHER_BRICK_FENCE) {
                    Block below = clickedBlock.getRelative(BlockFace.DOWN);
                    if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                        boolean isSurrounded = true;
                        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                        for (BlockFace face : faces) {
                            Material type = below.getRelative(face).getType();
                            if (type != Material.FURNACE && type != Material.BLAST_FURNACE) {
                                isSurrounded = false;
                                break;
                            }
                        }
                        if (isSurrounded) {
                            event.setCancelled(true);
                            codexCrafting.executeBlockDuplicatorCraft(player, clickedBlock, dropper);
                            return;
                        }
                    }
                }

                // Link Waypoint Teleport Plate
                if (clickedBlock.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    ItemStack handItem = event.getItem();
                    if (handItem != null && arcaneItems.isCustomItem(handItem, "explorer.waypoint_compass")) {
                        event.setCancelled(true);
                        ItemMeta compassMeta = handItem.getItemMeta();
                        NamespacedKey linkKey = space.qclid.arcanum.compat.Compat.key("linked_waypoint");
                        String linkedWp = compassMeta.getPersistentDataContainer().get(linkKey, PersistentDataType.STRING);
                        if (linkedWp == null) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This compass is not linked to any waypoint!")));
                            return;
                        }
                        PlayerSettings settings = dataManager.get(player.getUniqueId());
                        Location wpLoc = null;
                        if (settings != null) {
                            wpLoc = settings.waypoints.get(linkedWp);
                        }
                        if (wpLoc == null) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The waypoint linked to this compass ('") + linkedWp + toSmallCaps("') no longer exists!")));
                            return;
                        }

                        String plateCoordKey = clickedBlock.getWorld().getName() + ":" + clickedBlock.getX() + ":" + clickedBlock.getY() + ":" + clickedBlock.getZ();
                        dataManager.teleportPlates.put(plateCoordKey, wpLoc);
                        dataManager.markPlatesDirty();
                        dataManager.save();

                        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully linked Waypoint Teleport Plate to: ") + C_GOLD + linkedWp));
                        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
                        clickedBlock.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, clickedBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                        return;
                    }
                }

                if (player.isSneaking()) {
                    // Shift-right-click crafting trigger
                    if (clickedBlock.getType() == Material.CRAFTING_TABLE) {
                        Dropper dropper = codexCrafting.getDropperForStructure(clickedBlock, "heavy_forge");
                        if (dropper != null) {
                            event.setCancelled(true);
                            codexCrafting.executeDropperCraft(player, dropper, "heavy_forge", clickedBlock.getLocation().add(0.5, 1.1, 0.5));
                            return;
                        }
                        dropper = codexCrafting.getDropperForStructure(clickedBlock, "arcane_table");
                        if (dropper != null) {
                            event.setCancelled(true);
                            codexCrafting.executeDropperCraft(player, dropper, "arcane_table", clickedBlock.getLocation().add(0.5, 1.1, 0.5));
                            return;
                        }
                    } else if (codexCrafting.isAnvil(clickedBlock.getType())) {
                        Dropper dropper = codexCrafting.getDropperForStructure(clickedBlock, "upgrade_table");
                        if (dropper != null) {
                            event.setCancelled(true);
                            codexCrafting.executeDropperCraft(player, dropper, "upgrade_table", clickedBlock.getLocation().add(0.5, 1.1, 0.5));
                            return;
                        }
                    } else if (clickedBlock.getType() == Material.BLAST_FURNACE) {
                        Dropper dropper = codexCrafting.getDropperForStructure(clickedBlock, "heavy_alloy_forge");
                        if (dropper != null) {
                            event.setCancelled(true);
                            codexCrafting.executeDropperCraft(player, dropper, "heavy_alloy_forge", clickedBlock.getLocation().add(0.5, 1.1, 0.5));
                            return;
                        }
                    }
                } else {
                    // Normal right-click: machine open detection
                    if (clickedBlock.getType() == Material.CRAFTING_TABLE) {
                        ItemStack heldItem = event.getItem();
                        boolean holdingCustomLead = false;
                        if (heldItem != null && heldItem.getType() == Material.LEAD) {
                            ItemMeta heldMeta = heldItem.getItemMeta();
                            if (heldMeta != null) {
                                NamespacedKey hk = space.qclid.arcanum.compat.Compat.key("item_id");
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
            }
        }

        // ── Custom item handling ───────────────────────────────────────────────
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 1. Right-click Codex Book
        NamespacedKey codexKey = space.qclid.arcanum.compat.Compat.key("codex");
        if (meta.getPersistentDataContainer().has(codexKey, PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (event.getAction().name().contains("RIGHT")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
                CodexMainGui.open(player, registry);
            }
            return;
        }

        // 2. Custom Codex Items — only fire on right-click
        NamespacedKey itemKey = space.qclid.arcanum.compat.Compat.key("item_id");
        String itemId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        
        NamespacedKey remediumKey = space.qclid.arcanum.compat.Compat.key("rune_remedium");
        if (meta.getPersistentDataContainer().has(remediumKey, PersistentDataType.INTEGER)) {
            if (player.isSneaking() && event.getAction().name().contains("RIGHT")) {
                event.setCancelled(true);
                if (player.hasCooldown(item.getType())) return;
                
                double currentHp = player.getHealth();
                if (currentHp > 1.0) {
                    player.setHealth(Math.max(1.0, currentHp * 0.5));
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (isNegativeEffect(effect.getType())) {
                            player.removePotionEffect(effect.getType());
                        }
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1)); // Strength II
                    
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.8f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.WITCH, player.getLocation(), 20, 0.5, 1.0, 0.5, 0.1);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Remedium: Purged negative effects for 50% health!")));
                    setCooldown(player, item.getType(), 400); // 20s cooldown
                }
                return;
            }
        }

        if (itemId == null) return;

        if (!event.getAction().name().contains("RIGHT")) return;

        if (itemId.equals("arcane.tomes.glintblade_phalanx")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.GLOBE_BANNER_PATTERN)) return;
            setCooldown(player, Material.GLOBE_BANNER_PATTERN, 300); // 15s cooldown
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
            
            final UUID playerUuid = player.getUniqueId();
            int[] tick = new int[]{0};
            
            // Spawn 4 invisible marker armor stands holding diamond swords
            java.util.List<org.bukkit.entity.ArmorStand> stands = new java.util.ArrayList<>();
            Location spawnLoc = player.getLocation();
            for (int i = 0; i < 4; i++) {
                org.bukkit.entity.ArmorStand stand = spawnLoc.getWorld().spawn(spawnLoc, org.bukkit.entity.ArmorStand.class, s -> {
                    s.setInvisible(true);
                    s.setSmall(true);
                    s.setMarker(true);
                    s.setGravity(false);
                    s.setPersistent(false);
                    s.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                    s.setRightArmPose(new org.bukkit.util.EulerAngle(-Math.PI / 2.0, 0, 0));
                });
                stands.add(stand);
            }
            
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                tick[0]++;
                Player p = Bukkit.getPlayer(playerUuid);
                
                if (p == null || !p.isOnline() || p.isDead() || tick[0] > 300 || stands.isEmpty()) {
                    t.cancel();
                    // Clean up stands
                    for (org.bukkit.entity.ArmorStand s : stands) {
                        s.remove();
                    }
                    stands.clear();
                    return;
                }
                
                Location loc = p.getLocation();
                double angleOffset = tick[0] * 0.15;
                int currentBlades = stands.size();
                for (int i = 0; i < currentBlades; i++) {
                    double angle = angleOffset + i * (Math.PI * 2.0 / currentBlades);
                    double x = Math.cos(angle) * 1.5;
                    double z = Math.sin(angle) * 1.5;
                    Location bladeLoc = loc.clone().add(x, 0.5, z);
                    
                    // Face the rotation direction
                    bladeLoc.setYaw((float) Math.toDegrees(angle) + 90f);
                    stands.get(i).teleport(bladeLoc);
                }
                
                if (tick[0] % 10 == 0) {
                    LivingEntity target = null;
                    
                    // 1. Check phalanx_target metadata
            if (hasMetadata(p, plugin, "phalanx_target")) {
                        try {
                            UUID targetUuid = UUID.fromString(getStringMetadata(p, plugin, "phalanx_target"));
                            Entity ent = Bukkit.getEntity(targetUuid);
                            if (ent instanceof LivingEntity le && !le.isDead() && le.getWorld().equals(p.getWorld()) && le.getLocation().distance(p.getLocation()) <= 6.0) {
                                target = le;
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to parse phalanx_target metadata: " + e.getMessage());
                        }
                    }
                    
                    // 2. Find nearby hostiles, angered neutral mobs, or players the player might be fighting
                    if (target == null) {
                        for (Entity entity : p.getNearbyEntities(6.0, 3.0, 6.0)) {
                            if (entity instanceof LivingEntity le && !le.isDead() && isCombatPartner(p, le)) {
                                target = le;
                                break;
                            }
                        }
                    }
                    
                    if (target != null) {
                        org.bukkit.entity.ArmorStand standToRemove = stands.remove(stands.size() - 1);
                        standToRemove.remove();
                        
                        target.damage(6.0, p);
                        Location mLoc = target.getEyeLocation();
                        mLoc.getWorld().playSound(mLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f);
                        mLoc.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, mLoc, 8, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }, 1L, 1L);
            return;
        }

        if (itemId.startsWith("explorer.tools.locator.")) {
            event.setCancelled(true);
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("charges");
            int charges = meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 10);
            if (charges <= 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This locator has no charges remaining!")));
                return;
            }
            
            String struct = itemId.substring(itemId.lastIndexOf('.') + 1);
            NamespacedKey structureKey = null;
            
            switch (struct.toLowerCase()) {
                case "village": structureKey = NamespacedKey.minecraft("village_plains"); break;
                case "stronghold": structureKey = NamespacedKey.minecraft("stronghold"); break;
                case "end_city": structureKey = NamespacedKey.minecraft("end_city"); break;
                case "mineshaft": structureKey = NamespacedKey.minecraft("mineshaft"); break;
                case "mansion": structureKey = NamespacedKey.minecraft("woodland_mansion"); break;
                case "monument": structureKey = NamespacedKey.minecraft("ocean_monument"); break;
                case "fortress": structureKey = NamespacedKey.minecraft("fortress"); break;
                case "pillager_outpost": structureKey = NamespacedKey.minecraft("pillager_outpost"); break;
                case "desert_pyramid": structureKey = NamespacedKey.minecraft("desert_pyramid"); break;
                case "jungle_pyramid": structureKey = NamespacedKey.minecraft("jungle_temple"); break;
                case "swamp_hut": structureKey = NamespacedKey.minecraft("swamp_hut"); break;
                case "ocean_ruin": structureKey = NamespacedKey.minecraft("ocean_ruin_cold"); break;
                case "shipwreck": structureKey = NamespacedKey.minecraft("shipwreck"); break;
                case "igloo": structureKey = NamespacedKey.minecraft("igloo"); break;
            }
            
            if (structureKey == null) {
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Structure locator type not supported!")));
                return;
            }
            
            org.bukkit.generator.structure.Structure structureObj = org.bukkit.Registry.STRUCTURE.get(structureKey);
            
            Location found = null;
            if (structureObj != null) {
                org.bukkit.util.StructureSearchResult searchResult = player.getWorld().locateNearestStructure(player.getLocation(), structureObj, 100, false);
                if (searchResult != null) {
                    found = searchResult.getLocation();
                }
            }
            
            if (found != null) {
                charges--;
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, charges);
                String structName = Character.toUpperCase(struct.charAt(0)) + struct.substring(1).replace("_", " ");
                meta.lore(List.of(
                        MM.deserialize(C_GRAY + toSmallCaps("Locates the nearest " + structName + ".")),
                        MM.deserialize(""),
                        MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + charges + " / 10")
                ));
                item.setItemMeta(meta);
                
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Nearest ") + C_GOLD + structName + C_GREEN + toSmallCaps(" located at: ") + C_YELLOW + found.getBlockX() + ", " + found.getBlockY() + ", " + found.getBlockZ()));
            } else {
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("No structure found within 100 chunks!")));
            }
            return;
        }

        if (itemId == null) return;

        if (!event.getAction().name().contains("RIGHT")) return;

        // Grappling Hook & Web Slingers
        if (itemId.startsWith("explorer.grappling_hook.") || itemId.startsWith("explorer.tools.web_slinger.")) {
            Block target = event.getClickedBlock();
            if (target != null) {
                String targetName = target.getType().name();
                if (targetName.contains("FENCE") || targetName.contains("WALL")) {
                    // Let vanilla handle leashing to fence posts
                    return;
                }
            }

            event.setCancelled(true);
            if (player.hasCooldown(Material.LEAD)) return;

            double range = 10.0;
            if (itemId.endsWith("diamond")) range = 25.0;
            else if (itemId.endsWith("netherite")) range = 50.0;

            // Shoot projectile
            Snowball snowball = player.launchProjectile(Snowball.class);
            setMetadata(snowball, plugin, "grapple_range", range);
            setMetadata(snowball, plugin, "grapple_owner", player.getUniqueId().toString());
            if (itemId.startsWith("explorer.tools.web_slinger.")) {
                setMetadata(snowball, plugin, "web_slinger_projectile", true);
            }

            // Spawn invisible leash ArmorStand as the visual wire anchor
            ArmorStand stand = player.getWorld().spawn(player.getEyeLocation(), ArmorStand.class, s -> {
                s.setInvisible(true);
                s.setMarker(true);
                s.setGravity(false);
                s.setSilent(true);
                s.setPersistent(false);
            });
            stand.setLeashHolder(player);

            setCooldown(player, Material.LEAD, 80); // 4 s cooldown
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

        if (itemId.equals("arcane.ranged.wand_of_levitation")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.FEATHER)) return;
            setCooldown(player, Material.FEATHER, 300); // 15s cooldown
            Snowball snowball = player.launchProjectile(Snowball.class);
            setMetadata(snowball, plugin, "levitation_projectile", true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 1.2f);
            return;
        }

        if (itemId.equals("arcane.ranged.staff_of_the_stormlord")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.LIGHTNING_ROD)) return;

            Block targetBlock = player.getTargetBlockExact(20);
            Location strikeLoc = targetBlock != null ? targetBlock.getLocation() : null;
            if (strikeLoc == null) {
                org.bukkit.util.RayTraceResult ray = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 20.0);
                if (ray != null && ray.getHitBlock() != null) {
                    strikeLoc = ray.getHitBlock().getLocation();
                }
            }

            if (strikeLoc != null) {
                setCooldown(player, Material.LIGHTNING_ROD, 400); // 20s cooldown
                strikeLoc.getWorld().strikeLightning(strikeLoc);
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1f);
            } else {
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("No block in range (max 20 blocks)!")));
            }
            return;
        }

        if (itemId.equals("explorer.exploration.ore_scanner")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.SPYGLASS)) return;
            setCooldown(player, Material.SPYGLASS, 600); // 30s cooldown

            player.playSound(player.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1f, 1.5f);

            Location center = player.getLocation();
            List<Block> ores = new ArrayList<>();
            int radius = 10;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        Material type = b.getType();
                        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE ||
                            type == Material.GOLD_ORE || type == Material.DEEPSLATE_GOLD_ORE ||
                            type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE) {
                            ores.add(b);
                        }
                    }
                }
            }

            if (ores.isEmpty()) {
                player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("No ores detected nearby.")));
                return;
            }

            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Detected ") + C_ORANGE + ores.size() + C_GREEN + toSmallCaps(" nearby ores! Highlighted for 5 seconds.")));

            int[] count = new int[]{0};
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (count[0] >= 10 || !player.isOnline()) {
                    t.cancel();
                    return;
                }
                for (Block b : ores) {
                    Material tType = b.getType();
                    if (tType == Material.DIAMOND_ORE || tType == Material.DEEPSLATE_DIAMOND_ORE ||
                        tType == Material.GOLD_ORE || tType == Material.DEEPSLATE_GOLD_ORE ||
                        tType == Material.IRON_ORE || tType == Material.DEEPSLATE_IRON_ORE) {
                        
                        Location blockLoc = b.getLocation().add(0.5, 0.5, 0.5);
                        b.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, blockLoc, 3, 0.2, 0.2, 0.2, 0.0);
                    }
                }
                count[0]++;
            }, 1L, 10L);
            return;
        }

        if (itemId.equals("arcane.wand_of_embers")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.BLAZE_ROD)) return;
            player.launchProjectile(org.bukkit.entity.SmallFireball.class);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            setCooldown(player, Material.BLAZE_ROD, 30); // 1.5 s cooldown
            return;
        }

        if (itemId.equals("arcane.ranged.staff_of_supplant")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.IRON_HOE)) return;
            setCooldown(player, Material.IRON_HOE, 60); // 3s cooldown
            Snowball snowball = player.launchProjectile(Snowball.class);
            setMetadata(snowball, plugin, "supplant_projectile", true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 1f);
            return;
        }

        if (itemId.equals("arcane.trinkets.totem_of_fallacy")) {
            event.setCancelled(true);
            Location spawn = player.getRespawnLocation();
            if (spawn == null) {
                spawn = player.getWorld().getSpawnLocation();
            }

            // Reduce durability of all items to 10%
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.getType() != Material.AIR) {
                    ItemMeta invMeta = invItem.getItemMeta();
                    if (invMeta instanceof Damageable damageable) {
                        int max = invItem.getType().getMaxDurability();
                        if (max > 0) {
                            damageable.setDamage((int) (max * 0.9));
                            invItem.setItemMeta((ItemMeta) damageable);
                        }
                    }
                }
            }

            player.teleport(spawn);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 100, 0.5, 1, 0.5);
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Teleported to spawn, but at a cost...")));

            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
            return;
        }

        if (itemId.equals("arcane.materials.empty_vial")) {
            event.setCancelled(true);
            double currentHealth = player.getHealth();
            double damage = currentHealth * 0.60;
            player.damage(damage);

            org.bukkit.inventory.EquipmentSlot hand = event.getHand();
            if (hand != null) {
                if (item.getAmount() <= 1) {
                    player.getInventory().setItem(hand, arcaneItems.bloodItem.clone());
                } else {
                    item.setAmount(item.getAmount() - 1);
                    player.getInventory().setItem(hand, item);
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItemNaturally(player.getLocation(), arcaneItems.bloodItem.clone());
                    } else {
                        player.getInventory().addItem(arcaneItems.bloodItem.clone());
                    }
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
            player.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("You extracted your blood into the vial.")));
            return;
        }

        if (itemId.equals("arcane.materials.soul_orb")) {
            event.setCancelled(true);
            player.giveExp(500);
            if (item.getAmount() <= 1) {
                player.getInventory().setItem(event.getHand(), null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(event.getHand(), item);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("You absorbed the Soul Orb for 500 XP!")));
            return;
        }

        if (itemId.equals("explorer.gadgets.ender_backpack")) {
            event.setCancelled(true);
            player.openInventory(player.getEnderChest());
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
            return;
        }

        if (itemId.equals("explorer.gadgets.safari_lasso")) {
            NamespacedKey mobKey = space.qclid.arcanum.compat.Compat.key("safari_mob");
            String mobTypeStr = meta.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
            if (mobTypeStr != null) {
                event.setCancelled(true);
                Block clickedBlock = event.getClickedBlock();
                BlockFace face = event.getBlockFace();
                if (clickedBlock != null && face != null) {
                    Location spawnLoc = clickedBlock.getRelative(face).getLocation().add(0.5, 0.0, 0.5);
                    org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(mobTypeStr);
                    org.bukkit.entity.Entity spawned = player.getWorld().spawnEntity(spawnLoc, type);

                    NamespacedKey nameKey = space.qclid.arcanum.compat.Compat.key("safari_name");
                    String customName = meta.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
                    if (customName != null && !customName.isEmpty()) {
                        spawned.setCustomName(customName);
                        spawned.setCustomNameVisible(true);
                    }

                    NamespacedKey ageKey = space.qclid.arcanum.compat.Compat.key("safari_age");
                    Integer age = meta.getPersistentDataContainer().get(ageKey, PersistentDataType.INTEGER);
                    if (age != null && spawned instanceof org.bukkit.entity.Ageable ageable) {
                        ageable.setAge(age);
                    }

                    NamespacedKey colorKey = space.qclid.arcanum.compat.Compat.key("safari_color");
                    String colorStr = meta.getPersistentDataContainer().get(colorKey, PersistentDataType.STRING);
                    if (colorStr != null && spawned instanceof org.bukkit.entity.Sheep sheep) {
                        sheep.setColor(org.bukkit.DyeColor.valueOf(colorStr));
                    }

                    meta.getPersistentDataContainer().remove(mobKey);
                    meta.getPersistentDataContainer().remove(nameKey);
                    meta.getPersistentDataContainer().remove(ageKey);
                    meta.getPersistentDataContainer().remove(colorKey);

                    meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Safari Lasso")));
                    meta.lore(List.of(
                            MM.deserialize(C_GRAY + toSmallCaps("Captures passive animals for transport.")),
                            MM.deserialize(""),
                            MM.deserialize(C_YELLOW + toSmallCaps("Right-click a passive mob to capture.")),
                            MM.deserialize(C_YELLOW + toSmallCaps("Right-click a block to release the mob."))
                    ));
                    item.setItemMeta(meta);

                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 0.8f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, spawnLoc, 15, 0.2, 0.2, 0.2, 0.05);
                }
            }
            return;
        }

        if (itemId.equals("explorer.gadgets.void_bag")) {
            event.setCancelled(true);
            VoidBagInventoryHolder voidHolder = new VoidBagInventoryHolder();
            Inventory inv = Bukkit.createInventory(voidHolder, 9, parse("<dark_gray>» " + C_RED + toSmallCaps("Void Bag")));
            voidHolder.setInventory(inv);
            player.openInventory(inv);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 0.8f);
            return;
        }

        if (itemId.equals("explorer.gadgets.void_bag_refund")) {
            event.setCancelled(true);
            ItemStack[] backup = lastVoidedItems.remove(player.getUniqueId());
            if (backup == null) {
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("No voided items to recover!")));
                return;
            }

            // Consume one refund item
            org.bukkit.inventory.EquipmentSlot hand = event.getHand();
            if (item.getAmount() <= 1) {
                player.getInventory().setItem(hand, null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(hand, item);
            }

            // Restore items to inventory or drop on ground
            int restored = 0;
            for (ItemStack voided : backup) {
                if (voided == null || voided.getType() == Material.AIR) continue;
                java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(voided);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                restored++;
            }

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f);
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Restored ") + restored + toSmallCaps(" item(s) from the void!")));
            return;
        }

        if (itemId.equals("explorer.tools.webber")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.COBWEB)) return;
            setCooldown(player, Material.COBWEB, 20); // 1s cooldown

            Snowball snowball = player.launchProjectile(Snowball.class);
            setMetadata(snowball, plugin, "web_projectile", true);
            player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_HURT, 1f, 1.5f);
            return;
        }

        if (itemId.equals("explorer.tools.builders_wand")) {
            event.setCancelled(true);
            Block clicked = event.getClickedBlock();
            BlockFace face = event.getBlockFace();
            if (clicked != null && face != null) {
                Material type = clicked.getType();
                if (player.getInventory().contains(type)) {
                    Block targetBlock = clicked.getRelative(face);
                    int placed = 0;
                    for (int i = 0; i < 9; i++) {
                        Block current = targetBlock.getRelative(face, i);
                        if (current.getType() == Material.AIR || current.getType() == Material.CAVE_AIR || current.getType() == Material.WATER) {
                            if (takeFromInventory(player, type)) {
                                current.setType(type);
                                placed++;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (placed > 0) {
                        player.playSound(clicked.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);
                    }
                }
            }
            return;
        }

        if (itemId.equals("explorer.gadgets.thermal_canteen")) {
            event.setCancelled(true);
            NamespacedKey key = space.qclid.arcanum.compat.Compat.key("charges");
            int charges = meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 4);

            Block target = event.getClickedBlock();
            if (target != null && (target.getType() == Material.CAMPFIRE || target.getType() == Material.SOUL_CAMPFIRE || target.getType() == Material.LAVA)) {
                if (charges < 4) {
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 4);
                    meta.lore(List.of(
                            MM.deserialize(C_GRAY + toSmallCaps("Flask cures effects and restores hunger.")),
                            MM.deserialize(""),
                            MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + "4 / 4")
                    ));
                    item.setItemMeta(meta);
                    player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1f, 1f);
                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Refilled your Thermal Canteen!")));
                } else {
                    player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Canteen is already full!")));
                }
                return;
            }

            if (charges > 0) {
                charges--;
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, charges);
                meta.lore(List.of(
                        MM.deserialize(C_GRAY + toSmallCaps("Flask cures effects and restores hunger.")),
                        MM.deserialize(""),
                        MM.deserialize(C_YELLOW + toSmallCaps("Charges") + ": " + C_ORANGE + charges + " / 4")
                ));
                item.setItemMeta(meta);

                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    PotionEffectType type = effect.getType();
                    if (isNegativeEffect(type)) {
                        player.removePotionEffect(type);
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1f, 1f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Canteen is empty! Refill at campfire or lava.")));
            }
            return;
        }

        if (itemId.equals("explorer.tools.beastmasters_flute")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.BAMBOO)) return;
            setCooldown(player, Material.BAMBOO, 1200);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 1.5f, 1f);
            player.getWorld().spawnParticle(org.bukkit.Particle.NOTE, player.getLocation().add(0, 1.5, 0), 10, 0.5, 0.5, 0.5);

            int count = 0;
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Monster monster) {
                    monster.setTarget(null);
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 4));
                    monster.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, monster.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2);
                    count++;
                }
            }
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Pacified ") + C_YELLOW + count + C_GREEN + toSmallCaps(" nearby monsters.")));
            return;
        }

        if (itemId.equals("explorer.tools.portable_smelter")) {
            event.setCancelled(true);
            Inventory inv = Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.FURNACE, parse("<dark_gray>» " + C_ORANGE + toSmallCaps("Portable Smelter")));
            player.openInventory(inv);
            codexPassiveTask.activeSmelters.put(player.getUniqueId(), inv);
            player.playSound(player.getLocation(), Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1f, 1f);
            return;
        }
        if (itemId.equals("explorer.tools.portable_furnace")) {
            event.setCancelled(true);
            Inventory inv = Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.FURNACE, parse("<dark_gray>» " + C_ORANGE + toSmallCaps("Portable Furnace")));
            player.openInventory(inv);
            codexPassiveTask.activeFurnaces.put(player.getUniqueId(), inv);
            player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1f, 1f);
            return;
        }
        if (itemId.equals("explorer.tools.portable_crafting_table")) {
            event.setCancelled(true);
            player.openWorkbench(null, true);
            return;
        }
        if (itemId.equals("explorer.tools.portable_anvil")) {
            event.setCancelled(true);
            player.openAnvil(null, true);
            return;
        }
        if (itemId.equals("explorer.tools.portable_grindstone")) {
            event.setCancelled(true);
            player.openGrindstone(null, true);
            return;
        }
        if (itemId.equals("explorer.tools.portable_stonecutter")) {
            event.setCancelled(true);
            player.openStonecutter(null, true);
            return;
        }
        if (itemId.equals("explorer.tools.portable_smithing_table")) {
            event.setCancelled(true);
            player.openSmithingTable(null, true);
            return;
        }

        if (itemId.equals("explorer.waypoint_compass")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.COMPASS)) return;
            NamespacedKey linkKey = space.qclid.arcanum.compat.Compat.key("linked_waypoint");
            String linkedWp = meta.getPersistentDataContainer().get(linkKey, PersistentDataType.STRING);
            if (player.isSneaking() || linkedWp == null) {
                openWaypointLinkGui(player);
            } else {
                teleportToWaypoint(player, linkedWp, item);
            }
            return;
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
        Inventory inv = Bukkit.createInventory(linkHolder, size, parse("<dark_gray>» " + G_GOLD + toSmallCaps("Link Waypoint")));
        linkHolder.setInventory(inv);

        ItemStack glass = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(parse(" "));
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
                wpMeta.displayName(parse(C_ORANGE + toSmallCaps(wpName)));
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
                NamespacedKey linkKey = space.qclid.arcanum.compat.Compat.key("linked_waypoint");
                meta.getPersistentDataContainer().remove(linkKey);
                meta.displayName(parse(G_GOLD + toSmallCaps("Waypoint Compass")));
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

        setCooldown(player, Material.COMPASS, 200);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Teleported to waypoint: ") + C_ORANGE + waypointName));
    }

    @EventHandler
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
        String itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (itemId == null) return;

        // Set metadata to prevent double interact
        setMetadata(player, plugin, "interacted_entity_this_tick", true);
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
            removeMetadata(player, plugin, "interacted_entity_this_tick");
        }, 1L);

        if (itemId.equals("arcane.materials.empty_vial")) {
            org.bukkit.entity.Entity clicked = event.getRightClicked();
            if (clicked.getType() == org.bukkit.entity.EntityType.SPIDER ||
                clicked.getType() == org.bukkit.entity.EntityType.CAVE_SPIDER ||
                clicked.getType() == org.bukkit.entity.EntityType.BEE ||
                clicked.getType() == org.bukkit.entity.EntityType.PUFFERFISH) {

                event.setCancelled(true);
                player.damage(2.0); // 1 heart of damage

                org.bukkit.inventory.EquipmentSlot hand = event.getHand();
                if (hand != null) {
                    if (item.getAmount() <= 1) {
                        player.getInventory().setItem(hand, arcaneItems.poisonVialItem.clone());
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().setItem(hand, item);
                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItemNaturally(player.getLocation(), arcaneItems.poisonVialItem.clone());
                        } else {
                            player.getInventory().addItem(arcaneItems.poisonVialItem.clone());
                        }
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
                player.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("You extracted venom into the vial.")));
            }
            return;
        }

        if (!itemId.equals("explorer.gadgets.safari_lasso")) return;

        event.setCancelled(true);

        NamespacedKey mobKey = space.qclid.arcanum.compat.Compat.key("safari_mob");
        if (meta.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This lasso already contains a mob!")));
            return;
        }

        org.bukkit.entity.Entity clicked = event.getRightClicked();
        if (clicked instanceof org.bukkit.entity.Animals || clicked instanceof org.bukkit.entity.WaterMob || clicked instanceof org.bukkit.entity.Ambient) {
            String mobType = clicked.getType().name();
            String customName = clicked.getCustomName() != null ? clicked.getCustomName() : "";

            if (item.getAmount() > 1) {
                ItemStack singleLasso = item.clone();
                singleLasso.setAmount(1);
                item.setAmount(item.getAmount() - 1);

                ItemMeta singleMeta = singleLasso.getItemMeta();
                singleMeta.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mobType);
                if (!customName.isEmpty()) {
                    NamespacedKey nameKey = space.qclid.arcanum.compat.Compat.key("safari_name");
                    singleMeta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, customName);
                }

                if (clicked instanceof org.bukkit.entity.Ageable ageable) {
                    NamespacedKey ageKey = space.qclid.arcanum.compat.Compat.key("safari_age");
                    singleMeta.getPersistentDataContainer().set(ageKey, PersistentDataType.INTEGER, ageable.getAge());
                }

                if (clicked instanceof org.bukkit.entity.Sheep sheep) {
                    NamespacedKey colorKey = space.qclid.arcanum.compat.Compat.key("safari_color");
                    singleMeta.getPersistentDataContainer().set(colorKey, PersistentDataType.STRING, sheep.getColor().name());
                }

                String formattedName = mobType.replace("_", " ").toLowerCase();
                singleMeta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Safari Lasso") + C_GRAY + " (" + C_GREEN + toSmallCaps(formattedName) + C_GRAY + ")"));
                singleMeta.lore(List.of(
                        MM.deserialize(C_GRAY + toSmallCaps("Contains: ") + C_GREEN + toSmallCaps(formattedName)),
                        MM.deserialize(""),
                        MM.deserialize(C_YELLOW + toSmallCaps("Right-click a block to release the mob."))
                ));
                singleLasso.setItemMeta(singleMeta);

                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), singleLasso);
                } else {
                    player.getInventory().addItem(singleLasso);
                }
            } else {
                meta.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mobType);
                if (!customName.isEmpty()) {
                    NamespacedKey nameKey = space.qclid.arcanum.compat.Compat.key("safari_name");
                    meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, customName);
                }

                if (clicked instanceof org.bukkit.entity.Ageable ageable) {
                    NamespacedKey ageKey = space.qclid.arcanum.compat.Compat.key("safari_age");
                    meta.getPersistentDataContainer().set(ageKey, PersistentDataType.INTEGER, ageable.getAge());
                }

                if (clicked instanceof org.bukkit.entity.Sheep sheep) {
                    NamespacedKey colorKey = space.qclid.arcanum.compat.Compat.key("safari_color");
                    meta.getPersistentDataContainer().set(colorKey, PersistentDataType.STRING, sheep.getColor().name());
                }

                String formattedName = mobType.replace("_", " ").toLowerCase();
                meta.displayName(parse(G_GOLD + "<bold>" + toSmallCaps("Safari Lasso") + C_GRAY + " (" + C_GREEN + toSmallCaps(formattedName) + C_GRAY + ")"));
                meta.lore(List.of(
                        MM.deserialize(C_GRAY + toSmallCaps("Contains: ") + C_GREEN + toSmallCaps(formattedName)),
                        MM.deserialize(""),
                        MM.deserialize(C_YELLOW + toSmallCaps("Right-click a block to release the mob."))
                ));
                item.setItemMeta(meta);
            }

            clicked.remove();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.8f);
            player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, clicked.getLocation().add(0, 0.5, 0), 15, 0.2, 0.2, 0.2, 0.05);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Prevent temp webs from dropping anything
        Block block = event.getBlock();
        if (block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            String coordKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
            if (dataManager.teleportPlates.containsKey(coordKey)) {
                dataManager.teleportPlates.remove(coordKey);
                dataManager.markPlatesDirty();
                dataManager.save();
                player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Waypoint Teleport Plate link removed.")));
            }
        }

        if (block.getType() == Material.COBWEB && hasMetadata(block, plugin, "temp_web")) {
            event.setDropItems(false);
            removeMetadata(block, plugin, "temp_web");
            return;
        }

        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        NamespacedKey dbKey = space.qclid.arcanum.compat.Compat.key("rune_dwarfs_blessing");
        NamespacedKey tkKey = space.qclid.arcanum.compat.Compat.key("rune_telekinesis");
        NamespacedKey timberKey = space.qclid.arcanum.compat.Compat.key("rune_timber");

        boolean hasSmelt = meta.getPersistentDataContainer().has(dbKey, PersistentDataType.INTEGER);
        boolean hasTele = meta.getPersistentDataContainer().has(tkKey, PersistentDataType.INTEGER);
        boolean hasTimber = meta.getPersistentDataContainer().has(timberKey, PersistentDataType.INTEGER);

        if (hasTimber && (block.getType().name().contains("LOG") || block.getType().name().contains("WOOD"))) {
            Material blockType = block.getType();
            List<Block> logs = new ArrayList<>();
            findContiguousLogs(block, blockType, logs, new java.util.HashSet<>(), 128);

            event.setCancelled(true);
            for (Block log : logs) {
                if (log.getType() == blockType) {
                    java.util.Collection<ItemStack> logDrops = log.getDrops(item, player);
                    log.setType(Material.AIR);
                    damageTool(player, item, 1);

                    for (ItemStack drop : logDrops) {
                        ItemStack finalDrop = hasSmelt ? smeltItemIfPossible(drop) : drop;
                        if (hasTele) {
                            java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);
                            for (ItemStack dropLeft : leftover.values()) {
                                log.getWorld().dropItemNaturally(log.getLocation(), dropLeft);
                            }
                        } else {
                            log.getWorld().dropItemNaturally(log.getLocation(), finalDrop);
                        }
                    }
                }
            }
            return;
        }

        // Daedalus' Touch — check BEFORE smelt/tele so it handles ore blocks first
        NamespacedKey dtKey = space.qclid.arcanum.compat.Compat.key("rune_daedalus_touch");
        boolean hasDaedalus = meta.getPersistentDataContainer().has(dtKey, PersistentDataType.INTEGER);
        if (hasDaedalus && player.isSneaking() && block.getType().name().contains("ORE")) {
            Material oreType = block.getType();
            List<Block> ores = new ArrayList<>();
            findContiguousOres(block, oreType, ores, new java.util.HashSet<>(), 16);
            
            event.setCancelled(true);
            for (Block ore : ores) {
                if (ore.getType() == oreType) {
                    java.util.Collection<ItemStack> oreDrops = ore.getDrops(item, player);
                    ore.setType(Material.AIR);
                    damageTool(player, item, 1);
                    
                    for (ItemStack drop : oreDrops) {
                        ItemStack finalDrop = hasSmelt ? smeltItemIfPossible(drop) : drop;
                        if (hasTele) {
                            java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);
                            for (ItemStack dropLeft : leftover.values()) {
                                ore.getWorld().dropItemNaturally(ore.getLocation(), dropLeft);
                            }
                        } else {
                            ore.getWorld().dropItemNaturally(ore.getLocation(), finalDrop);
                        }
                    }
                }
            }
            return;
        }

        if (hasSmelt || hasTele) {
            event.setCancelled(true);
            java.util.Collection<ItemStack> drops = block.getDrops(item, player);
            block.setType(Material.AIR);
            damageTool(player, item, 1);

            for (ItemStack drop : drops) {
                ItemStack finalDrop = hasSmelt ? smeltItemIfPossible(drop) : drop;
                if (hasTele) {
                    java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);
                    for (ItemStack dropLeft : leftover.values()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), dropLeft);
                    }
                } else {
                    block.getWorld().dropItemNaturally(block.getLocation(), finalDrop);
                }
            }

            if (hasTele) {
                int xp = event.getExpToDrop();
                if (xp > 0) {
                    event.setExpToDrop(0);
                    player.giveExp(xp);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.5f);
                }
            }
            return;
        }

        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
        String itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (itemId != null && itemId.equals("explorer.tools.excavation_drill")) {
            event.setCancelled(true);
            Block target = event.getBlock();
            BlockFace hitFace = getBlockFacePlayerIsLookingAt(player, target);

            int xMin = -1, xMax = 1, yMin = -1, yMax = 1, zMin = -1, zMax = 1;
            if (hitFace == BlockFace.UP || hitFace == BlockFace.DOWN) {
                yMin = 0; yMax = 0;
            } else if (hitFace == BlockFace.EAST || hitFace == BlockFace.WEST) {
                xMin = 0; xMax = 0;
            } else if (hitFace == BlockFace.NORTH || hitFace == BlockFace.SOUTH) {
                zMin = 0; zMax = 0;
            }

            List<Block> toBreak = new ArrayList<>();
            for (int dx = xMin; dx <= xMax; dx++) {
                for (int dy = yMin; dy <= yMax; dy++) {
                    for (int dz = zMin; dz <= zMax; dz++) {
                        Block b = target.getRelative(dx, dy, dz);
                        if (b.getType().getHardness() >= 0 && b.getType() != Material.BEDROCK && b.getType() != Material.AIR) {
                            toBreak.add(b);
                        }
                    }
                }
            }

            for (Block b : toBreak) {
                java.util.Collection<ItemStack> drillDrops = b.getDrops(item, player);
                b.setType(Material.AIR);
                damageTool(player, item, 1);

                for (ItemStack drop : drillDrops) {
                    ItemStack finalDrop = hasSmelt ? smeltItemIfPossible(drop) : drop;
                    if (hasTele) {
                        java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);
                        for (ItemStack dropLeft : leftover.values()) {
                            b.getWorld().dropItemNaturally(b.getLocation(), dropLeft);
                        }
                    } else {
                        b.getWorld().dropItemNaturally(b.getLocation(), finalDrop);
                    }
                }
            }
        }
    }

    private BlockFace getBlockFacePlayerIsLookingAt(Player player, Block block) {
        List<Block> lastTwo = player.getLastTwoTargetBlocks(null, 5);
        if (lastTwo.size() > 1) {
            Block b1 = lastTwo.get(0);
            Block b2 = lastTwo.get(1);
            return b2.getFace(b1);
        }
        return BlockFace.UP;
    }

    private void damageTool(Player player, ItemStack item, int amount) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + amount);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            } else {
                item.setItemMeta((ItemMeta) damageable);
            }
        }
    }

    private boolean takeFromInventory(Player player, Material material) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return true;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    private boolean isNegativeEffect(PotionEffectType type) {
        return type == PotionEffectType.POISON ||
                type == PotionEffectType.WITHER ||
                type == PotionEffectType.BLINDNESS ||
                type == PotionEffectType.SLOWNESS ||
                type == PotionEffectType.WEAKNESS ||
                type == PotionEffectType.NAUSEA ||
                type == PotionEffectType.MINING_FATIGUE ||
                type == PotionEffectType.HUNGER;
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
            case CLAY_BALL: return Material.BRICK;
            case NETHERRACK: return Material.NETHER_BRICK;
            default: return null;
        }
    }

    private ItemStack smeltItemIfPossible(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        Material m = item.getType();
        if (m.name().contains("_LOG") || m.name().contains("_WOOD")) {
            ItemStack charcoal = new ItemStack(Material.CHARCOAL, item.getAmount());
            return charcoal;
        }
        Material smeltedMat = getSmeltResult(m);
        if (smeltedMat != null) {
            ItemStack result = new ItemStack(smeltedMat, item.getAmount());
            return result;
        }
        return item;
    }

    private void findContiguousLogs(Block current, Material logType, List<Block> result, java.util.Set<Location> visited, int limit) {
        if (result.size() >= limit || visited.contains(current.getLocation())) return;
        visited.add(current.getLocation());

        if (current.getType() == logType) {
            result.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        findContiguousLogs(current.getRelative(dx, dy, dz), logType, result, visited, limit);
                    }
                }
            }
        }
    }

    private String detectStructure(Block craftingTable) {
        // 1. Heavy Forge: Crafting Table on top of Blast Furnace
        Block below = craftingTable.getRelative(BlockFace.DOWN);
        if (below.getType() == Material.BLAST_FURNACE) {
            return "heavy_forge";
        }

        // 2. Arcana Table: Crafting Table horizontally adjacent to at least 2 Bookshelves
        int bookshelves = 0;
        BlockFace[] faces = {
                BlockFace.NORTH,
                BlockFace.SOUTH,
                BlockFace.EAST,
                BlockFace.WEST
        };
        for (BlockFace face : faces) {
            if (craftingTable.getRelative(face).getType() == Material.BOOKSHELF) {
                bookshelves++;
            }
        }
        if (bookshelves >= 2) {
            return "arcane_table";
        }

        return "none";
    }

    // ── Enchanter & Disenchanter GUIs ────────────────────────────────────────

    private void openEnchanterGUI(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item!")));
            return;
        }

        List<Enchantment> possible = new ArrayList<>();
        for (Enchantment ench : Registry.ENCHANTMENT) {
            if (ench.canEnchantItem(held) || held.getType() == Material.ENCHANTED_BOOK) possible.add(ench);
        }
        if (possible.isEmpty()) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item cannot be enchanted!")));
            return;
        }

        int size = Math.min(54, Math.max(9, ((possible.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, parse(G_GOLD + toSmallCaps("Enchanter")));

        for (Enchantment ench : possible) {
            int currentLevel = held.getItemMeta() instanceof EnchantmentStorageMeta esMeta
                    ? esMeta.getStoredEnchantLevel(ench)
                    : held.getEnchantmentLevel(ench);
            if (currentLevel >= ench.getMaxLevel()) continue;

            int nextLevel = currentLevel + 1;
            int cost      = (ench.getMaxLevel() == 1) ? 3 : nextLevel;

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(ench, nextLevel, true);
                meta.displayName(parse(G_GOLD + toSmallCaps(ench.key().value().replace("_", " ")) + " " + nextLevel));
                meta.lore(List.of(MM.deserialize(C_YELLOW + toSmallCaps("Cost") + ": " + C_ORANGE + cost + " " + toSmallCaps("diamonds"))));
                book.setItemMeta(meta);
            }
            inv.addItem(book);
        }
        player.openInventory(inv);
    }

    private void openDisenchanterGUI(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item!")));
            return;
        }

        Map<Enchantment, Integer> enchants = held.getItemMeta() instanceof EnchantmentStorageMeta esMeta
                ? esMeta.getStoredEnchants()
                : held.getEnchantments();

        if (enchants.isEmpty()) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item has no enchantments!")));
            return;
        }

        int size = Math.min(54, Math.max(9, ((enchants.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, parse(G_GOLD + toSmallCaps("Disenchanter")));

        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            int refund = (e.getKey().getMaxLevel() == 1) ? 3 : e.getValue();
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(e.getKey(), e.getValue(), true);
                meta.displayName(parse(G_GOLD + toSmallCaps(e.getKey().key().value().replace("_", " ")) + " " + e.getValue()));
                meta.lore(List.of(MM.deserialize(C_GREEN + toSmallCaps("Refund") + ": " + C_ORANGE + refund + " " + toSmallCaps("diamonds"))));
                book.setItemMeta(meta);
            }
            inv.addItem(book);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof CodexInventoryHolder || 
            event.getInventory().getHolder() instanceof WaypointLinkInventoryHolder) {
            return;
        }

        net.kyori.adventure.text.Component titleComp = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(titleComp);

        boolean isDisenchant = plainTitle.contains(toSmallCaps("Disenchanter"));
        boolean isEnchant    = !isDisenchant && plainTitle.contains(toSmallCaps("Enchanter"));
        if (!isEnchant && !isDisenchant) return;

        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.ENCHANTED_BOOK) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) return;

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) clicked.getItemMeta();
        if (bookMeta == null || !bookMeta.hasStoredEnchants()) return;

        Map.Entry<Enchantment, Integer> entry = bookMeta.getStoredEnchants().entrySet().iterator().next();
        Enchantment ench = entry.getKey();
        int level        = entry.getValue();

        if (isEnchant) {
            int cost = (ench.getMaxLevel() == 1) ? 3 : level;
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
                player.closeInventory();
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You need " + cost + " diamonds!")));
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));

            if (held.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) held.getItemMeta();
                if (meta != null) {
                    meta.addStoredEnchant(ench, level, true);
                    held.setItemMeta(meta);
                }
            } else {
                held.addUnsafeEnchantment(ench, level);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
            openEnchanterGUI(player);

        } else {
            int refund = (ench.getMaxLevel() == 1) ? 3 : level;

            if (held.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) held.getItemMeta();
                if (meta != null) {
                    meta.removeStoredEnchant(ench);
                    held.setItemMeta(meta);
                }
            } else {
                held.removeEnchantment(ench);
            }
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, refund));
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);

            Map<Enchantment, Integer> remaining = held.getItemMeta() instanceof EnchantmentStorageMeta esMeta
                    ? esMeta.getStoredEnchants()
                    : held.getEnchantments();
            if (remaining.isEmpty()) player.closeInventory();
            else openDisenchanterGUI(player);
        }
    }

    // ── Kinetic Crusher & Sifting Trommel ────────────────────────────────────

    @EventHandler
    public void onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        Block pistonBlock = event.getBlock();
        if (!(pistonBlock.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData)) return;

        if (pistonData.getFacing() == BlockFace.DOWN) {
            Block crushSpace = pistonBlock.getRelative(BlockFace.DOWN);
            Block hopperBlock = crushSpace.getRelative(BlockFace.DOWN);
            if (hopperBlock.getType() == Material.HOPPER) {
                Block baseCenter = hopperBlock.getRelative(BlockFace.DOWN);
                boolean baseValid = true;
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (baseCenter.getRelative(x, 0, z).getType() != Material.IRON_BLOCK) {
                            baseValid = false;
                            break;
                        }
                    }
                    if (!baseValid) break;
                }

                if (baseValid) {
                    Location searchLoc = crushSpace.getLocation().add(0.5, 0.5, 0.5);
                    java.util.Collection<org.bukkit.entity.Item> items = searchLoc.getWorld().getNearbyEntitiesByType(org.bukkit.entity.Item.class, searchLoc, 0.8);
                    for (org.bukkit.entity.Item itemEntity : items) {
                        ItemStack stack = itemEntity.getItemStack();
                        if (stack.getType() == Material.ENDER_PEARL) {
                            int amount = stack.getAmount();
                            itemEntity.remove();
                            ItemStack dust = arcaneItems.getCustomItem("arcane.materials.crushed_ender_dust");
                            if (dust != null) {
                                dust = dust.clone();
                                dust.setAmount(amount * 2);
                                crushSpace.getWorld().dropItemNaturally(crushSpace.getLocation().add(0.5, 0.1, 0.5), dust);
                            }
                            crushSpace.getWorld().playSound(crushSpace.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, 1.5f);
                            crushSpace.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, crushSpace.getLocation().add(0.5, 0.5, 0.5), 15, 0.2, 0.2, 0.2);
                        } else if (stack.getType() == Material.BLAZE_ROD) {
                            int amount = stack.getAmount();
                            itemEntity.remove();
                            ItemStack powder = new ItemStack(Material.BLAZE_POWDER, amount * 4);
                            crushSpace.getWorld().dropItemNaturally(crushSpace.getLocation().add(0.5, 0.1, 0.5), powder);
                            crushSpace.getWorld().playSound(crushSpace.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, 1.2f);
                            crushSpace.getWorld().spawnParticle(org.bukkit.Particle.LAVA, crushSpace.getLocation().add(0.5, 0.5, 0.5), 15, 0.2, 0.2, 0.2);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryPickupItem(org.bukkit.event.inventory.InventoryPickupItemEvent event) {
        if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.HOPPER) return;

        Block hopperBlock = event.getInventory().getLocation().getBlock();
        Block aboveBlock = hopperBlock.getRelative(BlockFace.UP);
        if (aboveBlock.getType() != Material.IRON_BARS) return;

        BlockFace[] horizontalFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        boolean copperSurrounded = true;
        for (BlockFace face : horizontalFaces) {
            Material type = hopperBlock.getRelative(face).getType();
            if (!type.name().contains("COPPER")) {
                copperSurrounded = false;
                break;
            }
        }
        if (!copperSurrounded) return;

        org.bukkit.entity.Item itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItemStack();
        Material itemType = stack.getType();

        if (itemType == Material.GRAVEL || itemType == Material.SAND || itemType == Material.RED_SAND || itemType == Material.DIRT) {
            event.setCancelled(true);

            int newAmount = stack.getAmount() - 1;
            if (newAmount <= 0) {
                itemEntity.remove();
            } else {
                stack.setAmount(newAmount);
                itemEntity.setItemStack(stack);
            }

            Location siftLoc = aboveBlock.getLocation().add(0.5, 0.2, 0.5);
            Sound breakSound = (itemType == Material.SAND || itemType == Material.RED_SAND) ? Sound.BLOCK_SAND_BREAK : Sound.BLOCK_GRAVEL_BREAK;
            siftLoc.getWorld().playSound(siftLoc, breakSound, 0.8f, 1f);
            siftLoc.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, siftLoc, 10, 0.2, 0.2, 0.2, Bukkit.createBlockData(itemType));

            double rand = Math.random();
            ItemStack output = null;
            if (rand < 0.05) {
                output = arcaneItems.getCustomItem("arcane.materials.fractured_geode");
            } else if (rand < 0.15) {
                output = new ItemStack(Material.RAW_GOLD);
            } else if (rand < 0.40) {
                output = new ItemStack(Material.FLINT);
            }

            if (output != null) {
                aboveBlock.getWorld().dropItemNaturally(aboveBlock.getLocation().add(0.5, 0.5, 0.5), output.clone());
                aboveBlock.getWorld().playSound(aboveBlock.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
            }
        }
    }

    private void setCooldown(Player player, Material material, int ticks) {
        if (codexPassiveTask.isWearingFullSet(player, "storm_weaver")) {
            ticks = ticks / 2;
        }
        player.setCooldown(material, ticks);
    }

    private void findContiguousOres(Block current, Material oreType, List<Block> result, java.util.Set<Location> visited, int limit) {
        if (result.size() >= limit || visited.contains(current.getLocation())) return;
        visited.add(current.getLocation());

        if (current.getType() == oreType) {
            result.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        findContiguousOres(current.getRelative(dx, dy, dz), oreType, result, visited, limit);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockDamage(org.bukkit.event.block.BlockDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        NamespacedKey idKey = space.qclid.arcanum.compat.Compat.key("item_id");
        String id = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (id != null && id.equals("explorer.tools.omni_tool")) {
            Material blockType = event.getBlock().getType();
            Material targetToolMat = Material.DIAMOND_PICKAXE;
            
            String name = blockType.name();
            if (name.contains("DIRT") || name.contains("SAND") || name.contains("GRAVEL") || name.contains("CLAY") || name.contains("SNOW") || name.contains("GRASS_BLOCK") || name.contains("SOUL_SAND") || name.contains("SOUL_SOIL")) {
                targetToolMat = Material.DIAMOND_SHOVEL;
            } else if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANKS") || name.contains("CHEST") || name.contains("FENCE") || name.contains("DOOR") || name.contains("STAIRS") || name.contains("SLAB")) {
                if (blockType.isSolid() && (name.contains("OAK") || name.contains("SPRUCE") || name.contains("BIRCH") || name.contains("JUNGLE") || name.contains("ACACIA") || name.contains("DARK_OAK") || name.contains("MANGROVE") || name.contains("CHERRY") || name.contains("BAMBOO") || name.contains("CRIMSON") || name.contains("WARPED"))) {
                    targetToolMat = Material.DIAMOND_AXE;
                }
            }
            
            if (item.getType() != targetToolMat) {
                item.setType(targetToolMat);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 0.5f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onPlayerItemDamage(org.bukkit.event.player.PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.getType() == Material.SHIELD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_ouroboros");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    Damageable damageable = (Damageable) meta;
                    int maxDurability = item.getType().getMaxDurability();
                    int currentDamage = damageable.getDamage();
                    int newDamage = currentDamage + event.getDamage();
                    
                    if (newDamage >= maxDurability) {
                        boolean consumed = false;
                        for (ItemStack invItem : player.getInventory().getContents()) {
                            if (invItem != null) {
                                ItemMeta im = invItem.getItemMeta();
                                if (im != null) {
                                    NamespacedKey itemIdKey = space.qclid.arcanum.compat.Compat.key("item_id");
                                    String id = im.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                                    if (id != null && id.equals("arcane.materials.ender_essence")) {
                                        invItem.setAmount(invItem.getAmount() - 1);
                                        consumed = true;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (consumed) {
                            event.setCancelled(true);
                            damageable.setDamage(0);
                            item.setItemMeta((ItemMeta) damageable);
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
                            player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 15, 0.3, 0.5, 0.3, 0.1);
                            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Ouroboros Shield consumed Ender Essence to restore durability!")));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
                String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if (id != null && id.equals("arcane.materials.blood_orb")) {
                    event.setCancelled(true);
                    
                    if (item.getAmount() <= 1) {
                        player.getInventory().setItem(event.getHand(), null);
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().setItem(event.getHand(), item);
                    }
                    
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Consumed Blood Orb: Hunger and Saturation completely restored!")));
                }
            }
        }
    }

    private boolean isCombatPartner(Player p, LivingEntity le) {
        if (p.equals(le)) return false;
        if (le instanceof Player otherPlayer) {
            if (hasMetadata(otherPlayer, plugin, "phalanx_target")) {
                try {
                    String targetUuidStr = getStringMetadata(otherPlayer, plugin, "phalanx_target");
                    if (p.getUniqueId().toString().equals(targetUuidStr)) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse phalanx_target metadata: " + e.getMessage());
                }
            }
            if (hasMetadata(p, plugin, "phalanx_target")) {
                try {
                    String targetUuidStr = getStringMetadata(p, plugin, "phalanx_target");
                    if (otherPlayer.getUniqueId().toString().equals(targetUuidStr)) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse phalanx_target metadata: " + e.getMessage());
                }
            }
        } else {
            if (le instanceof org.bukkit.entity.Monster) {
                return true;
            }
            if (le instanceof org.bukkit.entity.Creature creature && p.equals(creature.getTarget())) {
                return true;
            }
            if (hasMetadata(p, plugin, "phalanx_target")) {
                try {
                    String targetUuidStr = getStringMetadata(p, plugin, "phalanx_target");
                    if (le.getUniqueId().toString().equals(targetUuidStr)) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse phalanx_target metadata: " + e.getMessage());
                }
            }
            if (hasMetadata(le, plugin, "phalanx_target")) {
                try {
                    String targetUuidStr = getStringMetadata(le, plugin, "phalanx_target");
                    if (p.getUniqueId().toString().equals(targetUuidStr)) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse phalanx_target metadata: " + e.getMessage());
                }
            }
        }
        return false;
    }
}
