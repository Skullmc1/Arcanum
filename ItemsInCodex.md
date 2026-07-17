# Items in Codex

This document lists all custom items, runes, structures, and tools that have been fully created, registered, and are active in the Codex system.

---

## Crafting Stations & Machinery (Multiblock Structures)

### Arcana Table
- **ID**: `machinery.arcana_table`
- **Structure**: Crafting Table on top of a Dropper with Bookshelves on the sides of the Dropper.
- **Used to craft**: Mystical Arcane items and Enchantment Runes.

### Heavy Forge
- **ID**: `machinery.heavy_forge`
- **Structure**: Crafting Table on top of a Blast Furnace on top of a Dropper.
- **Used to forge**: Heavy Explorer tools, gadgets, and compressed alloys.

### Upgrade Table
- **ID**: `machinery.upgrade_table`
- **Structure**: Anvil on top of a Dropper on top of a Bookshelf.
- **Used to**: Combine matching Enchantment Runes to raise their level.

### Block Duplicator
- **ID**: `machinery.block_duplicator`
- **Structure**: Nether Brick Fence on top of a Dropper surrounded horizontally by Furnaces/Blast Furnaces.
- **Description**: Duplicates building blocks using Lava Buckets as fuel.

### Blood Altar
- **ID**: `machinery.blood_altar`
- **Structure**: Red Carpet on top of a Dropper on top of Obsidian.
- **Used to**: Perform dark sacrifice rituals to forge Demonic Runes.

### Blessings Altar
- **ID**: `machinery.blessings_altar`
- **Structure**: 3x3 Gold Block platform surrounded by Quartz, with a Dropper and Fence.
- **Used to**: Perform holy rituals to forge Holy Runes.

### Heavy Alloy Forge
- **ID**: `machinery.heavy_alloy_forge`
- **Structure**: Blast Furnace on top of a Dropper on top of a 3x3 Magma Block base.
- **Used to**: Smelt custom alloys.

### Enchanter
- **ID**: `machinery.enchanter`
- **Structure**: Enchanting Table on top of a 3x3 Diamond Block base.
- **Used to**: Enchant items using vanilla enchantments in exchange for Diamonds.

### Disenchanter
- **ID**: `machinery.disenchanter`
- **Structure**: Enchanting Table on top of a 3x3 Iron Block base.
- **Used to**: Remove enchantments from items and salvage materials.

### Kinetic Crusher
- **ID**: `machinery.kinetic_crusher`
- **Structure**: Piston on top of a Hopper on top of a 3x3 Iron Block base.
- **Used to**: Crush items (e.g. Ender Pearl -> Crushed Ender Dust).

### Sifting Trommel
- **ID**: `machinery.sifting_trommel`
- **Structure**: Iron Bars surrounded by 4 Copper Blocks on top of a Hopper.
- **Used to**: Sift materials for geode and mineral pieces.

### Auto Sifter
- **ID**: `machinery.auto_sifter`
- **Structure**: Hopper on top of a Dropper adjacent to a Cauldron.
- **Description**: Passively sifts Sand and Gravel into raw resources.

### Auto Smelter
- **ID**: `machinery.auto_smelter`
- **Structure**: Hopper on top of a Dropper adjacent to a Furnace.
- **Description**: Passively smelts items using fuel from its slot.

### Wither Death Chest
- **Structure**: A Chest with a Wither Skeleton Skull on top.
- **Description**: Automatically collects inventory items upon death. One per player.

---

## Arcane Category

### Normal Runes

#### Vampiric Bleed I / II / III (Sword)
- **IDs**: `arcane.lifesteal_rune`, `arcane.lifesteal_rune_2`, `arcane.lifesteal_rune_3`
- **Effect**: Heals on attack (15%/30%/45% of damage dealt).
- **Crafting**: Arcana Table. Upgrade via Upgrade Table.

#### Speed I / II / III (Boots)
- **IDs**: `arcane.speed_rune`, `arcane.speed_rune_2`, `arcane.speed_rune_3`
- **Effect**: Grants Speed I/II/III when worn.
- **Crafting**: Arcana Table. Upgrade via Upgrade Table.

#### Catch Flame I / II / III (Sword / Bow)
- **IDs**: `arcane.runes.catch_flame`, `arcane.runes.catch_flame_2`, `arcane.runes.catch_flame_3`
- **Effect**: Applies Fire Aspect II / Flame I. Spreads fire to nearby mobs (33%/66%/100%).
- **Crafting**: Arcana Table. Upgrade via Upgrade Table.

