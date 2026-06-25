# 📖 Items in Codex

This document lists all the custom items, runes, structures, and tools that have been fully created, registered, and are active in the Codex system.

---

## 🏗️ Crafting Stations & Machinery (Multiblock Structures)

### 1. Arcana Table
- **Item ID**: `machinery.arcana_table` (Reference item)
- **Structure**: Crafting Table horizontally adjacent to at least 2 Bookshelves, all on top of a Dropper.
- **Description**: Used to craft mystical Arcane items and Enchantment Runes.

### 2. Heavy Forge
- **Item ID**: `machinery.heavy_forge` (Reference item)
- **Structure**: Crafting Table placed directly on top of a Blast Furnace on top of a Dropper.
- **Description**: Used to forge heavy Explorer tools, gadgets, and compressed alloys.

### 3. Upgrade Table
- **Item ID**: `machinery.upgrade_table` (Reference item)
- **Structure**: Anvil placed on top of a Dropper on top of a Bookshelf.
- **Description**: Used to combine matching Enchantment Runes to raise their level.

### 4. Block Duplicator
- **Item ID**: `machinery.block_duplicator` (Reference item)
- **Structure**: Nether Brick Fence on top of a Dropper surrounded horizontally (North, South, East, West) by Furnaces and/or Blast Furnaces.
- **Description**: Expels a full stack of any valid building blocks placed inside the Dropper. Requires Lava Buckets as fuel (1 bucket per 2 stacks).

### 5. Blood Altar
- **Item ID**: `machinery.blood_altar` (Reference item)
- **Structure**: Red Carpet on top of a Dropper on top of an Obsidian block.
- **Description**: Used to perform dark sacrifice rituals (killing the player or reducing HP) to forge Demonium Runes.

### 6. Auto Sifter
- **Item ID**: `machinery.auto_sifter` (Reference item)
- **Structure**: Hopper on top of a Dropper adjacent to a Cauldron.
- **Description**: Passively sifts Sand and Gravel inside the Dropper into raw resources every 5 seconds.

### 7. Auto Smelter
- **Item ID**: `machinery.auto_smelter` (Reference item)
- **Structure**: Hopper on top of a Dropper adjacent to a Furnace.
- **Description**: Passively smelts items inside the Dropper using fuel from its slot.

---

## 🔮 Arcane Category

### 8. Lifesteal Rune I, II, III
- **Item IDs**: `arcane.lifesteal_rune`, `arcane.lifesteal_rune_2`, `arcane.lifesteal_rune_3`
- **Description**: Applied to swords via hand-swapping. On hit, heals the attacker for 15% / 30% / 45% of damage dealt.

### 9. Speed Rune I, II, III
- **Item IDs**: `arcane.speed_rune`, `arcane.speed_rune_2`, `arcane.speed_rune_3`
- **Description**: Applied to boots. Grants passive Speed I, II, or III while equipped.

### 10. Catch Flame Rune I, II, III
- **Item IDs**: `arcane.runes.catch_flame`, `arcane.runes.catch_flame_2`, `arcane.runes.catch_flame_3`
- **Description**: Applied to weapons. Swords get Fire Aspect II, Bows get Flame I. Fires spread to nearby entities with 33% / 66% / 100% chance.

### 11. Demonium Rune I, II, III
- **Item IDs**: `arcane.runes.demonium`, `arcane.runes.demonium_2`, `arcane.runes.demonium_3`
- **Description**: Applied to helmets. Passively ignites all entities in a 2-block radius (excluding the wearer) for 2s / 5s / 10s.

### 12. Corrosive Slash I, II, III, IV (Axe)
- **Item ID**: `arcane.runes.corrosive_slash` (Levels 1-4)
- **Description**: Melee attacks with axes reduce target armor toughness rating (stacks up to 5 times, expires in 15 seconds). Scales up to 10s duration at Level 4.

### 13. Scorch (Sword)
- **Item ID**: `arcane.runes.scorch` (Level 1)
- **Description**: Sword hits reduce enemy armor (stacks up to 10 times, max 90% reduction) and deal 1 tick of fire damage.

### 14. Glacial Thorns I, II, III, IV, V (Armor)
- **Item ID**: `arcane.runes.glacial_thorns` (Levels 1-5)
- **Description**: Attackers have a chance to be afflicted with Weakness. Level 5 guarantees Weakness II.

