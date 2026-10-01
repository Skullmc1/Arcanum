# Skills System — Phase 1 (Framework + Running + Mining) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the skills framework (XP, levels, persistence, `/skills` GUI, admin commands, anti-exploit tracking) with the Running and Mining skills fully working.

**Architecture:** A new `space.qclid.arcanum.skills` package. Pure-Java logic (`SkillCurve`, `PlayerSkills`, `RunningPerks`, `MiningPerks`, `PackedPos`) is unit-tested with JUnit. Bukkit-facing classes (`SkillManager`, `RunningSkill`, `MiningSkill`, `ToolSpeedService`, GUI, `SkillsFeature`) wrap that logic. Each skill implements the `Skill` interface and is a Bukkit `Listener`; `SkillsFeature` wires everything and runs one 5-tick scheduler.

**Tech Stack:** Java 21 bytecode (compiled on JDK 26), Paper API 1.21.1, Gradle Kotlin DSL, JUnit 5, Adventure/MiniMessage, Paper Brigadier commands.

**Spec:** `docs/superpowers/specs/2026-10-01-skills-system-design.md` (Phases 2 and 3 — Logging, Farming, Monster Hunting, Fishing, Excavation, Archery, Acrobatics — get their own plans after this phase is verified in-game.)

## Global Constraints

- Single jar for Minecraft 1.21.1 → 26.3; compile against Paper API `1.21.1-R0.1-SNAPSHOT`, `options.release = 21`.
- Never reference renamed/retyped Bukkit constants directly (`Attribute.*`, `Biome`, ...): go through `space.qclid.arcanum.compat.Compat` or `Keyed`. Do not call methods on types that are class-vs-interface on different versions.
- Package root `space.qclid.arcanum`; skills code lives in `space.qclid.arcanum.skills`. Pure logic classes must not import `org.bukkit.*` (so they run in unit tests).
- Max skill level 150; XP curve `round(a * L^p)`, defaults `a = 12`, `p = 1.3`.
- Running perk tiers: 25 → Speed I after 10 blocks; 50 → Speed II after 5 blocks; 100 → Speed II instantly; 150 → Speed III instantly.
- Mining: virtual Efficiency +1 per 25 levels (max +6, +20% break speed each); virtual Fortune I/II/III at 50/100/150 stacking with the tool; level 150 instant-smelts iron/gold/copper/ancient debris drops; all skipped with Silk Touch.
- Data files: `plugins/Arcanum/skills.yml` (config), `plugins/Arcanum/skill-data.yml` (progress). Key namespace for PDC keys stays `dashboard` via `Compat.key(...)`.
- Creative/spectator players and disabled worlds earn no XP and receive no perks.
- Commit messages end with the `Co-Authored-By` trailer supplied by the session. Build/run commands use the Gradle wrapper (`./gradlew`) from the repo root (`D:\projects\Dashboard`).

## Review Focus

1. **Player-placed ore/stone broken again** → no XP and no boosted drops (tracker; unit-test `PackedPos` wrap, live-test place+break).
2. **Silk Touch pickaxe on an ore** → vanilla silk drop, never fortune/smelt (live checklist item).
3. **Corrupt or hand-edited `skill-data.yml`** (bad UUID, unknown skill id, negative or huge XP) → server still starts, bad rows skipped/clamped, file backed up rather than overwritten.
4. **XP at/above the cap, `/skills set` to level 150, negative XP** → level stays within 1–150, no exception (`SkillCurve` and `PlayerSkills` tests).
5. **Sprint XP cheats:** teleport, boat/horse, elytra, swimming, creative flight → none award XP or start the speed streak (live checklist).

---

## File Structure

**Create**
- `src/test/java/space/qclid/arcanum/skills/SkillCurveTest.java`, `PlayerSkillsTest.java`, `perk/RunningPerksTest.java`, `perk/MiningPerksTest.java`, `PackedPosTest.java`
- `src/main/java/space/qclid/arcanum/skills/SkillType.java` — the nine skills (id, display name, icon material name)
- `.../skills/SkillCurve.java` — XP ↔ level maths
- `.../skills/PlayerSkills.java` — total XP per skill for one player
- `.../skills/perk/Roman.java`, `RunningPerks.java`, `MiningPerks.java` — pure perk rules
- `.../skills/PackedPos.java` — pure chunk-local block position packing
- `.../skills/SkillsConfig.java` + `src/main/resources/skills.yml` — config reader and defaults
- `.../skills/SkillManager.java` — XP, levels, persistence, popups, admin setters
- `.../skills/Skill.java` — skill interface
- `.../skills/PlacedBlockTracker.java` — player-placed block memory (chunk PDC)
- `.../skills/ToolSpeedService.java` — virtual Efficiency via block-break-speed attribute
- `.../skills/skill/RunningSkill.java`, `MiningSkill.java`
- `.../skills/gui/SkillsInventoryHolder.java`, `SkillsGui.java`, `SkillsGuiListener.java`
- `.../skills/SkillsFeature.java` — wiring, commands, schedulers
- `tools/compat-scan.py` — repeatable class/interface compatibility scan (from the Biome fix)

**Modify**
- `build.gradle.kts` — JUnit
- `src/main/java/space/qclid/arcanum/compat/Compat.java` — `BLOCK_BREAK_SPEED`
- `src/main/java/space/qclid/arcanum/feature/ActionBarFeature.java` — skill XP popup
- `src/main/java/space/qclid/arcanum/ArcanumPlugin.java` — create/wire/shutdown `SkillsFeature`
- `docs/superpowers/specs/2026-10-01-skills-system-design.md` — three corrections (Task 1)
- `README.md` — document skills (Task 8)

---

### Task 1: Test infrastructure, `SkillType`, `SkillCurve`, spec corrections

**Files:**
- Modify: `build.gradle.kts`, `docs/superpowers/specs/2026-10-01-skills-system-design.md`
- Create: `src/main/java/space/qclid/arcanum/skills/SkillType.java`, `src/main/java/space/qclid/arcanum/skills/SkillCurve.java`
- Test: `src/test/java/space/qclid/arcanum/skills/SkillCurveTest.java`

**Interfaces:**
- Produces:
  - `enum SkillType { RUNNING, MINING, LOGGING, FARMING, MONSTER_HUNTING, FISHING, EXCAVATION, ARCHERY, ACROBATICS }` with `String id()`, `String displayName()`, `String icon()` (Material name), `static Optional<SkillType> fromId(String)`
  - `SkillCurve(double a, double p, int maxLevel)`: `int maxLevel()`, `long xpToNext(int level)`, `long totalXpForLevel(int level)`, `int levelForXp(long totalXp)`, `double progress(long totalXp)`, `long xpIntoLevel(long totalXp)`, `long maxTotalXp()`

- [ ] **Step 1: Add JUnit to the build**

Edit `build.gradle.kts`: inside the existing `dependencies { ... }` block add the two test lines, and add a `tasks.test` block at the end of the file.

```kotlin
dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
```

