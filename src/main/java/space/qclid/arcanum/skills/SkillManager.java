package space.qclid.arcanum.skills;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/**
 * Owns every player's skill XP: awards it, detects level-ups, shows the XP popup and persists to
 * {@code skill-data.yml}. All calls happen on the server thread.
 */
public final class SkillManager {

    private static final long POPUP_MILLIS = 2000;

    private record Popup(SkillType type, long gained, long expiresAt) {}

    private final JavaPlugin plugin;
    private final SkillsConfig config;
    private final SkillCurve curve;
    private final SkillDataStore store;
    private final Map<UUID, PlayerSkills> players = new HashMap<>();
    private final Set<UUID> dirty = new HashSet<>();
    private final Map<UUID, Map<SkillType, Double>> carry = new HashMap<>();
    private final Map<UUID, Popup> popups = new HashMap<>();

    public SkillManager(JavaPlugin plugin, SkillsConfig config, SkillCurve curve) {
        this.plugin = plugin;
        this.config = config;
        this.curve = curve;
        this.store = new SkillDataStore(new File(plugin.getDataFolder(), "skill-data.yml"), plugin.getLogger(), curve);
        players.putAll(store.load());
    }

    public SkillCurve curve() { return curve; }

    public boolean isEnabled(SkillType type) {
        return !config.disabledSkills.contains(type);
    }

    /** Creative/spectator players and disabled worlds never earn XP or perks. */
    public boolean canEarn(Player player) {
        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return false;
        return !config.disabledWorlds.contains(player.getWorld().getName());
    }

    private PlayerSkills skills(UUID id) {
        return players.computeIfAbsent(id, k -> new PlayerSkills());
    }

    public int level(UUID id, SkillType type) {
        return skills(id).level(type, curve);
    }

    public long xp(UUID id, SkillType type) {
        return skills(id).getXp(type);
    }

    /** Awards (possibly fractional) XP. Whole XP is applied; the remainder is carried over. */
    public void addXp(Player player, SkillType type, double amount) {
        if (amount <= 0 || !isEnabled(type) || !canEarn(player)) return;
        UUID id = player.getUniqueId();

        Map<SkillType, Double> c = carry.computeIfAbsent(id, k -> new EnumMap<>(SkillType.class));
        double total = c.getOrDefault(type, 0.0) + amount;
        long whole = (long) Math.floor(total);
        c.put(type, total - whole);
        if (whole <= 0) return;

        PlayerSkills s = skills(id);
        int before = s.level(type, curve);
        long next = curve.clampedAdd(s.getXp(type), whole);
        if (next == s.getXp(type)) return;
        s.setXp(type, next);
        dirty.add(id);

        int after = s.level(type, curve);
        pushPopup(id, type, whole);
        if (after > before) onLevelUp(player, type, after);
    }

    /** Admin: sets total XP directly (clamped to the cap). */
    public void setXp(UUID id, SkillType type, long totalXp) {
        skills(id).setXp(type, Math.min(Math.max(0, totalXp), curve.maxTotalXp()));
        dirty.add(id);
    }

    private void pushPopup(UUID id, SkillType type, long gained) {
        long now = System.currentTimeMillis();
        Popup old = popups.get(id);
        long sum = (old != null && old.type() == type && old.expiresAt() > now) ? old.gained() + gained : gained;
        popups.put(id, new Popup(type, sum, now + POPUP_MILLIS));
    }

    /** MiniMessage text for the action bar while a recent XP gain is showing, otherwise {@code null}. */
    public String popupFor(UUID id) {
        Popup p = popups.get(id);
        if (p == null) return null;
        if (p.expiresAt() < System.currentTimeMillis()) {
            popups.remove(id);
            return null;
        }
        PlayerSkills s = skills(id);
        long xp = s.getXp(p.type());
        int level = curve.levelForXp(xp);
        String progress = level >= curve.maxLevel()
            ? "MAX"
            : curve.xpIntoLevel(xp) + "/" + curve.xpToNext(level);
        return C_GREEN + "+" + p.gained() + " " + toSmallCaps(p.type().displayName())
            + " " + C_GRAY + "(" + progress + ")";
    }

    private void onLevelUp(Player player, SkillType type, int level) {
        player.sendMessage(MM.deserialize(
            C_GOLD + toSmallCaps(type.displayName() + " leveled up to ") + C_YELLOW + level + C_GOLD + "!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    /** Writes all progress to disk if anything changed. */
    public void save() {
        if (dirty.isEmpty()) return;
        if (store.save(players)) dirty.clear();
    }
}