### 15. Tidal Sweep I, II, III (Swords)
- **Item ID**: `arcane.runes.tidal_sweep` (Levels 1-3)
- **Description**: Adds wider sweep damage. Incompatible with Sweeping Edge.

### 16. Photosynthesis (Durability Gear)
- **Item ID**: `arcane.runes.photosynthesis` (Level 1)
- **Description**: Slowly repairs item durability while standing on dirt/grass under direct sunlight.

### 17. Artemis's Blessing (Bows)
- **Item ID**: `arcane.runes.artemis_blessing` (Level 1)
- **Description**: Silences bow shot sounds and adds +5 levels of Punch.

### 18. Static Charge I, II, III, IV, V, VI, VII (Chestplate)
- **Item ID**: `arcane.runes.static_charge` (Levels 1-7)
- **Description**: Accumulates charge while moving. At 100%, next melee strike deals +25% bonus damage and releases electrical particles.

### 19. Phantom Backstab I, II, III, IV (Swords)
- **Item ID**: `arcane.runes.phantom_backstab` (Levels 1-4)
- **Description**: Attacks have a 50% chance to summon a spectral sword behind the target that strikes for delayed damage after 1 second.

### 20. Bleed I, II, III, IV, V, VI, VII, VIII (Swords & Axes)
- **Item ID**: `arcane.runes.bleed` (Levels 1-8)
- **Description**: Applies ticking bleeding stacks on hit. Crafted on the Blood Altar.

### 21. Poison Spores I, II, III, IV, V (Swords & Axes)
- **Item ID**: `arcane.runes.poison_spores` (Levels 1-5)
- **Description**: Applies standard poison on hit.

### 22. Gas Cloud I, II, III, IV (Armor)
- **Item ID**: `arcane.runes.gas_cloud` (Levels 1-4)
- **Description**: Spawns a decay gas cloud on hit that deals ticking damage to entering enemies.

### 23. Heavy Draw I, II, III, IV, V (Bows)
- **Item ID**: `arcane.runes.heavy_draw` (Levels 1-5)
- **Description**: Arrow draw time is increased by 50%, but final arrow damage is raised by up to +50%.

### 24. Soul Harvester (Hoes)
- **Item ID**: `arcane.runes.soul_harvester` (Level 1)
- **Description**: Kills have a chance to drop Soul Orbs which yield experience.

### 25. Speed Boots
- **Item ID**: `arcane.speed_boots`
- **Description**: Diamond Boots pre-infused with the Speed I effect.

### 26. Shadow Cloak & Superior Shadow Cloak
- **Item IDs**: `arcane.armor.shadow_cloak`, `arcane.armor.superior_shadow_cloak`
- **Description**: Banner-based wearable cloaks. Grant invisibility, speed, and swift sneak under specific light conditions or when crouched.

### 27. Gale Chestplate
- **Item ID**: `arcane.armor.gale_chestplate`
- **Description**: Grants permanent Slow Falling I and allows double jumping.

### 28. Venomous Scythe
- **Item ID**: `arcane.melee.venomous_scythe`
- **Description**: Stacks poison on hit and deals bonus damage to poisoned mobs.

### 29. Siphon Blade
- **Item ID**: `arcane.melee.siphon_blade`
- **Description**: Restores 15% of your max health upon killing an enemy.

### 30. Wand of Embers
- **Item ID**: `arcane.wand_of_embers`
- **Description**: Right-click to fire fireball projectiles.

### 31. Staff of Supplant
- **Item ID**: `arcane.ranged.staff_of_supplant`
- **Description**: Projectile swaps positions of the player and the hit entity.

### 32. Wand of Transmutation
- **Item ID**: `arcane.ranged.wand_of_transmutation`
- **Description**: Beam turns hostile mobs into passive farm animals for 15s.

### 33. Wand of Levitation
- **Item ID**: `arcane.ranged.wand_of_levitation`
- **Description**: Projectile gives Levitation I for 5 seconds to targets.

### 34. Staff of the Stormlord
- **Item ID**: `arcane.ranged.staff_of_the_stormlord`
- **Description**: Strikes a lightning bolt on target block within 20 blocks.

### 35. Amulet of the Phoenix
- **Item ID**: `arcane.trinkets.amulet_of_the_phoenix`
- **Description**: Resurrects player with a fire explosion. Has 3 charges before breaking.

### 36. Totem of Fallacy
- **Item ID**: `arcane.trinkets.totem_of_fallacy`
- **Description**: Teleports you to spawn at the cost of reducing all inventory items' durability to 10%.

