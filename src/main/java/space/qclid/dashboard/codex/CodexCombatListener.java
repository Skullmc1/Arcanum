package space.qclid.dashboard.codex;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.dashboard.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexCombatListener implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;

    public CodexCombatListener(JavaPlugin plugin, DataManager dataManager, CodexManager manager, CodexRegistry registry,
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
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // 4. Wand of Levitation Projectile
        if (snowball.hasMetadata("levitation_projectile")) {
            if (event.getHitEntity() instanceof LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0)); // Levitation I for 5 seconds
                target.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHULKER_BULLET_HIT, 1f, 1f);
            }
            snowball.remove();
            return;
        }

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
                org.bukkit.block.Block webBlock = loc.getBlock();
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

    private void placeTemporaryWeb(org.bukkit.block.Block block, long ticks) {
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
        
        NamespacedKey catchFlameKey = new NamespacedKey(plugin, "rune_catch_flame");
        Integer level = meta.getPersistentDataContainer().get(catchFlameKey, PersistentDataType.INTEGER);
        if (level != null && event.getProjectile() instanceof org.bukkit.entity.Arrow arrow) {
            arrow.setMetadata("catch_flame_level", new FixedMetadataValue(plugin, level));
        }

        // Zephyr
        NamespacedKey zKey = new NamespacedKey(plugin, "rune_zephyr");
        if (meta.getPersistentDataContainer().has(zKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            arrow.setGravity(false);
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (!arrow.isValid() || arrow.isDead() || arrow.isOnGround()) {
                    t.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, arrow.getLocation(), 1, 0, 0, 0, 0);
            }, 1L, 1L);
        }

        // Artemis's Blessing
        NamespacedKey abKey = new NamespacedKey(plugin, "rune_artemis_blessing");
        if (meta.getPersistentDataContainer().has(abKey, PersistentDataType.INTEGER) && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            arrow.setKnockbackStrength(arrow.getKnockbackStrength() + 5);
        }

        // Heavy Draw
        NamespacedKey hdKey = new NamespacedKey(plugin, "rune_heavy_draw");
        Integer hdLvl = meta.getPersistentDataContainer().get(hdKey, PersistentDataType.INTEGER);
        if (hdLvl != null && event.getProjectile() instanceof org.bukkit.entity.AbstractArrow arrow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
            double multiplier = 1.0 + (hdLvl * 0.10);
            arrow.setDamage(arrow.getDamage() * multiplier);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Redirection check (if player is target)
        if (event.getEntity() instanceof Player player) {
            int redirLvl = 0;
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    ItemMeta aMeta = armor.getItemMeta();
                    if (aMeta != null) {
                        NamespacedKey key = new NamespacedKey(plugin, "rune_redirection");
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
                    NamespacedKey krKey = new NamespacedKey(plugin, "rune_kinetic_rebound");
                    if (sMeta.getPersistentDataContainer().has(krKey, PersistentDataType.INTEGER)) {
                        if (Math.random() <= 0.75) {
                            event.setCancelled(true);
                            org.bukkit.projectiles.ProjectileSource source = proj.getShooter();
                            if (source instanceof LivingEntity shooter) {
                                org.bukkit.util.Vector returnVec = shooter.getEyeLocation().toVector().subtract(proj.getLocation().toVector()).normalize().multiply(1.5);
                                org.bukkit.entity.Projectile reflected = (org.bukkit.entity.Projectile) proj.getWorld().spawnEntity(proj.getLocation(), proj.getType());
                                reflected.setShooter(defender);
                                reflected.setVelocity(returnVec);

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
                        NamespacedKey key = new NamespacedKey(plugin, "rune_glacial_thorns");
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
                        NamespacedKey key = new NamespacedKey(plugin, "rune_gas_cloud");
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
                if (arrow.hasMetadata("catch_flame_level")) {
                    int level = arrow.getMetadata("catch_flame_level").get(0).asInt();
                    targetEntity.setMetadata("catch_flame_level", new FixedMetadataValue(plugin, level));
                }
            }
        }

        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Static Charge attack boost check
        if (player.hasMetadata("static_charge_amount")) {
            double charge = player.getMetadata("static_charge_amount").get(0).asDouble();
            if (charge >= 100.0) {
                player.removeMetadata("static_charge_amount", plugin);
                event.setDamage(event.getDamage() * 1.25);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.8f);
                target.getWorld().spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            }
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");
                String itemId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

                // 1. Lifesteal Rune
                NamespacedKey lifestealKey = new NamespacedKey(plugin, "rune_lifesteal");
                Integer lvl = meta.getPersistentDataContainer().get(lifestealKey, PersistentDataType.INTEGER);
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

                // Corrosive Slash (Axe - Max 4)
                if (weapon.getType().name().contains("AXE")) {
                    NamespacedKey csKey = new NamespacedKey(plugin, "rune_corrosive_slash");
                    Integer csLvl = meta.getPersistentDataContainer().get(csKey, PersistentDataType.INTEGER);
                    if (csLvl != null) {
                        int currentStacks = target.hasMetadata("corrosive_slash_stacks") 
                                            ? target.getMetadata("corrosive_slash_stacks").get(0).asInt() : 0;
                        int newStacks = Math.min(5, currentStacks + 1);
                        target.setMetadata("corrosive_slash_stacks", new FixedMetadataValue(plugin, newStacks));

                        double reduction = newStacks * csLvl * 1.5;
                        applyToughnessReduction(target, "corrosive_slash", reduction);

                        int duration = csLvl * 50; // 2.5s per level in ticks
                        final int runId = target.hasMetadata("corrosive_slash_run") 
                                          ? target.getMetadata("corrosive_slash_run").get(0).asInt() + 1 : 1;
                        target.setMetadata("corrosive_slash_run", new FixedMetadataValue(plugin, runId));

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (target.isValid() && target.hasMetadata("corrosive_slash_run") && target.getMetadata("corrosive_slash_run").get(0).asInt() == runId) {
                                target.removeMetadata("corrosive_slash_stacks", plugin);
                                target.removeMetadata("corrosive_slash_run", plugin);
                                removeToughnessReduction(target, "corrosive_slash");
                            }
                        }, duration);
                    }
                }

                // Scorch (Sword - Max 1)
                if (weapon.getType().name().contains("SWORD")) {
                    NamespacedKey scrKey = new NamespacedKey(plugin, "rune_scorch");
                    if (meta.getPersistentDataContainer().has(scrKey, PersistentDataType.INTEGER)) {
                        int currentStacks = target.hasMetadata("scorch_stacks") 
                                            ? target.getMetadata("scorch_stacks").get(0).asInt() : 0;
                        int newStacks = Math.min(10, currentStacks + 1);
                        target.setMetadata("scorch_stacks", new FixedMetadataValue(plugin, newStacks));

                        AttributeInstance armorAttr = target.getAttribute(org.bukkit.attribute.Attribute.ARMOR);
                        if (armorAttr != null) {
                            double baseArmor = armorAttr.getBaseValue();
                            double reduction = baseArmor * 0.09 * newStacks;
                            applyArmorReduction(target, "scorch", reduction);
                        }

                        target.setFireTicks(Math.max(target.getFireTicks(), 20));
                        target.damage(1.0); // true fire damage tick

                        int duration = 160; // 8s
                        final int runId = target.hasMetadata("scorch_run") 
                                          ? target.getMetadata("scorch_run").get(0).asInt() + 1 : 1;
                        target.setMetadata("scorch_run", new FixedMetadataValue(plugin, runId));

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (target.isValid() && target.hasMetadata("scorch_run") && target.getMetadata("scorch_run").get(0).asInt() == runId) {
                                target.removeMetadata("scorch_stacks", plugin);
                                target.removeMetadata("scorch_run", plugin);
                                removeArmorReduction(target, "scorch");
                            }
                        }, duration);
                    }
                }

                // Tidal Sweep (Swords - Max 3)
                if (weapon.getType().name().contains("SWORD")) {
                    NamespacedKey tsKey = new NamespacedKey(plugin, "rune_tidal_sweep");
                    Integer tsLvl = meta.getPersistentDataContainer().get(tsKey, PersistentDataType.INTEGER);
                    if (tsLvl != null) {
                        double range = 3.0 + tsLvl * 0.8;
                        double angle = 60.0 - tsLvl * 5.0;
                        Location eye = player.getEyeLocation();
                        org.bukkit.util.Vector dir = eye.getDirection().setY(0).normalize();
                        for (Entity entity : target.getNearbyEntities(range, 2.0, range)) {
                            if (entity instanceof LivingEntity le && !le.equals(player) && !le.equals(target)) {
                                org.bukkit.util.Vector toLe = le.getLocation().subtract(player.getLocation()).toVector().setY(0).normalize();
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
                    }
                }

                // Phantom Backstab (Swords - Max 4)
                NamespacedKey pbKey = new NamespacedKey(plugin, "rune_phantom_backstab");
                Integer pbLvl = meta.getPersistentDataContainer().get(pbKey, PersistentDataType.INTEGER);
                if (pbLvl != null) {
                    if (Math.random() <= 0.50) {
                        player.sendMessage(MM.deserialize(C_PURPLE + toSmallCaps("Phantom Backstab prepared!")));
                        Location behind = target.getLocation().add(target.getLocation().getDirection().multiply(-1.0));
                        behind.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, behind, 10, 0.2, 0.5, 0.2, 0.05);

                        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
                            if (target.isValid() && !target.isDead()) {
                                double backstabDmg = event.getDamage() * (0.2 + pbLvl * 0.15);
                                target.damage(backstabDmg, player);
                                target.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, target.getLocation().add(0, 0.8, 0), 1);
                                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1f, 1.5f);
                            }
                        }, 20L);
                    }
                }

                // Bleed (Swords/Axes - Max 8)
                NamespacedKey bleedKey = new NamespacedKey(plugin, "rune_bleed");
                Integer bleedLvl = meta.getPersistentDataContainer().get(bleedKey, PersistentDataType.INTEGER);
                if (bleedLvl != null) {
                    int bleedStacks = target.hasMetadata("bleed_stacks") 
                                      ? target.getMetadata("bleed_stacks").get(0).asInt() : 0;
                    bleedStacks = Math.min(8, bleedStacks + 1);
                    target.setMetadata("bleed_stacks", new FixedMetadataValue(plugin, bleedStacks));

                    final int runId = target.hasMetadata("bleed_run") 
                                      ? target.getMetadata("bleed_run").get(0).asInt() + 1 : 1;
                    target.setMetadata("bleed_run", new FixedMetadataValue(plugin, runId));

                    final int finalLvl = bleedLvl;
                    final int finalStacks = bleedStacks;
                    int[] tick = new int[]{0};
                    plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                        if (!target.isValid() || target.isDead() || tick[0] >= 5) {
                            t.cancel();
                            if (target.isValid()) {
                                target.removeMetadata("bleed_stacks", plugin);
                                target.removeMetadata("bleed_run", plugin);
                            }
                            return;
                        }
                        if (target.hasMetadata("bleed_run") && target.getMetadata("bleed_run").get(0).asInt() != runId) {
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
                NamespacedKey psKey = new NamespacedKey(plugin, "rune_poison_spores");
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
                NamespacedKey csSharpKey = new NamespacedKey(plugin, "rune_crude_sharpness");
                Integer csSharpLvl = meta.getPersistentDataContainer().get(csSharpKey, PersistentDataType.INTEGER);
                if (csSharpLvl != null) {
                    double flatBonus = csSharpLvl * 1.0;
                    event.setDamage(event.getDamage() + flatBonus);
                    target.getWorld().spawnParticle(org.bukkit.Particle.CRIT, target.getLocation().add(0, 0.8, 0), 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
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
        Player killer = entity.getKiller();
        if (killer != null) {
            ItemStack mainHand = killer.getInventory().getItemInMainHand();
            if (arcaneItems.isCustomItem(mainHand, "arcane.melee.siphon_blade")) {
                double maxHealth = killer.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
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
                    NamespacedKey key = new NamespacedKey(plugin, "rune_soul_harvester");
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

    private void applyToughnessReduction(LivingEntity target, String name, double value) {
        AttributeInstance attr = target.getAttribute(Attribute.ARMOR_TOUGHNESS);
        if (attr != null) {
            UUID modUuid = UUID.nameUUIDFromBytes(name.getBytes());
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getUniqueId().equals(modUuid)) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) attr.removeModifier(existing);

            AttributeModifier mod = new AttributeModifier(modUuid, name, -value, AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(mod);
        }
    }

    private void removeToughnessReduction(LivingEntity target, String name) {
        AttributeInstance attr = target.getAttribute(Attribute.ARMOR_TOUGHNESS);
        if (attr != null) {
            UUID modUuid = UUID.nameUUIDFromBytes(name.getBytes());
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getUniqueId().equals(modUuid)) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) attr.removeModifier(existing);
        }
    }

    private void applyArmorReduction(LivingEntity target, String name, double value) {
        AttributeInstance attr = target.getAttribute(Attribute.ARMOR);
        if (attr != null) {
            UUID modUuid = UUID.nameUUIDFromBytes(name.getBytes());
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getUniqueId().equals(modUuid)) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) attr.removeModifier(existing);

            AttributeModifier mod = new AttributeModifier(modUuid, name, -value, AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(mod);
        }
    }

    private void removeArmorReduction(LivingEntity target, String name) {
        AttributeInstance attr = target.getAttribute(Attribute.ARMOR);
        if (attr != null) {
            UUID modUuid = UUID.nameUUIDFromBytes(name.getBytes());
            AttributeModifier existing = null;
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getUniqueId().equals(modUuid)) {
                    existing = modifier;
                    break;
                }
            }
            if (existing != null) attr.removeModifier(existing);
        }
    }
}
