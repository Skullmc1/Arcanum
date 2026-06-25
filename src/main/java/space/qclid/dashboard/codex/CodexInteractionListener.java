package space.qclid.dashboard.codex;

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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.codex.gui.CodexMainGui;
import space.qclid.dashboard.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

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

    public CodexInteractionListener(JavaPlugin plugin, DataManager dataManager, CodexManager manager, CodexRegistry registry,
                                    ArcaneItems arcaneItems, ExplorerItems explorerItems, CodexCrafting codexCrafting,
                                    CodexPassiveTask codexPassiveTask, Map<UUID, String> activeMachine) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = manager;
        this.registry = registry;
        this.arcaneItems = arcaneItems;
        this.explorerItems = explorerItems;
        this.codexCrafting = codexCrafting;
        this.codexPassiveTask = codexPassiveTask;
        this.activeMachine = activeMachine;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("interacted_entity_this_tick")) {
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
                        NamespacedKey linkKey = new NamespacedKey(plugin, "linked_waypoint");
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
                    }
                } else {
                    // Normal right-click: machine open detection
                    if (clickedBlock.getType() == Material.CRAFTING_TABLE) {
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
            snowball.setMetadata("grapple_range", new FixedMetadataValue(plugin, range));
            snowball.setMetadata("grapple_owner", new FixedMetadataValue(plugin, player.getUniqueId()));
            if (itemId.startsWith("explorer.tools.web_slinger.")) {
                snowball.setMetadata("web_slinger_projectile", new FixedMetadataValue(plugin, true));
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

        if (itemId.equals("arcane.ranged.wand_of_levitation")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.FEATHER)) return;
            player.setCooldown(Material.FEATHER, 300); // 15s cooldown
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("levitation_projectile", new FixedMetadataValue(plugin, true));
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
                player.setCooldown(Material.LIGHTNING_ROD, 400); // 20s cooldown
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
            player.setCooldown(Material.SPYGLASS, 600); // 30s cooldown

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
            player.setCooldown(Material.BLAZE_ROD, 30); // 1.5 s cooldown
            return;
        }

        if (itemId.equals("arcane.ranged.staff_of_supplant")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.IRON_HOE)) return;
            player.setCooldown(Material.IRON_HOE, 60); // 3s cooldown
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("supplant_projectile", new FixedMetadataValue(plugin, true));
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
            NamespacedKey mobKey = new NamespacedKey(plugin, "safari_mob");
            String mobTypeStr = meta.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
            if (mobTypeStr != null) {
                event.setCancelled(true);
                Block clickedBlock = event.getClickedBlock();
                BlockFace face = event.getBlockFace();
                if (clickedBlock != null && face != null) {
                    Location spawnLoc = clickedBlock.getRelative(face).getLocation().add(0.5, 0.0, 0.5);
                    org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(mobTypeStr);
                    org.bukkit.entity.Entity spawned = player.getWorld().spawnEntity(spawnLoc, type);

                    NamespacedKey nameKey = new NamespacedKey(plugin, "safari_name");
                    String customName = meta.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
                    if (customName != null && !customName.isEmpty()) {
                        spawned.setCustomName(customName);
                        spawned.setCustomNameVisible(true);
                    }

                    NamespacedKey ageKey = new NamespacedKey(plugin, "safari_age");
                    Integer age = meta.getPersistentDataContainer().get(ageKey, PersistentDataType.INTEGER);
                    if (age != null && spawned instanceof org.bukkit.entity.Ageable ageable) {
                        ageable.setAge(age);
                    }

                    NamespacedKey colorKey = new NamespacedKey(plugin, "safari_color");
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

        if (itemId.equals("explorer.tools.webber")) {
            event.setCancelled(true);
            if (player.hasCooldown(Material.COBWEB)) return;
            player.setCooldown(Material.COBWEB, 20); // 1s cooldown

            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("web_projectile", new FixedMetadataValue(plugin, true));
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
            NamespacedKey key = new NamespacedKey(plugin, "charges");
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
            player.setCooldown(Material.BAMBOO, 1200);
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
            NamespacedKey linkKey = new NamespacedKey(plugin, "linked_waypoint");
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
                NamespacedKey linkKey = new NamespacedKey(plugin, "linked_waypoint");
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

        player.setCooldown(Material.COMPASS, 200);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Teleported to waypoint: ") + C_ORANGE + waypointName));
    }

    @EventHandler
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (itemId == null) return;

        // Set metadata to prevent double interact
        player.setMetadata("interacted_entity_this_tick", new FixedMetadataValue(plugin, true));
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
            player.removeMetadata("interacted_entity_this_tick", plugin);
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

        NamespacedKey mobKey = new NamespacedKey(plugin, "safari_mob");
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
                    NamespacedKey nameKey = new NamespacedKey(plugin, "safari_name");
                    singleMeta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, customName);
                }

                if (clicked instanceof org.bukkit.entity.Ageable ageable) {
                    NamespacedKey ageKey = new NamespacedKey(plugin, "safari_age");
                    singleMeta.getPersistentDataContainer().set(ageKey, PersistentDataType.INTEGER, ageable.getAge());
                }

                if (clicked instanceof org.bukkit.entity.Sheep sheep) {
                    NamespacedKey colorKey = new NamespacedKey(plugin, "safari_color");
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
                    NamespacedKey nameKey = new NamespacedKey(plugin, "safari_name");
                    meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, customName);
                }

                if (clicked instanceof org.bukkit.entity.Ageable ageable) {
                    NamespacedKey ageKey = new NamespacedKey(plugin, "safari_age");
                    meta.getPersistentDataContainer().set(ageKey, PersistentDataType.INTEGER, ageable.getAge());
                }

                if (clicked instanceof org.bukkit.entity.Sheep sheep) {
                    NamespacedKey colorKey = new NamespacedKey(plugin, "safari_color");
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
                dataManager.save();
                player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Waypoint Teleport Plate link removed.")));
            }
        }

        if (block.getType() == Material.COBWEB && block.hasMetadata("temp_web")) {
            event.setDropItems(false);
            block.removeMetadata("temp_web", plugin);
            return;
        }

        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        NamespacedKey dbKey = new NamespacedKey(plugin, "rune_dwarfs_blessing");
        NamespacedKey tkKey = new NamespacedKey(plugin, "rune_telekinesis");
        NamespacedKey timberKey = new NamespacedKey(plugin, "rune_timber");

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

        NamespacedKey key = new NamespacedKey(plugin, "item_id");
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
}
