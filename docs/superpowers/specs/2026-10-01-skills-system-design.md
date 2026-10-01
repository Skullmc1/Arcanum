# Skills System — Design

Date: 2026-10-01 · Status: approved in chat, awaiting written-spec review

## 1. Goal

Add an experience-based skills system to Arcanum. Players level skills by doing the matching activity
(sprinting, mining, ...). Skill levels (1–150) unlock perks that scale with level. The system must work
on every supported Minecraft version (1.21.1 → 26.3) from the single existing jar.

Success criteria:
- Nine skills exist and level up from legitimate play: Running, Mining, Logging, Farming, Monster Hunting,
  Fishing, Excavation, Archery, Acrobatics.
- Perks unlock at the levels given in section 6 and are applied correctly in a live server on 1.21.1 and 26.3.
- Progress persists across restarts, deaths and relogs.
- Pacing is long-term: level 25 ≈ a few hours of focused play, level 150 ≈ weeks of regular play. The curve is configurable.
- Obvious XP exploits (placed blocks, spawner mobs, vehicles) do not award XP.
- The compatibility byte-code scan reports no class/interface mismatches for the new code.

Non-goals (not in this version): per-player perk toggles, party/shared XP, PlaceholderAPI/MySQL, Codex item
integration, prestige/reset system, leaderboards.

## 2. Architecture

New package `space.qclid.arcanum.skills`. One framework, one small class per skill.

| Unit | Responsibility | Depends on |
|---|---|---|
| `SkillType` | Enum of the nine skills: id, display name, icon material, colour. | — |
| `SkillCurve` | Pure functions: `xpToNext(level)`, `levelForXp(totalXp)`, progress fraction. | config values |
| `PlayerSkills` | Total XP per `SkillType` for one player; derives level/progress via `SkillCurve`. | `SkillCurve` |
| `SkillManager` | Loads/saves `PlayerSkills`, `addXp(player, skill, amount)`, level-up detection, shows action-bar popup and level-up message. | `PlayerSkills`, `SkillsConfig` |
| `SkillsConfig` | Reads `skills.yml`: curve parameters, XP values, perk thresholds, per-skill enable flag, disabled worlds. | — |
| `Skill` (interface) | `SkillType type()`; each implementation is a `Listener` that awards XP and applies perks. | `SkillManager` |
| `RunningSkill` … `AcrobaticsSkill` | One class per skill (nine). | `Skill`, `SkillManager` |
| `PlacedBlockTracker` | Remembers player-placed XP-giving blocks (ores, logs, stems) per chunk. | — |
| `SkillsGui`, `SkillsInventoryHolder` | `/skills` GUI, same holder pattern as the Codex GUIs. | `SkillManager` |
| `SkillsFeature` | Wires everything, registers `/skills` and admin sub-commands, schedulers. | all |

`ArcanumPlugin.onEnable` creates `SkillsFeature` next to the other features and registers its commands in the
existing `LifecycleEvents.COMMANDS` handler. `onDisable` saves skill data.

Perk numbers (levels, amounts) are read from `skills.yml` with the defaults in section 6 compiled in, so a missing
or partial config still works. Special behaviour (tree-felling, instant smelting, area harvest) is code.

## 3. XP and levels

- Max level 150. XP needed to go from level L to L+1: `round(a * L^p)`, defaults `a = 12`, `p = 1.3`
  (≈ 8.5k total XP for level 25, ≈ 520k for level 150). Both configurable.
- Calibration target: an active player earns roughly 5–6k XP/hour in a skill's own activity. A unit test prints the
  table of total XP per level so pacing can be reviewed and tuned.
- XP is stored as a total (`long`) per skill; level is derived. Reaching the cap stops XP gain (XP still displays full).
- Creative/spectator players and players in disabled worlds earn no XP and receive no perks.

Default XP sources (all in `skills.yml`):

| Skill | XP award |
|---|---|
| Running | 0.3 per block sprinted (counted in whole blocks) |
| Mining | stone-type 1, coal 8, copper 10, iron 15, redstone/lapis 12, gold 25, emerald 40, diamond 60, ancient debris 100, nether quartz/gold 10 |
| Logging | 6 per log |
| Farming | 8 per fully grown crop harvested; 25 per animal bred |
| Monster Hunting | 2 × victim max health (hostile mobs only, killed by the player) |
| Fishing | catch 40, junk 10, treasure 120 |
| Excavation | dirt/grass/sand/gravel/clay/soul sand 2 |
| Archery | 10 per arrow hit on a living non-player entity, +1 per 10 blocks of distance (max +10) |
| Acrobatics | 6 per heart of fall damage survived; 0.2 per block swum |

## 4. Anti-exploit rules

- **Placed blocks:** `PlacedBlockTracker` stores packed block positions in the chunk's `PersistentDataContainer` for
  XP-giving block types only (ores, logs, melons/pumpkins). Breaking a tracked block gives no XP and no perk drops,
  and the entry is removed. Blocks moved by pistons lose tracking (known limitation).
- **Crops:** XP and perks only when `Ageable` is at max age. Replant does not give XP.
- **Mobs:** no Monster Hunting XP for spawner-spawned mobs (`SpawnReason.SPAWNER`) or for the Codex training dummy (`/codexdummy`).
- **Running:** only counts while sprinting (jumping allowed), not riding, not gliding, not flying, not in water.
  Per-tick distance is capped to reject teleports.
- **Archery:** hits on armor stands and dummies give no XP.
- **Fishing:** XP only from `PlayerFishEvent.State.CAUGHT_FISH`.
- **Acrobatics:** fall XP only for damage actually taken (>0 after reductions); swim XP only when in water and moving.

## 5. Data and persistence

