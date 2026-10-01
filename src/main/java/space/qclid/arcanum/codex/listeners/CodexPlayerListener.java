package space.qclid.arcanum.codex.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
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

public class CodexPlayerListener implements Listener {

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

    public CodexPlayerListener(CodexContext ctx) {
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
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        org.bukkit.event.inventory.InventoryType type = event.getInventory().getType();
        if (type == org.bukkit.event.inventory.InventoryType.WORKBENCH
                || type == org.bukkit.event.inventory.InventoryType.CRAFTING) {
            activeMachine.remove(player.getUniqueId());
        }

        if (event.getInventory().getHolder() instanceof VoidBagInventoryHolder) {
            // Capture backup before clearing
            lastVoidedItems.put(player.getUniqueId(), event.getInventory().getContents());
            event.getInventory().clear();
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Void Bag items have been deleted!")));
            player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Craft a Void Bag Refund to reclaim them!")));
        }

        // Return portable furnace/smelter items on close
        if (codexPassiveTask.activeFurnaces.containsKey(player.getUniqueId()) && event.getInventory().equals(codexPassiveTask.activeFurnaces.get(player.getUniqueId()))) {
            returnFurnaceItems(player, codexPassiveTask.activeFurnaces.remove(player.getUniqueId()));
        }
        if (codexPassiveTask.activeSmelters.containsKey(player.getUniqueId()) && event.getInventory().equals(codexPassiveTask.activeSmelters.get(player.getUniqueId()))) {
            returnFurnaceItems(player, codexPassiveTask.activeSmelters.remove(player.getUniqueId()));
        }

        // Refresh only custom items upon inventory close
        refreshItemLore(player.getItemOnCursor(), plugin);
        for (ItemStack item : event.getInventory().getContents()) {
            if (hasCustomData(item)) refreshItemLore(item, plugin);
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (hasCustomData(item)) refreshItemLore(item, plugin);
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        // Refresh immediately (only custom items)
        if (hasCustomData(event.getCurrentItem())) refreshItemLore(event.getCurrentItem(), plugin);
        if (hasCustomData(event.getCursor())) refreshItemLore(event.getCursor(), plugin);
    }

    /** Quick check if an item has plugin custom data before doing full lore refresh. */
    private boolean hasCustomData(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return !meta.getPersistentDataContainer().isEmpty();
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        // Frostbite Ring
        boolean hasRing = arcaneItems.isCustomItem(main, "arcane.trinkets.frostbite_ring") ||
                          arcaneItems.isCustomItem(off, "arcane.trinkets.frostbite_ring");
        if (hasRing) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getType() == Material.WATER) {
                block.setType(Material.FROSTED_ICE);
            }
        }

