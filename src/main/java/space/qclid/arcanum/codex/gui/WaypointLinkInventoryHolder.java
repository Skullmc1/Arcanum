package space.qclid.arcanum.codex.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom InventoryHolder for the Waypoint Compass linking GUI.
 * Maps slot clicks directly to waypoint names.
 */
public class WaypointLinkInventoryHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, String> slotMap = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public Map<Integer, String> getSlotMap() {
        return slotMap;
    }
}
