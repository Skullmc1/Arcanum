package space.qclid.arcanum.codex.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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

public class CodexRuneListener implements Listener {

    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;
    private final ExplorerItems explorerItems;
    private final CodexCrafting codexCrafting;
    private final CodexPassiveTask codexPassiveTask;
    private final Map<UUID, String> activeMachine;

    public CodexRuneListener(CodexContext ctx) {
        this.plugin = ctx.plugin();
        this.dataManager = ctx.dataManager();
        this.manager = ctx.manager();
        this.registry = ctx.registry();
        this.arcaneItems = ctx.arcaneItems();
        this.explorerItems = ctx.explorerItems();
        this.codexCrafting = ctx.codexCrafting();
        this.codexPassiveTask = ctx.codexPassiveTask();
        this.activeMachine = ctx.activeMachine();
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand == null) return;
        Material mainType = mainHand.getType();
        if (mainType != Material.PRIZE_POTTERY_SHERD && mainType != Material.GUSTER_BANNER_PATTERN && mainType != Material.PRISMARINE_SHARD) return;
        if (offHand == null || offHand.getType() == Material.AIR) return;

        ItemMeta mainMeta = mainHand.getItemMeta();
        if (mainMeta == null) return;

        NamespacedKey effectKey = space.qclid.arcanum.compat.Compat.key("rune_effect");
        NamespacedKey lvlKey = space.qclid.arcanum.compat.Compat.key("rune_level");
        NamespacedKey typeKey = space.qclid.arcanum.compat.Compat.key("rune_type");

        String effect = mainMeta.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
        Integer lvl = mainMeta.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
        String runeType = mainMeta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);

        if (effect == null || lvl == null || runeType == null) return;

        // This is a custom rune, cancel event
        event.setCancelled(true);

        boolean valid = false;
        if (runeType.equals("WEAPON_SWORD")) {
            valid = offHand.getType().name().contains("SWORD");
        } else if (runeType.equals("WEAPON_AXE")) {
            valid = offHand.getType().name().contains("AXE");
        } else if (runeType.equals("WEAPON_SWORD_AXE")) {
            valid = offHand.getType().name().contains("SWORD") || offHand.getType().name().contains("AXE");
        } else if (runeType.equals("ARMOR_BOOTS")) {
            valid = offHand.getType().name().contains("BOOTS");
        } else if (runeType.equals("ARMOR_CHESTPLATE")) {
            valid = offHand.getType().name().contains("CHESTPLATE");
        } else if (runeType.equals("ARMOR")) {
            valid = offHand.getType().name().contains("HELMET") || offHand.getType().name().contains("CHESTPLATE") || offHand.getType().name().contains("LEGGINGS") || offHand.getType().name().contains("BOOTS");
        } else if (runeType.equals("TOOLS")) {
            valid = offHand.getType().name().contains("PICKAXE") || offHand.getType().name().contains("SHOVEL") || offHand.getType().name().contains("AXE");
        } else if (runeType.equals("TOOLS_HOE")) {
            valid = offHand.getType().name().contains("PICKAXE") || offHand.getType().name().contains("SHOVEL") || offHand.getType().name().contains("AXE") || offHand.getType().name().contains("HOE");
        } else if (runeType.equals("TOOL_HOE")) {
            valid = offHand.getType().name().contains("HOE");
        } else if (runeType.equals("WEAPON_BOW")) {
            valid = offHand.getType() == Material.BOW || offHand.getType() == Material.CROSSBOW;
        } else if (runeType.equals("WEAPON_BOW_ONLY")) {
            valid = offHand.getType() == Material.BOW;
        } else if (runeType.equals("SHIELD")) {
            valid = offHand.getType() == Material.SHIELD;
        } else if (runeType.equals("DURABILITY")) {
            valid = offHand.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable;
        } else if (runeType.equals("MELEE_OR_STICK")) {
            valid = offHand.getType().name().contains("SWORD") || offHand.getType().name().contains("AXE") || offHand.getType().name().contains("PICKAXE") || offHand.getType().name().contains("SHOVEL") || offHand.getType().name().contains("HOE") || offHand.getType() == Material.STICK;
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

        // Check incompatibilities
        if (effect.equalsIgnoreCase("tidal_sweep")) {
            if (offMeta.hasEnchant(org.bukkit.enchantments.Enchantment.SWEEPING_EDGE)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Tidal Sweep is incompatible with Sweeping Edge!")));
                return;
            }
        }
        if (effect.equalsIgnoreCase("seismic_landing")) {
            if (offMeta.hasEnchant(org.bukkit.enchantments.Enchantment.FEATHER_FALLING)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Seismic Landing is incompatible with Feather Falling!")));
                return;
            }
        }

        NamespacedKey applyKey = space.qclid.arcanum.compat.Compat.key("rune_" + effect);
        Integer existingLvl = offMeta.getPersistentDataContainer().get(applyKey, PersistentDataType.INTEGER);

        int targetLvl = lvl;
        if (existingLvl != null) {
            if (lvl > existingLvl) {
                targetLvl = lvl;
            } else if (lvl == existingLvl) {
                int maxLvl = codexCrafting.getRuneMaxLevel(effect);
                if (existingLvl >= maxLvl) {
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

        offHand.setItemMeta(offMeta);
        space.qclid.arcanum.util.TextUtil.refreshItemLore(offHand, plugin);

        // Consume 1 rune safely
        if (mainHand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            mainHand.setAmount(mainHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(mainHand);
        }
        player.getInventory().setItemInOffHand(offHand);

        String roman = getRomanNum(targetLvl);
        String nameBase = getEffectDisplayName(effect);
        String displayEffectName = nameBase + " " + roman;

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
        player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation(), 30, 0.5, 1, 0.5);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully applied ") + C_GRAY + toSmallCaps(displayEffectName) + C_GREEN + toSmallCaps(" to your item!")));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = space.qclid.arcanum.compat.Compat.key("item_id");
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
                if (!arcaneItems.isCustomItem(matrix[0], "arcane.speed_rune") || !arcaneItems.isCustomItem(matrix[2], "arcane.speed_rune")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            } else if (itemId.equals("explorer.grappling_hook.diamond")) {
                ItemStack[] matrix = event.getInventory().getMatrix();
                // slot 4 is the middle slot in a 3x3 grid
                if (!arcaneItems.isCustomItem(matrix[4], "explorer.grappling_hook.iron")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            } else if (itemId.equals("explorer.grappling_hook.netherite")) {
                ItemStack[] matrix = event.getInventory().getMatrix();
                if (!arcaneItems.isCustomItem(matrix[4], "explorer.grappling_hook.diamond")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }
}
