# 📖 Proposed Codex Items (To Be Added)

This document details custom items planned for future implementation in the **Codex System** under the **Arcane** and **Explorer** categories.

---

## 🔮 Arcane Category

### 2. Wand of Levitation
*A magical wand that shoots a projectile rendering targets weightless.*
- **Item ID**: `arcane.ranged.wand_of_levitation`
- **Sub-category**: `arcane.ranged`
- **Material**: `FEATHER` (custom model / glowing display)
- **Unlock XP Cost**: 15 levels
- **Unlock Requirement**: `SHULKER_SHELL` (1) in inventory
- **Description**: Right-click to shoot a levitation bolt. Targets hit are given Levitation I for 5 seconds. Cooldown: 15 seconds.
- **Crafting Station**: Arcana Table (`machinery.arcana_table`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `SHULKER_SHELL` | `ENDER_PEARL` | `SHULKER_SHELL` |
  | `FEATHER` | `BLAZE_ROD` | `FEATHER` |
  | `FEATHER` | `BLAZE_ROD` | `FEATHER` |

### 3. Gale Chestplate
*Mystical infused armor allowing weightless flight and fall negation.*
- **Item ID**: `arcane.armor.gale_chestplate`
- **Sub-category**: `arcane.armor`
- **Material**: `DIAMOND_CHESTPLATE` (infused)
- **Unlock XP Cost**: 18 levels
- **Unlock Requirement**: `PHANTOM_MEMBRANE` (2) in inventory
- **Description**: Gives permanent Slow Falling I while worn. Allows double jumping (press jump in mid-air to boost upward, 10-second cooldown).
- **Crafting Station**: Arcana Table (`machinery.arcana_table`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `PHANTOM_MEMBRANE` | `DIAMOND_CHESTPLATE` | `PHANTOM_MEMBRANE` |
  | `FEATHER` | `EMERALD` | `FEATHER` |
  | `FEATHER` | `FEATHER` | `FEATHER` |

### 4. Siphon Blade
*A cursed blade that feeds on the life force of fallen foes.*
- **Item ID**: `arcane.melee.siphon_blade`
- **Sub-category**: `arcane.melee`
- **Material**: `DIAMOND_SWORD` (infused)
- **Unlock XP Cost**: 25 levels
- **Unlock Requirement**: `NETHER_STAR` (1) in inventory
- **Description**: Diamond sword that restores 15% of your max health upon killing an enemy.
- **Crafting Station**: Arcana Table (`machinery.arcana_table`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `NETHER_STAR` | `DIAMOND_SWORD` | `NETHER_STAR` |
  | `GHAST_TEAR` | `SOUL_SAND` | `GHAST_TEAR` |
  | `REDSTONE` | `REDSTONE` | `REDSTONE` |

---

## 🧭 Explorer Category

### 5. Waypoint Teleport Plate
*A ground-based teleportation structure that interacts with Waypoint Compasses.*
- **Item ID**: `explorer.navigation.teleportation_plate`
- **Sub-category**: `explorer.navigation`
- **Material**: `HEAVY_WEIGHTED_PRESSURE_PLATE`
- **Unlock XP Cost**: 14 levels
- **Unlock Requirement**: `ENDER_EYE` (1) in inventory
- **Description**: Right-click the placed plate with a Waypoint Compass to link it. When any player stands on the plate, they are instantly teleported to that linked waypoint.
- **Crafting Station**: Heavy Forge (`machinery.heavy_forge`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `ENDER_PEARL` | `HEAVY_WEIGHTED_PRESSURE_PLATE` | `ENDER_PEARL` |
  | `IRON_INGOT` | `REDSTONE_BLOCK` | `IRON_INGOT` |
  | `IRON_INGOT` | `IRON_INGOT` | `IRON_INGOT` |

### 6. Slime Boots
*Bouncy footgear designed to bypass gravity's limits.*
- **Item ID**: `explorer.gadgets.slime_boots`
- **Sub-category**: `explorer.gadgets`
- **Material**: `SLIME_BLOCK` (rendered as boots)
- **Unlock XP Cost**: 10 levels
- **Unlock Requirement**: `SLIME_BALL` (1) in inventory
- **Description**: Completely negates all fall damage and bounces the player upward based on fall velocity.
- **Crafting Station**: Heavy Forge (`machinery.heavy_forge`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `SLIME_BALL` | `null` | `SLIME_BALL` |
  | `SLIME_BLOCK` | `null` | `SLIME_BLOCK` |
  | `IRON_BOOTS` | `null` | `IRON_BOOTS` |

### 7. Ore Scanner
*An exploration gadget capable of detecting nearby ore veins.*
- **Item ID**: `explorer.exploration.ore_scanner`
- **Sub-category**: `explorer.exploration`
- **Material**: `SPYGLASS`
- **Unlock XP Cost**: 12 levels
- **Unlock Requirement**: `AMETHYST_SHARD` (4) in inventory
- **Description**: Right-click to highlight nearby valuable ores (Diamonds, Gold, Iron) in a 10-block radius for 5 seconds. Cooldown: 30 seconds.
- **Crafting Station**: Heavy Forge (`machinery.heavy_forge`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `AMETHYST_SHARD` | `SPYGLASS` | `AMETHYST_SHARD` |
  | `GOLD_INGOT` | `REDSTONE` | `GOLD_INGOT` |
  | `IRON_INGOT` | `IRON_INGOT` | `IRON_INGOT` |

### 8. Magnetic Ring
*A passive trinket that attracts nearby drops.*
- **Item ID**: `explorer.gadgets.magnetic_ring`
- **Sub-category**: `explorer.gadgets`
- **Material**: `GOLD_NUGGET`
- **Unlock XP Cost**: 8 levels
- **Unlock Requirement**: `IRON_INGOT` (4) in inventory
- **Description**: While held in inventory, pulls dropped items within a 5-block radius toward you.
- **Crafting Station**: Heavy Forge (`machinery.heavy_forge`)
- **Crafting Recipe**:
  | Column 1 | Column 2 | Column 3 |
  | :--- | :--- | :--- |
  | `GOLD_NUGGET` | `IRON_INGOT` | `GOLD_NUGGET` |
  | `IRON_INGOT` | `REDSTONE` | `IRON_INGOT` |
  | `GOLD_NUGGET` | `IRON_INGOT` | `GOLD_NUGGET` |
