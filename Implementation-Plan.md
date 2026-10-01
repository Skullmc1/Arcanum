# Arcanum Plugin — Implementation Plan

> Phased improvements across critical fixes, code quality, UX, feature gaps, and performance.

---

## Phase 1 — Critical Issues (Security, Stability, Data Loss)

### 1.1 Fix NPE in `PotionResistanceFeature.onPotionEffect`

**File:** `feature/PotionResistanceFeature.java:47-48`

**Problem:** The null check is inverted — `newEffect == null` returns (good), but the check on L47 checks `newEffect == null` then L48 calls `newEffect.getType()`. Actually the real issue: the guard only returns if `newEffect == null`, but the condition on L48 still sees `newEffect` as possibly null because L47 guards *against* null. **Wait** — on re-reading, the code says:
```java
if (newEffect == null || newEffect.getType().getCategory() != PotionEffectTypeCategory.HARMFUL) return;
```
This is actually fine if `newEffect == null` short-circuits. The actual NPE is: when `newEffect.getType()` returns null (unlikely), or when `event.getAction() != ADDED/CHANGED` and `newEffect` is null. Wait, the agent said the null check is on L47 but the code on L48 proceeds to call `getType()` — but it's the same line. Let me re-check. Actually looking at the code again:

```java
PotionEffect newEffect = event.getNewEffect();
if (newEffect == null || newEffect.getType().getCategory() != PotionEffectTypeCategory.HARMFUL) return;
```

This is fine — if `newEffect == null`, the `||` short-circuits. The agent was wrong about this specific issue. But there IS a potential issue: `event.getAction()` can be `REMOVED` or other actions, and `getNewEffect()` can return null. The check on L38-39 returns early for non-ADDED/CHANGED actions. So this is actually fine. Let me re-read what the agent said...

Ah, the agent said: "The null check on line 47 only checks `newEffect == null` but then line 48 proceeds to call `newEffect.getType().getCategory()`" — but they're the same line. The agent was confused because they're in a single if-statement. So this is NOT a bug. The short-circuit evaluation works correctly.

OK, so we don't need to fix #1. Let me note this as a false positive.

Actually, wait — the agent said "The null check on line 47 only checks newEffect == null but then line 48 proceeds to call newEffect.getType().getCategory()" - this is just one line L48. The short-circuit in Java means if `newEffect == null` is true, `newEffect.getType()` is never called. So this is fine.

I'll remove this from the plan.

### 1.2 Blood Altar `setHealth(0.0)` bypasses death event

**File:** `codex/crafting/CodexCrafting.java:397-398`

**Fix:**
```java
// Instead of:
player.setHealth(0.0);

// Use:
player.damage(player.getHealth());
```

### 1.3 HTTPS for update URL

**File:** `feature/UpdateFeature.java:28`

Already using `https://` — the agent flagged it but it's already HTTPS. Let me re-check...

```java
private static final String UPDATE_URL = "https://www.qclid.space/api/plugin-version";
```

Yes, it's already HTTPS. The agent was wrong here. I'll remove this.

### 1.4 Void Bag — new "Void Bag Refund" consumable

**Files:** `codex/gui/VoidBagInventoryHolder.java`, `codex/listeners/CodexPlayerListener.java:75-79`

**Problem:** Closing a Void Bag permanently deletes everything with no undo.

**Solution:** Create a new item **Void Bag Refund** — a one-use consumable that restores the last batch of voided items.

**Changes needed:**
- Add a `lastVoidedItems` map in `CodexFeature` or a shared context (`Map<UUID, ItemStack[]>`)
- When Void Bag closes, capture a snapshot of the inventory contents BEFORE clearing
- Store snapshot keyed by player UUID
- Create new item `explorer.gadgets.void_bag_refund` — a consumable item (e.g., a named paper/clock)
- On right-click with the refund item, restore the stored items to the player's inventory (or drop on ground if full), then consume the refund item

---

## Phase 2 — UX Improvements

### 2.1 Add `/waypoint list` command

**File:** `feature/WaypointFeature.java`

