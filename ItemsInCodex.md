# 📖 Items in Codex

This document lists all the custom items that have been fully created and registered in the Codex system.

---

## 🏗️ Crafting Stations (Multiblock Structures)

### 1. Arcana Table
- **Structure**: Crafting Table on top of a Dropper, with at least 2 Bookshelves horizontally adjacent to the Dropper.
- **Description**: Used to craft advanced Arcane items by placing ingredients in the Dropper and shift-right-clicking the Crafting Table.

### 2. Heavy Forge
- **Structure**: Crafting Table on top of a Blast Furnace on top of a Dropper.
- **Description**: Used to forge advanced Explorer tools by placing ingredients in the Dropper and shift-right-clicking the Crafting Table.

### 3. Upgrade Table
- **Structure**: Anvil on top of a Dropper on top of a Bookshelf.
- **Description**: Used to upgrade and combine items (such as runes) by placing ingredients in the Dropper and shift-right-clicking the Anvil.

---

## 🔮 Arcane Category

### 4. Lifesteal Rune I, II, III
- **Item IDs**: `arcane.lifesteal_rune`, `arcane.lifesteal_rune_2`, `arcane.lifesteal_rune_3`
- **Material**: `FIREWORK_STAR`
- **Description**: Applied to swords to heal on hit (heals 15%, 30%, 45% of damage respectively).

### 5. Speed Rune I, II, III
- **Item IDs**: `arcane.speed_rune`, `arcane.speed_rune_2`, `arcane.speed_rune_3`
- **Material**: `FIREWORK_STAR`
- **Description**: Applied to boots to grant Speed I, II, or III respectively.

### 5a. Catch Flame Rune I, II, III
- **Item IDs**: `arcane.runes.catch_flame`, `arcane.runes.catch_flame_2`, `arcane.runes.catch_flame_3`
- **Material**: `FIREWORK_STAR`
- **Description**: Applied to weapons. Sword automatically gets Fire Aspect II; Bow automatically gets Flame I. Passive fire spread chance: 33%, 66%, 100% respectively.

### 6. Speed Boots
- **Item ID**: `arcane.speed_boots`
- **Material**: `DIAMOND_BOOTS`
- **Description**: Pre-infused boots with Speed I.

### 7. Wand of Embers
- **Item ID**: `arcane.wand_of_embers`
- **Material**: `BLAZE_ROD`
- **Description**: Right-click to shoot fireballs.

### 8. Staff of Supplant
- **Item ID**: `arcane.ranged.staff_of_supplant`
- **Material**: `IRON_HOE` (custom model)
- **Description**: Projectile swaps player position with the target entity on hit.

### 9. Amulet of the Phoenix
- **Item ID**: `arcane.trinkets.amulet_of_the_phoenix`
- **Material**: `MAGMA_CREAM` (glowing)
- **Description**: Totem recovery with a fiery explosion knockback on death. 3 uses.

### 10. Totem of Fallacy
- **Item ID**: `arcane.trinkets.totem_of_fallacy`
- **Material**: `TOTEM_OF_UNDYING`
- **Description**: Teleports you to spawn at the cost of reducing all inventory items' durability to 10%.

### 11. Frostbite Ring
- **Item ID**: `arcane.trinkets.frostbite_ring`
- **Material**: `DIAMOND` (custom model)
- **Description**: Freezes water under your feet; extinguishes burning entities on hit.

### 12. Shadow Cloak
- **Item ID**: `arcane.armor.shadow_cloak`
- **Material**: `BLACK_BANNER` (wearable)
- **Description**: Invisibility + Speed I when light level < 7.

### 13. Superior Shadow Cloak
- **Item ID**: `arcane.armor.superior_shadow_cloak`
- **Material**: `BLACK_BANNER` (infused)
- **Description**: Invisibility + Speed II + Swift Sneak while crouched regardless of light level.

### 14. Venomous Scythe
- **Item ID**: `arcane.melee.venomous_scythe`
- **Material**: `IRON_HOE` (custom model)
- **Description**: Stacks poison (up to 5) and deals bonus damage to poisoned mobs.