#### Corrosive Slash I / II / III / IV (Axe)
- **ID**: `arcane.runes.corrosive_slash` (Levels 1-4)
- **Effect**: Reduces enemy armor toughness on axe hits (stacks up to 5, duration scales with level).
- **Crafting**: Arcana Table.

#### Scorch (Sword)
- **ID**: `arcane.runes.scorch` (Level 1)
- **Effect**: Reduces enemy armor on hit (stacks up to 10, max 90%). Deals fire damage tick.
- **Crafting**: Arcana Table.

#### Glacial Thorns I / II / III / IV / V (Armor)
- **ID**: `arcane.runes.glacial_thorns` (Levels 1-5)
- **Effect**: Attackers have a chance to be afflicted with Weakness. Level 5 guarantees Weakness II.
- **Crafting**: Arcana Table.

#### Tidal Sweep I / II / III (Sword)
- **ID**: `arcane.runes.tidal_sweep` (Levels 1-3)
- **Effect**: Wider sweep attack with longer reach. Incompatible with Sweeping Edge.
- **Crafting**: Arcana Table.

#### Photosynthesis (Durability Gear)
- **ID**: `arcane.runes.photosynthesis` (Level 1)
- **Effect**: Slowly restores item durability while on dirt/grass under sunlight.
- **Crafting**: Arcana Table.

#### Artemis's Blessing (Bow)
- **ID**: `arcane.runes.artemis_blessing` (Level 1)
- **Effect**: Silences bow shots and adds +5 levels of Punch.
- **Crafting**: Arcana Table.

#### Static Charge I / II / III / IV / V / VI / VII (Chestplate)
- **ID**: `arcane.runes.static_charge` (Levels 1-7)
- **Effect**: Generates charge while moving. Fully charged: next hit deals +25% damage.
- **Crafting**: Arcana Table.

#### Phantom Backstab I / II / III / IV (Sword)
- **ID**: `arcane.runes.phantom_backstab` (Levels 1-4)
- **Effect**: 50% chance to summon a spectral weapon copy behind the target that flies forward and strikes.
- **Crafting**: Blood Altar.

#### Bleed I / II / III / IV / V / VI / VII / VIII (Sword / Axe)
- **ID**: `arcane.runes.bleed` (Levels 1-8)
- **Effect**: Applies ticking bleeding stacks on melee hit.
- **Crafting**: Blood Altar.

#### Poison Spores I / II / III / IV / V (Sword / Axe)
- **ID**: `arcane.runes.poison_spores` (Levels 1-5)
- **Effect**: Applies standard Poison on hit. Scales duration and proc chance.
- **Crafting**: Arcana Table.

#### Gas Cloud I / II / III / IV (Armor)
- **ID**: `arcane.runes.gas_cloud` (Levels 1-4)
- **Effect**: Chance to release decay gas cloud on hit.
- **Crafting**: Arcana Table.

#### Heavy Draw I / II / III / IV / V (Bow)
- **ID**: `arcane.runes.heavy_draw` (Levels 1-5)
- **Effect**: +50% draw time but up to +50% arrow damage.
- **Crafting**: Arcana Table.

#### Soul Harvester (Hoe)
- **ID**: `arcane.runes.soul_harvester` (Level 1)
- **Effect**: Hoe kills have a chance to drop Soul Orbs granting massive XP.
- **Crafting**: Arcana Table.

#### Breach Surge (Sword / Axe)
- **ID**: `arcane.runes.breach_surge` (Level 1)
- **Effect**: 20% chance to release a homing energy projectile to a nearby target.
- **Crafting**: Arcana Table.

#### Rift Walk (Armor)
- **ID**: `arcane.runes.rift_walk` (Level 1)
- **Effect**: Double-tap sneak enters rift phase (invulnerable, cannot attack) for 5s.
- **Crafting**: Arcana Table.

#### Vortex I / II / III (Sword)
- **ID**: `arcane.runes.vortex` (Levels 1-3)
- **Effect**: Sweeping strikes pull in mobs within 10 blocks.
- **Crafting**: Arcana Table.

#### Shrapnel Shot I / II / III (Crossbow)
- **ID**: `arcane.runes.shrapnel_shot` (Levels 1-3)
- **Effect**: Shoots a shotgun blast of 4-10 piercing arrows.
- **Crafting**: Arcana Table.

### Demonic Runes

