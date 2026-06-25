package space.qclid.dashboard.codex;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexListener implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;

    // Track active block structures players are right-clicking
    public final Map<UUID, String> activeMachine = new HashMap<>();

    public CodexListener(JavaPlugin plugin, DataManager dataManager, CodexManager manager, CodexRegistry registry,
                         ArcaneItems arcaneItems, ExplorerItems explorerItems, CodexCrafting codexCrafting, CodexPassiveTask codexPassiveTask) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.manager = manager;
        this.registry = registry;
        this.arcaneItems = arcaneItems;
        this.explorerItems = explorerItems;
        this.codexCrafting = codexCrafting;
        this.codexPassiveTask = codexPassiveTask;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // ── Structure detection: right-clicking block trigger ──────────
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        org.bukkit.event.inventory.InventoryType type = event.getInventory().getType();
        if (type == org.bukkit.event.inventory.InventoryType.WORKBENCH
                || type == org.bukkit.event.inventory.InventoryType.CRAFTING) {
            activeMachine.remove(player.getUniqueId());
        }

        // Return portable furnace/smelter items on close
        if (codexPassiveTask.activeFurnaces.containsKey(player.getUniqueId()) && event.getInventory().equals(codexPassiveTask.activeFurnaces.get(player.getUniqueId()))) {
            returnFurnaceItems(player, codexPassiveTask.activeFurnaces.remove(player.getUniqueId()));
        }
        if (codexPassiveTask.activeSmelters.containsKey(player.getUniqueId()) && event.getInventory().equals(codexPassiveTask.activeSmelters.get(player.getUniqueId()))) {
            returnFurnaceItems(player, codexPassiveTask.activeSmelters.remove(player.getUniqueId()));
        }
    }

    private void returnFurnaceItems(Player player, Inventory inv) {
        for (int i = 0; i < 3; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
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

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // 1. Staff of Supplant
        if (snowball.hasMetadata("supplant_projectile")) {
            Player player = (Player) snowball.getShooter();
            if (player != null && player.isOnline() && event.getHitEntity() instanceof LivingEntity target) {
                Location playerLoc = player.getLocation();
                Location targetLoc = target.getLocation();

                player.teleport(targetLoc);
                target.teleport(playerLoc);

                playerLoc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, playerLoc, 30, 0.5, 1, 0.5);
                targetLoc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, targetLoc, 30, 0.5, 1, 0.5);

                playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
            snowball.remove();
            return;
        }

        // 2. Webber Projectile
        if (snowball.hasMetadata("web_projectile")) {
            Location loc = null;
            if (event.getHitBlock() != null) {
                loc = event.getHitBlock().getRelative(event.getHitBlockFace()).getLocation();
            } else if (event.getHitEntity() != null) {
                loc = event.getHitEntity().getLocation();
            }
            if (loc != null) {
                Block webBlock = loc.getBlock();
                if (webBlock.getType() == Material.AIR || webBlock.getType() == Material.CAVE_AIR) {
                    placeTemporaryWeb(webBlock, 100L); // 5 seconds
                }
            }
            snowball.remove();
            return;
        }

        // 3. Grappling Hooks & Web Slingers
        if (snowball.hasMetadata("grapple_range")) {
            Player player = (Player) snowball.getShooter();
            if (player == null || !player.isOnline()) return;

            Location hitLoc = event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5, 0.5, 0.5)
                    : (event.getHitEntity() != null ? event.getHitEntity().getLocation() : null);

            if (hitLoc != null) {
                org.bukkit.util.Vector vector = hitLoc.toVector().subtract(player.getLocation().toVector());
                double distance = vector.length();
                if (distance > 1) {
                    org.bukkit.util.Vector velocity = vector.normalize().multiply(1.5);
                    velocity.setY(Math.min(0.8, vector.getY() * 0.1 + 0.55));
                    player.setVelocity(velocity);
                    player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 0.8f, 1.3f);

                    if (snowball.hasMetadata("web_slinger_projectile")) {
                        trackLanding(player);
                    }
                }
            }
            snowball.remove();
        }
    }

    private void placeTemporaryWeb(Block block, long ticks) {
        block.setType(Material.COBWEB);
        block.setMetadata("temp_web", new FixedMetadataValue(plugin, true));

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
            if (block.getType() == Material.COBWEB && block.hasMetadata("temp_web")) {
                block.setType(Material.AIR);
                block.removeMetadata("temp_web", plugin);
            }
        }, ticks);
    }

    private void trackLanding(Player player) {
        final UUID uuid = player.getUniqueId();
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                t.cancel();
                return;
            }
            if (p.isOnGround()) {
                t.cancel();
                Block block = p.getLocation().getBlock();
                if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) {
                    placeTemporaryWeb(block, 100L); // 5 seconds
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_COBWEB_PLACE, 1f, 1f);
                }
            }
        }, 1L, 1L);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();
        if (bow == null || bow.getType() == Material.AIR) return;
        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;
        NamespacedKey catchFlameKey = new NamespacedKey(plugin, "rune_catch_flame");
        Integer level = meta.getPersistentDataContainer().get(catchFlameKey, PersistentDataType.INTEGER);
        if (level != null && event.getProjectile() instanceof org.bukkit.entity.Arrow arrow) {
            arrow.setMetadata("catch_flame_level", new FixedMetadataValue(plugin, level));
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand == null || mainHand.getType() != Material.FIREWORK_STAR) return;
        if (offHand == null || offHand.getType() == Material.AIR) return;

        ItemMeta mainMeta = mainHand.getItemMeta();
        if (mainMeta == null) return;

        NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
        NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
        NamespacedKey typeKey = new NamespacedKey(plugin, "rune_type");

        String effect = mainMeta.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
        Integer lvl = mainMeta.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
        String runeType = mainMeta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);

        if (effect == null || lvl == null || runeType == null) return;

        // This is a custom rune, cancel event
        event.setCancelled(true);

        boolean valid = false;
        if (runeType.equals("WEAPON_SWORD")) {
            valid = offHand.getType().name().contains("SWORD");
        } else if (runeType.equals("ARMOR_BOOTS")) {
            valid = offHand.getType().name().contains("BOOTS");
        } else if (runeType.equals("WEAPON_BOW_SWORD") || runeType.equals("WEAPON")) {
            valid = offHand.getType().name().contains("SWORD") || offHand.getType().name().contains("BOW");
        }

        if (!valid) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This rune cannot be applied to this item!")));
            return;
        }

        ItemMeta offMeta = offHand.getItemMeta();
        if (offMeta == null) return;

        NamespacedKey applyKey = new NamespacedKey(plugin, "rune_" + effect);
        Integer existingLvl = offMeta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);

        int targetLvl = lvl;
        if (existingLvl != null) {
            if (lvl > existingLvl) {
                targetLvl = lvl;
            } else if (lvl == existingLvl) {
                if (existingLvl >= 3) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item already has the maximum level of this enchantment!")));
                    return;
                }
                targetLvl = existingLvl + 1;
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("This item already has a higher level enchantment of the same type.")));
                return;
            }
        }

        offMeta.getPersistentDataContainer().set(applyKey, PersistentDataType.INTEGER, targetLvl);

        // Apply vanilla flame equivalents if Catch Flame
        if (effect.equalsIgnoreCase("catch_flame")) {
            if (offHand.getType().name().contains("SWORD")) {
                offMeta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
            } else if (offHand.getType().name().contains("BOW")) {
                offMeta.addEnchant(org.bukkit.enchantments.Enchantment.FLAME, 1, true);
            }
        }

        List<net.kyori.adventure.text.Component> lore = offMeta.lore();
        if (lore == null) lore = new ArrayList<>();

        String roman = targetLvl == 1 ? "I" : (targetLvl == 2 ? "II" : "III");
        String nameBase = "";
        if (effect.equalsIgnoreCase("lifesteal")) nameBase = "Lifesteal";
        else if (effect.equalsIgnoreCase("speed")) nameBase = "Speed";
        else if (effect.equalsIgnoreCase("catch_flame")) nameBase = "Catch Flame";

        String displayEffectName = nameBase + " " + roman;
        net.kyori.adventure.text.Component runeLoreComponent = MM.deserialize(C_PURPLE + toSmallCaps(displayEffectName) + " " + C_GRAY + toSmallCaps("(Applied)"));

        boolean found = false;
        for (int i = 0; i < lore.size(); i++) {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (plain.contains(toSmallCaps(nameBase))) {
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

        // Consume 1 rune safely
        if (mainHand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            mainHand.setAmount(mainHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(mainHand);
        }
        player.getInventory().setItemInOffHand(offHand);

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
        player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 30, 0.5, 1, 0.5);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully applied ") + C_PURPLE + toSmallCaps(displayEffectName) + C_GREEN + toSmallCaps(" to your item!")));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity targetEntity) {
            if (event.getDamager() instanceof org.bukkit.entity.Arrow arrow) {
                if (arrow.hasMetadata("catch_flame_level")) {
                    int level = arrow.getMetadata("catch_flame_level").get(0).asInt();
                    targetEntity.setMetadata("catch_flame_level", new FixedMetadataValue(plugin, level));
                }
            }
        }

        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack sword = player.getInventory().getItemInMainHand();
        if (sword != null && sword.getType() != Material.AIR) {
            ItemMeta meta = sword.getItemMeta();
            if (meta != null) {
                NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
                String itemId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

                // 1. Lifesteal Rune
                NamespacedKey applyKey = new NamespacedKey(plugin, "rune_lifesteal");
                Integer lvl = meta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);
                if (lvl != null) {
                    double damage = event.getFinalDamage();
                    double healAmount = damage * 0.15 * lvl;
                    double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1.2, 0), 4, 0.2, 0.2, 0.2);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f);
                }

                // Catch Flame Rune Check
                NamespacedKey catchFlameKey = new NamespacedKey(plugin, "rune_catch_flame");
                Integer catchFlameLvl = meta.getPersistentDataContainer().get(catchFlameKey, PersistentDataType.INTEGER);
                if (catchFlameLvl != null) {
                    target.setMetadata("catch_flame_level", new FixedMetadataValue(plugin, catchFlameLvl));
                }

                // 2. Venomous Scythe
                if (itemId != null && itemId.equals("arcane.melee.venomous_scythe")) {
                    int currentPoison = 0;
                    PotionEffect activePoison = target.getPotionEffect(PotionEffectType.POISON);
                    if (activePoison != null) {
                        currentPoison = activePoison.getAmplifier() + 1;
                    }
                    int nextPoison = Math.min(5, currentPoison + 1);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, nextPoison - 1));

                    if (currentPoison > 0) {
                        double bonus = currentPoison * 1.5;
                        event.setDamage(event.getDamage() + bonus);
                        target.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, target.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2);
                    }
                }
            }
        }

        // 3. Frostbite Ring
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        boolean hasRing = isCustomItem(main, "arcane.trinkets.frostbite_ring") ||
                          isCustomItem(off, "arcane.trinkets.frostbite_ring");
        if (hasRing && target.getFireTicks() > 0) {
            target.setFireTicks(0);
            target.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, target.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1.2f);
        }

        // 4. Stormcaller Medallion
        boolean hasMedallion = isCustomItem(main, "arcane.trinkets.stormcaller_medallion") ||
                               isCustomItem(off, "arcane.trinkets.stormcaller_medallion");
        if (hasMedallion && (player.getWorld().hasStorm() || player.getWorld().isThundering())) {
            if (Math.random() < 0.15) {
                player.getWorld().strikeLightning(target.getLocation());
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Prevent temp webs from dropping anything
        Block block = event.getBlock();
        if (block.getType() == Material.COBWEB && block.hasMetadata("temp_web")) {
            event.setDropItems(false);
            block.removeMetadata("temp_web", plugin);
            return;
        }

        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
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
                b.breakNaturally(item);
                damageTool(player, item, 1);
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        boolean hasRing = isCustomItem(main, "arcane.trinkets.frostbite_ring") ||
                          isCustomItem(off, "arcane.trinkets.frostbite_ring");
        if (hasRing) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getType() == Material.WATER) {
                block.setType(Material.FROSTED_ICE);
            }
        }
    }

    @EventHandler
    public void onEntityResurrect(org.bukkit.event.entity.EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        ItemStack amulet = null;
        boolean inMain = false;

        if (isCustomItem(main, "arcane.trinkets.amulet_of_the_phoenix")) {
            amulet = main;
            inMain = true;
        } else if (isCustomItem(off, "arcane.trinkets.amulet_of_the_phoenix")) {
            amulet = off;
        }

        if (amulet != null) {
            event.setCancelled(false); // Force resurrect
            ItemMeta meta = amulet.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(plugin, "uses");
                int uses = meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 3);
                uses--;
                if (uses <= 0) {
                    if (inMain) player.getInventory().setItemInMainHand(null);
                    else player.getInventory().setItemInOffHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                } else {
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, uses);
                    meta.lore(List.of(
                            MM.deserialize(C_GRAY + toSmallCaps("Grants totem recovery and fiery explosion knockback.")),
                            MM.deserialize(""),
                            MM.deserialize(C_YELLOW + toSmallCaps("Uses remaining") + ": " + C_ORANGE + uses)
                    ));
                    amulet.setItemMeta(meta);
                }
            }

            Location loc = player.getLocation();
            player.getWorld().createExplosion(loc, 3.0f, false, false);
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(4.0, 4.0, 4.0)) {
                if (entity instanceof LivingEntity le && !le.equals(player)) {
                    le.setFireTicks(100);
                    org.bukkit.util.Vector vec = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.5);
                    le.setVelocity(vec);
                }
            }
            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("The Amulet of the Phoenix saved your life!")));
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
