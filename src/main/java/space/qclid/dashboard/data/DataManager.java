package space.qclid.dashboard.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.PlayerSettings;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owns the per-player settings map and handles YAML persistence.
 */
public class DataManager {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns the settings for a player, creating a default entry if absent. */
    public PlayerSettings getOrCreate(UUID uuid) {
        return playerSettings.computeIfAbsent(uuid, k -> new PlayerSettings());
    }

    /** Returns the settings for a player, or {@code null} if not yet created. */
    public PlayerSettings get(UUID uuid) {
        return playerSettings.get(uuid);
    }

    /** Returns the full map for iteration (e.g. in schedulers). */
    public Map<UUID, PlayerSettings> all() {
        return playerSettings;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
            String uuid = entry.getKey().toString();
            PlayerSettings s = entry.getValue();

            config.set(uuid + ".showXyz",       s.showXyz);
            config.set(uuid + ".showBiome",      s.showBiome);
            config.set(uuid + ".showNetherXyz",  s.showNetherXyz);
            config.set(uuid + ".globalEnabled",  s.globalEnabled);

            if (s.linkedChest != null) config.set(uuid + ".linkedChest", s.linkedChest);
            if (s.deathItems  != null) config.set(uuid + ".deathItems",  Arrays.asList(s.deathItems));

            if (!s.waypoints.isEmpty()) {
                for (Map.Entry<String, org.bukkit.Location> wp : s.waypoints.entrySet()) {
                    config.set(uuid + ".waypoints." + wp.getKey(), wp.getValue());
                }
            }
        }

        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerSettings s = new PlayerSettings();

                s.showXyz       = config.getBoolean(uuidStr + ".showXyz",       true);
                s.showBiome     = config.getBoolean(uuidStr + ".showBiome",      true);
                s.showNetherXyz = config.getBoolean(uuidStr + ".showNetherXyz",  true);
                s.globalEnabled = config.getBoolean(uuidStr + ".globalEnabled",  true);
                s.linkedChest   = config.getLocation(uuidStr + ".linkedChest");

                List<?> deathItemsList = config.getList(uuidStr + ".deathItems");
                if (deathItemsList != null) {
                    s.deathItems = deathItemsList.toArray(new ItemStack[0]);
                }

                ConfigurationSection wpSection = config.getConfigurationSection(uuidStr + ".waypoints");
                if (wpSection != null) {
                    for (String wpName : wpSection.getKeys(false)) {
                        s.waypoints.put(wpName, wpSection.getLocation(wpName));
                    }
                }

                playerSettings.put(uuid, s);
            } catch (Exception ignored) {}
        }
    }
}
