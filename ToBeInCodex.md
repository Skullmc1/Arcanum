# 📖 Future Codex Items (Proposed Runes & Machinery for v1.10)

This document tracks upcoming enchantment runes, tools, materials, and machinery planned for the next expansion phase.

---

## 🏗️ New Machinery & Structures

### 1. Blessings Altar
- **Category**: Machinery
- **Structure**: 
  - Floor: 9x Gold Blocks arranged in a 3x3 grid, surrounded by a ring of Quartz Blocks (or Endstone Blocks).
  - Middle of one side: Replace one surrounding Quartz block with a Dropper, and place any type of Wooden Fence on top of the Dropper.
- **Crafting Method**: Stand outside and right-click the Wooden Fence while a living mob is positioned on top of the 3x3 gold floor. Lightning will repeatedly strike the mob until it dies, sacrificing it to craft the item.
- **Description**: Used to craft Holy runes.

### 2. Kinetic Crusher
- **Category**: Machinery
- **Structure**: 3x3 structure built in the world using Pistons, Iron Blocks, and Hoppers.
- **Crafting Method**: Drop items onto the center block and power the piston to crush them.
- **Yields**:
  - 1x Ender Pearl ➡️ 2x Crushed Ender Dust
  - 1x Blaze Rod ➡️ 4x Blaze Powder (high efficiency)

### 3. Sifting Trommel
- **Category**: Machinery
- **Structure**: Drop dirt, gravel, or sand to sift for rare resources.
- **Yields**: Raw Gold, Flint, or Fractured Geodes.

### 4. Heavy Alloy Forge
- **Category**: Machinery
- **Structure**: Massive multiblock furnace requiring a **Core of Heat** as a catalyst. Used to smelt custom alloys.

---

## 🔮 Custom Enchantment Runes

Runes are split into three alignments. The recipe bases remain standard books (e.g., Book, Book and Quill, Enchanted Book), but their finished item textures (base materials) are:
* **Normal**: Prize Pottery Sherd (`PRIZE_POTTERY_SHERD`)
* **Demonic**: Guster Banner Pattern (`GUSTER_BANNER_PATTERN`)
* **Holy**: Prismarine Shard (`PRISMARINE_SHARD`)

---

### Normal Runes (Arcana Table Crafting / Prize Pottery Sherd base)

#### 5. Breach Surge (Swords/Axes)
- **Item ID**: `arcane.runes.breach_surge`
- **Effect**: Striking an enemy has a 20% chance to release a homing energy projectile that seeks out a second nearby target, dealing damage and applying a brief glowing effect.
- **Custom Item**: **Surge Spark** (Amethyst Shard surrounded by Redstone Dust and Glowstone).

#### 6. Aegis Guard (Shields)
- **Item ID**: `explorer.runes.aegis_guard`
- **Effect**: Successfully blocking an attack grants the wielder 4 seconds of Resistance II and Regeneration I.
- **Custom Item**: **Gorgon Scale** (Scutes, Emeralds, and Obsidian).

#### 7. Mach Rush (Boots)
- **Item ID**: `explorer.runes.mach_rush`
- **Effect**: Sprinting continuously builds up a massive speed multiplier the longer you run in a straight line. Resets immediately if you stop moving, jump, or swing a weapon.
- **Custom Item**: **Kinetic Battery** (Redstone Blocks, Copper Ingots, and a Lightning Rod).

#### 8. Rift Walk (Leggings)
- **Item ID**: `arcane.runes.rift_walk`
- **Effect**: Double-tapping the sneak key shifts you into a parallel "rift" for 5 seconds (immune to all damage, semi-transparent, pass through entities, but cannot attack or interact).
- **Custom Item**: Requires 4x **Ender Essence** arranged around a Diamond Leggings.

#### 9. Daedalus' Touch (Pickaxes)
- **Item ID**: `explorer.runes.daedalus_touch`
- **Effect**: Sneak-mining an ore block triggers vein-mining, automatically breaking up to 16 connected blocks of the same type.
- **Custom Item**: **Synthetic Diamond** (required to reinforce).

#### 10. Ouroboros (Shields)
- **Item ID**: `explorer.runes.ouroboros`
- **Effect**: The shield never breaks. At 0 durability, it automatically consumes 1x **Ender Essence** from inventory to instantly restore itself to full durability.
- **Custom Item**: **Serpent's Scale** (Phantom Membranes, Slime Blocks, and Emeralds).

#### 11. Vortex (Swords)
- **Item ID**: `arcane.runes.vortex`
- **Effect**: Attacks have a chance to gather all mobs in a 10-block area in front of you and deal a sweep attack. Has 3 levels (increases pull chance).
- **Custom Item**: **Resonant Crystal**.