        // Basalt Trail
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            ItemMeta bMeta = boots.getItemMeta();
            if (bMeta != null) {
                NamespacedKey btKey = space.qclid.arcanum.compat.Compat.key("rune_basalt_trail");
                if (bMeta.getPersistentDataContainer().has(btKey, PersistentDataType.INTEGER)) {
                    Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if (below.getType() == Material.LAVA) {
                        below.setType(Material.BASALT);
                        setMetadata(below, plugin, "temp_basalt", true);

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (below.getType() == Material.BASALT && hasMetadata(below, plugin, "temp_basalt")) {
                                below.setType(Material.LAVA);
                                removeMetadata(below, plugin, "temp_basalt");
                            }
                        }, 200L);
                    }
                }
            }
        }

        // Static Charge
        ItemStack chest = player.getInventory().getChestplate();
        if (chest != null && chest.getType() != Material.AIR) {
            ItemMeta cMeta = chest.getItemMeta();
            if (cMeta != null) {
                NamespacedKey scKey = space.qclid.arcanum.compat.Compat.key("rune_static_charge");
                Integer scLvl = cMeta.getPersistentDataContainer().get(scKey, PersistentDataType.INTEGER);
                if (scLvl != null) {
                    double dist = event.getFrom().distance(event.getTo());
                    if (dist > 0.02) {
                        double charge = getDoubleMetadata(player, plugin, "static_charge_amount", 0.0);
                        if (charge < 100.0) {
                            charge = Math.min(100.0, charge + dist * (1.5 + scLvl * 0.5));
                            setMetadata(player, plugin, "static_charge_amount", charge);

                            if (charge >= 100.0) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 2f);
                                player.sendMessage(MM.deserialize(C_YELLOW + toSmallCaps("Static Charge fully loaded!")));
                            }

                            if (charge > 50.0 && Math.random() < 0.1) {
                                player.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, player.getLocation().add(0, 1, 0), 2, 0.2, 0.4, 0.2, 0.0);
                            }
                        }
                    }
                }
            }
        }

        // Hallowed Ground
        if (boots != null && boots.getType() != Material.AIR) {
            ItemMeta bMeta = boots.getItemMeta();
            if (bMeta != null) {
                NamespacedKey hgKey = space.qclid.arcanum.compat.Compat.key("rune_hallowed_ground");
                if (bMeta.getPersistentDataContainer().has(hgKey, PersistentDataType.INTEGER)) {
                    if (Bukkit.getCurrentTick() % 10 == 0) {
                        player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, player.getLocation(), 3, 0.2, 0.1, 0.2, 0.01);
                        
                        // Cure player wither/poison
                        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.POISON)) player.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
                        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.WITHER)) player.removePotionEffect(org.bukkit.potion.PotionEffectType.WITHER);
                        
                        // Damage Undead
                        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(1.5, 1.0, 1.5)) {
                            if (entity instanceof LivingEntity le && le.getCategory() == org.bukkit.entity.EntityCategory.UNDEAD) {
                                le.damage(2.0, player);
                            }
                        }
                    }
                }
            }
        }

        // Ashen Veil
        ItemStack legs = player.getInventory().getLeggings();
        if (legs != null && legs.getType() != Material.AIR) {
            ItemMeta lMeta = legs.getItemMeta();
            if (lMeta != null) {
                NamespacedKey avKey = space.qclid.arcanum.compat.Compat.key("rune_ashen_veil");
                if (lMeta.getPersistentDataContainer().has(avKey, PersistentDataType.INTEGER)) {
                    if (player.isSneaking() && Bukkit.getCurrentTick() % 10 == 0) {
                        player.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), 10, 0.5, 0.2, 0.5, 0.01);
                        
                        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(2.0, 1.0, 2.0)) {
                            if (entity instanceof LivingEntity le && !le.equals(player)) {
                                le.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
                                le.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WITHER, 60, 0));
                            }
                        }
                    }
                }
            }
        }

        // Mach Rush
        if (boots != null && boots.getType() != Material.AIR) {
            ItemMeta bMeta = boots.getItemMeta();
            if (bMeta != null) {
                NamespacedKey mrKey = space.qclid.arcanum.compat.Compat.key("rune_mach_rush");
                if (bMeta.getPersistentDataContainer().has(mrKey, PersistentDataType.INTEGER)) {
                    if (player.isSprinting() && player.getVelocity().lengthSquared() > 0.001) {
                        int ticks = getIntMetadata(player, plugin, "mach_rush_ticks", 0);
                        ticks++;
                        setMetadata(player, plugin, "mach_rush_ticks", ticks);
                        
                        int speedLvl = Math.min(3, ticks / 40); // Max Speed IV (amplifier 3)
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 40, speedLvl, true, false, true));
                        
                        if (ticks % 20 == 0) {
                            player.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, player.getLocation(), 5, 0.3, 0.1, 0.3, 0.05);
                        }
                    } else {
                        removeMetadata(player, plugin, "mach_rush_ticks");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        ItemStack amulet = null;
        boolean inMain = false;

        if (arcaneItems.isCustomItem(main, "arcane.trinkets.amulet_of_the_phoenix")) {
            amulet = main;
            inMain = true;
        } else if (arcaneItems.isCustomItem(off, "arcane.trinkets.amulet_of_the_phoenix")) {
            amulet = off;
        }

        if (amulet != null) {
            event.setCancelled(false); // Force resurrect
            ItemMeta meta = amulet.getItemMeta();
            if (meta != null) {
                NamespacedKey key = space.qclid.arcanum.compat.Compat.key("uses");
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

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (arcaneItems.isCustomItem(chestplate, "arcane.armor.gale_chestplate")) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);

            if (player.hasCooldown(Material.DIAMOND_CHESTPLATE)) return;

            org.bukkit.util.Vector velocity = player.getLocation().getDirection().multiply(1.2).setY(0.8);
            player.setVelocity(velocity);

            player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1f, 1.2f);

            player.setCooldown(Material.DIAMOND_CHESTPLATE, 200); // 10s cooldown
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            ItemStack boots = player.getInventory().getBoots();
            if (arcaneItems.isCustomItem(boots, "explorer.gadgets.slime_boots")) {
                event.setCancelled(true);
                double fallDist = player.getFallDistance();
                if (fallDist > 0.0) {
                    double bounceVelocity = Math.min(2.5, Math.max(0.4, fallDist * 0.12));
                    org.bukkit.util.Vector currentVel = player.getVelocity();
                    player.setVelocity(new org.bukkit.util.Vector(currentVel.getX(), bounceVelocity, currentVel.getZ()));
                    player.getWorld().spawnParticle(org.bukkit.Particle.ITEM_SLIME, player.getLocation(), 15, 0.4, 0.1, 0.4, 0.05);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);
                }
            } else if (boots != null && boots.getType() != Material.AIR) {
                ItemMeta bMeta = boots.getItemMeta();
                if (bMeta != null) {
                    NamespacedKey slKey = space.qclid.arcanum.compat.Compat.key("rune_seismic_landing");
                    Integer slLvl = bMeta.getPersistentDataContainer().get(slKey, PersistentDataType.INTEGER);
                    if (slLvl != null) {
                        double fallDist = player.getFallDistance();
                        if (fallDist > 3.0) {
                            double currentDamage = event.getDamage();
                            double negated = Math.min(10.0, currentDamage);
                            event.setDamage(currentDamage - negated);

                            double radius = 3.0 + slLvl * 1.0;
                            double damage = 4.0 + slLvl * 2.0;
                            Location landLoc = player.getLocation();
                            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, 2.0, radius)) {
                                if (entity instanceof LivingEntity le && !le.equals(player)) {
                                    le.damage(damage, player);
                                    org.bukkit.util.Vector push = le.getLocation().subtract(landLoc).toVector().normalize().multiply(0.8).setY(0.4 + slLvl * 0.1);
                                    le.setVelocity(push);
                                }
                            }

                            landLoc.getWorld().playSound(landLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.2f, 0.5f);
                            landLoc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, landLoc, 20, radius / 2.0, 0.2, radius / 2.0, 0.1);
                            landLoc.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, landLoc, 40, radius / 2.0, 0.2, radius / 2.0, 0.05, landLoc.getBlock().getRelative(BlockFace.DOWN).getBlockData());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            // 1. Resonance Ping
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && hand.getType() != Material.AIR) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_resonance_ping");
                    if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                        if (player.hasCooldown(hand.getType())) return;
                        player.setCooldown(hand.getType(), 100); // 5s cooldown
                        
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.2f);
                        Location center = player.getLocation();
                        List<org.bukkit.block.Block> ores = new ArrayList<>();
                        int radius = 10;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    org.bukkit.block.Block b = center.clone().add(x, y, z).getBlock();
                                    Material type = b.getType();
                                    if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE ||
                                        type == Material.GOLD_ORE || type == Material.DEEPSLATE_GOLD_ORE ||
                                        type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE ||
                                        type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE ||
                                        type == Material.ANCIENT_DEBRIS) {
                                        ores.add(b);
                                    }
                                }
                            }
                        }
                        
                        int[] count = new int[]{0};
                        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                            if (count[0] >= 4 || !player.isOnline()) {
                                t.cancel();
                                return;
                            }
                            for (org.bukkit.block.Block b : ores) {
                                Location blockLoc = b.getLocation().add(0.5, 0.5, 0.5);
                                b.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, blockLoc, 2, 0.2, 0.2, 0.2, 0.0);
                            }
                            count[0]++;
                        }, 1L, 10L);
                    }
                }
            }

            // 2. Rift Walk
            ItemStack legs = player.getInventory().getLeggings();
            if (legs != null && legs.getType() != Material.AIR) {
                ItemMeta meta = legs.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_rift_walk");
                    if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                        long lastSneak = getLongMetadata(player, plugin, "last_sneak_time", 0L);
                        long now = System.currentTimeMillis();
                        setMetadata(player, plugin, "last_sneak_time", now);
                        
                        if (now - lastSneak > 400 && now - lastSneak < 5000) {
                            player.sendMessage(MM.deserialize(C_GRAY + toSmallCaps("Sneak again quickly to activate Rift Walk.")));
                        }
                        
                        if (now - lastSneak < 400) {
                            if (player.hasCooldown(Material.DIAMOND_LEGGINGS)) {
                                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Rift Walk is on cooldown!")));
                                return;
                            }
                            player.setCooldown(Material.DIAMOND_LEGGINGS, 300); // 15s cooldown
                            
                            setMetadata(player, plugin, "rift_walk_active", true);
                            player.setCollidable(false);
                            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 100, 0));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
                            player.sendMessage(MM.deserialize(C_PURPLE + toSmallCaps("Shifted into the Rift! Immune to damage.")));
                            
                            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                                if (player.isOnline()) {
                                    removeMetadata(player, plugin, "rift_walk_active");
                                    player.setCollidable(true);
                                    player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                                    player.sendMessage(MM.deserialize(C_PURPLE + toSmallCaps("Rift Walk ended.")));
                                }
                            }, 100L);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerAnimation(org.bukkit.event.player.PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (event.getAnimationType() == org.bukkit.event.player.PlayerAnimationType.ARM_SWING) {
            removeMetadata(player, plugin, "mach_rush_ticks");
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            refreshItemLore(item, plugin);
        }
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            refreshItemLore(armor, plugin);
        }
        refreshItemLore(player.getInventory().getItemInOffHand(), plugin);
    }
}
