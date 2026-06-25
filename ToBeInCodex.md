# 📖 Future Codex Items (Proposed Enchantments & Utilities)

This document tracks upcoming enchantment runes, tools, and items planned for future releases. Recipes and unlock criteria are TBD (to be configured).

---

## 🔮 Custom Enchantment Runes

### 1. Corrosive Slash (Axe)
- **Item ID**: `arcane.runes.corrosive_slash`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 4
- **Description**: Reduces target armor toughness rating in stacks (up to 5 stacks max, stacks expire in 15 seconds). Level scales the duration of the reduction up to a maximum of 10 seconds at Level 4.

### 2. Scorch (Sword)
- **Item ID**: `arcane.runes.scorch`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1 (No levels)
- **Description**: Reduces target armor in stacks (up to 10 stacks max, reducing armor by 90% at max stacks) for a short duration. Secondary effect: deals 1 tick of fire damage on hit. Balanced and not overpowered.

### 3. Dwarf's Blessing (Tools)
- **Item ID**: `explorer.runes.dwarfs_blessing`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Applicable To**: Pickaxe, Shovel, Axe
- **Description**: Automatically smelts all blocks mined. When breaking logs with an axe, drops Charcoal instead of wood.

### 4. Glacial Thorns (Armor)
- **Item ID**: `arcane.runes.glacial_thorns`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 5
- **Description**: Enemies attacking you have a chance to be afflicted with Weakness. Level increases the application chance and severity. At Level 5, Weakness II is guaranteed.

### 5. Tidal Sweep (Swords)
- **Item ID**: `arcane.runes.tidal_sweep`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 3
- **Description**: Unleashes a larger, longer-reaching sweeping sweep but with a narrower angle of spread.
- **Incompatibilities**: Incompatible with `Sweeping Edge`. (This incompatibility must be explicitly documented in the Codex, on the item description, and return an error message on fusion attempt).

### 6. Seismic Landing (Boots)
- **Item ID**: `explorer.runes.seismic_landing`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 4
- **Description**: Negates up to 5 hearts of fall damage. Instead of taking the damage, landing heavily unleashes a massive shockwave that damages nearby entities and launches them into the air (damage and launch height scales with fall distance).
- **Incompatibilities**: Incompatible with `Feather Falling`.

### 7. Photosynthesis (Any Durability Gear)
- **Item ID**: `arcane.runes.photosynthesis`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Slowly restores item durability over time while the player stands on Dirt or Grass blocks and has direct line-of-sight to the sun.

### 8. Zephyr (Bows & Crossbows)
- **Item ID**: `explorer.runes.zephyr`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Gravity has no effect on fired arrows. Perfect for sniping over infinite distances.

### 9. Artemis's Blessing (Bows)
- **Item ID**: `arcane.runes.artemis_blessing`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Silences bow shot sounds (silent shooting) and adds +5 levels of Punch through.

### 10. Static Charge (Chestplate)
- **Item ID**: `arcane.runes.static_charge`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 7
- **Description**: Moving around generates an electrical charge. When fully charged, a revolving spark particle encircles the player, and their next attack deals a fixed +25% bonus damage. Higher levels decrease the travel distance required to reach full charge.

### 11. Phantom Backstab (Swords)
- **Item ID**: `arcane.runes.phantom_backstab`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 4
- **Description**: Attacking targets has a 50% chance to summon a spectral sword behind the target, striking them after 1 second. Spectral sword damage scales from 20% (Level 1) to 100% (Level 4) of your base attack damage.

### 12. Telekinesis (Tools)
- **Item ID**: `explorer.runes.telekinesis`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Teleports mined blocks and dropped experience orbs directly into the player's inventory.

### 13. Kinetic Rebound (Shields)
- **Item ID**: `explorer.runes.kinetic_rebound`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Blocking physical projectiles (arrows, tridents, fireballs) has a high chance to rebound them directly back towards the source entity.

### 14. Redirection (Armor)
- **Item ID**: `arcane.runes.redirection`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 6
- **Description**: Redirects a percentage of incoming damage to nearby tamed pets (dogs, cats, horses). Percentage scales with level, up to a maximum of 40% damage redirected at Level 6.

