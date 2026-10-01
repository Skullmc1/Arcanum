package space.qclid.arcanum.codex.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.arcanum.data.DataManager;

import space.qclid.arcanum.codex.core.*;
import space.qclid.arcanum.codex.items.*;
import space.qclid.arcanum.codex.crafting.*;
import space.qclid.arcanum.codex.tasks.*;
import space.qclid.arcanum.codex.gui.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;
import static space.qclid.arcanum.util.CodexUtil.*;

public class CodexCombatListener implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;
    private final java.util.Set<java.util.UUID> tidalSweepInProgress = new java.util.HashSet<>();

    public CodexCombatListener(CodexContext ctx) {
        this.plugin = ctx.plugin();
        this.dataManager = ctx.dataManager();
        this.manager = ctx.manager();
        this.registry = ctx.registry();
        this.arcaneItems = ctx.arcaneItems();
        this.explorerItems = ctx.explorerItems();
        this.codexCrafting = ctx.codexCrafting();
        this.codexPassiveTask = ctx.codexPassiveTask();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // 4. Wand of Levitation Projectile
        if (hasMetadata(snowball, plugin, "levitation_projectile")) {
            if (event.getHitEntity() instanceof LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0)); // Levitation I for 5 seconds
                target.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHULKER_BULLET_HIT, 1f, 1f);
            }
            snowball.remove();
            return;
        }

        // 1. Staff of Supplant
        if (hasMetadata(snowball, plugin, "supplant_projectile")) {
            if (!(snowball.getShooter() instanceof Player player) || !player.isOnline()) {
                snowball.remove();
                return;
            }
            if (event.getHitEntity() instanceof LivingEntity target) {
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
        if (hasMetadata(snowball, plugin, "web_projectile")) {
            Location loc = null;
            if (event.getHitBlock() != null) {
                loc = event.getHitBlock().getRelative(event.getHitBlockFace()).getLocation();
            } else if (event.getHitEntity() != null) {
                loc = event.getHitEntity().getLocation();
            }
            if (loc != null) {
                org.bukkit.block.Block webBlock = loc.getBlock();
                if (webBlock.getType() == Material.AIR || webBlock.getType() == Material.CAVE_AIR) {
                    placeTemporaryWeb(webBlock, 100L); // 5 seconds
                }
            }
            snowball.remove();
            return;
        }

        // 3. Grappling Hooks & Web Slingers
        if (hasMetadata(snowball, plugin, "grapple_range")) {
            if (!(snowball.getShooter() instanceof Player player) || !player.isOnline()) return;

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

                    if (hasMetadata(snowball, plugin, "web_slinger_projectile")) {
                        trackLanding(player);
                    }
                }
            }
            snowball.remove();
        }
    }

    private void placeTemporaryWeb(org.bukkit.block.Block block, long ticks) {
        block.setType(Material.COBWEB);
        setMetadata(block, plugin, "temp_web", true);

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
            if (block.getType() == Material.COBWEB && hasMetadata(block, plugin, "temp_web")) {
                block.setType(Material.AIR);
                removeMetadata(block, plugin, "temp_web");
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
                org.bukkit.block.Block block = p.getLocation().getBlock();
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
        
        NamespacedKey catchFlameKey = space.qclid.arcanum.compat.Compat.key("rune_catch_flame");
        Integer level = meta.getPersistentDataContainer().get(catchFlameKey, PersistentDataType.INTEGER);
        if (level != null && event.getProjectile() instanceof org.bukkit.entity.Arrow arrow) {
            setMetadata(arrow, plugin, "catch_flame_level", level);
        }

        // Zephyr
        NamespacedKey zKey = space.qclid.arcanum.compat.Compat.key("rune_zephyr");
        if (meta.getPersistentDataContainer().has(zKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            arrow.setGravity(false);
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (!arrow.isValid() || arrow.isDead() || arrow.isOnGround()) {
                    t.cancel();
                    return;
                }
                org.bukkit.util.Vector vel = arrow.getVelocity();
                double horizSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
                if (horizSpeed < 0.01) {
                    arrow.setVelocity(vel.clone().setY(vel.getY() - 0.1));
                }
                arrow.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, arrow.getLocation(), 1, 0, 0, 0, 0);
            }, 1L, 1L);
        }

        // Heavy Draw
        NamespacedKey hdKey = space.qclid.arcanum.compat.Compat.key("rune_heavy_draw");
        Integer hdLvl = meta.getPersistentDataContainer().get(hdKey, PersistentDataType.INTEGER);
        if (hdLvl != null && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            setMetadata(player, plugin, "skip_potion_resistance", true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
            double multiplier = 1.0 + (hdLvl * 0.10);
            arrow.setDamage(arrow.getDamage() * multiplier);
        }

        // Miasma
        NamespacedKey mKey = space.qclid.arcanum.compat.Compat.key("rune_miasma");
        if (meta.getPersistentDataContainer().has(mKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            setMetadata(arrow, plugin, "miasma_arrow", true);
        }

        // Brimstone
        NamespacedKey brKey = space.qclid.arcanum.compat.Compat.key("rune_brimstone");
        Integer brLvl = meta.getPersistentDataContainer().get(brKey, PersistentDataType.INTEGER);
        if (brLvl != null && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            setMetadata(arrow, plugin, "brimstone_level", brLvl);
        }

        // Apollo's Ray
        NamespacedKey arKey = space.qclid.arcanum.compat.Compat.key("rune_apollos_ray");
        if (meta.getPersistentDataContainer().has(arKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            if (event.getForce() >= 1.0f) {
                event.setCancelled(true);
                arrow.remove();
                
                Location start = player.getEyeLocation();
                org.bukkit.util.Vector dir = start.getDirection();
                
                for (double d = 0; d < 50; d += 0.5) {
                    Location particleLoc = start.clone().add(dir.clone().multiply(d));
                    particleLoc.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    particleLoc.getWorld().spawnParticle(org.bukkit.Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
                }
                
                org.bukkit.util.RayTraceResult ray = player.getWorld().rayTraceEntities(start, dir, 50.0, 0.5, entity -> !entity.equals(player) && entity instanceof LivingEntity);
                if (ray != null && ray.getHitEntity() instanceof LivingEntity le) {
                    le.damage(12.0, player);
                    le.setFireTicks(200);
                    setMetadata(le, plugin, "holy_fire", true);
                    le.getWorld().playSound(le.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 1.5f);
                    le.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, le.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.1);
                }
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1.5f);
            }
        }

        // Trinity's Well
        NamespacedKey twKey = space.qclid.arcanum.compat.Compat.key("rune_trinitys_well");
        if (meta.getPersistentDataContainer().has(twKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            setMetadata(arrow, plugin, "trinitys_well_arrow", true);
        }

        // Styx's Toll
        NamespacedKey stKey = space.qclid.arcanum.compat.Compat.key("rune_styxs_toll");
        if (meta.getPersistentDataContainer().has(stKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            setMetadata(arrow, plugin, "styxs_toll_arrow", true);
        }

        // Shrapnel Shot
        NamespacedKey ssKey = space.qclid.arcanum.compat.Compat.key("rune_shrapnel_shot");
        Integer ssLvl = meta.getPersistentDataContainer().get(ssKey, PersistentDataType.INTEGER);
        if (ssLvl != null && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            event.setCancelled(true);
            arrow.remove();

            // Consume 4 arrows from inventory
            int arrowsRemaining = 4;
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.getType() == Material.ARROW) {
                    int toRemove = Math.min(arrowsRemaining, invItem.getAmount());
                    invItem.setAmount(invItem.getAmount() - toRemove);
                    arrowsRemaining -= toRemove;
                    if (arrowsRemaining <= 0) break;
                }
            }
            if (arrowsRemaining > 0) return; // not enough arrows

            int count = 1 + ssLvl * 3;
            Location eyeLoc = player.getEyeLocation();
            org.bukkit.util.Vector dir = eyeLoc.getDirection();
            
            for (int i = 0; i < count; i++) {
                org.bukkit.util.Vector spread = dir.clone().add(new org.bukkit.util.Vector(
                    (Math.random() - 0.5) * 0.25,
                    (Math.random() - 0.5) * 0.25,
                    (Math.random() - 0.5) * 0.25
                )).normalize().multiply(1.8);
                
                org.bukkit.entity.Arrow extraArrow = player.launchProjectile(org.bukkit.entity.Arrow.class, spread);
                extraArrow.setPierceLevel(2);
                extraArrow.setKnockbackStrength(2);
                setMetadata(extraArrow, plugin, "shrapnel_arrow", true);
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1.2f);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Capture phalanx targets
        if (event.getEntity() instanceof Player victim) {
            if (event.getDamager() instanceof LivingEntity attacker) {
                setMetadata(victim, plugin, "phalanx_target", attacker.getUniqueId().toString());
            } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof LivingEntity attacker) {
                setMetadata(victim, plugin, "phalanx_target", attacker.getUniqueId().toString());
            }
        }
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof LivingEntity victim) {
            setMetadata(damager, plugin, "phalanx_target", victim.getUniqueId().toString());
        }
        if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player shooter && event.getEntity() instanceof LivingEntity victim) {
            setMetadata(shooter, plugin, "phalanx_target", victim.getUniqueId().toString());
        }

        // Redirection check (if player is target)
        if (event.getEntity() instanceof Player player) {
            setMetadata(player, plugin, "last_combat_time", System.currentTimeMillis());
            if (event.getDamager() instanceof Player attacker) {
                setMetadata(attacker, plugin, "last_combat_time", System.currentTimeMillis());
            }

            // Warding Halo
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && chest.getType() != Material.AIR) {
                ItemMeta cMeta = chest.getItemMeta();
                if (cMeta != null) {
                    NamespacedKey whKey = space.qclid.arcanum.compat.Compat.key("rune_warding_halo");
                    if (cMeta.getPersistentDataContainer().has(whKey, PersistentDataType.INTEGER)) {
                        int charges = hasMetadata(player, plugin, "warding_halo_charges") 
                                      ? getIntMetadata(player, plugin, "warding_halo_charges", 0) : 3;
                        if (charges > 0) {
                            charges--;
                            setMetadata(player, plugin, "warding_halo_charges", charges);
                            event.setCancelled(true);
                            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f);
                            player.getWorld().spawnParticle(org.bukkit.Particle.GUST, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                            player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Warding Halo: Blocked! " + charges + " charges remaining.")));
                            return;
                        }
                    }
                }
            }

            // Radial Blind
            if (player.isHandRaised() && player.getActiveItem() != null && player.getActiveItem().getType() == Material.SHIELD) {
                ItemStack shield = player.getActiveItem();
                ItemMeta sMeta = shield.getItemMeta();
                if (sMeta != null) {
                    NamespacedKey rbKey = space.qclid.arcanum.compat.Compat.key("rune_radial_blind");
                    if (sMeta.getPersistentDataContainer().has(rbKey, PersistentDataType.INTEGER)) {
                        if (event.getDamage() >= 6.0) {
                            event.setCancelled(true);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1.2f);
                            player.getWorld().spawnParticle(org.bukkit.Particle.FLASH, player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2);
                            
                            for (Entity entity : player.getNearbyEntities(8.0, 4.0, 8.0)) {
                                if (entity instanceof LivingEntity le && !le.equals(player)) {
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
                                }
                            }
                            player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Radial Blind triggered!")));
                            return;
                        }
                    }
                }
            }

            // Infernalis attacker igniting
            if (chest != null && chest.getType() != Material.AIR) {
                ItemMeta cMeta = chest.getItemMeta();
                if (cMeta != null) {
                    NamespacedKey infKey = space.qclid.arcanum.compat.Compat.key("rune_infernalis");
                    Integer infLvl = cMeta.getPersistentDataContainer().get(infKey, PersistentDataType.INTEGER);
                    if (infLvl != null && event.getDamager() instanceof LivingEntity attacker) {
                        attacker.setFireTicks(40 * infLvl);
                    }
                }
            }

            int redirLvl = 0;
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    ItemMeta aMeta = armor.getItemMeta();
                    if (aMeta != null) {
                        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_redirection");
                        Integer lvl = aMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                        if (lvl != null) {
                            redirLvl = Math.max(redirLvl, lvl);
                        }
                    }
                }
            }

            if (redirLvl > 0) {
                double redirectPercent = redirLvl * 0.066;
                List<org.bukkit.entity.Tameable> pets = new ArrayList<>();
                for (Entity entity : player.getNearbyEntities(10.0, 5.0, 10.0)) {
                    if (entity instanceof org.bukkit.entity.Tameable pet) {
                        if (pet.isTamed() && player.getUniqueId().equals(pet.getOwnerUniqueId()) && !pet.isDead()) {
                            pets.add(pet);
                        }
                    }
                }

                if (!pets.isEmpty()) {
                    double originalDamage = event.getDamage();
                    double redirectedDamage = originalDamage * redirectPercent;
                    double dividedDamage = redirectedDamage / pets.size();

                    event.setDamage(originalDamage - redirectedDamage);
                    for (org.bukkit.entity.Tameable pet : pets) {
                        pet.damage(dividedDamage);
                        pet.getWorld().spawnParticle(org.bukkit.Particle.HEART, pet.getLocation().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1);
                    }
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 1.5f);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Redirected ") + C_YELLOW + String.format("%.1f", redirectedDamage) + C_RED + toSmallCaps(" damage to your pets.")));
                }
            }
        }

        // Kinetic Rebound check
        if (event.getEntity() instanceof Player defender && event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
            if (defender.isHandRaised() && defender.getActiveItem() != null && defender.getActiveItem().getType() == Material.SHIELD) {
                ItemStack shield = defender.getActiveItem();
                ItemMeta sMeta = shield.getItemMeta();
                if (sMeta != null) {
                    NamespacedKey krKey = space.qclid.arcanum.compat.Compat.key("rune_kinetic_rebound");
                    if (sMeta.getPersistentDataContainer().has(krKey, PersistentDataType.INTEGER)) {
                        if (Math.random() <= 0.75) {
                            event.setCancelled(true);
                            org.bukkit.projectiles.ProjectileSource source = proj.getShooter();
                            if (source instanceof LivingEntity shooter) {
                                org.bukkit.util.Vector returnVec = shooter.getEyeLocation().toVector().subtract(proj.getLocation().toVector()).normalize().multiply(1.5);
                                org.bukkit.entity.Entity spawned = proj.getWorld().spawnEntity(proj.getLocation(), proj.getType());
                                if (spawned instanceof org.bukkit.entity.Projectile reflected) {
                                    reflected.setShooter(defender);
                                    reflected.setVelocity(returnVec);
                                }

                                defender.playSound(defender.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.8f);
                                defender.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, proj.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
                            }
                            proj.remove();
                        }
                    }
                }
            }
        }

        // Glacial Thorns check
        if (event.getEntity() instanceof Player targetPlayer && event.getDamager() instanceof LivingEntity attacker) {
            int highestLvl = 0;
            for (ItemStack armor : targetPlayer.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    ItemMeta meta = armor.getItemMeta();
                    if (meta != null) {
                        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_glacial_thorns");
                        Integer lvl = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                        if (lvl != null) {
                            highestLvl = Math.max(highestLvl, lvl);
                        }
                    }
                }
            }

            if (highestLvl > 0) {
                double chance = highestLvl * 0.20;
                if (Math.random() <= chance) {
                    int amplifier = highestLvl >= 5 ? 1 : 0;
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, amplifier));
                    attacker.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, attacker.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.02);
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                }
            }
        }

        // Gas Cloud check (take or deal damage)
        int gcLvl = 0;
        Player gasPlayer = null;
        if (event.getDamager() instanceof Player attackerPlayer) {
            gasPlayer = attackerPlayer;
        } else if (event.getEntity() instanceof Player targetPlayer) {
            gasPlayer = targetPlayer;
        }

        if (gasPlayer != null) {
            for (ItemStack armor : gasPlayer.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    ItemMeta aMeta = armor.getItemMeta();
                    if (aMeta != null) {
                        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_gas_cloud");
                        Integer lvl = aMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                        if (lvl != null) {
                            gcLvl = Math.max(gcLvl, lvl);
                        }
                    }
                }
            }
        }

        if (gcLvl > 0 && Math.random() <= 0.15) {
            Location cloudLoc = event.getEntity().getLocation();
            final int finalGcLvl = gcLvl;
            final Player finalGasPlayer = gasPlayer;
            int[] tick = new int[]{0};
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (tick[0] >= 6) {
                    t.cancel();
                    return;
                }
                cloudLoc.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, cloudLoc, 20, 1.5, 0.5, 1.5, 0.02);
                for (Entity entity : cloudLoc.getWorld().getNearbyEntities(cloudLoc, 2.0, 1.5, 2.0)) {
                    if (entity instanceof LivingEntity le && !le.equals(finalGasPlayer)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, finalGcLvl - 1));
                    }
                }
                tick[0]++;
            }, 1L, 10L);
        }

        if (event.getEntity() instanceof LivingEntity targetEntity) {
            if (event.getDamager() instanceof org.bukkit.entity.Arrow arrow) {
                Player shooterPlayer = arrow.getShooter() instanceof Player ? (Player) arrow.getShooter() : null;
                
                if (hasMetadata(arrow, plugin, "catch_flame_level")) {
                    int level = getIntMetadata(arrow, plugin, "catch_flame_level", 0);
                    setMetadata(targetEntity, plugin, "catch_flame_level", level);
                }

                // Miasma arrow hit
                if (hasMetadata(arrow, plugin, "miasma_arrow")) {
                    Location hitLoc = targetEntity.getLocation();
                    hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_EGG_THROW, 1f, 0.5f);
                    int[] tick = new int[]{0};
                    plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                        if (tick[0] >= 5) {
                            t.cancel();
                            return;
                        }
                        hitLoc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, hitLoc, 15, 2.0, 0.5, 2.0, 0.02);
                        hitLoc.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, hitLoc, 10, 2.0, 0.5, 2.0, 0.01);
                        for (Entity entity : hitLoc.getWorld().getNearbyEntities(hitLoc, 2.0, 1.5, 2.0)) {
                            if (entity instanceof LivingEntity le && (shooterPlayer == null || !le.equals(shooterPlayer))) {
                                le.damage(3.0, shooterPlayer);
                                for (ItemStack armor : le.getEquipment().getArmorContents()) {
                                    if (armor != null && armor.getType() != Material.AIR) {
                                        if (armor.getItemMeta() instanceof Damageable dmgMeta) {
                                            dmgMeta.setDamage(dmgMeta.getDamage() + 15);
                                            armor.setItemMeta((ItemMeta) dmgMeta);
                                        }
                                    }
                                }
                            }
                        }
                        tick[0]++;
                    }, 1L, 10L);
                }

                // Brimstone arrow hit
                if (hasMetadata(arrow, plugin, "brimstone_level")) {
                    int lvl = getIntMetadata(arrow, plugin, "brimstone_level", 0);
                    double radius = lvl == 1 ? 3.0 : 5.0;
                    Location hitLoc = targetEntity.getLocation();
                    hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);
                    hitLoc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, hitLoc, 15, radius / 2.0, 0.5, radius / 2.0, 0.05);
                    hitLoc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, hitLoc, 20, radius / 2.0, 0.5, radius / 2.0, 0.1);
                    
                    for (Entity entity : hitLoc.getWorld().getNearbyEntities(hitLoc, radius, 2.0, radius)) {
                        if (entity instanceof LivingEntity le) {
                            if (shooterPlayer == null || !le.equals(shooterPlayer)) {
                                le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
                                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
                                le.setFireTicks(100);
                            }
                        }
                    }
                }

                // Trinity's Well arrow hit
                if (hasMetadata(arrow, plugin, "trinitys_well_arrow") && shooterPlayer != null) {
                    if (targetEntity instanceof Player hitPlayer) {
                        event.setCancelled(true);
                        double maxHealth = hitPlayer.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH).getValue();
                        hitPlayer.setHealth(Math.min(maxHealth, hitPlayer.getHealth() + 4.0));
                        hitPlayer.getWorld().spawnParticle(org.bukkit.Particle.HEART, hitPlayer.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
                        hitPlayer.getWorld().playSound(hitPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
                    } else {
                        setMetadata(targetEntity, plugin, "trinity_marked", true);
                        targetEntity.getWorld().spawnParticle(org.bukkit.Particle.GLOW, targetEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (targetEntity.isValid()) {
                                removeMetadata(targetEntity, plugin, "trinity_marked");
                            }
                        }, 100L);
                    }
                }

                // Styx's Toll arrow hit
                if (hasMetadata(arrow, plugin, "styxs_toll_arrow")) {
                    targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 9));
                    setMetadata(targetEntity, plugin, "rooted", true);
                    targetEntity.getWorld().playSound(targetEntity.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.6f, 1.8f);
                    targetEntity.getWorld().spawnParticle(org.bukkit.Particle.CRIT, targetEntity.getLocation(), 20, 0.5, 0.1, 0.5, 0.02);
                    
                    plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                        if (targetEntity.isValid()) {
                            removeMetadata(targetEntity, plugin, "rooted");
                        }
                    }, 60L);
                }

            }
        }

        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Static Charge attack boost check
        if (hasMetadata(player, plugin, "static_charge_amount")) {
            double charge = getDoubleMetadata(player, plugin, "static_charge_amount", 0.0);
            if (charge >= 100.0) {
                removeMetadata(player, plugin, "static_charge_amount");
                event.setDamage(event.getDamage() * 1.5);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.8f);
                target.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            }
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                NamespacedKey itemKey = space.qclid.arcanum.compat.Compat.key("item_id");
                String itemId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

                // Infernalis extra fire damage
                ItemStack chestPlate = player.getInventory().getChestplate();
                if (chestPlate != null && chestPlate.getType() != Material.AIR) {
                    ItemMeta cMeta = chestPlate.getItemMeta();
                    if (cMeta != null) {
                        NamespacedKey infKey = space.qclid.arcanum.compat.Compat.key("rune_infernalis");
                        if (cMeta.getPersistentDataContainer().has(infKey, PersistentDataType.INTEGER)) {
                            event.setDamage(event.getDamage() * 1.20);
                            target.setFireTicks(Math.max(target.getFireTicks(), 40));
                        }
                    }
                }

                // Gloom durability repair
                if (chestPlate != null && chestPlate.getType() != Material.AIR) {
                    ItemMeta cMeta = chestPlate.getItemMeta();
                    if (cMeta != null) {
                        NamespacedKey glKey = space.qclid.arcanum.compat.Compat.key("rune_gloom");
                        if (cMeta.getPersistentDataContainer().has(glKey, PersistentDataType.INTEGER)) {
                            if (cMeta instanceof Damageable dmgMeta) {
                                dmgMeta.setDamage(Math.max(0, dmgMeta.getDamage() - 2));
                                chestPlate.setItemMeta((ItemMeta) dmgMeta);
                            }
                        }
                    }
                }

                // Trinity's Well mark leech
                if (hasMetadata(target, plugin, "trinity_marked")) {
                    double leechDmg = event.getFinalDamage();
                    double heal = leechDmg * 0.20;
                    double maxHp = player.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHp, player.getHealth() + heal));
                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1.2, 0), 2, 0.1, 0.1, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.8f);
                }

                // Breach Surge
                NamespacedKey bsKey = space.qclid.arcanum.compat.Compat.key("rune_breach_surge");
                if (meta.getPersistentDataContainer().has(bsKey, PersistentDataType.INTEGER)) {
                    if (Math.random() <= 0.20) {
                        LivingEntity secondary = null;
                        for (Entity entity : target.getNearbyEntities(8.0, 4.0, 8.0)) {
                            if (entity instanceof LivingEntity le && !le.equals(player) && !le.equals(target) && !le.isDead()) {
                                secondary = le;
                                break;
                            }
                        }
                        if (secondary != null) {
                            final LivingEntity secTarget = secondary;
                            secTarget.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 0.8f, 1.5f);
                            
                            final Location start = target.getEyeLocation();
                            final UUID secUuid = secTarget.getUniqueId();
                            int[] ticksRun = new int[]{0};
                            
                            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
                                ticksRun[0]++;
                                if (ticksRun[0] > 20) {
                                    task.cancel();
                                    return;
                                }
                                Entity currentSec = Bukkit.getEntity(secUuid);
                                if (!(currentSec instanceof LivingEntity livingSec) || livingSec.isDead()) {
                                    task.cancel();
                                    return;
                                }
                                Location targetLoc = livingSec.getEyeLocation();
                                double dist = start.distance(targetLoc);
                                if (dist <= 0.8) {
                                    task.cancel();
                                    livingSec.damage(5.0, player);
                                    currentSec.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, targetLoc, 10, 0.2, 0.2, 0.2, 0.05);
                                    currentSec.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.2f);
                                    return;
                                }
                                org.bukkit.util.Vector dir = targetLoc.toVector().subtract(start.toVector()).normalize().multiply(0.8);
                                start.add(dir);
                                start.getWorld().spawnParticle(org.bukkit.Particle.CHERRY_LEAVES, start, 3, 0.05, 0.05, 0.05, 0.01);
                                start.getWorld().spawnParticle(org.bukkit.Particle.WITCH, start, 2, 0.02, 0.02, 0.02, 0.0);
                            }, 1L, 1L);
                        }
                    }
                }

                // Fenrir's Bite
                NamespacedKey fbKey = space.qclid.arcanum.compat.Compat.key("rune_fenrirs_bite");
                if (meta.getPersistentDataContainer().has(fbKey, PersistentDataType.INTEGER)) {
                    AttributeInstance maxHealthAttr = target.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH);
                    if (maxHealthAttr != null && target.getHealth() >= maxHealthAttr.getValue()) {
                        event.setDamage(event.getDamage() * 2.0);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 0.8f);
                        target.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.1);
                    }
                }

                // Anubis' Judgment
                NamespacedKey ajKey = space.qclid.arcanum.compat.Compat.key("rune_anubis_judgment");
                if (meta.getPersistentDataContainer().has(ajKey, PersistentDataType.INTEGER)) {
                    AttributeInstance maxHealthAttr = target.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH);
                    if (maxHealthAttr != null && (target.getHealth() / maxHealthAttr.getValue()) <= 0.20) {
                        setMetadata(target, plugin, "anubis_execute", true);
                        event.setDamage(99999.0);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.8f);
                        target.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
                    }
                }

                // Cerberus' Maw
                NamespacedKey cmKey = space.qclid.arcanum.compat.Compat.key("rune_cerberus_maw");
                if (meta.getPersistentDataContainer().has(cmKey, PersistentDataType.INTEGER)) {
                    setMetadata(target, plugin, "cerberus_bleeding", true);

                    final int runId = hasMetadata(target, plugin, "cerberus_bleed_run")
                                      ? getIntMetadata(target, plugin, "cerberus_bleed_run", 0) + 1 : 1;
                    setMetadata(target, plugin, "cerberus_bleed_run", runId);
                    
                    int[] tick = new int[]{0};
                    plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                        if (!target.isValid() || target.isDead() || tick[0] >= 5) {
                            t.cancel();
                            if (target.isValid()) {
                                removeMetadata(target, plugin, "cerberus_bleeding");
                                removeMetadata(target, plugin, "cerberus_bleed_run");
                            }
                            return;
                        }
                        if (hasMetadata(target, plugin, "cerberus_bleed_run") && getIntMetadata(target, plugin, "cerberus_bleed_run", 0) != runId) {
                            t.cancel();
                            return;
                        }
                        
                        double damage = 2.0;
                        target.damage(damage);
                        target.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 0.5, 0), 4, 0.1, 0.1, 0.1);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 0.7f);
                        
                        tick[0]++;
                    }, 20L, 20L);
                }

                // Vortex
                NamespacedKey vKey = space.qclid.arcanum.compat.Compat.key("rune_vortex");
                Integer vLvl = meta.getPersistentDataContainer().get(vKey, PersistentDataType.INTEGER);
                if (vLvl != null) {
                    double pullChance = vLvl * 0.15;
                    if (Math.random() <= pullChance) {
                        Location center = player.getEyeLocation().add(player.getLocation().getDirection().multiply(4.0));
                        center.getWorld().playSound(center, Sound.ENTITY_WIND_CHARGE_THROW, 1f, 0.8f);
                        center.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, center, 30, 1.5, 1.5, 1.5, 0.1);
                        
                        for (Entity entity : player.getNearbyEntities(10.0, 5.0, 10.0)) {
                            if (entity instanceof LivingEntity le && !le.equals(player) && !le.equals(target)) {
                                org.bukkit.util.Vector pullVec = center.toVector().subtract(le.getLocation().toVector());
                                double dist = pullVec.length();
                                if (dist > 1.0) {
                                    org.bukkit.util.Vector velocity = pullVec.normalize().multiply(1.2).setY(0.3);
                                    le.setVelocity(velocity);
                                    
                                    double sweepDmg = event.getDamage() * 0.5;
                                    le.damage(sweepDmg, player);
                                    le.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, le.getLocation().add(0, 0.8, 0), 1);
                                }
                            }
                        }
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.7f);
                    }
                }

                // Smite of Jupiter
                NamespacedKey sjKey = space.qclid.arcanum.compat.Compat.key("rune_smite_of_jupiter");
                if (meta.getPersistentDataContainer().has(sjKey, PersistentDataType.INTEGER)) {
                    if (player.getAttackCooldown() >= 0.9f) {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        event.setDamage(event.getDamage() + 6.0);
                        
                        if (target.getCategory() == org.bukkit.entity.EntityCategory.UNDEAD) {
                            setMetadata(target, plugin, "jupiter_smite_vaporize", true);
                            event.setDamage(event.getDamage() * 2.0);
                        }
                    }
                }

                // 1. Lifesteal Rune
                NamespacedKey lifestealKey = space.qclid.arcanum.compat.Compat.key("rune_lifesteal");
                Integer lvl = meta.getPersistentDataContainer().get(lifestealKey, PersistentDataType.INTEGER);
                if (lvl != null) {
                    double damage = event.getFinalDamage();
                    double healAmount = damage * 0.15 * lvl;
                    double maxHealth = player.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1.2, 0), 4, 0.2, 0.2, 0.2);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f);
                }

                // Catch Flame Rune Check
                NamespacedKey catchFlameKey = space.qclid.arcanum.compat.Compat.key("rune_catch_flame");
                Integer catchFlameLvl = meta.getPersistentDataContainer().get(catchFlameKey, PersistentDataType.INTEGER);
                if (catchFlameLvl != null) {
                    setMetadata(target, plugin, "catch_flame_level", catchFlameLvl);
                }

                // 2. Corrosive Scythe
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

                // Corrosive Slash (Axe - Max 4)
                if (weapon.getType().name().contains("AXE")) {
                    NamespacedKey csKey = space.qclid.arcanum.compat.Compat.key("rune_corrosive_slash");
                    Integer csLvl = meta.getPersistentDataContainer().get(csKey, PersistentDataType.INTEGER);
                    if (csLvl != null) {
                        int currentStacks = hasMetadata(target, plugin, "corrosive_slash_stacks") 
                                            ? getIntMetadata(target, plugin, "corrosive_slash_stacks", 0) : 0;
                        int newStacks = Math.min(5, currentStacks + 1);
                        setMetadata(target, plugin, "corrosive_slash_stacks", newStacks);
                        setMetadata(target, plugin, "corrosive_slash_level", csLvl);

                        int duration = csLvl * 50; // 2.5s per level in ticks
                        final int runId = hasMetadata(target, plugin, "corrosive_slash_run") 
                                          ? getIntMetadata(target, plugin, "corrosive_slash_run", 0) + 1 : 1;
                        setMetadata(target, plugin, "corrosive_slash_run", runId);

                        target.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, target.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0.0);

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (target.isValid() && hasMetadata(target, plugin, "corrosive_slash_run") && getIntMetadata(target, plugin, "corrosive_slash_run", 0) == runId) {
                                removeMetadata(target, plugin, "corrosive_slash_stacks");
                                removeMetadata(target, plugin, "corrosive_slash_level");
                                removeMetadata(target, plugin, "corrosive_slash_run");
                            }
                        }, duration);
                    }
                }

                // Scorch (Sword - Max 1)
                if (weapon.getType().name().contains("SWORD")) {
                    NamespacedKey scrKey = space.qclid.arcanum.compat.Compat.key("rune_scorch");
                    if (meta.getPersistentDataContainer().has(scrKey, PersistentDataType.INTEGER)) {
                        int currentStacks = hasMetadata(target, plugin, "scorch_stacks") 
                                            ? getIntMetadata(target, plugin, "scorch_stacks", 0) : 0;
                        int newStacks = Math.min(10, currentStacks + 1);
                        setMetadata(target, plugin, "scorch_stacks", newStacks);

                        AttributeInstance armorAttr = target.getAttribute(space.qclid.arcanum.compat.Compat.ARMOR);
                        if (armorAttr != null) {
                            double baseArmor = armorAttr.getBaseValue();
                            double reduction = baseArmor * 0.09 * newStacks;
                            applyArmorReduction(target, "scorch", reduction);
                        }

                        target.setFireTicks(Math.max(target.getFireTicks(), 20));
                        target.damage(1.0); // true fire damage tick

                        int duration = 160; // 8s
                        final int runId = hasMetadata(target, plugin, "scorch_run") 
                                          ? getIntMetadata(target, plugin, "scorch_run", 0) + 1 : 1;
                        setMetadata(target, plugin, "scorch_run", runId);

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (target.isValid() && hasMetadata(target, plugin, "scorch_run") && getIntMetadata(target, plugin, "scorch_run", 0) == runId) {
                                removeMetadata(target, plugin, "scorch_stacks");
                                removeMetadata(target, plugin, "scorch_run");
                                removeArmorReduction(target, "scorch");
                            }
                        }, duration);
                    }
                }

                // Tidal Sweep (Swords - Max 3)
                if (weapon.getType().name().contains("SWORD")) {
                    NamespacedKey tsKey = space.qclid.arcanum.compat.Compat.key("rune_tidal_sweep");
                    Integer tsLvl = meta.getPersistentDataContainer().get(tsKey, PersistentDataType.INTEGER);
                    if (tsLvl != null) {
                        if (!tidalSweepInProgress.add(player.getUniqueId())) return;
                        try {
                            double range = 3.0 + tsLvl * 0.8;
                            double angle = 60.0 - tsLvl * 5.0;
                            Location eye = player.getEyeLocation();
                            org.bukkit.util.Vector dir = eye.getDirection().setY(0);
                            if (dir.lengthSquared() == 0) return;
                            dir.normalize();
                            for (Entity entity : target.getNearbyEntities(range, 2.0, range)) {
                                if (entity instanceof LivingEntity le && !le.equals(player) && !le.equals(target)) {
                                    org.bukkit.util.Vector toLe = le.getLocation().subtract(player.getLocation()).toVector().setY(0);
                                    if (toLe.lengthSquared() == 0) continue;
                                    toLe.normalize();
                                    double dot = dir.dot(toLe);
                                    double minDot = Math.cos(Math.toRadians(angle));
                                    if (dot >= minDot) {
                                        double dmg = event.getDamage() * (0.3 + tsLvl * 0.15);
                                        le.damage(dmg, player);
                                        le.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, le.getLocation().add(0, 0.8, 0), 1);
                                    }
                                }
                            }
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);
                        } finally {
                            tidalSweepInProgress.remove(player.getUniqueId());
                        }
                    }
                }

                // Phantom Backstab (Swords - Max 4)
                NamespacedKey pbKey = space.qclid.arcanum.compat.Compat.key("rune_phantom_backstab");
                Integer pbLvl = meta.getPersistentDataContainer().get(pbKey, PersistentDataType.INTEGER);
                if (pbLvl != null) {
                    if (Math.random() <= 0.50) {
                        player.sendMessage(MM.deserialize(C_PURPLE + toSmallCaps("Phantom Backstab prepared!")));
                        ItemStack weaponCopy = weapon.clone();
                        Location behind = target.getLocation().add(target.getLocation().getDirection().multiply(-2.0)).add(0, 0.5, 0);
                        ArmorStand stand = target.getWorld().spawn(behind, ArmorStand.class, s -> {
                            s.setVisible(false);
                            s.setMarker(true);
                            s.setGravity(false);
                            s.setCanPickupItems(false);
                            s.setItemInHand(weaponCopy);
                            s.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 40, 0, false, false));
                            setMetadata(s, plugin, "phantom_knife", true);
                        });
                        target.getWorld().playSound(behind, Sound.ENTITY_PHANTOM_AMBIENT, 0.5f, 1.2f);
                        target.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, behind, 15, 0.2, 0.5, 0.2, 0.05);

                        UUID targetUuid = target.getUniqueId();
                        double backstabDmg = event.getDamage() * (0.2 + pbLvl * 0.15);
                        int[] tick = new int[]{0};
                        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                            tick[0]++;
                            if (!stand.isValid() || tick[0] > 40) {
                                if (stand.isValid()) {
                                    stand.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, stand.getLocation(), 5, 0.1, 0.1, 0.1, 0.02);
                                    stand.remove();
                                }
                                t.cancel();
                                return;
                            }
                            Entity currentTarget = Bukkit.getEntity(targetUuid);
                            if (currentTarget == null || !currentTarget.isValid() || currentTarget.isDead()) {
                                stand.remove();
                                t.cancel();
                                return;
                            }
                            Location targetLoc = currentTarget.getLocation().add(0, 0.5, 0);
                            org.bukkit.util.Vector dir = targetLoc.toVector().subtract(stand.getLocation().toVector());
                            double dist = dir.length();
                            if (dist < 1.5 || tick[0] >= 20) {
                                if (currentTarget instanceof LivingEntity livingTarget) {
                                    livingTarget.damage(backstabDmg, player);
                                    livingTarget.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, targetLoc, 5, 0.2, 0.3, 0.2, 0.05);
                                    livingTarget.getWorld().playSound(targetLoc, Sound.ENTITY_PHANTOM_BITE, 1f, 1.5f);
                                }
                                stand.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, stand.getLocation(), 10, 0.2, 0.2, 0.2, 0.02);
                                stand.remove();
                                t.cancel();
                                return;
                            }
                            stand.teleport(stand.getLocation().add(dir.normalize().multiply(0.5)));
                            stand.setRotation(currentTarget.getLocation().getYaw(), currentTarget.getLocation().getPitch());
                            stand.getWorld().spawnParticle(org.bukkit.Particle.CRIT, stand.getLocation(), 2, 0.1, 0.1, 0.1, 0.01);
                        }, 1L, 1L);
                    }
                }

                // Bleed (Swords/Axes - Max 8)
                NamespacedKey bleedKey = space.qclid.arcanum.compat.Compat.key("rune_bleed");
                Integer bleedLvl = meta.getPersistentDataContainer().get(bleedKey, PersistentDataType.INTEGER);
                if (bleedLvl != null) {
                    int bleedStacks = hasMetadata(target, plugin, "bleed_stacks") 
                                      ? getIntMetadata(target, plugin, "bleed_stacks", 0) : 0;
                    bleedStacks = Math.min(8, bleedStacks + 1);
                    setMetadata(target, plugin, "bleed_stacks", bleedStacks);

                    final int runId = hasMetadata(target, plugin, "bleed_run") 
                                      ? getIntMetadata(target, plugin, "bleed_run", 0) + 1 : 1;
                    setMetadata(target, plugin, "bleed_run", runId);

                    final int finalLvl = bleedLvl;
                    final int finalStacks = bleedStacks;
                    int[] tick = new int[]{0};
                    plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                        if (!target.isValid() || target.isDead() || tick[0] >= 5) {
                            t.cancel();
                            if (target.isValid()) {
                                removeMetadata(target, plugin, "bleed_stacks");
                                removeMetadata(target, plugin, "bleed_run");
                            }
                            return;
                        }
                        if (hasMetadata(target, plugin, "bleed_run") && getIntMetadata(target, plugin, "bleed_run", 0) != runId) {
                            t.cancel();
                            return;
                        }

                        double bleedDamage = finalStacks * finalLvl * 0.4;
                        target.damage(bleedDamage);
                        target.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 0.8f);

                        tick[0]++;
                    }, 20L, 20L);
                }

                // Poison Spores (Swords/Axes - Max 5)
                NamespacedKey psKey = space.qclid.arcanum.compat.Compat.key("rune_poison_spores");
                Integer psLvl = meta.getPersistentDataContainer().get(psKey, PersistentDataType.INTEGER);
                if (psLvl != null) {
                    double chance = 0.2 + psLvl * 0.1;
                    if (Math.random() <= chance) {
                        int duration = 60 + psLvl * 20;
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 0));
                        target.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, target.getLocation().add(0, 0.5, 0), 10, 0.3, 0.5, 0.3, 0.02);
                    }
                }

                // Crude Sharpness (Tools/Sticks - Max 7)
                NamespacedKey csSharpKey = space.qclid.arcanum.compat.Compat.key("rune_crude_sharpness");
                Integer csSharpLvl = meta.getPersistentDataContainer().get(csSharpKey, PersistentDataType.INTEGER);
                if (csSharpLvl != null) {
                    double flatBonus = csSharpLvl * 1.0;
                    event.setDamage(event.getDamage() + flatBonus);
                    target.getWorld().spawnParticle(org.bukkit.Particle.CRIT, target.getLocation().add(0, 0.8, 0), 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }

        // Corrosive Slash — multiply damage based on stacks on target
        if (hasMetadata(target, plugin, "corrosive_slash_stacks")) {
            int stacks = getIntMetadata(target, plugin, "corrosive_slash_stacks", 0);
            int csLvl = getIntMetadata(target, plugin, "corrosive_slash_level", 0);
            double multiplier = 1.0 + stacks * csLvl * 0.1;
            event.setDamage(event.getDamage() * multiplier);
        }

        // Frostbite Ring check
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        boolean hasRing = arcaneItems.isCustomItem(main, "arcane.trinkets.frostbite_ring") ||
                          arcaneItems.isCustomItem(off, "arcane.trinkets.frostbite_ring");
        if (hasRing && target.getFireTicks() > 0) {
            target.setFireTicks(0);
            target.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, target.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1.2f);
        }

        // Stormcaller Medallion
        boolean hasMedallion = arcaneItems.isCustomItem(main, "arcane.trinkets.stormcaller_medallion") ||
                               arcaneItems.isCustomItem(off, "arcane.trinkets.stormcaller_medallion");
        if (hasMedallion && (player.getWorld().hasStorm() || player.getWorld().isThundering())) {
            if (Math.random() < 0.15) {
                player.getWorld().strikeLightning(target.getLocation());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Anubis' Judgment execute loot/XP doubling
        if (hasMetadata(entity, plugin, "anubis_execute")) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                extraDrops.add(drop.clone());
            }
            for (ItemStack extra : extraDrops) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), extra);
            }
            event.setDroppedExp(event.getDroppedExp() * 2);
        }

        // Cerberus' Maw blood orb drop
        if (hasMetadata(entity, plugin, "cerberus_bleeding")) {
            ItemStack bloodOrb = arcaneItems.getCustomItem("arcane.materials.blood_orb");
            if (bloodOrb != null) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), bloodOrb.clone());
            }
        }

        Player killer = entity.getKiller();
        if (killer != null) {
            ItemStack mainHand = killer.getInventory().getItemInMainHand();
            if (arcaneItems.isCustomItem(mainHand, "arcane.melee.siphon_blade")) {
                double maxHealth = killer.getAttribute(space.qclid.arcanum.compat.Compat.MAX_HEALTH).getValue();
                double currentHealth = killer.getHealth();
                double healAmount = maxHealth * 0.15;
                killer.setHealth(Math.min(maxHealth, currentHealth + healAmount));
                killer.getWorld().spawnParticle(org.bukkit.Particle.HEART, killer.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
                killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }

            // Soul Harvester mob drop check
            if (mainHand != null && mainHand.getType().name().contains("HOE")) {
                ItemMeta meta = mainHand.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = space.qclid.arcanum.compat.Compat.key("rune_soul_harvester");
                    if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                        if (Math.random() <= 0.05) {
                            entity.getWorld().dropItemNaturally(entity.getLocation(), arcaneItems.soulOrbItem.clone());
                            entity.getWorld().spawnParticle(org.bukkit.Particle.SOUL, entity.getLocation().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }
        }

        // 2. Lightning Essence mob drop check
        if (entity instanceof Monster) {
            if (entity.getLastDamageCause() != null && entity.getLastDamageCause().getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), arcaneItems.lightningEssenceItem.clone());
            }

            // Soul Orb standard mob drop check (1% chance from hostiles)
            if (Math.random() <= 0.01) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), arcaneItems.soulOrbItem.clone());
            }
        }
    }



    private void applyArmorReduction(LivingEntity target, String key, double value) {
        AttributeInstance attr = target.getAttribute(space.qclid.arcanum.compat.Compat.ARMOR);
        if (attr != null) {
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (key.equals(modifier.getName())) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) attr.removeModifier(existing);

            AttributeModifier mod = new AttributeModifier(new NamespacedKey("codex", key), -value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY);
            attr.addModifier(mod);
        }
    }

    private void removeArmorReduction(LivingEntity target, String key) {
        AttributeInstance attr = target.getAttribute(space.qclid.arcanum.compat.Compat.ARMOR);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (key.equals(modifier.getName())) {
                    attr.removeModifier(modifier);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageGeneral(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Valkyrie's Grace check
        double finalDmg = event.getFinalDamage();
        if (player.getHealth() - finalDmg <= 0) {
            boolean hasGrace = false;
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    ItemMeta aMeta = armor.getItemMeta();
                    if (aMeta != null) {
                        NamespacedKey vgKey = space.qclid.arcanum.compat.Compat.key("rune_valkyries_grace");
                        if (aMeta.getPersistentDataContainer().has(vgKey, PersistentDataType.INTEGER)) {
                            hasGrace = true;
                            break;
                        }
                    }
                }
            }
            
            if (hasGrace) {
                long cooldown = hasMetadata(player, plugin, "valkyrie_grace_cooldown") 
                                 ? getLongMetadata(player, plugin, "valkyrie_grace_cooldown", 0L) : 0L;
                if (System.currentTimeMillis() >= cooldown) {
                    event.setCancelled(true);
                    player.setHealth(2.0); // 1 heart
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2)); // Regen III
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1)); // Resistance II
                    
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.FLASH, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5);
                    
                    for (Entity entity : player.getNearbyEntities(8.0, 4.0, 8.0)) {
                        if (entity instanceof Player ally && !ally.equals(player)) {
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
                            ally.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Valkyrie's Grace shields you!")));
                        } else if (entity instanceof LivingEntity le && !(entity instanceof Player)) {
                            org.bukkit.util.Vector push = le.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.5).setY(0.4);
                            le.setVelocity(push);
                        }
                    }
                    
                    setMetadata(player, plugin, "valkyrie_grace_cooldown", System.currentTimeMillis() + 300000);
                    player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Valkyrie's Grace saved you from death!")));
                    return;
                }
            }
        }

        // Infernalis immune to external fire/lava
        ItemStack chest = player.getInventory().getChestplate();
        if (chest != null && chest.getType() != Material.AIR) {
            ItemMeta cMeta = chest.getItemMeta();
            if (cMeta != null) {
                NamespacedKey infKey = space.qclid.arcanum.compat.Compat.key("rune_infernalis");
                if (cMeta.getPersistentDataContainer().has(infKey, PersistentDataType.INTEGER)) {
                    if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE ||
                        event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA ||
                        event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK ||
                        event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.HOT_FLOOR) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDummyDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!hasMetadata(event.getEntity(), plugin, "codex_dummy")) return;
        event.setCancelled(true);

        double dmg = event.getFinalDamage();
        var cause = event.getCause();

        TextColor textColor = switch (cause) {
            case POISON -> TextColor.color(0x55FF55);
            case WITHER -> TextColor.color(0x555555);
            case FIRE, FIRE_TICK, LAVA -> TextColor.color(0xFFAA00);
            case MAGIC -> TextColor.color(0xFF55FF);
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> TextColor.color(0xFFFFFF);
            case PROJECTILE -> TextColor.color(0xFFFF55);
            case CUSTOM -> TextColor.color(0xAA0000);
            default -> TextColor.color(0xAAAAAA);
        };

        Location loc = event.getEntity().getLocation().add(0, 2.5, 0);
        TextDisplay display = event.getEntity().getWorld().spawn(loc, TextDisplay.class, d -> {
            d.text(net.kyori.adventure.text.Component.text("-" + Math.round(dmg) + " ❤").color(textColor));
            d.setBillboard(Display.Billboard.CENTER);
            d.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            d.setSeeThrough(true);
        });

        int[] tick = new int[]{0};
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
            tick[0]++;
            if (tick[0] >= 30) {
                t.cancel();
                display.remove();
                return;
            }
            display.teleport(display.getLocation().add(0, 0.03, 0));
        }, 1L, 1L);
    }
}
