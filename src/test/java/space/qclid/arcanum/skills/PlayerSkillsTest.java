package space.qclid.arcanum.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerSkillsTest {

    private final SkillCurve curve = new SkillCurve(12.0, 1.3, 150);

    @Test
    void unsetSkillHasNoXpAndIsLevelOne() {
        PlayerSkills s = new PlayerSkills();
        assertEquals(0, s.getXp(SkillType.MINING));
        assertEquals(1, s.level(SkillType.MINING, curve));
    }

    @Test
    void negativeXpIsClampedToZero() {
        PlayerSkills s = new PlayerSkills();
        s.setXp(SkillType.RUNNING, -50);
        assertEquals(0, s.getXp(SkillType.RUNNING));
    }

    @Test
    void levelComesFromTheCurve() {
        PlayerSkills s = new PlayerSkills();
        s.setXp(SkillType.MINING, curve.totalXpForLevel(25));
        assertEquals(25, s.level(SkillType.MINING, curve));
        assertEquals(1, s.level(SkillType.RUNNING, curve));
    }

    @Test
    void hugeXpStaysAtMaxLevel() {
        PlayerSkills s = new PlayerSkills();
        s.setXp(SkillType.MINING, Long.MAX_VALUE);
        assertEquals(150, s.level(SkillType.MINING, curve));
    }
}
