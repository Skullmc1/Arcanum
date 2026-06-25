package space.qclid.dashboard.codex.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.codex.*;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class CodexGuiListener implements Listener {

    private final JavaPlugin plugin;
    private final CodexRegistry registry;
    private final CodexManager manager;

    public CodexGuiListener(JavaPlugin plugin, CodexRegistry registry, CodexManager manager) {
        this.plugin = plugin;
        this.registry = registry;
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 1. Handle Codex GUI Clicks
        if (event.getInventory().getHolder() instanceof CodexInventoryHolder holder) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

            int slot = event.getSlot();
            handleCodexClick(player, holder, slot);
            return;
        }

        // 2. Handle Waypoint Linking GUI Clicks
        if (event.getInventory().getHolder() instanceof WaypointLinkInventoryHolder linkHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

            int slot = event.getSlot();
            handleLinkClick(player, linkHolder, slot);
        }
    }

    private void handleCodexClick(Player player, CodexInventoryHolder holder, int slot) {
        switch (holder.getType()) {
            case MAIN -> {
                String parentCategoryId = holder.getSlotMap().get(slot);
                if (parentCategoryId != null) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    CodexSubCategoryGui.open(player, registry, parentCategoryId);
                }
            }
            case SUB_CATEGORY -> {
                if (slot == 22) { // Back button in sub-categories
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    CodexMainGui.open(player, registry);
                    return;
                }
                String subCategoryId = holder.getSlotMap().get(slot);
                if (subCategoryId != null) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    CodexCategoryGui.open(player, registry, manager, subCategoryId, 0);
                }
            }
            case CATEGORY -> {
                if (slot == 45) { // Back button
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    // parentCategoryId here represents the top-level parent (e.g. "arcane", "explorer", "machinery")
                    CodexSubCategoryGui.open(player, registry, holder.getParentCategoryId());
                    return;
                }
                if (slot == 51) { // Prev page
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    CodexCategoryGui.open(player, registry, manager, holder.getContext(), holder.getPage() - 1);
                    return;
                }
                if (slot == 52) { // Next page
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    CodexCategoryGui.open(player, registry, manager, holder.getContext(), holder.getPage() + 1);
                    return;
                }

                String itemId = holder.getSlotMap().get(slot);
                if (itemId != null) {
                    if (manager.isUnlocked(player.getUniqueId(), itemId)) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                        CodexRecipeGui.open(player, registry, itemId, holder.getContext(), holder.getPage());
                    } else {
                        tryUnlock(player, itemId, holder);
                    }
                }
            }
            case RECIPE -> {
                if (slot == 45) { // Back button
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    // parentCategoryId here represents the subcategory ID (e.g. "arcane.runes")
                    CodexCategoryGui.open(player, registry, manager, holder.getParentCategoryId(), holder.getPage());
                }
            }
        }
    }

    private void tryUnlock(Player player, String itemId, CodexInventoryHolder holder) {
        CodexItem item = registry.getItem(itemId);
        if (item == null) return;

        // Check level
        if (player.getLevel() < item.getXpCost()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You do not have enough experience levels!")));
            return;
        }

        // Check items requirements
        java.util.ArrayList<String> missing = new java.util.ArrayList<>();
        for (ItemStack req : item.getItemRequirements()) {
            if (!hasRequirement(player, req)) {
                String reqName;
                if (req.getItemMeta() != null && req.getItemMeta().hasDisplayName()) {
                    reqName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(req.getItemMeta().displayName());
                } else {
                    reqName = req.getType().name().replace("_", " ").toLowerCase();
                }
                missing.add(req.getAmount() + "x " + toSmallCaps(reqName));
            }
        }

        if (!missing.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Missing requirements: ") + String.join(", ", missing)));
            return;
        }

        // Unlock!
        player.setLevel(player.getLevel() - item.getXpCost());
        manager.unlock(player.getUniqueId(), itemId);
        manager.save();

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Successfully unlocked: ") + G_GOLD + toSmallCaps(item.getDisplayName())));

        // Refresh Category GUI
        CodexCategoryGui.open(player, registry, manager, holder.getContext(), holder.getPage());
    }

    private void handleLinkClick(Player player, WaypointLinkInventoryHolder linkHolder, int slot) {
        String waypointName = linkHolder.getSlotMap().get(slot);
        if (waypointName == null) return;

        ItemStack compass = player.getInventory().getItemInMainHand();
        if (compass.getType() != org.bukkit.Material.COMPASS) {
            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must hold the compass in your main hand!")));
            player.closeInventory();
            return;
        }

        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "linked_waypoint");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, waypointName);
            meta.displayName(parse(G_GOLD + toSmallCaps("Waypoint Compass") + C_GRAY + " (" + C_ORANGE + waypointName + C_GRAY + ")"));
            meta.lore(List.of(
                    MM.deserialize(C_GRAY + toSmallCaps("Linked to: ") + C_ORANGE + waypointName),
                    MM.deserialize(""),
                    MM.deserialize(C_GREEN + toSmallCaps("Right-click to teleport here")),
                    MM.deserialize(C_YELLOW + toSmallCaps("Shift-Right-click to relink"))
            ));
            compass.setItemMeta(meta);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Compass linked to waypoint: ") + C_ORANGE + waypointName));
        player.closeInventory();
    }

    private boolean hasRequirement(Player player, ItemStack req) {
        if (req == null) return true;
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