Add a subcommand under the `waypoint` literal:
```
.then(Commands.literal("list")
    .executes(ctx -> {
        // Show player's waypoints as a clickable list
    }))
```

Output format: show each waypoint name with coordinates, and clickable buttons for navigate/tp/delete alongside each entry.

### 2.2 Diamond cost confirmation prompt

**File:** `feature/WaypointFeature.java:41-53`

Before deducting diamonds, send a chat confirmation message with clickable "confirm" / "cancel" buttons. Only deduct on confirm.

Implementation: Store a pending action in a `Map<UUID, PendingWaypoint>` and await a click callback.

### 2.3 Move flame particles further from player

**File:** `feature/NavigationFeature.java:39-44`

Change the particle spawn range from `1.0 to 2.0` to `2.0 to 4.0` to keep flames visibly ahead rather than at eye level:

```java
for (double i = 2.0; i <= 4.0; i += 0.5) {
```

Also consider switching from `Particle.FLAME` to `Particle.END_ROD` or `Particle.VILLAGER_HAPPY` to avoid the "on fire" illusion.

### 2.4 Potion resistance message — self-deleting + count suffix

**File:** `feature/PotionResistanceFeature.java:61-64`

**Problem:** Each resistance event spams a new chat message.

**Fix:**
- Track a message counter per player per effect type
- Store the last message's `Component` and use `player.sendMessage(msg)` with a tracking approach
- Since Paper doesn't support editing past messages easily via standard API, the simplest approach: send the message and include a count suffix (`(x2)`, `(x3)`, etc.)
- For self-deleting: use a delayed task to send the next message that overwrites visually, OR use the action bar instead of chat
- **Best approach:** Use action bar for resistance notifications instead of chat

### 2.5 `refreshItemLore` optimization

**Files:** `codex/listeners/CodexPlayerListener.java:90-114`

**Problem:** Called on EVERY inventory close/click, scanning all slots. Causes tick lag.

**Solution:** This is a performance issue (covered in Phase 5) but for UX the improvement is that it won't lag the player's inventory interactions. See Phase 5.1 for the technical fix.

### 2.6 Consistent toggle colors

**File:** `feature/ActionBarFeature.java:118-121`

Standardize all "disabled" states to use `<red>` consistently:

```
"global" enabled:  C_GOLD    "Arcanum HUD enabled!"
"global" disabled: "<red>"   "Arcanum HUD disabled!"   ✓
"xyz"    enabled:  C_ORANGE  "XYZ display enabled!"
"xyz"    disabled: "<red>"   "XYZ display disabled!"  ✓
"biome"  enabled:  C_YELLOW  "Biome display enabled!"
"biome"  disabled: "<red>"   "Biome display disabled!" ✓
"nether" enabled:  C_RED     "Nether XYZ enabled!"
"nether" disabled: "<red>"   "Nether XYZ disabled!"   ✗ (was "<dark_red>")
```

And use constants for all, not mixing raw MiniMessage tags with `C_*` constants.

### 2.7 Crafting station explanation in recipe GUI

**File:** `codex/gui/CodexRecipeGui.java`

**Problem:** Players can't tell which multiblock structure is required from the recipe view alone.

**Solution:** Add a lore line to the crafting station item in the recipe GUI that describes the structure name (e.g., `<gray>Crafting Station: <aqua>Arcana Table</aqua></gray>`). Map the station's `item_id` to a human-readable name with a brief description of the structure layout.

**Implementation:**
- In `CodexRecipeGui`, when setting the station item in slot 10, append lore describing the station:
  - `machinery.arcana_table` → "Arcana Table: Crafting Table above Dropper with 2+ adjacent Bookshelves"
  - `machinery.heavy_forge` → "Heavy Forge: Crafting Table above Blast Furnace above Dropper"
  - `machinery.blood_altar` → "Blood Altar: Red Carpet above Dropper above Obsidian"
  - `machinery.upgrade_table` → "Upgrade Table: Anvil above Dropper above Bookshelf"
  - etc.

---

## Phase 3 — Code Quality

### 3.1 Split monolithic `initRegistry()` (~2600 lines)

