package space.qclid.dashboard.codex;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Represents a single entry in the Codex — its display, unlock cost, and recipe data. */
public class CodexItem {

    private final String id;
    private final String displayName;
    private final ItemStack displayItem;
    private final int xpCost;
    private final List<ItemStack> itemRequirements;
    private final String description;
    private final ItemStack craftingStation;
    private final ItemStack[] recipe; // 9-slot array matching a 3×3 grid; null = empty slot
    private final boolean defaultUnlocked;

    private CodexItem(Builder b) {
        this.id               = b.id;
        this.displayName      = b.displayName;
        this.displayItem      = b.displayItem;
        this.xpCost           = b.xpCost;
        this.itemRequirements = Collections.unmodifiableList(new ArrayList<>(b.itemRequirements));
        this.description      = b.description;
        this.craftingStation  = b.craftingStation;
        this.recipe           = b.recipe.clone();
        this.defaultUnlocked  = b.defaultUnlocked;
    }

    public String          getId()               { return id; }
    public String          getDisplayName()      { return displayName; }
    public ItemStack       getDisplayItem()      { return displayItem.clone(); }
    public int             getXpCost()           { return xpCost; }
    public List<ItemStack> getItemRequirements() { return itemRequirements; }
    public String          getDescription()      { return description; }
    public ItemStack       getCraftingStation()  { return craftingStation != null ? craftingStation.clone() : null; }
    public ItemStack[]     getRecipe()           { return recipe.clone(); }
    public boolean         isDefaultUnlocked()   { return defaultUnlocked; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static class Builder {
        private final String id;
        private String displayName      = "Unnamed Item";
        private ItemStack displayItem;
        private int xpCost              = 1;
        private final List<ItemStack> itemRequirements = new ArrayList<>();
        private String description      = "";
        private ItemStack craftingStation;
        private ItemStack[] recipe      = new ItemStack[9];
        private boolean defaultUnlocked = false;

        public Builder(String id)                          { this.id = id; }
        public Builder displayName(String name)            { this.displayName = name;        return this; }
        public Builder displayItem(ItemStack item)         { this.displayItem = item;        return this; }
        public Builder xpCost(int cost)                    { this.xpCost = cost;             return this; }
        public Builder requires(ItemStack item)            { itemRequirements.add(item);     return this; }
        public Builder description(String desc)            { this.description = desc;        return this; }
        public Builder craftingStation(ItemStack station)  { this.craftingStation = station; return this; }
        public Builder defaultUnlocked(boolean val)        { this.defaultUnlocked = val;     return this; }
        public Builder recipe(ItemStack... items) {
            if (items.length != 9) throw new IllegalArgumentException("Recipe must have exactly 9 slots.");
            this.recipe = items;
            return this;
        }

        public CodexItem build() {
            if (displayItem == null) throw new IllegalStateException("CodexItem requires a displayItem.");
            return new CodexItem(this);
        }
    }
}