- `plugins/Arcanum/skills.yml` — config (copied from the jar on first start if missing).
- `plugins/Arcanum/skill-data.yml` — `players.<uuid>.<skillId>: <totalXp>`.
- Loaded on join, written on quit, every 5 minutes (dirty players only) and on disable.
- A corrupted or unknown skill id in the file is skipped with a console warning; it never prevents startup.

## 6. Skills and perks (defaults)

"Virtual enchant" means a bonus computed by the plugin and added on top of whatever the held tool already has.

### Running
| Level | Perk |
|---|---|
| 25 | Speed I after sprinting 10 blocks continuously |
| 50 | Speed II after 5 blocks |
| 100 | Speed II as soon as sprint starts |
| 150 | Speed III as soon as sprint starts |

Effect is reapplied while sprinting and removed shortly after sprint ends. It never reduces a stronger existing speed effect.

### Mining (pickaxe)
- **Efficiency:** +1 virtual level every 25 levels (max +6 at 150), implemented as a `block_break_speed`
  attribute modifier (+20% per virtual level) applied while a pickaxe is held and removed otherwise.
- **Fortune:** virtual Fortune I at 50, II at 100, III at 150. Effective fortune = tool fortune + virtual.
  Applies to ores using vanilla's fortune formula.
- **Level 150: instant smelt.** Iron, gold, copper ore drops (and deepslate variants) become ingots; ancient debris becomes
  netherite scrap; sand-type ores are unaffected. Smelted output still receives fortune.
- Fortune and smelting are skipped when the tool has Silk Touch.

### Logging (axe)
- **Efficiency:** same curve as Mining, applied while an axe is held.
- **Level 50: tree felling.** Breaking a log fells the connected natural tree (same log family, must touch leaves, max 256 logs,
  within a bounded radius). Costs durability per log; stops when the axe would break. Holding sneak cuts a single log.
- **Level 100:** 25% chance of a bonus log per log.
- **Level 150:** saplings that drop are planted automatically at the stump when the soil is valid.

### Farming (hoe / hand)
- **25:** right-click on a mature crop harvests and replants it.
- **50:** +1 extra crop drop chance of 25%.
- **75:** hoe right-click harvests mature crops in a 3×3 area.
- **150:** 5×5 area and doubled crop yield. Breeding animals has a 15% chance of twins from level 100.

### Monster Hunting (any weapon)
- **Looting:** virtual Looting I at 25, II at 75, III at 125, applied on top of the weapon's Looting
  (extra random amount per common drop; not applied to equipment drops).
- **Bonus XP:** vanilla XP from kills +10% per 25 levels (max +60% at 150).

### Fishing (fishing rod)
- 25/75/125: bite time shortened by 10/20/30%. 50/100: treasure chance +5/+10%. 150: 20% chance to catch a second item.

### Excavation (shovel)
- **Efficiency:** same curve as Mining, applied while a shovel is held.
- **50:** 20% chance of a bonus drop on gravel/clay/sand. **100:** 3×3 dig (hold sneak to disable). **150:** 5×5 dig.

### Archery (bow / crossbow)
- Arrow damage +0.5% per level (max +75% at 150).
- **100:** 20% chance to recover the arrow. **150:** 10% chance that a shot costs no arrow.

### Acrobatics
- Fall damage reduced by 0.3% per level (max 45% at 150), and a 150 perk that negates falls of 6 blocks or less.
- **50:** +10 s underwater breathing. **100:** Dolphin's Grace while swimming.

## 7. Player-facing UI

- `/skills` opens a GUI: one icon per skill showing level, XP progress bar and the next perk. Uses the plugin's
  existing small-caps/colour helpers.
- XP gain: action-bar popup, throttled (at most one update per second per skill).
- Level up: chat message, sound, and a title for milestone levels.
- Admin (OP or `arcanum.admin`): `/skills set <player> <skill> <level>`, `/skills addxp <player> <skill> <amount>`,
  `/skills reset <player> [skill]`.

## 8. Compatibility

- Single jar for 1.21.1 → 26.3. Attribute lookups go through `Compat`, never directly through `Attribute.*`.
- Do not call methods on types that changed between class and interface across versions (e.g. `Biome`, `Attribute`,
  `Enchantment`, `Sound`). Use `Keyed` / registry lookups. Tool enchant levels are read through `ItemMeta.getEnchantLevel`.
- After each build phase the byte-code scan (from the Biome fix) is re-run against both the 1.21.1 and 26.3 APIs
  and must report no `BAD` entries.
- Scheduling uses the plugin's existing region schedulers.

## 9. Error handling

- A failing perk handler must not break the event for other plugins: handlers catch and log once per skill per minute.
- Unknown config values fall back to defaults with a console warning.
- Level, XP and config are clamped to valid ranges (level 1–150, non-negative XP).

## 10. Testing

- **Unit tests (JUnit, pure logic):** `SkillCurve` (monotonic, level/xp round trip, cap), perk lookup by level,
  fortune formula distribution bounds, `PlayerSkills` XP/level math, packed block position encode/decode.
- **Live test on a dev server (1.21.1 and 26.3):** each skill's XP source and each perk threshold is exercised with
  `/skills set` and checked in-game. Server must start with no errors and the compatibility scan must be clean.
- No automated end-to-end test of gameplay; verification is manual with recorded results in the plan.

## 11. Delivery phases

1. **Framework + Running + Mining** (curve, manager, storage, config, GUI, commands, placed-block tracker). Playable checkpoint.
2. **Logging + Farming.**
3. **Monster Hunting, Fishing, Excavation, Archery, Acrobatics.**

Each phase ends with a working build, a clean compatibility scan and a commit.