**File:** `codex/CodexFeature.java:98-1900`

**Approach:** Move item registrations into separate methods per sub-category:
- `registerArcaneRunes()`
- `registerArcaneWeapons()`
- `registerArcaneArmor()`
- `registerArcaneMaterials()`
- `registerArcaneMachinery()`
- `registerExplorerNavigation()`
- `registerExplorerTools()`
- `registerExplorerGadgets()`
- etc.

Each method receives the `CodexCategory` object and adds items directly.

### 3.2 Replace `if-else` chains with `Map<String, ItemStack>`

**Files:**
- `codex/items/ArcaneItems.java:361-418`
- `codex/items/ExplorerItems.java:188-228`

**Approach:** Create a `Map<String, ItemStack>` at initialization time keyed by item ID, and replace `getCustomItem()` with a simple `map.get(id)` lookup (O(1) instead of O(n)).

### 3.3 Reduce constructor parameter explosion — Context object

**Files:** All `Codex*Listener` constructors (5+ listeners with 9 params each)

**Approach:** Create a `CodexContext` record or class holding:
```java
public record CodexContext(
    JavaPlugin plugin,
    DataManager dataManager,
    CodexManager manager,
    CodexRegistry registry,
    ArcaneItems arcaneItems,
    ExplorerItems explorerItems,
    CodexCrafting codexCrafting,
    CodexPassiveTask codexPassiveTask,
    Map<UUID, String> activeMachine
) {}
```

Pass this single object to each listener constructor.

### 3.4 Remove duplicate `hasRequirement` method

**Files:**
- `codex/gui/CodexCategoryGui.java:164-191`
- `codex/gui/CodexGuiListener.java:195-222`

**Approach:** Extract to a shared utility method in `TextUtil.java` or a new `CodexUtil.java`. The two copies differ only in how they resolve the `Plugin` reference — standardize both to accept a `Plugin` parameter.

### 3.5 Encapsulate `PlayerSettings`

**File:** `PlayerSettings.java`

Replace public fields with private + getters/setters:
```java
private boolean showXyz = true;
private boolean showBiome = true;
// ...
public boolean isShowXyz() { return showXyz; }
public void setShowXyz(boolean v) { showXyz = v; }
```

### 3.6 Rename underscore-prefixed fields

**File:** `ArcanumPlugin.java:74-75`

`_dataManager` → `dataManager`, `_updateFeature` → `updateFeature`

### 3.7 Fix `toSmallCaps` dead uppercase branches

**File:** `util/TextUtil.java:46-56`

**Problem:** `normal.indexOf(c)` will always match the lowercase occurrence first (index 0-25) because they appear before uppercase (26-51) in the string. Uppercase letters are never actually converted through the small caps mapping for uppercase — they match the lowercase indices.

**Fix:** Separate lowercase and uppercase normal strings, or use a `Map<Character, Character>` for the mapping.

### 3.8 Remove dead ternary

**File:** `codex/items/ArcaneItems.java:443`

`Color.fromRGB(128, 0, 128) != null ? ...` — `Color.fromRGB` never returns null. Simplify to just the value.

---

## Phase 4 — Content / Feature Gaps

### 4.1 Fix Corrosive Scythe display name

**File:** `codex/CodexFeature.java:837-838`

```java
.displayName("Armor Scythe")       // ← overwritten by next line
.displayName("Venomous Scythe")    // ← wins
```

**Fix:** Change to a single `.displayName("Corrosive Scythe")` and ensure `ItemsInCodex.md` refers to it consistently as "Corrosive Scythe". The description should mention "degrades enemy armor on hit."

Also check `ArcaneItems.java` for the `createVenomousScythe()` method and rename that to `createCorrosiveScythe()`.

### 4.2 Fix multiblock structures (Auto Sifter, Auto Smelter, Block Duplicator)

**File:** `codex/tasks/CodexPassiveTask.java:475-499`

**Problem:** The automation scanner doesn't differentiate between regular droppers and those intended for auto-machines. The smelting/sifting logic is partial.