#### Demonium I / II / III (Helmet)
- **IDs**: `arcane.runes.demonium`, `arcane.runes.demonium_2`, `arcane.runes.demonium_3`
- **Effect**: Ignites all entities in a 2-block radius for 2s/5s/10s.
- **Crafting**: Blood Altar.

#### Miasma (Bow)
- **ID**: `arcane.runes.miasma` (Level 1)
- **Effect**: Arrows detonate into a spore cloud shredding armor durability.
- **Crafting**: Blood Altar.

#### Fenrir's Bite (Axe)
- **ID**: `arcane.runes.fenrirs_bite` (Level 1)
- **Effect**: Deals double damage to targets at full HP.
- **Crafting**: Blood Altar.

#### Anubis' Judgment (Sword)
- **ID**: `arcane.runes.anubis_judgment` (Level 1)
- **Effect**: Instantly executes targets below 20% HP, dropping double loot/XP.
- **Crafting**: Blood Altar.

#### Gloom (Chestplate)
- **ID**: `arcane.runes.gloom` (Level 1)
- **Effect**: Slows nearby hostile mobs (5-block aura) and passively repairs armor durability on hit.
- **Crafting**: Blood Altar.

#### Cerberus' Maw (Axe)
- **ID**: `arcane.runes.cerberus_maw` (Level 1)
- **Effect**: Inflicts Bleed on cleave. Dying while bleeding drops a hunger-restoring blood orb.
- **Crafting**: Blood Altar.

#### Styx's Toll (Bow)
- **ID**: `arcane.runes.styxs_toll` (Level 1)
- **Effect**: Roots hit targets for 3 seconds.
- **Crafting**: Blood Altar.

#### Infernalis I / II (Chestplate)
- **ID**: `arcane.runes.infernalis` (Levels 1-2)
- **Effect**: Permanently ablaze, immune to fire/lava. Melee attacks gain +20% fire damage.
- **Crafting**: Blood Altar.

#### Remedium (Sword)
- **ID**: `arcane.runes.remedium` (Level 1)
- **Effect**: Sneak right-click consumes 50% HP to purge debuffs and grant Strength II.
- **Crafting**: Blood Altar.

#### Brimstone I / II (Bow / Crossbow)
- **ID**: `arcane.runes.brimstone` (Levels 1-2)
- **Effect**: Projectiles explode into burning ash inflicting Wither and Slowness.
- **Crafting**: Blood Altar.

#### Ashen Veil (Armor)
- **ID**: `arcane.runes.ashen_veil` (Level 1)
- **Effect**: Crouching leaves a trail of blinding/suffocating smoke.
- **Crafting**: Blood Altar.

### Holy Runes

#### Warding Halo (Chestplate)
- **ID**: `arcane.runes.warding_halo` (Level 1)
- **Effect**: Grants rings that nullify the next 3 incoming attacks.
- **Crafting**: Blessings Altar.

#### Apollo's Ray (Bow)
- **ID**: `arcane.runes.apollos_ray` (Level 1)
- **Effect**: Fully drawn arrows become hitscan light beams inflicting holy fire.
- **Crafting**: Blessings Altar.

#### Trinity's Well (Bow)
- **ID**: `arcane.runes.trinitys_well` (Level 1)
- **Effect**: Shooting allies heals; shooting enemies marks them for health-leech on melee hit.
- **Crafting**: Blessings Altar.

#### Valkyrie's Grace (Armor)
- **ID**: `arcane.runes.valkyries_grace` (Level 1)
- **Effect**: Fatal damage triggers knockback and grants Regeneration III / Resistance II.
- **Crafting**: Blessings Altar.

#### Smite of Jupiter (Sword)
- **ID**: `arcane.runes.smite_of_jupiter` (Level 1)
- **Effect**: Fully charged sweep attack calls silent lightning. Double damage vs Undead.
- **Crafting**: Blessings Altar.

### Armor

#### Speed Boots
- **ID**: `arcane.speed_boots`
- **Description**: Diamond Boots pre-infused with Speed Rune I.

#### Shadow Cloak
- **ID**: `arcane.armor.shadow_cloak`
- **Description**: Invisibility and Speed I when light level < 7.

#### Superior Shadow Cloak
- **ID**: `arcane.armor.superior_shadow_cloak`
- **Description**: Upgraded. Invisibility + Speed II + Swift Sneak while crouched.