### 15. Stormcaller Medallion
- **Item ID**: `arcane.trinkets.stormcaller_medallion`
- **Material**: `SUNFLOWER`
- **Description**: 15% lightning strike chance on hit during storms.

### 16. Wand of Transmutation
- **Item ID**: `arcane.ranged.wand_of_transmutation`
- **Material**: `BONE` (glowing)
- **Description**: Beam turns hostile mobs into passive farm animals for 15s.

### 17. Vitality Geode I
- **Item ID**: `arcane.trinkets.vitality_geode_1`
- **Material**: `AMETHYST_CLUSTER`
- **Description**: Increases max health by +4 (2 hearts) while in inventory.

### 18. Vitality Geode II
- **Item ID**: `arcane.trinkets.vitality_geode_2`
- **Material**: `AMETHYST_CLUSTER` (glowing)
- **Description**: Increases max health by +10 (5 hearts).

### 19. Vitality Geode III
- **Item ID**: `arcane.trinkets.vitality_geode_3`
- **Material**: `AMETHYST_CLUSTER` (bold)
- **Description**: Increases max health by +20 (10 hearts).

---

## 🧭 Explorer Category

### 20. Waypoint Compass
- **Item ID**: `explorer.waypoint_compass`
- **Material**: `COMPASS`
- **Description**: Teleports you to its linked waypoint.

### 21. Iron Grappling Hook
- **Item ID**: `explorer.grappling_hook.iron`
- **Material**: `LEAD`
- **Description**: Range: 10 blocks. Pulls you to clicked blocks.

### 22. Diamond Grappling Hook
- **Item ID**: `explorer.grappling_hook.diamond`
- **Material**: `LEAD`
- **Description**: Range: 25 blocks.

### 23. Netherite Grappling Hook
- **Item ID**: `explorer.grappling_hook.netherite`
- **Material**: `LEAD`
- **Description**: Range: 50 blocks.

### 24. Spelunker's Helmet
- **Item ID**: `explorer.armor.spelunkers_helmet`
- **Material**: `GOLDEN_HELMET`
- **Description**: Permanent Night Vision + glows hostile mobs in 15-block radius.

### 25. Depth Strider Flippers
- **Item ID**: `explorer.gadgets.depth_strider_flippers`
- **Material**: `LEATHER_BOOTS` (blue)
- **Description**: Swim speed & water breathing on water; slowness on land.

### 26. Builder's Wand
- **Item ID**: `explorer.tools.builders_wand`
- **Material**: `STICK`
- **Description**: Places up to 9 matching blocks in a line.

### 27. Webber
- **Item ID**: `explorer.tools.webber`
- **Material**: `COBWEB` (glowing)
- **Description**: Shoots temporary cobwebs (lasts 5s, breakable by sword with no drops).

### 28. Iron Web Slinger
- **Item ID**: `explorer.tools.web_slinger.iron`
- **Material**: `LEAD` (glowing)
- **Description**: Upgraded Iron Grappling Hook. Range 10. Places a temporary fall-breaking cobweb where you land.

### 29. Diamond Web Slinger
- **Item ID**: `explorer.tools.web_slinger.diamond`
- **Material**: `LEAD` (glowing)
- **Description**: Range 25. Places a temporary fall-breaking cobweb where you land.

### 30. Netherite Web Slinger
- **Item ID**: `explorer.tools.web_slinger.netherite`
- **Material**: `LEAD` (glowing)
- **Description**: Range 50. Places a temporary fall-breaking cobweb where you land.

### 31. Thermal Canteen
- **Item ID**: `explorer.gadgets.thermal_canteen`
- **Material**: `GLASS_BOTTLE` (glowing)
- **Description**: Cures effects & restores hunger. Max 4 charges. Refill at campfire/lava.

### 32. Beastmaster's Flute
- **Item ID**: `explorer.tools.beastmasters_flute`
- **Material**: `BAMBOO` (custom model)
- **Description**: Pacifies/sleeps hostiles in 5-block radius. Cooldown 60s.