**Fix approach:**
- **Auto Smelter:** When a dropper is found beneath a hopper adjacent to a furnace, scan the dropper for smeltable ores, pull fuel from the furnace's fuel slot, smelt items and output to the hopper/furnace result slot.
- **Auto Sifter:** When a dropper is found beneath a hopper adjacent to a cauldron, scan for sand/gravel, produce flint/clay/iron nuggets at defined intervals.
- **Block Duplicator:** Already partially implemented in `CodexCrafting.executeBlockDuplicatorCraft()` — needs to be wired to the passive task's automation tick.

**Implementation:**
- Add structure-check methods for each auto-machine in `CodexCrafting`
- In `CodexPassiveTask.runAutomations()`, check block patterns around each dropper to classify it, then delegate to the appropriate handler

### 4.3 Wire up waypoint teleport plates

**Files:** `feature/WaypointFeature.java`, `data/DataManager.java` (teleportPlates map), `codex/gui/CodexGuiListener.java`

**Problem:** The `teleportPlates` map is saved/loaded but never populated or triggered.

**Fix:**
1. Add a `PlayerInteractEvent` handler (or add to `CodexInteractionListener`) that detects when a player right-clicks a pressure plate while holding a Waypoint Compass
2. Save the plate location to `dataManager.teleportPlates` with the waypoint name as key
3. Add a `PlayerMoveEvent` check (in `CodexPlayerListener`) that detects when a player stands on a linked pressure plate and teleports them to the waypoint

### 4.4 Lightning Essence crafting station

**File:** `codex/CodexFeature.java:1339-1347`

**Problem:** Lightning Essence uses `ZOMBIE_HEAD` as its crafting station, which translates to "Mob Drop" in the recipe GUI. This is completely undiscoverable — a player would never guess they need to throw items at a Zombie Head (or place it somewhere specific).