```kotlin
tasks.test {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Write the failing test**

Create `src/test/java/space/qclid/arcanum/skills/SkillCurveTest.java`:

```java
package space.qclid.arcanum.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillCurveTest {

    private final SkillCurve curve = new SkillCurve(12.0, 1.3, 150);

    @Test
    void levelOneNeedsNoXp() {
        assertEquals(0, curve.totalXpForLevel(1));
        assertEquals(1, curve.levelForXp(0));
    }

    @Test
    void xpToNextFollowsTheFormula() {
        assertEquals(Math.round(12 * Math.pow(10, 1.3)), curve.xpToNext(10));
    }

    @Test
    void xpToNextIsZeroAtMaxLevel() {
        assertEquals(0, curve.xpToNext(150));
        assertEquals(0, curve.xpToNext(0));
    }

    @Test
    void totalXpIsStrictlyIncreasing() {
        for (int l = 2; l <= 150; l++) {
            assertTrue(curve.totalXpForLevel(l) > curve.totalXpForLevel(l - 1), "level " + l);
        }
    }

    @Test
    void levelForXpRoundTripsAtEveryBoundary() {
        for (int l = 1; l <= 150; l++) {
            long t = curve.totalXpForLevel(l);
            assertEquals(l, curve.levelForXp(t), "exactly at level " + l);
            if (l > 1) assertEquals(l - 1, curve.levelForXp(t - 1), "one xp short of level " + l);
        }
    }

    @Test
    void levelIsClampedToValidRange() {
        assertEquals(150, curve.levelForXp(Long.MAX_VALUE));
        assertEquals(1, curve.levelForXp(-5));
        assertEquals(curve.totalXpForLevel(150), curve.totalXpForLevel(999));
        assertEquals(0, curve.totalXpForLevel(-3));
    }

    @Test
    void progressIsAFractionAndFullAtMax() {
        long half = curve.totalXpForLevel(10) + curve.xpToNext(10) / 2;
        assertEquals(0.5, curve.progress(half), 0.02);
        assertEquals(0.0, curve.progress(curve.totalXpForLevel(10)), 1e-9);
        assertEquals(1.0, curve.progress(curve.maxTotalXp()), 1e-9);
        assertEquals(1.0, curve.progress(Long.MAX_VALUE), 1e-9);
    }

    @Test
    void defaultPacingMatchesTheSpec() {
        long to25 = curve.totalXpForLevel(25);
        long to150 = curve.totalXpForLevel(150);
        assertTrue(to25 >= 6_000 && to25 <= 12_000, "level 25 total was " + to25);
        assertTrue(to150 >= 400_000 && to150 <= 650_000, "level 150 total was " + to150);
        assertEquals(to150, curve.maxTotalXp());
    }

    @Test
    void rejectsInvalidMaxLevel() {
        assertThrows(IllegalArgumentException.class, () -> new SkillCurve(12.0, 1.3, 0));
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew test --tests "space.qclid.arcanum.skills.SkillCurveTest"`
Expected: FAIL — compilation error, `SkillCurve` does not exist.

- [ ] **Step 4: Implement `SkillType`**

Create `src/main/java/space/qclid/arcanum/skills/SkillType.java`:

```java
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
```

- [ ] **Step 5: Implement `SkillCurve`**

Create `src/main/java/space/qclid/arcanum/skills/SkillCurve.java`:

```java
package space.qclid.arcanum.skills;

/**
 * XP ↔ level maths. XP needed to go from level L to L+1 is {@code round(a * L^p)}.
 * Level 1 corresponds to 0 total XP; {@code maxLevel} is the cap.
 */
public final class SkillCurve {

    private final double a;
    private final double p;
    private final int maxLevel;
    /** cumulative[L] = total XP required to reach level L (index 0 unused). */
    private final long[] cumulative;

    public SkillCurve(double a, double p, int maxLevel) {
        if (maxLevel < 1) throw new IllegalArgumentException("maxLevel must be >= 1");
        this.a = a;
        this.p = p;
        this.maxLevel = maxLevel;
        this.cumulative = new long[maxLevel + 1];
        for (int l = 2; l <= maxLevel; l++) {
            cumulative[l] = cumulative[l - 1] + xpToNext(l - 1);
        }
    }

    public int maxLevel() { return maxLevel; }

    /** XP needed to advance from {@code level} to {@code level + 1}; 0 at or beyond the cap. */
    public long xpToNext(int level) {
        if (level < 1 || level >= maxLevel) return 0;
        return Math.max(1, Math.round(a * Math.pow(level, p)));
    }

    /** Total XP at which {@code level} is reached (clamped to 1..maxLevel). */
    public long totalXpForLevel(int level) {
        if (level < 1) return 0;
        return cumulative[Math.min(level, maxLevel)];
    }

    /** Level for a total XP value, clamped to 1..maxLevel. */
    public int levelForXp(long totalXp) {
        if (totalXp <= 0) return 1;
        int lo = 1;
        int hi = maxLevel;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (cumulative[mid] <= totalXp) lo = mid; else hi = mid - 1;
        }
        return lo;
    }

    /** XP earned inside the current level. */
    public long xpIntoLevel(long totalXp) {
        int l = levelForXp(totalXp);
        return Math.max(0, totalXp - cumulative[l]);
    }

    /** Fraction (0..1) of the way to the next level; 1.0 at the cap. */
    public double progress(long totalXp) {
        int l = levelForXp(totalXp);
        if (l >= maxLevel) return 1.0;
        return (double) (totalXp - cumulative[l]) / xpToNext(l);
    }

    public long maxTotalXp() { return cumulative[maxLevel]; }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew test --tests "space.qclid.arcanum.skills.SkillCurveTest"`
Expected: PASS (9 tests).

- [ ] **Step 7: Correct three points in the spec**

In `docs/superpowers/specs/2026-10-01-skills-system-design.md` make these three edits:
1. In section 4, replace the **Placed blocks** bullet's last sentence `Piston movement clears the entry.` with `Blocks moved by pistons lose tracking (known limitation).`
2. In section 4, replace `Running: only counts when the player is sprinting, on the ground, not riding, not gliding, not in water.` with `Running: only counts while sprinting (jumping allowed), not riding, not gliding, not flying, not in water.`
3. In section 2's table, delete the words `fires `SkillLevelUpEvent`, ` from the `SkillManager` row (no custom event is built — YAGNI).

- [ ] **Step 8: Commit**

```bash
git add build.gradle.kts src/main/java/space/qclid/arcanum/skills src/test docs/superpowers/specs
git commit -m "Skills: test infra, SkillType and SkillCurve"
```

---

### Task 2: `PlayerSkills` and the pure perk rules

**Files:**
- Create: `src/main/java/space/qclid/arcanum/skills/PlayerSkills.java`, `src/main/java/space/qclid/arcanum/skills/perk/Roman.java`, `src/main/java/space/qclid/arcanum/skills/perk/RunningPerks.java`, `src/main/java/space/qclid/arcanum/skills/perk/MiningPerks.java`
- Test: `src/test/java/space/qclid/arcanum/skills/PlayerSkillsTest.java`, `src/test/java/space/qclid/arcanum/skills/perk/RunningPerksTest.java`, `src/test/java/space/qclid/arcanum/skills/perk/MiningPerksTest.java`

**Interfaces:**
- Consumes: `SkillType`, `SkillCurve` from Task 1.
- Produces:
  - `PlayerSkills`: `long getXp(SkillType)`, `void setXp(SkillType, long)` (clamps ≥ 0), `int level(SkillType, SkillCurve)`
  - `Roman.roman(int)` → `"I".."X"` (else the number as text)
  - `RunningPerks(List<Tier>)`, `record Tier(int level, int amplifier, int blocks)`, `static List<Tier> DEFAULT_TIERS`, `static RunningPerks defaults()`, `Optional<Tier> tierFor(int level)`, `Optional<Tier> nextTier(int level)`, `static String describe(Tier)`, `String nextPerk(int level)` (nullable)
  - `MiningPerks(int efficiencyEvery, int maxEfficiency, int[] fortuneLevels, int smeltLevel)`, `static MiningPerks defaults()`, `int efficiencyBonus(int)`, `int fortuneBonus(int)`, `boolean instantSmelt(int)`, `int effectiveFortune(int toolFortune, int level)`, `double breakSpeedBonus(int level)`, `String nextPerk(int level)` (nullable)

- [ ] **Step 1: Write the failing tests**

`src/test/java/space/qclid/arcanum/skills/PlayerSkillsTest.java`:

```java
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
```

`src/test/java/space/qclid/arcanum/skills/perk/RunningPerksTest.java`:

```java
package space.qclid.arcanum.skills.perk;

import org.junit.jupiter.api.Test;
import space.qclid.arcanum.skills.perk.RunningPerks.Tier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RunningPerksTest {

    private final RunningPerks perks = RunningPerks.defaults();

    @Test
    void noPerkBelowLevel25() {
        assertTrue(perks.tierFor(1).isEmpty());
        assertTrue(perks.tierFor(24).isEmpty());
    }

    @Test
    void tierBoundariesMatchTheSpec() {
        assertEquals(new Tier(25, 0, 10), perks.tierFor(25).orElseThrow());
        assertEquals(new Tier(25, 0, 10), perks.tierFor(49).orElseThrow());
        assertEquals(new Tier(50, 1, 5), perks.tierFor(50).orElseThrow());
        assertEquals(new Tier(50, 1, 5), perks.tierFor(99).orElseThrow());
        assertEquals(new Tier(100, 1, 0), perks.tierFor(100).orElseThrow());
        assertEquals(new Tier(150, 2, 0), perks.tierFor(150).orElseThrow());
    }

    @Test
    void nextTierLooksAhead() {
        assertEquals(25, perks.nextTier(0).orElseThrow().level());
        assertEquals(50, perks.nextTier(25).orElseThrow().level());
        assertTrue(perks.nextTier(150).isEmpty());
    }

    @Test
    void unsortedTiersAreSorted() {
        RunningPerks p = new RunningPerks(List.of(new Tier(100, 1, 0), new Tier(25, 0, 10)));
        assertEquals(25, p.tierFor(60).orElseThrow().level());
        assertEquals(100, p.tierFor(120).orElseThrow().level());
    }

    @Test
    void describesTiersForTheGui() {
        assertEquals("Speed I after sprinting 10 blocks", RunningPerks.describe(new Tier(25, 0, 10)));
        assertEquals("Speed III as soon as you sprint", RunningPerks.describe(new Tier(150, 2, 0)));
    }

    @Test
    void nextPerkTextOrNullAtMax() {
        assertEquals("Level 25: Speed I after sprinting 10 blocks", perks.nextPerk(1));
        assertNull(perks.nextPerk(150));
    }
}
```

`src/test/java/space/qclid/arcanum/skills/perk/MiningPerksTest.java`:

```java
package space.qclid.arcanum.skills.perk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiningPerksTest {

    private final MiningPerks perks = MiningPerks.defaults();

    @Test
    void efficiencyGrowsEvery25LevelsAndCapsAtSix() {
        assertEquals(0, perks.efficiencyBonus(1));
        assertEquals(0, perks.efficiencyBonus(24));
        assertEquals(1, perks.efficiencyBonus(25));
        assertEquals(2, perks.efficiencyBonus(50));
        assertEquals(6, perks.efficiencyBonus(150));
        assertEquals(6, perks.efficiencyBonus(400));
    }

    @Test
    void fortuneUnlocksAt50100150() {
        assertEquals(0, perks.fortuneBonus(49));
        assertEquals(1, perks.fortuneBonus(50));
        assertEquals(1, perks.fortuneBonus(99));
        assertEquals(2, perks.fortuneBonus(100));
        assertEquals(3, perks.fortuneBonus(150));
    }

    @Test
    void instantSmeltOnlyAtMaxLevel() {
        assertFalse(perks.instantSmelt(149));
        assertTrue(perks.instantSmelt(150));
    }

    @Test
    void virtualFortuneStacksWithTheTool() {
        assertEquals(3, perks.effectiveFortune(3, 1));
        assertEquals(4, perks.effectiveFortune(3, 50));
        assertEquals(6, perks.effectiveFortune(3, 150));
        assertEquals(0, perks.effectiveFortune(0, 10));
    }

    @Test
    void breakSpeedBonusIsTwentyPercentPerEfficiencyLevel() {
        assertEquals(0.0, perks.breakSpeedBonus(10), 1e-9);
        assertEquals(0.2, perks.breakSpeedBonus(25), 1e-9);
        assertEquals(1.2, perks.breakSpeedBonus(150), 1e-9);
    }

    @Test
    void nextPerkJoinsPerksThatShareALevel() {
        assertEquals("Level 25: Efficiency +1", perks.nextPerk(1));
        assertEquals("Level 50: Efficiency +2, Fortune I", perks.nextPerk(49));
        assertEquals("Level 150: Efficiency +6, Fortune III, Instant ore smelting", perks.nextPerk(149));
        assertNull(perks.nextPerk(150));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew test`
Expected: FAIL — compilation errors (`PlayerSkills`, `RunningPerks`, `MiningPerks` missing).

- [ ] **Step 3: Implement `PlayerSkills`**

`src/main/java/space/qclid/arcanum/skills/PlayerSkills.java`:

```java
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
```

- [ ] **Step 4: Implement `Roman`**

`src/main/java/space/qclid/arcanum/skills/perk/Roman.java`:

```java
package space.qclid.arcanum.skills.perk;

/** Tiny roman-numeral helper (1..10) that does not depend on Bukkit. */
final class Roman {

    private static final String[] NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private Roman() {}

    static String roman(int n) {
        return n >= 1 && n < NUMERALS.length ? NUMERALS[n] : Integer.toString(n);
    }
}
```

- [ ] **Step 5: Implement `RunningPerks`**

`src/main/java/space/qclid/arcanum/skills/perk/RunningPerks.java`:

```java
package space.qclid.arcanum.skills.perk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Speed tiers unlocked by the Running skill. */
public final class RunningPerks {

    /** @param level   skill level that unlocks the tier
     *  @param amplifier Speed potion amplifier (0 = Speed I)
     *  @param blocks  blocks sprinted continuously before the effect starts (0 = immediately) */
    public record Tier(int level, int amplifier, int blocks) {}

    public static final List<Tier> DEFAULT_TIERS = List.of(
        new Tier(25, 0, 10),
        new Tier(50, 1, 5),
        new Tier(100, 1, 0),
        new Tier(150, 2, 0));

    private final List<Tier> tiers;

    public RunningPerks(List<Tier> tiers) {
        List<Tier> sorted = new ArrayList<>(tiers);
        sorted.sort(Comparator.comparingInt(Tier::level));
        this.tiers = List.copyOf(sorted);
    }

    public static RunningPerks defaults() {
        return new RunningPerks(DEFAULT_TIERS);
    }

    /** The highest tier whose level is at or below {@code level}. */
    public Optional<Tier> tierFor(int level) {
        Tier best = null;
        for (Tier t : tiers) {
            if (t.level() <= level) best = t;
        }
        return Optional.ofNullable(best);
    }

    /** The first tier strictly above {@code level}. */
    public Optional<Tier> nextTier(int level) {
        for (Tier t : tiers) {
            if (t.level() > level) return Optional.of(t);
        }
        return Optional.empty();
    }

    public static String describe(Tier t) {
        String speed = "Speed " + Roman.roman(t.amplifier() + 1);
        return t.blocks() <= 0
            ? speed + " as soon as you sprint"
            : speed + " after sprinting " + t.blocks() + " blocks";
    }

    /** Text for the GUI, or {@code null} when every tier is unlocked. */
    public String nextPerk(int level) {
        return nextTier(level).map(t -> "Level " + t.level() + ": " + describe(t)).orElse(null);
    }
}
```

- [ ] **Step 6: Implement `MiningPerks`**

`src/main/java/space/qclid/arcanum/skills/perk/MiningPerks.java`:

```java
package space.qclid.arcanum.skills.perk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Virtual Efficiency, Fortune and instant smelting for the Mining skill. */
public final class MiningPerks {

    private final int efficiencyEvery;
    private final int maxEfficiency;
    private final int[] fortuneLevels;
    private final int smeltLevel;
    private final TreeMap<Integer, List<String>> milestones = new TreeMap<>();

    public MiningPerks(int efficiencyEvery, int maxEfficiency, int[] fortuneLevels, int smeltLevel) {
        this.efficiencyEvery = Math.max(1, efficiencyEvery);
        this.maxEfficiency = Math.max(0, maxEfficiency);
        this.fortuneLevels = fortuneLevels.clone();
        this.smeltLevel = smeltLevel;
        for (int k = 1; k <= this.maxEfficiency; k++) {
            add(k * this.efficiencyEvery, "Efficiency +" + k);
        }
        for (int i = 0; i < this.fortuneLevels.length; i++) {
            add(this.fortuneLevels[i], "Fortune " + Roman.roman(i + 1));
        }
        add(this.smeltLevel, "Instant ore smelting");
    }

    public static MiningPerks defaults() {
        return new MiningPerks(25, 6, new int[] {50, 100, 150}, 150);
    }

    private void add(int level, String text) {
        milestones.computeIfAbsent(level, k -> new ArrayList<>()).add(text);
    }

    /** Virtual Efficiency levels at {@code level}. */
    public int efficiencyBonus(int level) {
        return Math.min(maxEfficiency, Math.max(0, level) / efficiencyEvery);
    }

    /** Virtual Fortune levels at {@code level}. */
    public int fortuneBonus(int level) {
        int n = 0;
        for (int l : fortuneLevels) {
            if (l <= level) n++;
        }
        return n;
    }

    public boolean instantSmelt(int level) {
        return level >= smeltLevel;
    }

    /** Fortune to use for drops: the tool's own level plus the virtual bonus. */
    public int effectiveFortune(int toolFortune, int level) {
        return Math.max(0, toolFortune) + fortuneBonus(level);
    }

    /** Amount added (as a fraction of base) to the block-break-speed attribute. */
    public double breakSpeedBonus(int level) {
        return 0.2 * efficiencyBonus(level);
    }

    /** Text for the GUI, or {@code null} when everything is unlocked. */
    public String nextPerk(int level) {
        Map.Entry<Integer, List<String>> e = milestones.higherEntry(level);
        if (e == null) return null;
        return "Level " + e.getKey() + ": " + String.join(", ", e.getValue());
    }
}
```

- [ ] **Step 7: Run all tests**

Run: `./gradlew test`
Expected: PASS (SkillCurveTest, PlayerSkillsTest, RunningPerksTest, MiningPerksTest).

- [ ] **Step 8: Commit**

```bash
git add src/main/java/space/qclid/arcanum/skills src/test
git commit -m "Skills: PlayerSkills and pure Running/Mining perk rules"
```

---

### Task 3: `PackedPos` and `PlacedBlockTracker`

**Files:**
- Create: `src/main/java/space/qclid/arcanum/skills/PackedPos.java`, `src/main/java/space/qclid/arcanum/skills/PlacedBlockTracker.java`
- Test: `src/test/java/space/qclid/arcanum/skills/PackedPosTest.java`

**Interfaces:**
- Produces:
  - `PackedPos.pack(int x, int y, int z)` → `long`, chunk-local (`x & 15`, `z & 15`, `y` offset by 2048)
  - `PlacedBlockTracker()` (a `Listener`; register it with the plugin): `void trackWhen(Predicate<Material> rule)`, `boolean consumeIfPlaced(Block block)` (true and removes the entry if the player placed it)

- [ ] **Step 1: Write the failing test**

`src/test/java/space/qclid/arcanum/skills/PackedPosTest.java`:

```java
package space.qclid.arcanum.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedPosTest {

    @Test
    void sameChunkLocalPositionPacksEqually() {
        assertEquals(PackedPos.pack(3, 64, 5), PackedPos.pack(3 + 16, 64, 5 + 32));
    }

    @Test
    void negativeCoordinatesWrapIntoTheChunk() {
        assertEquals(PackedPos.pack(15, 10, 14), PackedPos.pack(-1, 10, -2));
    }

    @Test
    void differentPositionsPackDifferently() {
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(2, 64, 1));
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(1, 64, 2));
        assertNotEquals(PackedPos.pack(1, 64, 1), PackedPos.pack(1, 65, 1));
    }

    @Test
    void deepAndHighYValuesStayDistinctAndNonNegative() {
        long deep = PackedPos.pack(0, -64, 0);
        long high = PackedPos.pack(0, 319, 0);
        assertTrue(deep >= 0);
        assertTrue(high >= 0);
        assertNotEquals(deep, high);
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew test --tests "space.qclid.arcanum.skills.PackedPosTest"`
Expected: FAIL — `PackedPos` does not exist.

- [ ] **Step 3: Implement `PackedPos`**

`src/main/java/space/qclid/arcanum/skills/PackedPos.java`:

```java
package space.qclid.arcanum.skills;

/** Packs a block position into a chunk-local long (so it can live in a chunk's persistent data). */
public final class PackedPos {

    private static final int Y_OFFSET = 2048;

    private PackedPos() {}

    public static long pack(int x, int y, int z) {
        return ((long) (y + Y_OFFSET) << 8) | ((long) (x & 15) << 4) | (long) (z & 15);
    }
}
```

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew test --tests "space.qclid.arcanum.skills.PackedPosTest"`
Expected: PASS.

- [ ] **Step 5: Implement `PlacedBlockTracker`**

`src/main/java/space/qclid/arcanum/skills/PlacedBlockTracker.java`:

```java
package space.qclid.arcanum.skills;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import space.qclid.arcanum.compat.Compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Remembers blocks players placed (only block types a skill rewards), stored in the
 * chunk's persistent data, so placing and re-breaking a block cannot farm XP.
 */
public final class PlacedBlockTracker implements Listener {

    private static final NamespacedKey KEY = Compat.key("placed_blocks");

    private final List<Predicate<Material>> rules = new ArrayList<>();

    /** Adds a rule: blocks of any type matching a rule are tracked when placed. */
    public void trackWhen(Predicate<Material> rule) {
        rules.add(rule);
    }

    private boolean isTrackedType(Material material) {
        for (Predicate<Material> rule : rules) {
            if (rule.test(material)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (!isTrackedType(block.getType())) return;
        add(block);
    }

    /** @return true (and forgets the block) if a player placed this block. */
    public boolean consumeIfPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        long[] stored = pdc.get(KEY, PersistentDataType.LONG_ARRAY);
        if (stored == null || stored.length == 0) return false;

        long packed = PackedPos.pack(block.getX(), block.getY(), block.getZ());
        int index = -1;
        for (int i = 0; i < stored.length; i++) {
            if (stored[i] == packed) { index = i; break; }
        }
        if (index < 0) return false;

        if (stored.length == 1) {
            pdc.remove(KEY);
        } else {
            long[] next = new long[stored.length - 1];
            System.arraycopy(stored, 0, next, 0, index);
            System.arraycopy(stored, index + 1, next, index, stored.length - index - 1);
            pdc.set(KEY, PersistentDataType.LONG_ARRAY, next);
        }
        return true;
    }

    private void add(Block block) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        long[] stored = pdc.get(KEY, PersistentDataType.LONG_ARRAY);
        long packed = PackedPos.pack(block.getX(), block.getY(), block.getZ());
        if (stored == null) {
            pdc.set(KEY, PersistentDataType.LONG_ARRAY, new long[] {packed});
            return;
        }
        for (long l : stored) {
            if (l == packed) return;
        }
        long[] next = new long[stored.length + 1];
        System.arraycopy(stored, 0, next, 0, stored.length);
        next[stored.length] = packed;
        pdc.set(KEY, PersistentDataType.LONG_ARRAY, next);
    }
}
```

- [ ] **Step 6: Compile**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/space/qclid/arcanum/skills src/test
git commit -m "Skills: PackedPos and PlacedBlockTracker"
```

---

### Task 4: Config, `SkillManager`, `Skill` interface

**Files:**
- Create: `src/main/resources/skills.yml`, `src/main/java/space/qclid/arcanum/skills/SkillsConfig.java`, `src/main/java/space/qclid/arcanum/skills/Skill.java`, `src/main/java/space/qclid/arcanum/skills/SkillManager.java`

**Interfaces:**
- Consumes: `SkillType`, `SkillCurve`, `PlayerSkills`, `RunningPerks`, `MiningPerks`.
- Produces:
  - `SkillsConfig` public final fields: `double curveA, curveP`, `int maxLevel`, `double runningXpPerBlock`, `Map<String, Double> miningXp`, `RunningPerks running`, `MiningPerks mining`, `Set<String> disabledWorlds`, `Set<SkillType> disabledSkills`; `static SkillsConfig load(ConfigurationSection section, Logger log)`
  - `interface Skill extends Listener { SkillType type(); String nextPerk(int level); default void tick(Player player) {} }`
  - `SkillManager(JavaPlugin plugin, SkillsConfig config, SkillCurve curve)`: `SkillCurve curve()`, `boolean isEnabled(SkillType)`, `boolean canEarn(Player)`, `int level(UUID, SkillType)`, `long xp(UUID, SkillType)`, `void addXp(Player, SkillType, double)`, `void setXp(UUID, SkillType, long)`, `String popupFor(UUID)` (MiniMessage string or null), `void save()`

- [ ] **Step 1: Write the default config**

`src/main/resources/skills.yml`:

```yaml
# Arcanum skills configuration. Delete this file to regenerate the defaults.

curve:
  a: 12.0          # XP from level L to L+1 is round(a * L^p)
  p: 1.3
  max-level: 150

disabled-worlds: []   # e.g. [world_the_end]
disabled-skills: []   # skill ids, e.g. [fishing]

xp:
  running-per-block: 0.3
  mining:             # XP per block, keyed by Material name
    STONE: 1
    DEEPSLATE: 1
    GRANITE: 1
    DIORITE: 1
    ANDESITE: 1
    TUFF: 1
    NETHERRACK: 1
    BASALT: 1
    BLACKSTONE: 1
    END_STONE: 1
    COAL_ORE: 8
    DEEPSLATE_COAL_ORE: 8
    COPPER_ORE: 10
    DEEPSLATE_COPPER_ORE: 10
    IRON_ORE: 15
    DEEPSLATE_IRON_ORE: 15
    REDSTONE_ORE: 12
    DEEPSLATE_REDSTONE_ORE: 12
    LAPIS_ORE: 12
    DEEPSLATE_LAPIS_ORE: 12
    GOLD_ORE: 25
    DEEPSLATE_GOLD_ORE: 25
    NETHER_GOLD_ORE: 10
    NETHER_QUARTZ_ORE: 10
    EMERALD_ORE: 40
    DEEPSLATE_EMERALD_ORE: 40
    DIAMOND_ORE: 60
    DEEPSLATE_DIAMOND_ORE: 60
    ANCIENT_DEBRIS: 100

perks:
  running:
    tiers:            # highest tier at or below your level applies
      - {level: 25, amplifier: 0, blocks: 10}
      - {level: 50, amplifier: 1, blocks: 5}
      - {level: 100, amplifier: 1, blocks: 0}
      - {level: 150, amplifier: 2, blocks: 0}
  mining:
    efficiency-every: 25   # +1 virtual Efficiency per this many levels
    max-efficiency: 6
    fortune-levels: [50, 100, 150]
    smelt-level: 150
```

- [ ] **Step 2: Implement `SkillsConfig`**

`src/main/java/space/qclid/arcanum/skills/SkillsConfig.java`:

```java
package space.qclid.arcanum.skills;

import org.bukkit.configuration.ConfigurationSection;
import space.qclid.arcanum.skills.perk.MiningPerks;
import space.qclid.arcanum.skills.perk.RunningPerks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/** Values read from {@code skills.yml}; every field falls back to a safe default. */
public final class SkillsConfig {

    private static final Map<String, Double> DEFAULT_MINING_XP = defaultMiningXp();

    public final double curveA;
    public final double curveP;
    public final int maxLevel;
    public final double runningXpPerBlock;
    public final Map<String, Double> miningXp;
    public final RunningPerks running;
    public final MiningPerks mining;
    public final Set<String> disabledWorlds;
    public final Set<SkillType> disabledSkills;

    private SkillsConfig(double curveA, double curveP, int maxLevel, double runningXpPerBlock,
                         Map<String, Double> miningXp, RunningPerks running, MiningPerks mining,
                         Set<String> disabledWorlds, Set<SkillType> disabledSkills) {
        this.curveA = curveA;
        this.curveP = curveP;
        this.maxLevel = maxLevel;
        this.runningXpPerBlock = runningXpPerBlock;
        this.miningXp = miningXp;
        this.running = running;
        this.mining = mining;
        this.disabledWorlds = disabledWorlds;
        this.disabledSkills = disabledSkills;
    }

    public static SkillsConfig load(ConfigurationSection s, Logger log) {
        double a = s.getDouble("curve.a", 12.0);
        double p = s.getDouble("curve.p", 1.3);
        int max = s.getInt("curve.max-level", 150);
        if (a <= 0 || p < 0 || max < 1) {
            log.warning("Invalid curve in skills.yml, using defaults.");
            a = 12.0;
            p = 1.3;
            max = 150;
        }

        Map<String, Double> miningXp = new HashMap<>();
        ConfigurationSection mx = s.getConfigurationSection("xp.mining");
        if (mx != null) {
            for (String key : mx.getKeys(false)) {
                miningXp.put(key.toUpperCase(), Math.max(0.0, mx.getDouble(key)));
            }
        }
        if (miningXp.isEmpty()) miningXp.putAll(DEFAULT_MINING_XP);

        Set<SkillType> disabled = new HashSet<>();
        for (String id : s.getStringList("disabled-skills")) {
            SkillType.fromId(id).ifPresentOrElse(disabled::add,
                () -> log.warning("Unknown skill id in disabled-skills: " + id));
        }

        int[] fortune = s.getIntegerList("perks.mining.fortune-levels").stream().mapToInt(Integer::intValue).toArray();
        if (fortune.length == 0) fortune = new int[] {50, 100, 150};

        return new SkillsConfig(
            a, p, max,
            Math.max(0.0, s.getDouble("xp.running-per-block", 0.3)),
            Map.copyOf(miningXp),
            new RunningPerks(readTiers(s, log)),
            new MiningPerks(
                s.getInt("perks.mining.efficiency-every", 25),
                s.getInt("perks.mining.max-efficiency", 6),
                fortune,
                s.getInt("perks.mining.smelt-level", 150)),
            new HashSet<>(s.getStringList("disabled-worlds")),
            disabled);
    }

    private static List<RunningPerks.Tier> readTiers(ConfigurationSection s, Logger log) {
        List<RunningPerks.Tier> tiers = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("perks.running.tiers")) {
            try {
                tiers.add(new RunningPerks.Tier(num(m, "level"), num(m, "amplifier"), num(m, "blocks")));
            } catch (RuntimeException e) {
                log.warning("Invalid running tier in skills.yml: " + m);
            }
        }
        return tiers.isEmpty() ? RunningPerks.DEFAULT_TIERS : tiers;
    }

    private static int num(Map<?, ?> m, String key) {
        return ((Number) m.get(key)).intValue();
    }

    private static Map<String, Double> defaultMiningXp() {
        Map<String, Double> m = new HashMap<>();
        for (String n : new String[] {"STONE", "DEEPSLATE", "GRANITE", "DIORITE", "ANDESITE", "TUFF",
                "NETHERRACK", "BASALT", "BLACKSTONE", "END_STONE"}) m.put(n, 1.0);
        m.put("COAL_ORE", 8.0);        m.put("DEEPSLATE_COAL_ORE", 8.0);
        m.put("COPPER_ORE", 10.0);     m.put("DEEPSLATE_COPPER_ORE", 10.0);
        m.put("IRON_ORE", 15.0);       m.put("DEEPSLATE_IRON_ORE", 15.0);
        m.put("REDSTONE_ORE", 12.0);   m.put("DEEPSLATE_REDSTONE_ORE", 12.0);
        m.put("LAPIS_ORE", 12.0);      m.put("DEEPSLATE_LAPIS_ORE", 12.0);
        m.put("GOLD_ORE", 25.0);       m.put("DEEPSLATE_GOLD_ORE", 25.0);
        m.put("NETHER_GOLD_ORE", 10.0); m.put("NETHER_QUARTZ_ORE", 10.0);
        m.put("EMERALD_ORE", 40.0);    m.put("DEEPSLATE_EMERALD_ORE", 40.0);
        m.put("DIAMOND_ORE", 60.0);    m.put("DEEPSLATE_DIAMOND_ORE", 60.0);
        m.put("ANCIENT_DEBRIS", 100.0);
        return m;
    }
}
```

- [ ] **Step 3: Implement the `Skill` interface**

`src/main/java/space/qclid/arcanum/skills/Skill.java`:

```java
package space.qclid.arcanum.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/** One skill: awards XP, applies perks, and describes its next unlock for the GUI. */
public interface Skill extends Listener {

    SkillType type();

    /** Text describing the next perk above {@code level}, or {@code null} when all are unlocked. */
    String nextPerk(int level);

    /** Called every 5 ticks for each online player (only while the skill is enabled). */
    default void tick(Player player) {}
}
```

- [ ] **Step 4: Implement `SkillManager`**

`src/main/java/space/qclid/arcanum/skills/SkillManager.java`:

```java
package space.qclid.arcanum.skills;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
    private final Map<UUID, PlayerSkills> players = new HashMap<>();
    private final Set<UUID> dirty = new HashSet<>();
    private final Map<UUID, Map<SkillType, Double>> carry = new HashMap<>();
    private final Map<UUID, Popup> popups = new HashMap<>();

    public SkillManager(JavaPlugin plugin, SkillsConfig config, SkillCurve curve) {
        this.plugin = plugin;
        this.config = config;
        this.curve = curve;
        load();
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

        Map<SkillType, Double> c = carry.computeIfAbsent(id, k -> new java.util.EnumMap<>(SkillType.class));
        double total = c.getOrDefault(type, 0.0) + amount;
        long whole = (long) Math.floor(total);
        c.put(type, total - whole);
        if (whole <= 0) return;

        PlayerSkills s = skills(id);
        int before = s.level(type, curve);
        long next = Math.min(curve.maxTotalXp(), s.getXp(type) + whole);
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

    private File dataFile() {
        return new File(plugin.getDataFolder(), "skill-data.yml");
    }

    private void load() {
        File file = dataFile();
        if (!file.exists()) return;

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            File backup = new File(plugin.getDataFolder(), "skill-data.yml.corrupt-" + System.currentTimeMillis());
            plugin.getLogger().severe("Could not read skill-data.yml (" + e.getMessage()
                + "). Moved it to " + backup.getName() + " and starting empty.");
            if (!file.renameTo(backup)) {
                plugin.getLogger().severe("Could not back up the corrupt skill-data.yml.");
            }
            return;
        }

        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("skill-data.yml: skipping invalid player id '" + key + "'");
                continue;
            }
            ConfigurationSection ps = root.getConfigurationSection(key);
            if (ps == null) continue;
            PlayerSkills s = new PlayerSkills();
            for (String skillId : ps.getKeys(false)) {
                Optional<SkillType> type = SkillType.fromId(skillId);
                if (type.isEmpty()) {
                    plugin.getLogger().warning("skill-data.yml: skipping unknown skill '" + skillId + "'");
                    continue;
                }
                s.setXp(type.get(), Math.min(Math.max(0, ps.getLong(skillId)), curve.maxTotalXp()));
            }
            players.put(id, s);
        }
    }

    /** Writes all progress to disk if anything changed. */
    public void save() {
        if (dirty.isEmpty()) return;
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerSkills> e : players.entrySet()) {
            for (SkillType t : SkillType.values()) {
                long xp = e.getValue().getXp(t);
                if (xp > 0) yaml.set("players." + e.getKey() + "." + t.id(), xp);
            }
        }
        File file = dataFile();
        File tmp = new File(plugin.getDataFolder(), "skill-data.yml.tmp");
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(tmp);
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            dirty.clear();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save skill-data.yml: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 5: Compile**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL. If the compiler flags `Sound.ENTITY_PLAYER_LEVELUP`, check the existing code's Sound usage (`grep -rn "Sound\." src/main/java | head`) and use the same constant style.

- [ ] **Step 6: Commit**

```bash
git add src/main
git commit -m "Skills: config, Skill interface and SkillManager"
```

---

### Task 5: `RunningSkill`

**Files:**
- Create: `src/main/java/space/qclid/arcanum/skills/skill/RunningSkill.java`

**Interfaces:**
- Consumes: `Skill`, `SkillManager`, `SkillsConfig`, `RunningPerks`.
- Produces: `RunningSkill(SkillManager manager, SkillsConfig config)` implementing `Skill` (type `RUNNING`).

- [ ] **Step 1: Implement `RunningSkill`**

`src/main/java/space/qclid/arcanum/skills/skill/RunningSkill.java`:

```java
package space.qclid.arcanum.skills.skill;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;
import space.qclid.arcanum.skills.SkillsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Running: sprinting earns XP per block and, at higher levels, grants Speed after a streak of
 * continuous sprinting. Sampled from the plugin's 5-tick scheduler.
 */
public final class RunningSkill implements Skill {

    /** Largest horizontal move accepted in one sample; anything bigger is a teleport. */
    private static final double MAX_STEP = 4.0;
    private static final int EFFECT_TICKS = 30;

    private final SkillManager manager;
    private final SkillsConfig config;
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final Map<UUID, Double> streak = new HashMap<>();

    public RunningSkill(SkillManager manager, SkillsConfig config) {
        this.manager = manager;
        this.config = config;
    }

    @Override
    public SkillType type() {
        return SkillType.RUNNING;
    }

    @Override
    public String nextPerk(int level) {
        return config.running.nextPerk(level);
    }

    @Override
    public void tick(Player player) {
        UUID id = player.getUniqueId();
        Location now = player.getLocation();
        Location prev = lastLocation.put(id, now.clone());

        boolean sprinting = manager.canEarn(player)
            && player.isSprinting()
            && !player.isInsideVehicle()
            && !player.isGliding()
            && !player.isFlying()
            && !player.isInWater();
        if (!sprinting || prev == null || !now.getWorld().equals(prev.getWorld())) {
            streak.remove(id);
            return;
        }

        double dx = now.getX() - prev.getX();
        double dz = now.getZ() - prev.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > MAX_STEP) {
            streak.remove(id);
            return;
        }

        double run = streak.merge(id, dist, Double::sum);
        manager.addXp(player, SkillType.RUNNING, dist * config.runningXpPerBlock);

        int level = manager.level(id, SkillType.RUNNING);
        config.running.tierFor(level).ifPresent(tier -> {
            if (run >= tier.blocks()) applySpeed(player, tier.amplifier());
        });
    }

    private void applySpeed(Player player, int amplifier) {
        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        if (current != null && current.getAmplifier() > amplifier) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_TICKS, amplifier, true, false, true));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        lastLocation.remove(id);
        streak.remove(id);
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main
git commit -m "Skills: Running skill"
```

---

### Task 6: `Compat.BLOCK_BREAK_SPEED`, `ToolSpeedService`, `MiningSkill`

**Files:**
- Modify: `src/main/java/space/qclid/arcanum/compat/Compat.java`
- Create: `src/main/java/space/qclid/arcanum/skills/ToolSpeedService.java`, `src/main/java/space/qclid/arcanum/skills/skill/MiningSkill.java`

**Interfaces:**
- Consumes: `SkillManager`, `SkillsConfig`, `MiningPerks`, `PlacedBlockTracker`, `Skill`.
- Produces:
  - `Compat.BLOCK_BREAK_SPEED` (`Attribute`, may be `null` if the server lacks it)
  - `ToolSpeedService(SkillManager)` (a `Listener`): `void register(SkillType type, Predicate<Material> tool, IntToDoubleFunction bonusByLevel)`, `void update(Player)`, `void clear(Player)`
  - `MiningSkill(SkillManager manager, SkillsConfig config, PlacedBlockTracker tracker)` implementing `Skill`; `static boolean isPickaxe(Material)`

- [ ] **Step 1: Add the attribute to `Compat`**

In `Compat.java`, add the constant under `ARMOR`:

```java
    /** Block-break-speed attribute ("player.block_break_speed" on 1.21.1, "block_break_speed" later); may be null. */
    public static final Attribute BLOCK_BREAK_SPEED = optionalAttribute("block_break_speed");
```

and add this method next to `attribute(...)`:

```java
    private static Attribute optionalAttribute(String name) {
        for (String prefix : new String[] {"", "player.", "generic."}) {
            Attribute a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(prefix + name));
            if (a != null) return a;
        }
        return null;
    }
```

- [ ] **Step 2: Implement `ToolSpeedService`**

`src/main/java/space/qclid/arcanum/skills/ToolSpeedService.java`:

```java
package space.qclid.arcanum.skills;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import space.qclid.arcanum.compat.Compat;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;

/**
 * Virtual Efficiency: while a player holds the tool that matches a skill, a block-break-speed
 * modifier scaled by the skill level is applied; it is removed otherwise.
 */
public final class ToolSpeedService implements Listener {

    private static final NamespacedKey KEY = Compat.key("skill_efficiency");

    private record Rule(Predicate<Material> tool, IntToDoubleFunction bonusByLevel) {}

    private final SkillManager manager;
    private final Map<SkillType, Rule> rules = new EnumMap<>(SkillType.class);

    public ToolSpeedService(SkillManager manager) {
        this.manager = manager;
    }

    public void register(SkillType type, Predicate<Material> tool, IntToDoubleFunction bonusByLevel) {
        rules.put(type, new Rule(tool, bonusByLevel));
    }

    /** Re-evaluates the modifier from the held item; cheap enough for a 5-tick scheduler. */
    public void update(Player player) {
        Attribute attribute = Compat.BLOCK_BREAK_SPEED;
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        double amount = 0.0;
        if (manager.canEarn(player)) {
            Material held = player.getInventory().getItemInMainHand().getType();
            for (Map.Entry<SkillType, Rule> e : rules.entrySet()) {
                if (manager.isEnabled(e.getKey()) && e.getValue().tool().test(held)) {
                    amount = e.getValue().bonusByLevel().applyAsDouble(manager.level(player.getUniqueId(), e.getKey()));
                    break;
                }
            }
        }

        AttributeModifier existing = find(instance);
        if (existing != null) {
            if (Math.abs(existing.getAmount() - amount) < 1e-9) return;
            instance.removeModifier(existing);
        }
        if (amount > 0) {
            instance.addModifier(new AttributeModifier(KEY, amount, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.ANY));
        }
    }

    /** Removes our modifier (on quit and when the plugin shuts down). */
    public void clear(Player player) {
        Attribute attribute = Compat.BLOCK_BREAK_SPEED;
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = find(instance);
        if (existing != null) instance.removeModifier(existing);
    }

    private AttributeModifier find(AttributeInstance instance) {
        for (AttributeModifier m : instance.getModifiers()) {
            if (KEY.equals(m.getKey())) return m;
        }
        return null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clear(event.getPlayer());
    }
}
```

- [ ] **Step 3: Implement `MiningSkill`**

`src/main/java/space/qclid/arcanum/skills/skill/MiningSkill.java`:

```java
package space.qclid.arcanum.skills.skill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import space.qclid.arcanum.skills.PlacedBlockTracker;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;
import space.qclid.arcanum.skills.SkillsConfig;

import java.util.Collection;
import java.util.Map;

/**
 * Mining: XP for stone and ores, virtual Fortune on ores, and instant smelting at max level.
 * Virtual Efficiency is applied separately by {@code ToolSpeedService}.
 */
public final class MiningSkill implements Skill {

    private static final Map<Material, Material> SMELTED = Map.of(
        Material.RAW_IRON, Material.IRON_INGOT,
        Material.RAW_GOLD, Material.GOLD_INGOT,
        Material.RAW_COPPER, Material.COPPER_INGOT,
        Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

    private final SkillManager manager;
    private final SkillsConfig config;
    private final PlacedBlockTracker tracker;

    public MiningSkill(SkillManager manager, SkillsConfig config, PlacedBlockTracker tracker) {
        this.manager = manager;
        this.config = config;
        this.tracker = tracker;
        tracker.trackWhen(m -> isOre(m) || config.miningXp.containsKey(m.name()));
    }

    public static boolean isOre(Material m) {
        return m.name().endsWith("_ORE") || m == Material.ANCIENT_DEBRIS;
    }

    public static boolean isPickaxe(Material m) {
        return m.name().endsWith("_PICKAXE");
    }

    @Override
    public SkillType type() {
        return SkillType.MINING;
    }

    @Override
    public String nextPerk(int level) {
        return config.mining.nextPerk(level);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!manager.isEnabled(SkillType.MINING) || !manager.canEarn(player)) return;

        Block block = event.getBlock();
        Material type = block.getType();
        double xp = config.miningXp.getOrDefault(type.name(), 0.0);
        boolean ore = isOre(type);
        if (xp <= 0 && !ore) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!isPickaxe(tool.getType())) return;

        // A block the player placed earns nothing and gets no perks.
        if (tracker.consumeIfPlaced(block)) return;

        manager.addXp(player, SkillType.MINING, xp);
        if (!ore) return;
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        int level = manager.level(player.getUniqueId(), SkillType.MINING);
        int toolFortune = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int fortune = config.mining.effectiveFortune(toolFortune, level);
        boolean smelt = config.mining.instantSmelt(level);
        if (fortune == toolFortune && !smelt) return; // vanilla drops are already right

        ItemStack boosted = tool.clone();
        if (fortune > toolFortune) boosted.addUnsafeEnchantment(Enchantment.FORTUNE, fortune);
        Collection<ItemStack> drops = block.getDrops(boosted, player);

        event.setDropItems(false);
        Location at = block.getLocation().add(0.5, 0.5, 0.5);
        for (ItemStack drop : drops) {
            ItemStack out = smelt ? smelted(drop) : drop;
            block.getWorld().dropItemNaturally(at, out);
        }
    }

    private static ItemStack smelted(ItemStack drop) {
        Material result = SMELTED.get(drop.getType());
        if (result == null) return drop;
        return new ItemStack(result, drop.getAmount());
    }
}
```

- [ ] **Step 4: Compile**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL. If `m.getKey()` on `AttributeModifier` is not found, replace the comparison in `ToolSpeedService.find` with `KEY.getKey().equals(m.getName())`-style matching used by `CodexCombatListener.applyArmorReduction` and note it in the commit message.

- [ ] **Step 5: Commit**

```bash
git add src/main
git commit -m "Skills: Mining skill, virtual efficiency via attribute"
```

---

### Task 7: GUI, `SkillsFeature`, command, action-bar popup, plugin wiring

**Files:**
- Create: `src/main/java/space/qclid/arcanum/skills/gui/SkillsInventoryHolder.java`, `SkillsGui.java`, `SkillsGuiListener.java`, `src/main/java/space/qclid/arcanum/skills/SkillsFeature.java`
- Modify: `src/main/java/space/qclid/arcanum/feature/ActionBarFeature.java`, `src/main/java/space/qclid/arcanum/ArcanumPlugin.java`

**Interfaces:**
- Consumes: everything from Tasks 1–6.
- Produces:
  - `SkillsGui.open(Player, SkillManager, List<Skill>)`
  - `SkillsFeature(JavaPlugin)`: `SkillManager manager()`, `String popupFor(UUID)`, `void registerCommands(Commands)`, `void start()`, `void shutdown()`
  - `ActionBarFeature.setSkillPopup(java.util.function.Function<java.util.UUID, String>)`

- [ ] **Step 1: Implement the inventory holder and listener**

`src/main/java/space/qclid/arcanum/skills/gui/SkillsInventoryHolder.java`:

```java
package space.qclid.arcanum.skills.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Marks the /skills inventory so clicks in it can be cancelled. */
public final class SkillsInventoryHolder implements InventoryHolder {

    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
```

`src/main/java/space/qclid/arcanum/skills/gui/SkillsGuiListener.java`:

```java
package space.qclid.arcanum.skills.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** The skills GUI is read-only: cancel every click and drag that involves it. */
public final class SkillsGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SkillsInventoryHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SkillsInventoryHolder) event.setCancelled(true);
    }
}
```

- [ ] **Step 2: Implement `SkillsGui`**

`src/main/java/space/qclid/arcanum/skills/gui/SkillsGui.java`:

```java
package space.qclid.arcanum.skills.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.qclid.arcanum.skills.Skill;
import space.qclid.arcanum.skills.SkillCurve;
import space.qclid.arcanum.skills.SkillManager;
import space.qclid.arcanum.skills.SkillType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/** The /skills screen: one icon per enabled skill with level, progress bar and next perk. */
public final class SkillsGui {

    private static final int SIZE = 36;
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int BAR_WIDTH = 20;

    private SkillsGui() {}

    public static void open(Player player, SkillManager manager, List<Skill> skills) {
        SkillsInventoryHolder holder = new SkillsInventoryHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, parse(C_GOLD + "<bold>" + toSmallCaps("Skills")));
        holder.setInventory(inv);

        int slot = 0;
        for (Skill skill : skills) {
            if (!manager.isEnabled(skill.type()) || slot >= SLOTS.length) continue;
            inv.setItem(SLOTS[slot++], icon(player.getUniqueId(), skill, manager));
        }
        player.openInventory(inv);
    }

    private static ItemStack icon(UUID id, Skill skill, SkillManager manager) {
        SkillType type = skill.type();
        SkillCurve curve = manager.curve();
        long xp = manager.xp(id, type);
        int level = curve.levelForXp(xp);

        Material material = Material.matchMaterial(type.icon());
        ItemStack item = new ItemStack(material != null ? material : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(parse(C_GOLD + "<bold>" + toSmallCaps(type.displayName())));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<Component> lore = new ArrayList<>();
        lore.add(parse(C_YELLOW + toSmallCaps("Level " + level + " / " + curve.maxLevel())));
        lore.add(parse(bar(curve.progress(xp))));
        lore.add(parse(level >= curve.maxLevel()
            ? C_GREEN + toSmallCaps("Max level")
            : C_GRAY + curve.xpIntoLevel(xp) + " / " + curve.xpToNext(level) + " xp"));
        lore.add(Component.empty());
        String next = skill.nextPerk(level);
        lore.add(parse(next == null
            ? C_GREEN + toSmallCaps("All perks unlocked")
            : C_PURPLE + toSmallCaps("Next: ") + C_GRAY + next));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String bar(double progress) {
        int filled = (int) Math.round(progress * BAR_WIDTH);
        StringBuilder sb = new StringBuilder(C_GREEN);
        for (int i = 0; i < BAR_WIDTH; i++) {
            if (i == filled) sb.append(C_GRAY);
            sb.append(i < filled ? "█" : "░");
        }
        return sb.toString();
    }
}
```

- [ ] **Step 3: Implement `SkillsFeature`**

`src/main/java/space/qclid/arcanum/skills/SkillsFeature.java`:

```java
package space.qclid.arcanum.skills;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.arcanum.skills.gui.SkillsGui;
import space.qclid.arcanum.skills.gui.SkillsGuiListener;
import space.qclid.arcanum.skills.skill.MiningSkill;
import space.qclid.arcanum.skills.skill.RunningSkill;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/** Wires the skills subsystem into the plugin: config, manager, skills, GUI, commands, schedulers. */
public final class SkillsFeature {

    private final JavaPlugin plugin;
    private final SkillManager manager;
    private final ToolSpeedService toolSpeed;
    private final List<Skill> skills = new ArrayList<>();

    public SkillsFeature(JavaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) plugin.saveResource("skills.yml", false);
        SkillsConfig config = SkillsConfig.load(YamlConfiguration.loadConfiguration(file), plugin.getLogger());
        SkillCurve curve = new SkillCurve(config.curveA, config.curveP, config.maxLevel);
        this.manager = new SkillManager(plugin, config, curve);

        PlacedBlockTracker tracker = new PlacedBlockTracker();
        this.toolSpeed = new ToolSpeedService(manager);

        skills.add(new RunningSkill(manager, config));
        skills.add(new MiningSkill(manager, config, tracker));
        toolSpeed.register(SkillType.MINING, MiningSkill::isPickaxe, config.mining::breakSpeedBonus);

        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(tracker, plugin);
        pm.registerEvents(toolSpeed, plugin);
        pm.registerEvents(new SkillsGuiListener(), plugin);
        for (Skill skill : skills) pm.registerEvents(skill, plugin);
    }

    public SkillManager manager() { return manager; }

    /** XP popup text for the action bar, or null. */
    public String popupFor(UUID id) { return manager.popupFor(id); }

    /** Starts the 5-tick sampler and the 5-minute autosave. */
    public void start() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Skill skill : skills) {
                    if (manager.isEnabled(skill.type())) skill.tick(player);
                }
                toolSpeed.update(player);
            }
        }, 1L, 5L);
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> manager.save(), 6000L, 6000L);
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) toolSpeed.clear(player);
        manager.save();
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    private static boolean isAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("arcanum.admin") || sender.hasPermission("dashboard.admin");
    }

    private static void say(CommandSender sender, String miniMessage) {
        sender.sendMessage(MM.deserialize(miniMessage));
    }

    private static java.util.Optional<Player> firstPlayer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        List<Player> targets = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        return targets.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(targets.get(0));
    }

    public void registerCommands(Commands commands) {
        var root = Commands.literal("skills")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                SkillsGui.open(player, manager, skills);
                return 1;
            })
            .then(Commands.literal("set")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { for (SkillType t : SkillType.values()) b.suggest(t.id()); return b.buildFuture(); })
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 150))
                            .executes(ctx -> {
                                var target = firstPlayer(ctx);
                                var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                                if (target.isEmpty() || type.isEmpty()) {
                                    say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                    return 1;
                                }
                                int level = Math.min(IntegerArgumentType.getInteger(ctx, "level"), manager.curve().maxLevel());
                                manager.setXp(target.get().getUniqueId(), type.get(), manager.curve().totalXpForLevel(level));
                                say(ctx.getSource().getSender(), C_GREEN + toSmallCaps(target.get().getName() + "'s " + type.get().displayName() + " is now level " + level));
                                return 1;
                            })))))
            .then(Commands.literal("addxp")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { for (SkillType t : SkillType.values()) b.suggest(t.id()); return b.buildFuture(); })
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                var target = firstPlayer(ctx);
                                var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                                if (target.isEmpty() || type.isEmpty()) {
                                    say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                    return 1;
                                }
                                manager.addXp(target.get(), type.get(), LongArgumentType.getLong(ctx, "amount"));
                                say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Added XP."));
                                return 1;
                            })))))
            .then(Commands.literal("reset")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .executes(ctx -> {
                        var target = firstPlayer(ctx);
                        if (target.isEmpty()) {
                            say(ctx.getSource().getSender(), C_RED + toSmallCaps("Player not found."));
                            return 1;
                        }
                        for (SkillType t : SkillType.values()) manager.setXp(target.get().getUniqueId(), t, 0);
                        say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Reset all skills for " + target.get().getName()));
                        return 1;
                    })
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { for (SkillType t : SkillType.values()) b.suggest(t.id()); return b.buildFuture(); })
                        .executes(ctx -> {
                            var target = firstPlayer(ctx);
                            var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                            if (target.isEmpty() || type.isEmpty()) {
                                say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                return 1;
                            }
                            manager.setXp(target.get().getUniqueId(), type.get(), 0);
                            say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Reset " + type.get().displayName()));
                            return 1;
                        }))));
        commands.register(root.build(), "Open your skills", List.of());
    }
}
```

- [ ] **Step 4: Add the popup hook to `ActionBarFeature`**

In `ActionBarFeature.java`:

1. Add a field and setter below `private final UpdateFeature updateFeature;`:

```java
    private java.util.function.Function<java.util.UUID, String> skillPopup;

    /** Provides MiniMessage text for the skill XP popup (null when none is showing). */
    public void setSkillPopup(java.util.function.Function<java.util.UUID, String> skillPopup) {
        this.skillPopup = skillPopup;
    }
```

2. In `update(Player player)`, replace the first two lines

```java
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
        if (!settings.globalEnabled) return;
```

with

```java
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
        String popup = skillPopup == null ? null : skillPopup.apply(player.getUniqueId());
        if (!settings.globalEnabled) {
            if (popup != null) player.sendActionBar(MM.deserialize(popup));
            return;
        }
```

3. Replace the last line of `update`

```java
        if (!sb.isEmpty()) player.sendActionBar(MM.deserialize(sb.toString().trim()));
```

with

```java
        if (popup != null) {
            if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
            sb.append(popup);
        }
        if (!sb.isEmpty()) player.sendActionBar(MM.deserialize(sb.toString().trim()));
```

- [ ] **Step 5: Wire it into `ArcanumPlugin`**

In `ArcanumPlugin.java`:

1. After the `ActionBarFeature actionBarFeature = ...` line add:

```java
        SkillsFeature        skillsFeature         = new SkillsFeature(this);
        actionBarFeature.setSkillPopup(skillsFeature::popupFor);
```

2. In the `LifecycleEvents.COMMANDS` handler add `skillsFeature.registerCommands(commands);` after `codexFeature.registerCommands(commands);`.

3. After the existing update-check scheduler block add `skillsFeature.start();`.

4. Add the field `private SkillsFeature skillsFeature;` next to the other fields, set `this.skillsFeature = skillsFeature;` where the other fields are assigned, and in `onDisable()` add `if (skillsFeature != null) skillsFeature.shutdown();` before the existing save calls.

5. Add `import space.qclid.arcanum.skills.SkillsFeature;` with the other imports.

- [ ] **Step 6: Build and run the unit tests**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all tests pass, jar at `build/libs/Arcanum-1.12.jar`.

- [ ] **Step 7: Commit**

```bash
git add src/main
git commit -m "Skills: /skills GUI, admin commands, action-bar popup and plugin wiring"
```

---

### Task 8: Compatibility scan, live server check, docs

**Files:**
- Create: `tools/compat-scan.py`
- Modify: `README.md`

- [ ] **Step 1: Add the repeatable compatibility scan**

Create `tools/compat-scan.py` (the scan used for the Biome fix, made reusable):

```python
#!/usr/bin/env python3
"""Flags calls in the built jar on Bukkit types whose kind (class vs interface) differs between
two Paper API versions. Usage: compat-scan.py <classes-dir> <paper-api-old.jar> <paper-api-new.jar>
Requires `javap` on PATH. Exit code 1 if any BAD entry is found."""
import glob
import re
import subprocess
import sys

PREFIXES = ("org/bukkit", "io/papermc", "com/destroystokyo")


def main(classes_dir, old_jar, new_jar):
    classes = glob.glob(classes_dir + "/**/*.class", recursive=True)
    out = subprocess.run(["javap", "-c", "-p"] + classes, capture_output=True,
                         text=True, encoding="utf-8", errors="replace").stdout
    calls = {}
    current = None
    for line in out.splitlines():
        m = re.match(r"(?:public |final |abstract )*(?:class|interface|enum) ([\w.$]+)", line)
        if m:
            current = m.group(1)
        m = re.search(r"(invokevirtual|invokeinterface|invokestatic|invokespecial)\s+#\d+\s+// "
                      r"(?:Method|InterfaceMethod) ([\w/$]+)\.([\w<>$]+):", line)
        if m and m.group(2).startswith(PREFIXES):
            calls.setdefault((m.group(1), m.group(2)), set()).add((m.group(3), current))

    owners = sorted({o for _, o in calls})

    def kinds(jar):
        res = {}
        for i in range(0, len(owners), 40):
            names = [o.replace("/", ".") for o in owners[i:i + 40]]
            r = subprocess.run(["javap", "-cp", jar] + names, capture_output=True,
                               text=True, encoding="utf-8", errors="replace")
            for l in r.stdout.splitlines():
                mm = re.match(r"(?:public |protected |final |abstract |static |sealed )*"
                              r"(class|interface|enum) ([\w.$]+)", l)
                if mm:
                    res[mm.group(2).replace(".", "/")] = "interface" if mm.group(1) == "interface" else "class"
        return res

    old, new = kinds(old_jar), kinds(new_jar)
    bad = 0
    for (kind, owner), where in sorted(calls.items()):
        for label, k in (("old", old.get(owner)), ("new", new.get(owner))):
            if k is None:
                continue
            if (kind == "invokeinterface" and k != "interface") or (kind == "invokevirtual" and k == "interface"):
                print("BAD", label, owner, kind, sorted(where)[:4])
                bad += 1
        if kind == "invokestatic" and old.get(owner) and new.get(owner) and old[owner] != new[owner]:
            print("BAD static kind change", owner, old[owner], new[owner], sorted(where)[:3])
            bad += 1
    print("owners checked:", len(owners), "| problems:", bad)
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main(*sys.argv[1:4]))
```

- [ ] **Step 2: Run the scan against the built jar**

Run (from the repo root; `javap` is in `C:\Program Files\Java\jdk-26.0.1\bin`):

```bash
export PATH="/c/Program Files/Java/jdk-26.0.1/bin:$PATH"
mkdir -p build/scan && cd build/scan && unzip -qo ../libs/Arcanum-1.12.jar && cd ../..
G=~/.gradle/caches/modules-2/files-2.1/io.papermc.paper/paper-api
python tools/compat-scan.py build/scan "$(cygpath -w $(ls $G/1.21.1-R0.1-SNAPSHOT/*/paper-api-1.21.1-R0.1-SNAPSHOT.jar))" "$(cygpath -w $(ls $G/26.3.build.140-beta/*/paper-api-26.3.build.140-beta.jar))"
```

Expected: `owners checked: N | problems: 0` and exit code 0. Any `BAD` line must be fixed (go through `Keyed`/`Compat`) before continuing.

- [ ] **Step 3: Boot servers on both ends of the range**

Run each and confirm startup is clean:

```bash
(sleep 45; echo stop) | timeout 300 ./gradlew runServer -PmcVersion=26.3 --console=plain > /tmp/skills263.log 2>&1
grep -E "Arcanum|Done \(|Exception|IncompatibleClass|NoSuch" /tmp/skills263.log
```

Then the same with `-PmcVersion=1.21.1` (delete `run/plugins/Arcanum` and `run/world*` first when switching from a newer version to an older one).
Expected for both: `Arcanum ... enabled!`, `Done (...)`, no `Exception`, `IncompatibleClassChangeError` or `NoSuchMethodError` lines, and `plugins/Arcanum/skills.yml` created.

- [ ] **Step 4: Document the skills in the README**

In `README.md`, under the Features bullets add:

```markdown
- **Skills**: nine XP-based skills (Running and Mining available now, more coming) that unlock perks as you level up to 150, such as Speed while sprinting, virtual Fortune and instant ore smelting. Open `/skills` to see your progress.
```

and add these rows to the Commands table:

```markdown
| `/skills` | | Open your skill levels and next perks |
| `/skills set \| addxp \| reset <player> ...` | | Admin: change a player's skill progress (OP or `arcanum.admin`) |
```

- [ ] **Step 5: Commit**

```bash
git add tools README.md
git commit -m "Skills: compatibility scan tool, README docs"
```

- [ ] **Step 6: In-game acceptance checklist (needs a player; run by the user)**

On 26.3 and on 1.21.1, with an op account in survival:
1. `/skills` opens a 4-row GUI showing Running and Mining at level 1; clicking items does nothing.
2. Sprint ~20 blocks: action bar shows `+N Running`; level text updates in `/skills`.
3. `/skills set <you> running 25`, sprint 12 blocks continuously: Speed I appears after ~10 blocks. Set 50: Speed II after ~5. Set 150: Speed III immediately.
4. Ride a boat or horse while "sprinting", swim, use elytra, and teleport: no Running XP and no streak.
5. `/skills set <you> mining 150`; mine iron ore with an iron pickaxe: ingots drop instead of raw iron, with extra fortune-style amounts; mining is noticeably faster than at level 1.
6. Mine the same ore with a Silk Touch pickaxe: the ore block drops. Place an ore block and break it again: no XP, vanilla drops.
7. Switch to creative: no XP. `/skills reset <you>` clears levels.
8. Restart the server: levels persist. Hold a pickaxe at mining 150, `stop` the server, restart: speed modifier is gone until the pickaxe is held again.
