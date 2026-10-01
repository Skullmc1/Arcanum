package space.qclid.arcanum.codex.core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom InventoryHolder that tags all Codex GUI inventories.
 * Avoids fragile title-string detection; use {@code instanceof} checks instead.
 */
public class CodexInventoryHolder implements InventoryHolder {

    public enum Type { MAIN, SUB_CATEGORY, CATEGORY, RECIPE }

    private Inventory inventory;
    private final Type type;
    
    /** 
     * Context meanings:
     * - MAIN: null
     * - SUB_CATEGORY: parentCategoryId (e.g. "arcane", "explorer", "machinery")
     * - CATEGORY: subCategoryId (e.g. "arcane.runes", "explorer.gadgets")
     * - RECIPE: itemId (e.g. "arcane.lifesteal_rune")
     */
    private final String context;
    
    /** Parent category/sub-category ID used for back navigation. */
    private final String parentCategoryId;
    
    /** Current page (CATEGORY) or parent page (RECIPE, for back navigation). */
    private final int page;
    
    /** Slot → context mappings (e.g., Slot -> categoryId or Slot -> itemId). */
    private final Map<Integer, String> slotMap = new HashMap<>();

    public CodexInventoryHolder(Type type, String context, String parentCategoryId, int page) {
        this.type             = type;
        this.context          = context;
        this.parentCategoryId = parentCategoryId;
        this.page             = page;
    }

    @Override 
    public Inventory getInventory() { 
        return inventory; 
    }
    
    public void setInventory(Inventory inv) { 
        this.inventory = inv; 
    }
    
    public Type getType() { 
        return type; 
    }
    
    public String getContext() { 
        return context; 
    }
    
    public String getParentCategoryId() { 
        return parentCategoryId; 
    }
    
    public int getPage() { 
        return page; 
    }
    
    public Map<Integer, String> getSlotMap() { 
        return slotMap; 
    }
}
