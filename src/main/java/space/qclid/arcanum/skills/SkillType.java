package space.qclid.arcanum.skills;

import java.util.Optional;

/** The skills a player can level. Pure data: no Bukkit types, so it can be unit tested. */
public enum SkillType {
    RUNNING("running", "Running", "LEATHER_BOOTS"),
    MINING("mining", "Mining", "DIAMOND_PICKAXE"),
    LOGGING("logging", "Logging", "IRON_AXE"),
    FARMING("farming", "Farming", "DIAMOND_HOE"),
    MONSTER_HUNTING("monster_hunting", "Monster Hunting", "IRON_SWORD"),
    FISHING("fishing", "Fishing", "FISHING_ROD"),
    EXCAVATION("excavation", "Excavation", "IRON_SHOVEL"),
    ARCHERY("archery", "Archery", "BOW"),
    ACROBATICS("acrobatics", "Acrobatics", "FEATHER");

    private final String id;
    private final String displayName;
    private final String icon;

    SkillType(String id, String displayName, String icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    /** Stable identifier used in config and data files. */
    public String id() { return id; }

    public String displayName() { return displayName; }

    /** Name of the {@code Material} shown as this skill's icon. */
    public String icon() { return icon; }

    public static Optional<SkillType> fromId(String id) {
        if (id == null) return Optional.empty();
        for (SkillType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return Optional.of(t);
        }
        return Optional.empty();
    }
}