#### Gale Chestplate
- **ID**: `arcane.armor.gale_chestplate`
- **Description**: Permanent Slow Falling I + double jumping.

#### Storm-Weaver Armor Set
- **IDs**: `arcane.armor.storm_weaver.{helmet,chestplate,leggings,boots}`
- **Description**: Mage armor. Set bonus: Speed II, Jump Boost II, lightning immunity, lightning on hit.

### Melee Weapons

#### Corrosive Scythe
- **ID**: `arcane.melee.venomous_scythe`
- **Description**: Degrades enemy armor on each hit, reducing their protection over time.

#### Siphon Blade
- **ID**: `arcane.melee.siphon_blade`
- **Description**: Restores 15% max HP on killing an enemy.

### Ranged Weapons

#### Wand of Embers
- **ID**: `arcane.wand_of_embers`
- **Description**: Fires fireball projectiles on right-click.

#### Staff of Supplant
- **ID**: `arcane.ranged.staff_of_supplant`
- **Description**: Projectile swaps positions with the hit entity.

#### Wand of Transmutation
- **ID**: `arcane.ranged.wand_of_transmutation`
- **Description**: Beam turns hostile mobs into passive animals for 15s.

#### Wand of Levitation
- **ID**: `arcane.ranged.wand_of_levitation`
- **Description**: Projectile gives Levitation I for 5s.

#### Staff of the Stormlord
- **ID**: `arcane.ranged.staff_of_the_stormlord`
- **Description**: Calls lightning on target block within 20 blocks.

#### Tome of Glintblade Phalanx
- **ID**: `arcane.tomes.glintblade_phalanx`
- **Description**: Summons 4 floating phantom swords that orbit and home to targets.

### Trinkets

#### Amulet of the Phoenix
- **ID**: `arcane.trinkets.amulet_of_the_phoenix`
- **Description**: Resurrects with fire explosion. 3 charges before breaking.

#### Totem of Fallacy
- **ID**: `arcane.trinkets.totem_of_fallacy`
- **Description**: Teleports to spawn; reduces inventory item durability to 10%.

#### Frostbite Ring
- **ID**: `arcane.trinkets.frostbite_ring`
- **Description**: Freezes water under feet; extinguishes burning entities on hit.

#### Stormcaller Medallion
- **ID**: `arcane.trinkets.stormcaller_medallion`
- **Description**: 15% lightning strike chance on hit during storms.

#### Vitality Geode I / II / III
- **IDs**: `arcane.trinkets.vitality_geode_{1,2,3}`
- **Description**: Increases max HP by +4 / +10 / +20 while in inventory.

### Materials & Ingredients

- **Ender Essence** (`arcane.materials.ender_essence`): Compressed ender magic.
- **Potent Spider Web** (`arcane.materials.potent_spider_web`): Sticky web compound.
- **Hardened Coal I / II / Max** (`arcane.materials.hardened_coal_{1,2,max}`): Pressurized coal.
- **Hardened Base I / II / Max** (`arcane.materials.hardened_base_{1,2,max}`): Compressed clay-copper.
- **Synthetic Diamond** (`arcane.materials.synthetic_diamond`): Artificial diamond.
- **Synthetic Emerald** (`arcane.materials.synthetic_emerald`): Artificial emerald.
- **Echoing Core** (`arcane.materials.echoing_core`): Resonating deep-sea core.
- **Immolation Totem** (`arcane.materials.immolation_totem`): Volatile fire reagent.
- **Nature's Embrace** (`arcane.materials.natures_embrace`): Green orb of forest power.
- **Sun's Brilliance** (`arcane.materials.suns_brilliance`): Solar-infused flower.
- **Empty Vial** (`arcane.materials.empty_vial`): Glass container for blood.
- **Blood** (`arcane.materials.blood`): Player blood.
- **Poison Vial** (`arcane.materials.poison_vial`): Concentrated venom.
- **Soul Orb** (`arcane.materials.soul_orb`): Drops from mobs, grants XP.
- **Lightning Essence** (`arcane.materials.lightning_essence`): Trace of lightning.
- **Link Stone** (`arcane.materials.link_stone`): Crystal binding agent.
- **Transmutation Core** (`arcane.materials.transmutation_core`): Pulsing matter-changing core.
- **Core of Heat** (`explorer.materials.core_of_heat`): Hot orb of compressed magma.
- **Resonant Plate** (`explorer.materials.resonant_plate`): Elastic force-reflecting plate.
- **Blessing of the Void** (`explorer.materials.blessing_of_the_void`): Ender-encased elytra.
- **Blue Gold** (`arcane.materials.blue_gold`): Gold-iron alloy.
- **Rose Gold** (`arcane.materials.rose_gold`): Copper-gold alloy.
- **Bronzed Steel** (`arcane.materials.bronzed_steel`): Copper-iron steel.
- **Abyssal Alloy** (`arcane.materials.abyssal_alloy`): Blood-infused nether alloy.
- **Crushed Ender Dust** (`arcane.materials.crushed_ender_dust`): Fine ender powder.
- **Fractured Geode** (`arcane.materials.fractured_geode`): Cracked mineral shell.

