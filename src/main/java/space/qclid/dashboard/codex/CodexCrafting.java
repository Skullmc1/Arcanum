package space.qclid.dashboard.codex;

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
        }
        return null;
    }

    public void executeDropperCraft(Player player, Dropper dropper, String structureType, Location dropLoc) {
        Inventory inv = dropper.getInventory();
        ItemStack[] contents = inv.getContents();

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

                inv.setItem(1, null);
                inv.setItem(4, null);

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
        ItemStack slot4 = contents[4];
        ItemStack slot1 = contents[1];
        if (slot4 == null || slot4.getType() == Material.AIR) return null;
        if (slot1 == null || slot1.getType() == Material.AIR) return null;

        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1) continue;
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                return null;
            }
        }

        ItemMeta meta4 = slot4.getItemMeta();
        ItemMeta meta1 = slot1.getItemMeta();
        if (meta4 == null || meta1 == null) return null;

        NamespacedKey effectKey = new NamespacedKey(plugin, "rune_effect");
        NamespacedKey lvlKey = new NamespacedKey(plugin, "rune_level");
        NamespacedKey itemKey = new NamespacedKey(plugin, "item_id");

        String effect4 = meta4.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);
        String effect1 = meta1.getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);

        Integer lvl4 = meta4.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);
        Integer lvl1 = meta1.getPersistentDataContainer().get(lvlKey, PersistentDataType.INTEGER);

        String id4 = meta4.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        String id1 = meta1.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

        if (effect4 == null || effect1 == null || lvl4 == null || lvl1 == null || id4 == null || id1 == null) return null;

        if (effect4.equals(effect1) && lvl4.equals(lvl1)) {
            if (slot4.getAmount() != 1 || slot1.getAmount() != 1) return null;

            int nextLevel = lvl4 + 1;
            if (nextLevel > 3) return null;

            String baseId = id4;
            if (baseId.endsWith("_2")) baseId = baseId.substring(0, baseId.length() - 2);
            else if (baseId.endsWith("_3")) baseId = baseId.substring(0, baseId.length() - 2);

            return arcaneItems.createEnchantmentRune(baseId, effect4, nextLevel);
        }

        return null;
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
        return "none";
    }
}