### 37. Frostbite Ring
- **Item ID**: `arcane.trinkets.frostbite_ring`
- **Description**: Freezes water under your feet; extinguishes burning entities on hit.

### 38. Stormcaller Medallion
- **Item ID**: `arcane.trinkets.stormcaller_medallion`
- **Description**: Gives a 15% chance to strike lightning on hit during storms.

### 39. Vitality Geode I, II, III
- **Item IDs**: `arcane.trinkets.vitality_geode_1`, `arcane.trinkets.vitality_geode_2`, `arcane.trinkets.vitality_geode_3`
- **Description**: Passively increases maximum health by +4 / +10 / +20 while held in inventory.

---

## 🧭 Exploration Category

### 40. Dwarf's Blessing (Tools)
- **Item ID**: `explorer.runes.dwarfs_blessing` (Level 1)
- **Description**: Automatically smelts mined blocks. Breaks logs into charcoal.

### 41. Seismic Landing I, II, III, IV (Boots)
- **Item ID**: `explorer.runes.seismic_landing` (Levels 1-4)
- **Description**: Negates fall damage up to 5 hearts, releasing a damage shockwave. Incompatible with Feather Falling.

### 42. Zephyr (Bows/Crossbows)
- **Item ID**: `explorer.runes.zephyr` (Level 1)
- **Description**: Removes gravity influence from arrows.

### 43. Telekinesis (Tools)
- **Item ID**: `explorer.runes.telekinesis` (Level 1)
- **Description**: Teleports block drops and XP directly to player inventory.

### 44. Kinetic Rebound (Shields)
- **Item ID**: `explorer.runes.kinetic_rebound` (Level 1)
- **Description**: High chance to reflect blocked projectiles back at shooters.

### 45. Timber (Axes)
- **Item ID**: `explorer.runes.timber` (Level 1)
- **Description**: Breaks entire contiguous tree structures when a single log is broken.

### 46. Crude Sharpness I, II, III, IV, V, VI, VII (Tools & Sticks)
- **Item ID**: `explorer.runes.crude_sharpness` (Levels 1-7)
- **Description**: Adds flat melee damage boost.

### 47. Basalt Trail (Boots)
- **Item ID**: `explorer.runes.basalt_trail` (Level 1)
- **Description**: Cools lava beneath feet into temporary basalt.

### 48. Void Walker (Boots)
- **Item ID**: `explorer.runes.void_walker` (Level 1)
- **Description**: Rescues player back to safe ground if they fall into the void.

### 49. Waypoint Compass
- **Item ID**: `explorer.waypoint_compass`
- **Description**: Coordinates linker that teleports player to waypoint on right-click.

### 50. Waypoint Teleport Plate
- **Item ID**: `explorer.navigation.teleportation_plate`
- **Description**: Pressure plate linked with a Waypoint Compass to teleport players on step.

### 51. Ore Scanner
- **Item ID**: `explorer.exploration.ore_scanner`
- **Description**: Right-click to highlight nearby ores (Diamond, Gold, Iron) with particles.

### 52. Grappling Hooks (Iron, Diamond, Netherite)
- **Item IDs**: `explorer.grappling_hook.iron`, `explorer.grappling_hook.diamond`, `explorer.grappling_hook.netherite`
- **Description**: Lead-based grapple hooks with ranges of 10 / 25 / 50 blocks.

### 53. Webber
- **Item ID**: `explorer.tools.webber`
- **Description**: Crossbow-like tool that fires temporary cobwebs.

### 54. Web Slingers (Iron, Diamond, Netherite)
- **Item IDs**: `explorer.tools.web_slinger.iron`, `explorer.tools.web_slinger.diamond`, `explorer.tools.web_slinger.netherite`
- **Description**: Grappling Hooks upgraded to place temporary safety webs when landing.

### 55. Thermal Canteen
- **Item ID**: `explorer.gadgets.thermal_canteen`
- **Description**: Stores up to 4 charges of campfire heat/lava. Restores hunger and cures effects.

### 56. Beastmaster's Flute
- **Item ID**: `explorer.tools.beastmasters_flute`
- **Description**: pacifies hostile mobs in a 5-block radius for a short duration.

### 57. Excavation Drill
- **Item ID**: `explorer.tools.excavation_drill`
- **Description**: Mining tool that breaks blocks in a 3x3 pattern.