---

## Explorer Category

### Normal Runes

#### Dwarf's Blessing (Tools)
- **ID**: `explorer.runes.dwarfs_blessing` (Level 1)
- **Effect**: Auto-smelts mined blocks. Logs drop Charcoal.
- **Crafting**: Heavy Forge.

#### Seismic Landing I / II / III / IV (Boots)
- **ID**: `explorer.runes.seismic_landing` (Levels 1-4)
- **Effect**: Negates fall damage (up to 5 hearts), releases damage shockwave. Incompatible with Feather Falling.
- **Crafting**: Heavy Forge.

#### Zephyr (Bow / Crossbow)
- **ID**: `explorer.runes.zephyr` (Level 1)
- **Effect**: Removes arrow gravity.
- **Crafting**: Heavy Forge.

#### Telekinesis (Tools)
- **ID**: `explorer.runes.telekinesis` (Level 1)
- **Effect**: Teleports mined blocks and XP to inventory.
- **Crafting**: Heavy Forge.

#### Kinetic Rebound (Shield)
- **ID**: `explorer.runes.kinetic_rebound` (Level 1)
- **Effect**: High chance to reflect blocked projectiles.
- **Crafting**: Heavy Forge.

#### Timber (Axe)
- **ID**: `explorer.runes.timber` (Level 1)
- **Effect**: Breaks entire trees when one log is broken.
- **Crafting**: Heavy Forge.

#### Crude Sharpness I / II / III / IV / V / VI / VII (Tools / Stick)
- **ID**: `explorer.runes.crude_sharpness` (Levels 1-7)
- **Effect**: Flat melee damage boost to all tools and sticks.
- **Crafting**: Heavy Forge.

#### Basalt Trail (Boots)
- **ID**: `explorer.runes.basalt_trail` (Level 1)
- **Effect**: Cools lava beneath feet into temporary Basalt.
- **Crafting**: Heavy Forge.

#### Void Walker (Boots)
- **ID**: `explorer.runes.void_walker` (Level 1)
- **Effect**: Teleports you to safety if you fall 20+ blocks into the void.
- **Crafting**: Heavy Forge.

#### Aegis Guard (Shield)
- **ID**: `explorer.runes.aegis_guard` (Level 1)
- **Effect**: Blocking grants Resistance II and Regeneration I for 4s.
- **Crafting**: Heavy Forge.

#### Mach Rush (Boots)
- **ID**: `explorer.runes.mach_rush` (Level 1)
- **Effect**: Sprinting builds speed multiplier. Resets on stop/jump/swing.
- **Crafting**: Heavy Forge.

#### Daedalus' Touch (Pickaxe)
- **ID**: `explorer.runes.daedalus_touch` (Level 1)
- **Effect**: Sneak-mining vein-mines up to 16 connected blocks.
- **Crafting**: Heavy Forge.

#### Ouroboros (Shield)
- **ID**: `explorer.runes.ouroboros` (Level 1)
- **Effect**: Shield never breaks; at 0 durability consumes Ender Essence to repair.
- **Crafting**: Heavy Forge.

#### Resonance Ping (Pickaxe)
- **ID**: `explorer.runes.resonance_ping` (Level 1)
- **Effect**: Sneaking outlines valuable ores within 10 blocks for 2s.
- **Crafting**: Heavy Forge.

#### Naiad's Repel (Armor)
- **ID**: `explorer.runes.naiads_repel` (Level 1)
- **Effect**: Creates 3x3 pocket of breathable air when submerged.
- **Crafting**: Heavy Forge.

### Holy Runes

#### Hermes' Tread (Boots)
- **ID**: `explorer.runes.hermes_tread` (Level 1)
- **Effect**: Permanent Speed II and auto step-up.
- **Crafting**: Blessings Altar.