#### 12. Shrapnel Shot (Crossbows)
- **Item ID**: `arcane.runes.shrapnel_shot`
- **Effect**: Shoots a shotgun blast of arrows (4 at lvl 1, up to 10 at lvl 3) with 2 punch through and knockback. Incompatible with Multishot and Quick Charge.
- **Custom Item**: **Radiant Core**.

#### 13. Resonance Ping (Pickaxes)
- **Item ID**: `explorer.runes.resonance_ping`
- **Effect**: Sneaking while holding this pickaxe sends a particle pulse. Outlines of valuable ores within a 10-block radius glow faintly through walls for 2 seconds.
- **Custom Item**: **Resonant Crystal** (Amethyst Shards, Note Block, and Ender Essence).

#### 14. Naiad's Repel (Leggings)
- **Item ID**: `explorer.runes.naiads_repel`
- **Effect**: Pushes water away, creating a 3x3 pocket of breathable air around you when submerged.
- **Custom Item**: **Abyssal Sponge**.

---

### Demonic Runes (Blood Altar Crafting / Guster Banner Pattern base)

#### 15. Miasma (Bows)
- **Item ID**: `arcane.runes.miasma`
- **Effect**: Arrows detonate on impact, releasing a 4x4 cloud of spores that damages caught entities and shreds their armor durability.
- **Custom Item**: **Corrupted Spore** (Fermented Spider Eye, Slimeballs, and a Pufferfish).

#### 16. Fenrir's Bite (Axes)
- **Item ID**: `arcane.runes.fenrirs_bite`
- **Effect**: Deals 100% bonus damage if the target is at maximum health.
- **Custom Item**: **Beast Fang** (Bone Blocks, Iron Ingots, and Flint).

#### 17. Anubis' Judgment (Swords)
- **Item ID**: `arcane.runes.anubis_judgment`
- **Effect**: Executing strikes on enemies below 20% health instantly kills them, dropping double normal loot and XP.
- **Custom Item**: **Jackal Idol** (Gold Ingots, Black Dye, and a Nether Star).

#### 18. Gloom (Chestplate)
- **Item ID**: `arcane.runes.gloom`
- **Effect**: Slows all nearby hostile mobs in a 5-block dark aura, passively siphoning a portion of damage dealt to repair armor durability.
- **Custom Item**: **Cursed Diamond** (Synthetic Diamond corrupted with Wither Skeleton Skulls and Soul Sand).

#### 19. Cerberus' Maw (Axes)
- **Item ID**: `arcane.runes.cerberus_maw`
- **Effect**: Cleaving an enemy inflicts armor-bypassing Bleed. If the target dies while bleeding, drops a blood orb that restores hunger and saturation.
- **Custom Item**: **Beast Fang** (Bone Blocks, Iron Ingots, and Hardened Coal Max).

#### 20. Styx's Toll (Bows)
- **Item ID**: `arcane.runes.styxs_toll`
- **Effect**: Roots hit targets to the ground for 3 seconds, disabling movement and teleportation.
- **Custom Item**: **Underworld Coin** (Gold Nuggets, Nether Star fragment, and Obsidian).

#### 21. Infernalis (Chestplate)
- **Item ID**: `arcane.runes.infernalis`
- **Effect**: Permanently burns wearer (makes immune to fire/lava). Melee attackers are ignited, and wearer's melee attacks deal +20% bonus fire damage. Has 2 levels.
- **Custom Item**: **Cinder Core** (Synthetic Diamond surrounded by Magma Blocks and Blaze Powder).

#### 22. Remedium (Swords)
- **Item ID**: `arcane.runes.remedium`
- **Effect**: Sneak right-click consumes 50% health to purge negative effects and grant 10s Strength II.
- **Custom Item**: **Vial of Demonic Blood** (Ghast Tear, Fermented Spider Eye, and Ender Essence).

#### 23. Brimstone (Bows / Crossbows)
- **Item ID**: `arcane.runes.brimstone`
- **Effect**: Projectiles explode into burning ash (inflicts Wither I and slowness on non-demonic targets). 2 levels (increases area size).
- **Custom Item**: **Sulfur Clump** (Gunpowder, Blaze Powder, and Hardened Coal Max).

#### 24. Ashen Veil (Leggings)
- **Item ID**: `arcane.runes.ashen_veil`
- **Effect**: Crouching leaves a trail of black smoke and ash clouds that blind/suffocate enemies.
- **Custom Item**: **Withered Wrap** (Phantom Membranes infused with Wither Skeleton Skulls and Black Dye).

---

### Holy Runes (Blessings Altar Crafting / Prismarine Shard base)

#### 25. Hermes' Tread (Boots)
- **Item ID**: `explorer.runes.hermes_tread`
- **Effect**: Grants permanent Speed II and allows the wearer to step up full 1-block elevations automatically without needing to jump (auto-jump bypass).
- **Custom Item**: **Winged Insignia** (Feathers, Gold Nuggets, and Phantom Membranes).