### 58. Ender Backpack
- **Item ID**: `explorer.gadgets.ender_backpack`
- **Description**: Opens ender chest storage remotely.

### 59. Safari Lasso
- **Item ID**: `explorer.gadgets.safari_lasso`
- **Description**: Captures passive mobs into the item, and spawns them back with saved states.

### 60. Void Bag
- **Item ID**: `explorer.gadgets.void_bag`
- **Description**: Opens virtual 9-slot inventory that deletes all items inside upon closing.

### 61. Steam Jetpack
- **Item ID**: `explorer.armor.steam_jetpack`
- **Description**: Sneak in mid-air to receive thruster boost. Fueled by coal.

### 62. Bottle of Lightning
- **Item ID**: `explorer.gadgets.bottle_of_lightning`
- **Description**: A bottle filled with electrical energy. Used to craft staff of stormlord.

### 63. Magnetic Ring
- **Item ID**: `explorer.gadgets.magnetic_ring`
- **Description**: Pulls dropped items within 5 blocks towards the player.

### 64. Portable Utility Screens
- **Item IDs**: 
  - `explorer.tools.portable_smelter`
  - `explorer.tools.portable_furnace`
  - `explorer.tools.portable_crafting_table`
  - `explorer.tools.portable_anvil`
  - `explorer.tools.portable_smithing_table`
  - `explorer.tools.portable_grindstone`
  - `explorer.tools.portable_stonecutter`
- **Description**: Access work block screens directly from inventory anywhere.

---

## 🧬 Custom Materials & Reagents

### 65. Ender Essence
- **Item ID**: `arcane.materials.ender_essence`
- **Description**: Crafting material made of 9 Ender Pearls.

### 66. Potent Spider Web
- **Item ID**: `arcane.materials.potent_spider_web`
- **Description**: Highly sticky cobweb compound.

### 67. Hardened Coal I, II, Max
- **Item IDs**: `arcane.materials.hardened_coal_1`, `arcane.materials.hardened_coal_2`, `arcane.materials.hardened_coal_max`
- **Description**: Pressurized coal grades.

### 68. Hardened Base I, II, Max
- **Item IDs**: `arcane.materials.hardened_base_1`, `arcane.materials.hardened_base_2`, `arcane.materials.hardened_base_max`
- **Description**: Compressed clay-copper reagents.

### 69. Synthetic Diamond & Synthetic Emerald
- **Item IDs**: `arcane.materials.synthetic_diamond`, `arcane.materials.synthetic_emerald`
- **Description**: Forged gemstones.

### 70. Echoing Core
- **Item ID**: `arcane.materials.echoing_core`
- **Description**: Resonating deep-sea core.

### 71. Immolation Totem
- **Item ID**: `arcane.materials.immolation_totem`
- **Description**: Heat reagent made with Gold and Lava Buckets.

### 72. Nature's Embrace
- **Item ID**: `arcane.materials.natures_embrace`
- **Description**: Green orb crafted by surrounding a Heart of the Sea with 8 leaf types.

### 73. Sun's Brilliance
- **Item ID**: `arcane.materials.suns_brilliance`
- **Description**: Solar-infused flower made by letting an Immolation Totem sit on the ground under direct sunlight for 10s.

### 74. Blessing of the Void
- **Item ID**: `explorer.materials.blessing_of_the_void`
- **Description**: Ender-encased Elytra ingredient.

### 75. Resonant Plate
- **Item ID**: `explorer.materials.resonant_plate`
- **Description**: Force-absorbing metal plate.

### 76. Link Stone
- **Item ID**: `arcane.materials.link_stone`
- **Description**: Crystal binding agent used to craft Redirection.

### 77. Empty Vial & Blood
- **Item IDs**: `arcane.materials.empty_vial`, `arcane.materials.blood`
- **Description**: Containers for extracting and storing player blood.

### 78. Poison Vial
- **Item ID**: `arcane.materials.poison_vial`
- **Description**: Extracted spider/pufferfish venom.

### 79. Soul Orb
- **Item ID**: `arcane.materials.soul_orb`
- **Description**: Floating soul that drops from mobs and yields XP on consumption.

### 80. Core of Heat
- **Item ID**: `explorer.materials.core_of_heat`
- **Description**: Hot orb forged from Magma Blocks and Blaze Powder.

### 81. Lightning Essence
- **Item ID**: `arcane.materials.lightning_essence`
- **Description**: Rare trace element dropped from mobs struck by natural lightning.