#### Radial Blind (Shield)
- **ID**: `explorer.runes.radial_blind` (Level 1)
- **Effect**: Blocking heavy attacks blinds/slows mobs within 8 blocks.
- **Crafting**: Blessings Altar.

#### Hallowed Ground (Boots)
- **ID**: `explorer.runes.hallowed_ground` (Level 1)
- **Effect**: Walking creates holy fire trail that damages Undead.
- **Crafting**: Blessings Altar.

### Navigation

#### Waypoint Compass
- **ID**: `explorer.waypoint_compass`
- **Description**: Links to a waypoint and teleports on right-click.

#### Waypoint Teleport Plate
- **ID**: `explorer.navigation.teleportation_plate`
- **Description**: Pressure plate linked with a Waypoint Compass. Teleports players standing on it.

### Exploration

#### Ore Scanner
- **ID**: `explorer.exploration.ore_scanner`
- **Description**: Highlights nearby valuable ores in a 10-block radius.

#### Structure Locators
- **IDs**: `explorer.tools.locator.{village,stronghold,end_city}`
- **Description**: 10-charge locators for specific structures.

#### Omni Tool
- **ID**: `explorer.tools.omni_tool`
- **Description**: Adapts its form to harvest whatever block you look at.

### Gadgets

#### Grappling Hooks (Iron / Diamond / Netherite)
- **IDs**: `explorer.grappling_hook.{iron,diamond,netherite}`
- **Description**: Ranges of 10 / 25 / 50 blocks.

#### Web Slingers (Iron / Diamond / Netherite)
- **IDs**: `explorer.tools.web_slinger.{iron,diamond,netherite}`
- **Description**: Grappling Hooks upgraded to place safety webs on landing.

#### Thermal Canteen
- **ID**: `explorer.gadgets.thermal_canteen`
- **Description**: 4 charges; restores hunger and cures effects.

#### Ender Backpack
- **ID**: `explorer.gadgets.ender_backpack`
- **Description**: Opens ender chest remotely.

#### Safari Lasso
- **ID**: `explorer.gadgets.safari_lasso`
- **Description**: Captures and releases passive mobs.

#### Void Bag
- **ID**: `explorer.gadgets.void_bag`
- **Description**: Virtual trash inventory; items vanish on close.

#### Bottle of Lightning
- **ID**: `explorer.gadgets.bottle_of_lightning`
- **Description**: Electrical energy source used in storm staff crafting.

#### Depth Strider Flippers
- **ID**: `explorer.gadgets.depth_strider_flippers`
- **Description**: Swim speed + water breathing; Slowness on land.

#### Slime Boots
- **ID**: `explorer.gadgets.slime_boots`
- **Description**: Negates fall damage and bounces upward.

#### Magnetic Ring
- **ID**: `explorer.gadgets.magnetic_ring`
- **Description**: Pulls dropped items within 5 blocks toward you.

### Tools

#### Webber
- **ID**: `explorer.tools.webber`
- **Description**: Fires temporary cobwebs (5s duration).

#### Beastmaster's Flute
- **ID**: `explorer.tools.beastmasters_flute`
- **Description**: Pacifies hostiles in a 5-block radius. 60s cooldown.

#### Excavation Drill
- **ID**: `explorer.tools.excavation_drill`
- **Description**: 3x3 block-breaking pickaxe.

#### Builder's Wand
- **ID**: `explorer.tools.builders_wand`
- **Description**: Places up to 9 blocks in a line.

#### Portable Utility Screens
- **IDs**: `explorer.tools.portable_{smelter,furnace,crafting_table,anvil,smithing_table,grindstone,stonecutter}`
- **Description**: Access work block screens from inventory anywhere.

### Armor

#### Spelunker's Helmet
- **ID**: `explorer.armor.spelunkers_helmet`
- **Description**: Night Vision + highlights hostile mobs with Glowing.

#### Steam Jetpack
- **ID**: `explorer.armor.steam_jetpack`
- **Description**: Sneak mid-air for thruster boost. Fueled by coal.

#### Webbed Armor Set
- **IDs**: `explorer.armor.webbed.{helmet,chestplate,leggings,boots}`
- **Description**: Chainmail stats. Full set: climb vertical walls.

#### Aegis Vanguard Armor Set
- **IDs**: `explorer.armor.aegis_vanguard.{helmet,chestplate,leggings,boots}`
- **Description**: Tank armor. Full set: Slowness I, Resistance II, mob-taunt shockwave.
