package space.qclid.dashboard.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.PlayerSettings;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the per-player settings map and handles YAML persistence.
 */
public class DataManager {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();
    private final Set<UUID> dirtyPlayers = new HashSet<>();
    private int saveCounter = 0;
    public final Map<String, org.bukkit.Location> teleportPlates = new HashMap<>();

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

    /** Marks a player's settings as dirty (needs saving). */
    public void markDirty(UUID uuid) {
        dirtyPlayers.add(uuid);
    }

    /** Marks teleport plates as dirty (needs saving). */
    public void markPlatesDirty() {
        dirtyPlayers.add(null); // sentinel: null key means teleport plates changed
    }

    /** Save a single player's settings. */
    public void save(UUID uuid) {
        markDirty(uuid);
        save();
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        YamlConfiguration config;

        // Load existing data to merge with (read once, re-use)
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = new YamlConfiguration();
        }

        // Serialize dirty players only
        for (UUID uuid : dirtyPlayers) {
            if (uuid == null) continue; // skip sentinel
            PlayerSettings s = playerSettings.get(uuid);
            if (s == null) continue;
            String key = uuid.toString();
            config.set(key + ".showXyz",       s.showXyz);
            config.set(key + ".showBiome",      s.showBiome);
            config.set(key + ".showNetherXyz",  s.showNetherXyz);
            config.set(key + ".globalEnabled",  s.globalEnabled);
            if (s.linkedChest != null) config.set(key + ".linkedChest", s.linkedChest);
            if (s.deathChest  != null) config.set(key + ".deathChest",  s.deathChest);
            if (s.deathItems  != null) config.set(key + ".deathItems",  Arrays.asList(s.deathItems));
            if (!s.waypoints.isEmpty()) {
                for (Map.Entry<String, org.bukkit.Location> wp : s.waypoints.entrySet()) {
                    config.set(key + ".waypoints." + wp.getKey(), wp.getValue());
                }
            }
        }

        // Serialize teleport plates if dirty (sentinel null key)
        if (dirtyPlayers.contains(null) && !teleportPlates.isEmpty()) {
            for (Map.Entry<String, org.bukkit.Location> entry : teleportPlates.entrySet()) {
                config.set("teleportPlates." + entry.getKey().replace(".", "[dot]"), entry.getValue());
            }
        }

        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }

        // Full save every 10 saves to clean stale entries
        saveCounter++;
        if (saveCounter >= 10) {
            saveCounter = 0;
            fullSave();
        }

        dirtyPlayers.clear();
    }

    private void fullSave() {
        try {
            File file = new File(plugin.getDataFolder(), "data.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
                String key = entry.getKey().toString();
                PlayerSettings s = entry.getValue();
                config.set(key + ".showXyz",       s.showXyz);
                config.set(key + ".showBiome",      s.showBiome);
                config.set(key + ".showNetherXyz",  s.showNetherXyz);
                config.set(key + ".globalEnabled",  s.globalEnabled);
                if (s.linkedChest != null) config.set(key + ".linkedChest", s.linkedChest);
                if (s.deathChest  != null) config.set(key + ".deathChest",  s.deathChest);
                if (s.deathItems  != null) config.set(key + ".deathItems",  Arrays.asList(s.deathItems));
                if (!s.waypoints.isEmpty()) {
                    for (Map.Entry<String, org.bukkit.Location> wp : s.waypoints.entrySet()) {
                        config.set(key + ".waypoints." + wp.getKey(), wp.getValue());
                    }
                }
            }
            if (!teleportPlates.isEmpty()) {
                for (Map.Entry<String, org.bukkit.Location> entry : teleportPlates.entrySet()) {
                    config.set("teleportPlates." + entry.getKey().replace(".", "[dot]"), entry.getValue());
                }
            }
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection platesSection = config.getConfigurationSection("teleportPlates");
        if (platesSection != null) {
            for (String key : platesSection.getKeys(false)) {
                String originalKey = key.replace("[dot]", ".");
                teleportPlates.put(originalKey, platesSection.getLocation(key));
            }
        }

        for (String uuidStr : config.getKeys(false)) {
            if (uuidStr.equals("teleportPlates")) continue;
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerSettings s = new PlayerSettings();

                s.showXyz       = config.getBoolean(uuidStr + ".showXyz",       true);
                s.showBiome     = config.getBoolean(uuidStr + ".showBiome",      true);
                s.showNetherXyz = config.getBoolean(uuidStr + ".showNetherXyz",  true);
                s.globalEnabled = config.getBoolean(uuidStr + ".globalEnabled",  true);
                s.linkedChest   = config.getLocation(uuidStr + ".linkedChest");
                s.deathChest    = config.getLocation(uuidStr + ".deathChest");

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
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player settings for " + uuidStr + ": " + e.getMessage());
            }
        }
    }
}
