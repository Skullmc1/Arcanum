package space.qclid.dashboard.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.UUID;

public final class CodexUtil {

    private CodexUtil() {}

    public static NamespacedKey entityKey(JavaPlugin plugin, String key) {
        return new NamespacedKey(plugin, key);
    }

    public static void setMetadata(Entity entity, JavaPlugin plugin, String key, boolean value) {
        entity.getPersistentDataContainer().set(entityKey(plugin, key), PersistentDataType.INTEGER, value ? 1 : 0);
    }

    public static void setMetadata(Entity entity, JavaPlugin plugin, String key, int value) {
        entity.getPersistentDataContainer().set(entityKey(plugin, key), PersistentDataType.INTEGER, value);
    }

    public static void setMetadata(Entity entity, JavaPlugin plugin, String key, double value) {
        entity.getPersistentDataContainer().set(entityKey(plugin, key), PersistentDataType.DOUBLE, value);
    }

    public static void setMetadata(Entity entity, JavaPlugin plugin, String key, long value) {
        entity.getPersistentDataContainer().set(entityKey(plugin, key), PersistentDataType.LONG, value);
    }

    public static void setMetadata(Entity entity, JavaPlugin plugin, String key, String value) {
        entity.getPersistentDataContainer().set(entityKey(plugin, key), PersistentDataType.STRING, value);
    }

    public static int getIntMetadata(Entity entity, JavaPlugin plugin, String key, int def) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(entityKey(plugin, key)) ? pdc.get(entityKey(plugin, key), PersistentDataType.INTEGER) : def;
    }

    public static double getDoubleMetadata(Entity entity, JavaPlugin plugin, String key, double def) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(entityKey(plugin, key)) ? pdc.get(entityKey(plugin, key), PersistentDataType.DOUBLE) : def;
    }

    public static long getLongMetadata(Entity entity, JavaPlugin plugin, String key, long def) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(entityKey(plugin, key)) ? pdc.get(entityKey(plugin, key), PersistentDataType.LONG) : def;
    }

    public static String getStringMetadata(Entity entity, JavaPlugin plugin, String key) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(entityKey(plugin, key)) ? pdc.get(entityKey(plugin, key), PersistentDataType.STRING) : null;
    }

    public static boolean hasMetadata(Entity entity, JavaPlugin plugin, String key) {
        return entity.getPersistentDataContainer().has(entityKey(plugin, key));
    }

    public static void removeMetadata(Entity entity, JavaPlugin plugin, String key) {
        entity.getPersistentDataContainer().remove(entityKey(plugin, key));
    }

    @SuppressWarnings("removal")
    public static void setMetadata(Block block, JavaPlugin plugin, String key, boolean value) {
        block.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    @SuppressWarnings("removal")
    public static boolean hasMetadata(Block block, JavaPlugin plugin, String key) {
        return block.hasMetadata(key);
    }

    @SuppressWarnings("removal")
    public static void removeMetadata(Block block, JavaPlugin plugin, String key) {
        block.removeMetadata(key, plugin);
    }

    public static void setSkullTexture(SkullMeta meta, String skinUrl, JavaPlugin plugin) {
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(skinUrl));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set skull texture: " + e.getMessage());
        }
    }

    public static boolean hasRequirement(Player player, ItemStack req) {
        if (req == null) return true;
        JavaPlugin plugin = JavaPlugin.getPlugin(space.qclid.dashboard.DashboardPlugin.class);
        ItemMeta reqMeta = req.getItemMeta();
        String reqId = null;
        if (reqMeta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            reqId = reqMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }

        if (reqId == null) {
            return player.getInventory().containsAtLeast(req, req.getAmount());
        }

        int found = 0;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == req.getType()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (reqId.equals(id)) {
                        found += item.getAmount();
                    }
                }
            }
        }
        return found >= req.getAmount();
    }
}