#### 26. Warding Halo (Chestplate)
- **Item ID**: `arcane.runes.warding_halo`
- **Effect**: Grants protective rings that nullify the damage of the next 3 incoming attacks. Halo takes 30s out of combat to regenerate.
- **Custom Item**: **Blazing Chakram** (Blaze Rods circular pattern around Iron Block in Arcana Table).

#### 27. Radial Blind (Shields)
- **Item ID**: `explorer.runes.radial_blind`
- **Effect**: Blocking a heavy attack emits a blinding flash, applying Blindness and severe Slowness to all hostile mobs within 8 blocks.
- **Custom Item**: **Radiant Core** (Sunflower, Glowstone Dust, and a Ghast Tear).

#### 28. Apollo's Ray (Bows)
- **Item ID**: `arcane.runes.apollos_ray`
- **Effect**: Fully drawn arrows transform into hitscan beams of pure light, instantly hitting targets and igniting them with unextinguishable holy fire.
- **Custom Item**: **Sun-Kissed Feather** (Feather infused in Heavy Forge with Magma Cream, Blaze Powder, and Gold Ingots).

#### 29. Trinity's Well (Bows)
- **Item ID**: `arcane.runes.trinitys_well`
- **Effect**: Shooting allies heals them. Shooting enemies marks them with light, allowing allies attacking the mark to leech health.
- **Custom Item**: **Radiant Geode** (Vitality Geode infused with Gold Blocks and a Golden Apple).

#### 30. Valkyrie's Grace (Armor)
- **Item ID**: `arcane.runes.valkyries_grace`
- **Effect**: Fatal damage triggers a blinding knockback flash and grants you and nearby allies Regeneration III and Resistance II (5m cooldown).
- **Custom Item**: **Sun-Kissed Feather** (Feather infused with Magma Cream, Blaze Powder, and Synthetic Emerald).

#### 31. Hallowed Ground (Boots)
- **Item ID**: `explorer.runes.hallowed_ground`
- **Effect**: Walking creates holy fire trail that damages Undead mobs and cures poison/wither for players.
- **Custom Item**: **Purified Core** (Sunflower, Glowstone Dust, and Amethyst Cluster).

#### 32. Smite of Jupiter (Swords)
- **Item ID**: `arcane.runes.smite_of_jupiter`
- **Effect**: Fully charged sweep attack calls down localized silent lightning. Vaporizes Undead targets into extra XP.
- **Custom Item**: **Stormcaller Medallion** (consumes previous trinket).

---

## 🎒 New Custom Tools & Items (Non-Rune)

### 33. Structure Locators (Village, Stronghold, End City, etc.)
- **Item IDs**: `explorer.tools.locator.village`, `explorer.tools.locator.stronghold`, etc.
- **Description**: 10-charge locating device. Requires blocks from the structure itself (e.g. Village: Bell, beds, chests).

### 34. Webbed Armor Set (Helmet, Chestplate, Leggings, Boots)
- **Item ID**: `explorer.armor.webbed`
- **Set Bonus**: Climb vertical walls like ladders.

### 35. Aegis Vanguard Armor Set (Helmet, Chestplate, Leggings, Boots)
- **Item ID**: `explorer.armor.aegis_vanguard`
- **Description**: Heavy tank armor crafted with **Blue Gold** and **Synthetic Diamond**. Comes with built-in Unbreaking III.
- **Set Bonus**: Grants permanent Slowness I and Resistance II. Emits a pulse every 10s forcing all mobs in an 8-block radius to target you.

### 36. Storm-Weaver Armor Set (Helmet, Chestplate, Leggings, Boots)
- **Item ID**: `arcane.armor.storm_weaver`
- **Description**: Mage armor crafted with **Lightning Essence** and **Rose Gold**.
- **Set Bonus**: Reduces spell cooldowns by 50%. Passively regenerates charges for magical items while moving.

### 37. Glintblade Phalanx (Magical Tome)
- **Item ID**: `arcane.tomes.glintblade_phalanx`
- **Description**: Summons 4 floating phantom daggers that autonomously target hostiles entering a 6-block radius.

### 38. Custom Materials & Alloys
- **Item IDs**:
  - `arcane.materials.blue_gold` (3 Gold + 1 Iron in Heavy Alloy Forge)
  - `arcane.materials.rose_gold` (Copper + Gold)
  - `arcane.materials.bronzed_steel` (Iron + Copper + Hardened Coal I)
  - `arcane.materials.abyssal_alloy` (Netherite Scrap + Blood + Synthetic Emerald)
  - `arcane.materials.crushed_ender_dust` (Ender Pearl crushed in Kinetic Crusher)
  - `arcane.materials.fractured_geode` (Sifted from Trommel)
