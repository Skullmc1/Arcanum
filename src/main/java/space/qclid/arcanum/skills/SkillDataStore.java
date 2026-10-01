package space.qclid.arcanum.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Reads and writes {@code skill-data.yml}. Reading never throws and never destroys data: an unreadable
 * file is moved aside, and a file with rows that had to be skipped is copied to a {@code .bak} first.
 */
final class SkillDataStore {

    private final File file;
    private final Logger log;
    private final SkillCurve curve;

    SkillDataStore(File file, Logger log, SkillCurve curve) {
        this.file = file;
        this.log = log;
        this.curve = curve;
    }

    Map<UUID, PlayerSkills> load() {
        Map<UUID, PlayerSkills> players = new HashMap<>();
        if (!file.exists()) return players;

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            File aside = sibling(".corrupt-" + System.currentTimeMillis());
            log.severe("Could not read " + file.getName() + " (" + e.getMessage()
                + "). Keeping it as " + aside.getName() + " and starting empty.");
            if (!file.renameTo(aside)) copyOrWarn(aside);
            return players;
        }

        boolean skipped = false;
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) return players;
        for (String key : root.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                log.warning(file.getName() + ": skipping invalid player id '" + key + "'");
                skipped = true;
                continue;
            }
            ConfigurationSection ps = root.getConfigurationSection(key);
            if (ps == null) {
                log.warning(file.getName() + ": skipping malformed entry for " + key);
                skipped = true;
                continue;
            }
            PlayerSkills skills = new PlayerSkills();
            for (String skillId : ps.getKeys(false)) {
                Optional<SkillType> type = SkillType.fromId(skillId);
                if (type.isEmpty()) {
                    log.warning(file.getName() + ": skipping unknown skill '" + skillId + "'");
                    skipped = true;
                    continue;
                }
                if (!(ps.get(skillId) instanceof Number n)) {
                    log.warning(file.getName() + ": skipping non-numeric XP for " + key + "/" + skillId);
                    skipped = true;
                    continue;
                }
                skills.setXp(type.get(), Math.min(Math.max(0, n.longValue()), curve.maxTotalXp()));
            }
            players.put(id, skills);
        }

        if (skipped) {
            File bak = sibling(".bak-" + System.currentTimeMillis());
            copyOrWarn(bak);
            log.warning("Some rows were skipped; the original file was copied to " + bak.getName());
        }
        return players;
    }

    /** @return true if everything was written. */
    boolean save(Map<UUID, PlayerSkills> players) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerSkills> e : players.entrySet()) {
            for (SkillType t : SkillType.values()) {
                long xp = e.getValue().getXp(t);
                if (xp > 0) yaml.set("players." + e.getKey() + "." + t.id(), xp);
            }
        }
        File tmp = sibling(".tmp");
        try {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            yaml.save(tmp);
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.severe("Could not save " + file.getName() + ": " + e.getMessage());
            return false;
        }
    }

    private File sibling(String suffix) {
        return new File(file.getParentFile(), file.getName() + suffix);
    }

    private void copyOrWarn(File target) {
        try {
            Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.severe("Could not back up " + file.getName() + ": " + e.getMessage());
        }
    }
}