### 33. Excavation Drill
- **Item ID**: `explorer.tools.excavation_drill`
- **Material**: `IRON_PICKAXE` (custom model)
- **Description**: Heavy mining tool that breaks blocks in a 3x3 pattern.

### 34. Portable Smelter
- **Item ID**: `explorer.tools.portable_smelter`
- **Material**: `BLAST_FURNACE`
- **Description**: Open a portable blast furnace. Returns unsmelted items on close.

### 35. Portable Furnace
- **Item ID**: `explorer.tools.portable_furnace`
- **Material**: `FURNACE`
- **Description**: Open a portable furnace. Returns unsmelted items on close.

### 36. Portable Crafting Table
- **Item ID**: `explorer.tools.portable_crafting_table`
- **Material**: `CRAFTING_TABLE`
- **Description**: Open crafting grid anywhere.

### 37. Portable Anvil
- **Item ID**: `explorer.tools.portable_anvil`
- **Material**: `ANVIL`
- **Description**: Open anvil screen anywhere.

### 38. Portable Smithing Table
- **Item ID**: `explorer.tools.portable_smithing_table`
- **Material**: `SMITHING_TABLE`
- **Description**: Open smithing table anywhere.

### 39. Portable Grindstone
- **Item ID**: `explorer.tools.portable_grindstone`
- **Material**: `GRINDSTONE`
- **Description**: Open grindstone anywhere.

### 40. Portable Stonecutter
- **Item ID**: `explorer.tools.portable_stonecutter`
- **Material**: `STONECUTTER`
- **Description**: Open stonecutter anywhere.

---

## 🧬 Custom Materials & Ingredients

### 41. Ender Essence
- **Item ID**: `arcane.materials.ender_essence`
- **Material**: `ENDER_PEARL` (glowing)
- **Description**: Condensed ender magic for advanced crafting.

### 42. Potent Spider Web
- **Item ID**: `arcane.materials.potent_spider_web`
- **Material**: `COBWEB` (glowing)
- **Description**: Highly sticky web used for binding tools (like Webber).

### 43. Hardened Coal I
- **Item ID**: `arcane.materials.hardened_coal_1`
- **Material**: `COAL` (glowing)
- **Description**: Press-forged coal reagent.

### 44. Hardened Coal II
- **Item ID**: `arcane.materials.hardened_coal_2`
- **Material**: `COAL` (glowing)
- **Description**: Moderately crystallized coal.

### 45. Hardened Coal Max
- **Item ID**: `arcane.materials.hardened_coal_max`
- **Material**: `COAL` (glowing)
- **Description**: Max compressed coal ready for synthetic diamonds.

### 46. Hardened Base I
- **Item ID**: `arcane.materials.hardened_base_1`
- **Material**: `CLAY_BALL` (glowing)
- **Description**: Forged clay-copper reagent.

### 47. Hardened Base II
- **Item ID**: `arcane.materials.hardened_base_2`
- **Material**: `CLAY_BALL` (glowing)
- **Description**: Moderately pressed clay-copper compound.

### 48. Hardened Base Max
- **Item ID**: `arcane.materials.hardened_base_max`
- **Material**: `CLAY_BALL` (glowing)
- **Description**: Max compressed compound ready for synthetic emeralds.

### 49. Synthetic Diamond
- **Item ID**: `arcane.materials.synthetic_diamond`
- **Material**: `DIAMOND` (glowing)
- **Description**: Dense artificially forged diamond.

### 50. Synthetic Emerald
- **Item ID**: `arcane.materials.synthetic_emerald`
- **Material**: `EMERALD` (glowing)
- **Description**: Artificial pressurized emerald.

### 51. Echoing Core
- **Item ID**: `arcane.materials.echoing_core`
- **Material**: `HEART_OF_THE_SEA` (glowing)
- **Description**: Resonating deep-sea core.

### 52. Immolation Totem
- **Item ID**: `arcane.materials.immolation_totem`
- **Material**: `TOTEM_OF_UNDYING` (glowing)
- **Description**: A fire reagent made with gold and lava buckets in the Arcana Table. Consuming it returns empty Buckets.

