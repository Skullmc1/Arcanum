package space.qclid.dashboard.codex.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Central registry that holds all CodexCategories and their CodexItems. */
public class CodexRegistry {

    private final List<CodexCategory> categories = new ArrayList<>();

    public void registerCategory(CodexCategory category) {
        categories.add(category);
    }

    public List<CodexCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /** Returns all sub-categories registered under a specific parent category ID. */
    public List<CodexCategory> getCategoriesForParent(String parentId) {
        return categories.stream()
                .filter(c -> c.getParentCategoryId().equalsIgnoreCase(parentId))
                .toList();
    }

    public CodexCategory getCategory(String id) {
        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
    }

    /** Searches all categories for an item by id. */
    public CodexItem getItem(String itemId) {
        return categories.stream()
                .flatMap(c -> c.getItems().stream())
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElse(null);
    }

    /** Returns the category that contains the given item id, or null. */
    public CodexCategory getCategoryForItem(String itemId) {
        return categories.stream()
                .filter(c -> c.getItems().stream().anyMatch(i -> i.getId().equals(itemId)))
                .findFirst().orElse(null);
    }
}
