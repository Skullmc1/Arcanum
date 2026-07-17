package space.qclid.dashboard.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class CodexUtil {

    private CodexUtil() {}

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
