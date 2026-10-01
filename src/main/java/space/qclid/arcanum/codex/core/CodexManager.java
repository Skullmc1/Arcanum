package space.qclid.arcanum.codex.core;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * Manages per-player unlock state for the Codex.
 * Persists to codex.yml (separate from data.yml).
 */
public class CodexManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<String>> unlocked = new HashMap<>();

    public CodexManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public boolean isUnlocked(UUID uuid, String itemId) {
        Set<String> set = unlocked.get(uuid);
        return set != null && set.contains(itemId);
    }

    public void unlock(UUID uuid, String itemId) {
        unlocked.computeIfAbsent(uuid, k -> new HashSet<>()).add(itemId);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        File file = new File(plugin.getDataFolder(), "codex.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Set<String>> entry : unlocked.entrySet()) {
            config.set(entry.getKey().toString() + ".unlocked", new ArrayList<>(entry.getValue()));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save codex.yml: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "codex.yml");
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<?> list = config.getList(uuidStr + ".unlocked");
                if (list != null) {
                    Set<String> set = new HashSet<>();
                    for (Object o : list) if (o instanceof String s) set.add(s);
                    unlocked.put(uuid, set);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load codex data for " + uuidStr + ": " + e.getMessage());
            }
        }
    }
}
