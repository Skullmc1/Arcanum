package space.qclid.dashboard.codex.core;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A named group of CodexItems shown as one icon in the main Codex GUI. */
public class CodexCategory {

    private final String id;
    private final String parentCategoryId; // e.g., "arcane", "explorer", "machinery"
    private final String displayName;
    private final ItemStack icon;
    private final List<CodexItem> items = new ArrayList<>();

    public CodexCategory(String id, String parentCategoryId, String displayName, ItemStack icon) {
        this.id               = id;
        this.parentCategoryId = parentCategoryId;
        this.displayName      = displayName;
        this.icon             = icon;
    }

    public String           getId()               { return id; }
    public String           getParentCategoryId() { return parentCategoryId; }
    public String           getDisplayName()      { return displayName; }
    public ItemStack        getIcon()             { return icon.clone(); }
    public List<CodexItem>  getItems()            { return Collections.unmodifiableList(items); }

    public void addItem(CodexItem item) { items.add(item); }
}
