package space.qclid.dashboard.codex.crafting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import space.qclid.dashboard.codex.core.*;
import space.qclid.dashboard.codex.items.*;

import java.util.ArrayList;
import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexCrafting {

    private final JavaPlugin plugin;
    private final CodexManager manager;
    private final CodexRegistry registry;
    private final ArcaneItems arcaneItems;

    public CodexCrafting(JavaPlugin plugin, CodexManager manager, CodexRegistry registry, ArcaneItems arcaneItems) {
        this.plugin = plugin;
        this.manager = manager;
        this.registry = registry;
        this.arcaneItems = arcaneItems;
    }

    public boolean isAnvil(Material material) {
        return material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL;
    }

    public boolean isValidEnchanter(Block clickedBlock) {
        if (clickedBlock.getType() != Material.ENCHANTING_TABLE) return false;
        Block below = clickedBlock.getRelative(BlockFace.DOWN);
        return check3x3Base(below, Material.DIAMOND_BLOCK);
    }

    public boolean isValidDisenchanter(Block clickedBlock) {
        if (clickedBlock.getType() != Material.ENCHANTING_TABLE) return false;
        Block below = clickedBlock.getRelative(BlockFace.DOWN);
        return check3x3Base(below, Material.IRON_BLOCK);
    }

    private boolean check3x3Base(Block centerBlock, Material material) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (centerBlock.getRelative(x, 0, z).getType() != material) {
                    return false;
                }
            }
        }
        return true;
    }

    public Dropper getDropperForStructure(Block clickedBlock, String structureType) {
        if (structureType.equals("arcane_table")) {
            Block below = clickedBlock.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                int bookshelves = 0;
                BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                for (BlockFace face : faces) {
                    if (below.getRelative(face).getType() == Material.BOOKSHELF) {
                        bookshelves++;
                    }
                }
                if (bookshelves >= 2) {
                    return dropper;
                }
            }
        } else if (structureType.equals("heavy_forge")) {
            Block below = clickedBlock.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.BLAST_FURNACE) {
                Block belowFurnace = below.getRelative(BlockFace.DOWN);
                if (belowFurnace.getType() == Material.DROPPER && belowFurnace.getState() instanceof Dropper dropper) {
                    return dropper;
                }
            }
        } else if (structureType.equals("upgrade_table")) {
            Block below = clickedBlock.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                Block belowDropper = below.getRelative(BlockFace.DOWN);
                if (belowDropper.getType() == Material.BOOKSHELF) {
                    return dropper;
                }
            }
        } else if (structureType.equals("blessings_altar")) {
            Block below = clickedBlock.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                BlockFace centerDir = findBlessingsAltarGoldCenter(below);
                if (centerDir != null) {
                    return dropper;
                }
            }
        } else if (structureType.equals("heavy_alloy_forge")) {
            if (clickedBlock.getType() == Material.BLAST_FURNACE) {
                Block below = clickedBlock.getRelative(BlockFace.DOWN);
                if (below.getType() == Material.DROPPER && below.getState() instanceof Dropper dropper) {
                    Block belowDropper = below.getRelative(BlockFace.DOWN);
                    boolean magmaValid = true;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            if (belowDropper.getRelative(x, 0, z).getType() != Material.MAGMA_BLOCK) {
                                magmaValid = false;
                                break;
                            }
                        }
                        if (!magmaValid) break;
                    }
                    if (magmaValid) {
                        return dropper;
                    }
                }
            }
        }
        return null;
    }

    public void executeDropperCraft(Player player, Dropper dropper, String structureType, Location dropLoc) {
        Inventory inv = dropper.getInventory();
        ItemStack[] contents = inv.getContents();

        boolean empty = true;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }

        if (empty) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The dropper is empty!")));
            return;
        }

        if (structureType.equals("upgrade_table")) {
            ItemStack combinedResult = tryCombineRunes(contents);
            if (combinedResult != null) {
                ItemMeta resMeta = combinedResult.getItemMeta();
                NamespacedKey idKey = new NamespacedKey(plugin, "item_id");
                String resId = resMeta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);

                if (resId != null && !manager.isUnlocked(player.getUniqueId(), resId)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You haven't unlocked this recipe in the Codex!")));
                    return;
                }

                for (int i = 0; i < 9; i++) {
                    inv.setItem(i, null);
                }

                dropLoc.getWorld().dropItemNaturally(dropLoc, combinedResult);
                dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
                dropLoc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, dropLoc, 20, 0.5, 0.5, 0.5);

                String displayNameStr = combinedResult.getItemMeta() != null && combinedResult.getItemMeta().hasDisplayName()
                        ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(combinedResult.getItemMeta().displayName())
                        : "Upgraded Rune";
                player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Crafted: ") + displayNameStr));
                return;
            }
        }

        for (CodexCategory category : registry.getCategories()) {
            for (CodexItem item : category.getItems()) {
                String itemStation = getStationType(item.getCraftingStation());
                if (itemStation.equals(structureType)) {
                    if (recipeMatches(contents, item)) {
                        if (!manager.isUnlocked(player.getUniqueId(), item.getId())) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You haven't unlocked this recipe in the Codex!")));
                            return;
                        }

                        consumeRecipe(dropper, item);
                        dropLoc.getWorld().dropItemNaturally(dropLoc, item.getDisplayItem());

                        // Play crafting sound and particles
                        if (structureType.equals("arcane_table")) {
                            dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
                            dropLoc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, dropLoc, 30, 0.5, 0.5, 0.5);
                        } else if (structureType.equals("heavy_forge")) {
                            dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_ANVIL_USE, 0.8f, 0.8f);
                            dropLoc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, dropLoc, 15, 0.3, 0.3, 0.3);
                        } else if (structureType.equals("upgrade_table")) {
                            dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
                            dropLoc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, dropLoc, 20, 0.5, 0.5, 0.5);
                        }

                        player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Crafted: ") + item.getDisplayName()));
                        return;
                    }
                }
            }
        }

        // No recipe matched
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Invalid recipe! Check the Codex for correct layouts.")));
    }

    private boolean recipeMatches(ItemStack[] dropperContents, CodexItem codexItem) {
        ItemStack[] recipe = codexItem.getRecipe();
        for (int i = 0; i < 9; i++) {
            ItemStack dropperItem = dropperContents[i];
            ItemStack recipeItem = recipe[i];

            if (recipeItem == null || recipeItem.getType() == Material.AIR) {
                if (dropperItem != null && dropperItem.getType() != Material.AIR) {
                    return false;
                }
                continue;
            }

            if (dropperItem == null || dropperItem.getType() == Material.AIR) {
                return false;
            }

            if (dropperItem.getType() != recipeItem.getType()) {
                return false;
            }

            if (dropperItem.getAmount() < recipeItem.getAmount()) {
                return false;
            }

            ItemMeta recipeMeta = recipeItem.getItemMeta();
            String expectedCustomId = null;
            if (recipeMeta != null) {
                NamespacedKey key = new NamespacedKey(plugin, "item_id");
                expectedCustomId = recipeMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            }

            ItemMeta dropperMeta = dropperItem.getItemMeta();
            String dropperCustomId = null;
            if (dropperMeta != null) {
                NamespacedKey key = new NamespacedKey(plugin, "item_id");
                dropperCustomId = dropperMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            }

            if (expectedCustomId != null) {
                if (!expectedCustomId.equals(dropperCustomId)) {
                    return false;
                }
            } else {
                if (dropperCustomId != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void consumeRecipe(Dropper dropper, CodexItem codexItem) {
        Inventory inv = dropper.getInventory();
        ItemStack[] recipe = codexItem.getRecipe();
        for (int i = 0; i < 9; i++) {
            ItemStack recipeItem = recipe[i];
            if (recipeItem == null || recipeItem.getType() == Material.AIR) continue;
            ItemStack dropperItem = inv.getItem(i);
            if (dropperItem != null) {
                int newAmount = dropperItem.getAmount() - recipeItem.getAmount();
                if (newAmount <= 0) {
                    if (dropperItem.getType() == Material.LAVA_BUCKET || dropperItem.getType() == Material.WATER_BUCKET) {
                        inv.setItem(i, new ItemStack(Material.BUCKET));
                    } else {
                        inv.setItem(i, null);
                    }
                } else {
                    dropperItem.setAmount(newAmount);
                    inv.setItem(i, dropperItem);
                }
            }
        }
    }

    private ItemStack tryCombineRunes(ItemStack[] contents) {
        List<ItemStack> runeStacks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                runeStacks.add(item);
            }
        }

        int totalAmount = 0;
        for (ItemStack stack : runeStacks) {
            totalAmount += stack.getAmount();
        }
        if (totalAmount != 2) return null;

        ItemStack firstStack = runeStacks.get(0);
        Material firstType = firstStack.getType();
        if (firstType != Material.PRIZE_POTTERY_SHERD && firstType != Material.GUSTER_BANNER_PATTERN && firstType != Material.PRISMARINE_SHARD) return null;

        ItemMeta firstMeta = firstStack.getItemMeta();
        if (firstMeta == null) return null;

        NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
        NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
        NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");

        String effect = firstMeta.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
        Integer lvl = firstMeta.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
        String id = firstMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

        if (effect == null || lvl == null || id == null) return null;

        if (runeStacks.size() == 2) {
            ItemStack secondStack = runeStacks.get(1);
            if (secondStack.getType() != firstType) return null;

            ItemMeta secondMeta = secondStack.getItemMeta();
            if (secondMeta == null) return null;

            String effect2 = secondMeta.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
            Integer lvl2 = secondMeta.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
            String id2 = secondMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

            if (effect2 == null || lvl2 == null || id2 == null) return null;

            if (!effect.equals(effect2) || !lvl.equals(lvl2) || !id.equals(id2)) {
                return null;
            }
        }

        int maxLvl = getRuneMaxLevel(effect);
        int nextLevel = lvl + 1;
        if (nextLevel > maxLvl) return null;

        String baseId = id;
        if (baseId.matches(".+_\\d+")) {
            baseId = baseId.substring(0, baseId.lastIndexOf('_'));
        }

        return arcaneItems.createEnchantmentRune(baseId, effect, nextLevel);
    }

    public void executeBloodAltarCraft(Player player, Dropper dropper, Location dropLoc) {
        Inventory inv = dropper.getInventory();
        ItemStack[] contents = inv.getContents();

        boolean empty = true;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }

        if (empty) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The dropper is empty!")));
            return;
        }

        for (CodexCategory category : registry.getCategories()) {
            for (CodexItem item : category.getItems()) {
                String itemStation = getStationType(item.getCraftingStation());
                if (itemStation.equals("blood_altar")) {
                    if (recipeMatches(contents, item)) {
                        if (!manager.isUnlocked(player.getUniqueId(), item.getId())) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You haven't unlocked this recipe in the Codex!")));
                            return;
                        }

                        consumeRecipe(dropper, item);
                        dropLoc.getWorld().dropItemNaturally(dropLoc, item.getDisplayItem());

                        // Sacrifice visual effects
                        dropLoc.getWorld().playSound(dropLoc, Sound.ENTITY_WITHER_SPAWN, 1f, 0.8f);
                        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, dropLoc, 50, 0.5, 0.5, 0.5);
                        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, dropLoc, 30, 0.5, 0.5, 0.5);

                        // Perform health sacrifice
                        if (player.getWorld().isHardcore()) {
                            double current = player.getHealth();
                            double damage = current * 0.90;
                            double newHealth = Math.max(1.0, current - damage);
                            player.setHealth(newHealth);
                            player.damage(0.01); // flash red
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The altar demands blood! You survive by a thread...")));
                        } else {
                            player.setHealth(0.0); // sacrifice
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The altar has claimed your soul as sacrifice!")));
                        }
                        return;
                    }
                }
            }
        }

        // No recipe matched
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Invalid demonic recipe! Check the Codex for correct layouts.")));
    }

    private String getStationType(ItemStack station) {
        if (station == null) return "none";
        ItemMeta meta = station.getItemMeta();
        if (meta == null) {
            if (station.getType() == Material.CRAFTING_TABLE) {
                return "crafting_table";
            }
            return "none";
        }
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) {
            if (station.getType() == Material.CRAFTING_TABLE) {
                return "crafting_table";
            }
            return "none";
        }
        if (id.equals("machinery.arcana_table")) return "arcane_table";
        if (id.equals("machinery.heavy_forge")) return "heavy_forge";
        if (id.equals("machinery.upgrade_table")) return "upgrade_table";
        if (id.equals("machinery.blood_altar")) return "blood_altar";
        if (id.equals("machinery.blessings_altar")) return "blessings_altar";
        if (id.equals("machinery.heavy_alloy_forge")) return "heavy_alloy_forge";
        return "none";
    }

    private BlockFace findBlessingsAltarGoldCenter(Block dropperBlock) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace face : faces) {
            Block center = dropperBlock.getRelative(face, 2);
            if (center.getType() == Material.GOLD_BLOCK) {
                boolean goldValid = true;
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (center.getRelative(x, 0, z).getType() != Material.GOLD_BLOCK) {
                            goldValid = false;
                            break;
                        }
                    }
                    if (!goldValid) break;
                }
                if (!goldValid) continue;

                boolean borderValid = true;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
                        Block borderBlock = center.getRelative(dx, 0, dz);
                        if (borderBlock.getX() == dropperBlock.getX() && borderBlock.getZ() == dropperBlock.getZ()) {
                            continue;
                        }
                        Material type = borderBlock.getType();
                        boolean matches = type.name().contains("QUARTZ") || type.name().contains("END_STONE");
                        if (!matches) {
                            borderValid = false;
                            break;
                        }
                    }
                    if (!borderValid) break;
                }

                if (borderValid) {
                    return face;
                }
            }
        }
        return null;
    }

    public void triggerBlessingsAltarSacrifice(Player player, Block fenceBlock, Dropper dropper, Location goldCenterLoc) {
        Location searchLoc = goldCenterLoc.clone().add(0.5, 1.0, 0.5);
        org.bukkit.entity.LivingEntity targetMob = null;
        for (org.bukkit.entity.Entity entity : searchLoc.getWorld().getNearbyEntities(searchLoc, 1.8, 2.0, 1.8)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)) {
                targetMob = (org.bukkit.entity.LivingEntity) entity;
                break;
            }
        }
        if (targetMob == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("A living sacrifice must be placed on the gold altar floor!")));
            return;
        }

        final org.bukkit.entity.LivingEntity sacrifice = targetMob;
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("The heavens align... The sacrifice begins!")));
        sacrifice.setMetadata("altar_sacrifice", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            if (!sacrifice.isValid() || sacrifice.isDead()) {
                task.cancel();
                executeHolyCraft(player, dropper, goldCenterLoc);
                return;
            }
            Location mobLoc = sacrifice.getLocation();
            mobLoc.getWorld().strikeLightning(mobLoc);
            sacrifice.damage(4.0); // 2 hearts of damage per strike
        }, 1L, 15L);
    }

    private void executeHolyCraft(Player player, Dropper dropper, Location dropLoc) {
        if (player == null || !player.isOnline()) return;
        Inventory inv = dropper.getInventory();
        ItemStack[] contents = inv.getContents();

        boolean empty = true;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }

        if (empty) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("The dropper is empty!")));
            return;
        }

        for (CodexCategory category : registry.getCategories()) {
            for (CodexItem item : category.getItems()) {
                String itemStation = getStationType(item.getCraftingStation());
                if (itemStation.equals("blessings_altar")) {
                    if (recipeMatches(contents, item)) {
                        if (!manager.isUnlocked(player.getUniqueId(), item.getId())) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You haven't unlocked this recipe in the Codex!")));
                            return;
                        }

                        consumeRecipe(dropper, item);
                        dropLoc.getWorld().dropItemNaturally(dropLoc.clone().add(0.5, 1.1, 0.5), item.getDisplayItem());

                        dropLoc.getWorld().playSound(dropLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.FIREWORK, dropLoc.clone().add(0.5, 1.5, 0.5), 50, 0.5, 0.5, 0.5, 0.1);
                        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, dropLoc.clone().add(0.5, 1.5, 0.5), 30, 0.5, 0.5, 0.5, 0.1);

                        player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Crafted: ") + item.getDisplayName()));
                        return;
                    }
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.0f);
        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Invalid holy recipe! Check the Codex for correct layouts.")));
    }

    public void executeBlockDuplicatorCraft(Player player, Block fenceBlock, Dropper dropper) {
        if (!manager.isUnlocked(player.getUniqueId(), "machinery.block_duplicator")) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You haven't unlocked the Block Duplicator in the Codex!")));
            return;
        }

        Inventory inv = dropper.getInventory();
        ItemStack[] contents = inv.getContents();

        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getAmount() >= 1 && isValidBuildingBlock(item)) {
                validSlots.add(i);
            }
        }

        if (validSlots.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("No valid building blocks found in the dropper!")));
            return;
        }

        int N = validSlots.size();
        int requiredLava = (N + 1) / 2;

        // Find adjacent furnaces/blast furnaces (North, South, East, West relative to the dropper block)
        BlockFace[] horizontalFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        List<org.bukkit.block.Furnace> adjacentFurnaces = new ArrayList<>();
        for (BlockFace face : horizontalFaces) {
            Block adjBlock = dropper.getBlock().getRelative(face);
            if (adjBlock.getState() instanceof org.bukkit.block.Furnace furnace) {
                adjacentFurnaces.add(furnace);
            }
        }

        int availableLava = 0;
        for (org.bukkit.block.Furnace furnace : adjacentFurnaces) {
            Inventory furnaceInv = furnace.getInventory();
            for (ItemStack item : furnaceInv.getContents()) {
                if (item != null && item.getType() == Material.LAVA_BUCKET) {
                    availableLava += item.getAmount();
                }
            }
        }

        if (availableLava < requiredLava) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Insufficient fuel! Requires " + requiredLava + " Lava Buckets in adjacent furnaces/blast furnaces (found " + availableLava + ").")));
            return;
        }

        // Consume lava buckets
        int lavaToConsume = requiredLava;
        for (org.bukkit.block.Furnace furnace : adjacentFurnaces) {
            if (lavaToConsume <= 0) break;
            Inventory furnaceInv = furnace.getInventory();
            ItemStack[] fContents = furnaceInv.getContents();
            boolean changed = false;
            for (int i = 0; i < fContents.length; i++) {
                ItemStack item = fContents[i];
                if (item != null && item.getType() == Material.LAVA_BUCKET) {
                    int amount = item.getAmount();
                    if (amount <= lavaToConsume) {
                        lavaToConsume -= amount;
                        // Replace with empty buckets
                        furnaceInv.setItem(i, new ItemStack(Material.BUCKET, amount));
                        changed = true;
                    } else {
                        item.setAmount(amount - lavaToConsume);
                        furnaceInv.setItem(i, item);
                        ItemStack emptyBuckets = new ItemStack(Material.BUCKET, lavaToConsume);
                        java.util.Map<Integer, ItemStack> leftover = furnaceInv.addItem(emptyBuckets);
                        for (ItemStack drop : leftover.values()) {
                            furnace.getLocation().getWorld().dropItemNaturally(furnace.getLocation(), drop);
                        }
                        lavaToConsume = 0;
                        changed = true;
                    }
                    if (lavaToConsume <= 0) break;
                }
            }
            if (changed) {
                furnace.update();
            }
        }

        // Duplicate building blocks
        Location dropLoc = fenceBlock.getLocation().add(0.5, 1.2, 0.5);
        for (int slotIndex : validSlots) {
            ItemStack itemInDropper = inv.getItem(slotIndex);
            if (itemInDropper != null && itemInDropper.getAmount() >= 1 && isValidBuildingBlock(itemInDropper)) {
                // Drop a full stack of cloned item
                ItemStack duplicatedStack = itemInDropper.clone();
                duplicatedStack.setAmount(64);
                dropLoc.getWorld().dropItemNaturally(dropLoc, duplicatedStack);

                // Consume 1 item from dropper slot
                int newAmount = itemInDropper.getAmount() - 1;
                if (newAmount <= 0) {
                    inv.setItem(slotIndex, null);
                } else {
                    itemInDropper.setAmount(newAmount);
                    inv.setItem(slotIndex, itemInDropper);
                }
            }
        }

        // Play sound and spawn smoke & flame particles
        dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f);
        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, dropLoc, 30, 0.5, 0.5, 0.5, 0.05);
        dropLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, dropLoc, 20, 0.5, 0.5, 0.5, 0.05);
        player.sendActionBar(MM.deserialize(C_GREEN + toSmallCaps("Duplicated " + N + " stacks of blocks!")));
    }

    public boolean isValidBuildingBlock(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        Material material = item.getType();
        if (!material.isBlock()) return false;
        if (!material.isSolid()) return false;
        if (material.isInteractable()) return false;

        String name = material.name();
        if (name.contains("SHULKER_BOX")) return false;
        if (name.contains("ORE")) return false;

        // Exclude raw metal blocks
        if (material == Material.RAW_IRON_BLOCK || material == Material.RAW_GOLD_BLOCK || material == Material.RAW_COPPER_BLOCK) return false;

        // Exclude valuable mineral blocks
        if (material == Material.DIAMOND_BLOCK || material == Material.EMERALD_BLOCK || material == Material.NETHERITE_BLOCK 
            || material == Material.GOLD_BLOCK || material == Material.IRON_BLOCK || material == Material.LAPIS_BLOCK 
            || material == Material.REDSTONE_BLOCK || material == Material.COAL_BLOCK || material == Material.COPPER_BLOCK) return false;

        // Exclude spawners
        if (material == Material.SPAWNER) return false;

        // Exclude obsidian
        if (material == Material.OBSIDIAN || material == Material.CRYING_OBSIDIAN) return false;

        // Exclude bedrock, barriers, command blocks, structure blocks, light blocks, jigsaw, structure voids
        if (material == Material.BEDROCK || material == Material.BARRIER || material == Material.COMMAND_BLOCK 
            || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK 
            || material == Material.STRUCTURE_BLOCK || material == Material.JIGSAW || material == Material.LIGHT) return false;

        return true;
    }

    public int getRuneMaxLevel(String effect) {
        if (effect == null) return 1;
        switch (effect.toLowerCase()) {
            case "lifesteal": return 3;
            case "speed": return 3;
            case "catch_flame": return 3;
            case "demonium": return 3;
            case "corrosive_slash": return 4;
            case "scorch": return 1;
            case "dwarfs_blessing": return 1;
            case "glacial_thorns": return 5;
            case "tidal_sweep": return 3;
            case "seismic_landing": return 4;
            case "photosynthesis": return 1;
            case "zephyr": return 1;
            case "static_charge": return 7;
            case "phantom_backstab": return 4;
            case "telekinesis": return 1;
            case "kinetic_rebound": return 1;
            case "redirection": return 6;
            case "bleed": return 8;
            case "poison_spores": return 5;
            case "gas_cloud": return 4;
            case "heavy_draw": return 5;
            case "timber": return 1;
            case "soul_harvester": return 1;
            case "crude_sharpness": return 7;
            case "basalt_trail": return 1;
            case "void_walker": return 1;
            default: return 1;
        }
    }
}