**The fix depends on the intended mechanic.** If it's supposed to be:
- **A natural drop:** Remove the crafting station requirement and make it a mob drop from lightning-related mobs, or make the recipe use a natural method (e.g., the recipe with all-null ingredients means it's automatic / uncraftable — suggest making it a rare drop from Creepers struck by lightning, or from a new multiblock)
- **A specific structure:** Define a new multiblock (e.g., a Lightning Rod on top of a Dropper on top of Copper Blocks) and use `machinery.{new_id}` as the station
- **Keep as-is:** Add clear lore to the item in the Codex explaining "Acquired by putting a Creeper Head on a Lightning Rod during a thunderstorm" (or similar discoverable mechanic)

**Recommendation:** Create a new multiblock "Lightning Catcher" (Copper Rod above Dropper above 3x3 Copper Block base) and assign it as the crafting station.

---

## Phase 5 — Performance

### 5.1 Optimize `refreshItemLore`

**File:** `codex/listeners/CodexPlayerListener.java:90-114`

**Problem:** Called on every inventory close/click, scanning all slots. Causes tick lag.

**Fix:**
- Add a dirty flag: only refresh items that have actually changed (compare before/after)
- Cache the lore result per item hash and only regenerate when the PDC data changes
- Remove the 1-tick delayed duplicate call (the `runDelayed` in `onInventoryClick`)
- At most refresh once per tick per player, not on every event

### 5.2 Optimize `tickCatchFlameSpread`

**File:** `codex/tasks/CodexPassiveTask.java:193-224`

**Problem:** Iterates ALL entities in ALL worlds every 10 ticks.

**Fix:**
- Use `world.getEntitiesByClass(Player.class)` to only scan players
- Track entities that are actually on fire with a metadata flag to avoid re-scanning
- Use a spatial index or partition the scan

### 5.3 Optimize `runAutomations` block scanning

**File:** `codex/tasks/CodexPassiveTask.java:475-499`

**Problem:** Scans 2601+ blocks per player every 100 ticks.

**Fix:**
- Cache known dropper locations in a map, only re-scan when a player places/breaks a block (listen to `BlockPlaceEvent` / `BlockBreakEvent`)
- Or reduce the scan radius from 17×9×17 to a smaller window
- Or use chunk-based scanning instead of per-player

### 5.4 Incremental `DataManager.save()`

**File:** `data/DataManager.java:49-84`

**Problem:** Full YAML serialization of ALL players on every single change.

**Fix:**
- Track which players have unsaved changes with a `Set<UUID> dirtyPlayers`
- Only serialize changed players on each save
- Perform a full save every N saves or on server shutdown
- Use a more efficient serialization format (e.g. JSON with a streaming writer) or batch writes

### 5.5 Final performance audit

After all Phase 1-4 changes are complete, re-run a performance audit:
- Profile the new Void Bag Refund code path
- Profile the new `/waypoint list` rendering
- Profile the auto-machine automation ticks
- Profile the teleport plate `PlayerMoveEvent` check
- Ensure no new listener adds per-tick O(n) scanning

---

## Progress Status

| Phase | Status | Details |
|-------|--------|---------|
| Phase 1 — Critical | ✅ Complete | Blood Altar fix, Void Bag Refund item |
| Phase 2 — UX | ✅ Complete | Navigation particles, toggle colors, potion resistance action bar + count, `/waypoint list`, diamond confirmation, crafting station descriptions |
| Phase 3 — Code Quality | ✅ Complete | Underscore naming, dead ternary, toSmallCaps fix, if-else→Maps, CodexContext record, hasRequirement dedup, initRegistry split into 4 methods |
| Phase 4 — Content | ⏳ Pending | Corrosive Scythe rename, multiblock logic, teleport plates, Lightning Essence |
| Phase 5 — Performance | ⏳ Pending | refreshItemLore, catch flame, automation scans, incremental save |

---

## Summary of All Changes by File

| File | Phase | Change |
|------|-------|--------|
| `feature/PotionResistanceFeature.java` | 1 | Use action bar + count suffix for messages |
| `codex/crafting/CodexCrafting.java` | 1 | `setHealth(0)` → `damage()` |
| `codex/listeners/CodexPlayerListener.java` | 1, 5 | Void Bag refund capture; optimize refreshItemLore |
| `codex/gui/VoidBagInventoryHolder.java` | 1 | Add refund mechanics |
| `feature/WaypointFeature.java` | 2, 4 | Add `/waypoint list`, add confirmation prompt, wire teleport plates |
| `feature/NavigationFeature.java` | 2 | Move particles further from player |
| `feature/ActionBarFeature.java` | 2 | Consistent toggle colors |
| `codex/gui/CodexRecipeGui.java` | 2 | Add station description lore |
| `codex/CodexFeature.java` | 3, 4 | Split initRegistry, fix scythe name, fix Lightning Essence |
| `codex/items/ArcaneItems.java` | 3 | Map-based getCustomItem, fix ternary |
| `codex/items/ExplorerItems.java` | 3 | Map-based getCustomItem |
| `codex/core/CodexContext.java` | 3 | New context object (new file) |
| `codex/gui/CodexCategoryGui.java` | 3 | Remove duplicate hasRequirement |
| `codex/gui/CodexGuiListener.java` | 3 | Remove duplicate hasRequirement |
| `util/TextUtil.java` | 3 | Fix toSmallCaps uppercase, shared hasRequirement |
| `PlayerSettings.java` | 3 | Encapsulate fields |
| `ArcanumPlugin.java` | 3 | Rename underscore fields |
| `data/DataManager.java` | 4, 5 | Wire teleport plates, incremental save |
| `codex/listeners/CodexInteractionListener.java` | 4 | Plate linking handler |
| `codex/tasks/CodexPassiveTask.java` | 4, 5 | Fix auto-machine logic, optimize scans |
| `codex/listeners/CodexCombatListener.java` | 3 | Use CodexContext |

---

## Priority Order for Implementation

1. **Phase 1** (Critical) — Data loss, stability, new item
2. **Phase 3** (Code Quality) — Enables cleaner Phase 2/4/5 work
3. **Phase 2** (UX) — Player-facing improvements
4. **Phase 4** (Content) — Filling feature gaps
5. **Phase 5** (Performance) — Optimization, final audit