### 15. Bleed (Swords & Axes)
- **Item ID**: `arcane.runes.bleed`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 8
- **Description**: Applies a poison-like bleeding effect that continuously drains health. Levels affect the proc chance and duration. Multiple hits apply stacks that increase bleed potency.
- **Crafting method**: Crafted on the Blood Altar using 6 Blood Vials and a Wither Skeleton Skull.

### 16. Poison Spores (Swords & Axes)
- **Item ID**: `arcane.runes.poison_spores`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 5
- **Description**: Applies a standard Poison effect on hit. Level scales the poison duration and chance to trigger.

### 17. Gas Cloud (Armor)
- **Item ID**: `arcane.runes.gas_cloud`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 4
- **Description**: Dealing or taking damage has a chance to release a lingering gas cloud at your location. Enemies entering the cloud take continuous decay damage. Duration and damage scale with level.

### 18. Heavy Draw (Bows)
- **Item ID**: `arcane.runes.heavy_draw`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 5
- **Description**: Increases arrow draw time by 50% but raises final arrow damage by a scaling percentage (up to +50% at Level 5).

### 19. Timber (Axes)
- **Item ID**: `explorer.runes.timber`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Breaking one log block instantly breaks all connected logs in the same tree structure. Safe checks ensure placed structure blocks are not broken.

### 20. Soul Harvester (Hoes)
- **Item ID**: `arcane.runes.soul_harvester`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Killing enemies with the equipped hoe has a small chance to drop a physical "Soul Orb" item, which can be right-clicked to consume for a massive burst of experience points.

### 21. Crude Sharpness (Tools & Sticks)
- **Item ID**: `explorer.runes.crude_sharpness`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 7
- **Description**: Adds flat bonus melee damage similar to Sharpness. Max level 7 damage equals vanilla Sharpness V. Works on any tool and sticks.

### 22. Basalt Trail (Boots)
- **Item ID**: `explorer.runes.basalt_trail`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Frost walker equivalent for lava. Cools lava blocks directly beneath your feet into temporary Basalt blocks, allowing walking across lava lakes.
- **Requirements**: Requires custom item **Core of Heat** to unlock/craft.

### 23. Void Walker (Boots)
- **Item ID**: `explorer.runes.void_walker`
- **Material**: `FIREWORK_STAR`
- **Max Level**: 1
- **Description**: Freefalling 20 blocks triggers a chorus fruit teleportation effect, instantly rescuing you to a safe ground location. Designed as a critical safety net to prevent void deaths.

---

## 🎒 Custom Materials & Utilities

### 24. Core of Heat
- **Item ID**: `explorer.materials.core_of_heat`
- **Material**: `MAGMA_CREAM` (glowing)
- **Description**: An intensely hot orb forged by compressing Magma Blocks and Blaze Powder. Required to craft the Basalt Trail rune.

---

## 🏗️ Automated Machinery

### 25. Block Duplicator
- **Item ID**: `machinery.block_duplicator`
- **Material**: `NETHER_BRICK_FENCE`
- **Description**: An advanced machine that duplicates construction materials. Place building blocks (e.g. Block of Quartz, Deepslate blocks, Concrete, etc.) inside the dropper and right-click the Nether Brick Fence to duplicate them into full stacks (64) expelled above the fence.
  - **Fuel Cost**: Requires Lava Buckets placed in the surrounding furnaces/blast furnaces. One Lava Bucket duplicates up to 2 slots (stacks) of blocks.
  - **Multi-slot Duplication**: If multiple slots in the dropper are filled with valid blocks, it duplicates all of them in a single activation, consuming lava fuel proportionally (e.g., 1 Lava Bucket for 1-2 stacks, 2 Lava Buckets for 3-4 stacks, etc.).
- **Structure Layout**:
  - **Top block**: Nether Brick Fence (acts as the button/trigger).
  - **Middle block**: Dropper (below the fence).
  - **Adjacent blocks**: Dropper is surrounded horizontally by Furnaces and/or Blast Furnaces.
