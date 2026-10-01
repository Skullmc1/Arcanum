package space.qclid.arcanum.skills;

import java.util.EnumMap;
import java.util.Map;

/** Total XP per skill for one player. Level is always derived from XP via a {@link SkillCurve}. */
public final class PlayerSkills {

    private final Map<SkillType, Long> xp = new EnumMap<>(SkillType.class);

    public long getXp(SkillType type) {
        return xp.getOrDefault(type, 0L);
    }

    public void setXp(SkillType type, long value) {
        xp.put(type, Math.max(0L, value));
    }

    public int level(SkillType type, SkillCurve curve) {
        return curve.levelForXp(getXp(type));
    }
}
