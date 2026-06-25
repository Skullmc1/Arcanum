# 📖 Future Codex Items (Proposed Runes & Machinery for v1.10)

This document tracks upcoming enchantment runes, tools, materials, and machinery planned for the next expansion phase.

---

## 🏗️ New Machinery

### 1. Blessings Altar
- **Category**: Machinery
- **Structure**: 
  - Floor: 9x Gold Blocks arranged in a 3x3 grid, surrounded by a ring of Quartz Blocks (or Endstone Blocks).
  - Middle of one side: Replace one surrounding Quartz block with a Dropper, and place any type of Wooden Fence on top of the Dropper.
- **Crafting Method**: Stand outside and right-click the Wooden Fence while a living mob is positioned on top of the 3x3 gold floor. Lightning will repeatedly strike the mob until it dies, sacrificing it to craft the item.
- **Description**: Used to craft Holy runes.

---

## 🔮 Custom Enchantment Runes

Runes are split into three alignments. The recipe bases remain standard books (e.g., Book, Book and Quill, Enchanted Book), but their finished item textures (base materials) are:
* **Normal**: Prize Pottery Sherd (`PRIZE_POTTERY_SHERD`)
* **Demonic**: Guster Banner Pattern (`GUSTER_BANNER_PATTERN`)
* **Holy**: Prismarine Shard (`PRISMARINE_SHARD`)

---

### Normal Runes (Arcana Table Crafting / Prize Pottery Sherd base)

#### 2. Breach Surge (Swords/Axes)
- **Item ID**: `arcane.runes.breach_surge`
- **Effect**: Striking an enemy has a 20% chance to release a homing energy projectile that seeks out a second nearby target, dealing damage and applying a brief glowing effect.
- **Custom Item**: **Surge Spark** (Amethyst Shard surrounded by Redstone Dust and Glowstone).

#### 3. Aegis Guard (Shields)
- **Item ID**: `explorer.runes.aegis_guard`
- **Effect**: Successfully blocking an attack grants the wielder 4 seconds of Resistance II and Regeneration I.
- **Custom Item**: **Gorgon Scale** (Scutes, Emeralds, and Obsidian).

#### 4. Mach Rush (Boots)
- **Item ID**: `explorer.runes.mach_rush`
- **Effect**: Sprinting continuously builds up a massive speed multiplier the longer you run in a straight line. Resets immediately if you stop moving, jump, or swing a weapon.
- **Custom Item**: **Kinetic Battery** (Redstone Blocks, Copper Ingots, and a Lightning Rod).

#### 5. Rift Walk (Leggings)
- **Item ID**: `arcane.runes.rift_walk`
- **Effect**: Double-tapping the sneak key shifts you into a parallel "rift" for 5 seconds (immune to all damage, semi-transparent, pass through entities, but cannot attack or interact).
- **Custom Item**: Requires 4x **Ender Essence** arranged around a Diamond Leggings.

#### 6. Daedalus' Touch (Pickaxes)
- **Item ID**: `explorer.runes.daedalus_touch`
- **Effect**: Sneak-mining an ore block triggers vein-mining, automatically breaking up to 16 connected blocks of the same type.
- **Custom Item**: **Synthetic Diamond** (required to reinforce).

#### 7. Ouroboros (Shields)
- **Item ID**: `explorer.runes.ouroboros`
- **Effect**: The shield never breaks. At 0 durability, it automatically consumes 1x **Ender Essence** from inventory to instantly restore itself to full durability.
- **Custom Item**: **Serpent's Scale** (Phantom Membranes, Slime Blocks, and Emeralds).

#### 8. Vortex (Swords)
- **Item ID**: `arcane.runes.vortex`
- **Effect**: Attacks have a chance to gather all mobs in a 10-block area in front of you and deal a sweep attack. Has 3 levels (increases pull chance).
- **Custom Item**: **Resonant Crystal**.

#### 9. Shrapnel Shot (Crossbows)
- **Item ID**: `arcane.runes.shrapnel_shot`
- **Effect**: Shoots a shotgun blast of arrows (4 at lvl 1, up to 10 at lvl 3) with 2 punch through and knockback. Incompatible with Multishot and Quick Charge.
- **Custom Item**: **Radiant Core**.

#### 10. Resonance Ping (Pickaxes)
- **Item ID**: `explorer.runes.resonance_ping`
- **Effect**: Sneaking while holding this pickaxe sends a particle pulse. Outlines of valuable ores within a 10-block radius glow faintly through walls for 2 seconds.
- **Custom Item**: **Resonance Crystal** (Amethyst Shards, Note Block, and Ender Essence).

#### 11. Naiad's Repel (Leggings)
- **Item ID**: `explorer.runes.naiads_repel`
- **Effect**: Pushes water away, creating a 3x3 pocket of breathable air around you when submerged.
- **Custom Item**: **Abyssal Sponge**.

---

### Demonic Runes (Blood Altar Crafting / Guster Banner Pattern base)

#### 12. Miasma (Bows)
- **Item ID**: `arcane.runes.miasma`
- **Effect**: Arrows detonate on impact, releasing a 4x4 cloud of spores that damages caught entities and shreds their armor durability.
- **Custom Item**: **Corrupted Spore** (Fermented Spider Eye, Slimeballs, and a Pufferfish).

#### 13. Fenrir's Bite (Axes)
- **Item ID**: `arcane.runes.fenrirs_bite`
- **Effect**: Deals 100% bonus damage if the target is at maximum health.
- **Custom Item**: **Beast Fang** (Bone Blocks, Iron Ingots, and Flint).

#### 14. Anubis' Judgment (Swords)
- **Item ID**: `arcane.runes.anubis_judgment`
- **Effect**: Executing strikes on enemies below 20% health instantly kills them, dropping double normal loot and XP.
- **Custom Item**: **Jackal Idol** (Gold Ingots, Black Dye, and a Nether Star).

#### 15. Gloom (Chestplate)
- **Item ID**: `arcane.runes.gloom`
- **Effect**: Slows all nearby hostile mobs in a 5-block dark aura, passively siphoning a portion of damage dealt to repair armor durability.
- **Custom Item**: **Cursed Diamond** (Synthetic Diamond corrupted with Wither Skeleton Skulls and Soul Sand).

#### 16. Cerberus' Maw (Axes)
- **Item ID**: `arcane.runes.cerberus_maw`
- **Effect**: Cleaving an enemy inflicts armor-bypassing Bleed. If the target dies while bleeding, drops a blood orb that restores hunger and saturation.
- **Custom Item**: **Beast Fang** (Bone Blocks, Iron Ingots, and Hardened Coal Max).

#### 17. Styx's Toll (Bows)
- **Item ID**: `arcane.runes.styxs_toll`
- **Effect**: Roots hit targets to the ground for 3 seconds, disabling movement and teleportation.
- **Custom Item**: **Underworld Coin** (Gold Nuggets, Nether Star fragment, and Obsidian).

#### 18. Infernalis (Chestplate)
- **Item ID**: `arcane.runes.infernalis`
- **Effect**: Permanently burns wearer (makes immune to fire/lava). Melee attackers are ignited, and wearer's melee attacks deal +20% bonus fire damage. Has 2 levels.
- **Custom Item**: **Cinder Core** (Synthetic Diamond surrounded by Magma Blocks and Blaze Powder).

#### 19. Remedium (Swords)
- **Item ID**: `arcane.runes.remedium`
- **Effect**: Sneak right-click consumes 50% health to purge negative effects and grant 10s Strength II.
- **Custom Item**: **Vial of Demonic Blood** (Ghast Tear, Fermented Spider Eye, and Ender Essence).

#### 20. Brimstone (Bows / Crossbows)
- **Item ID**: `arcane.runes.brimstone`
- **Effect**: Projectiles explode into burning ash (inflicts Wither I and slowness on non-demonic targets). 2 levels (increases area size).
- **Custom Item**: **Sulfur Clump** (Gunpowder, Blaze Powder, and Hardened Coal Max).

#### 21. Ashen Veil (Leggings)
- **Item ID**: `arcane.runes.ashen_veil`
- **Effect**: Crouching leaves a trail of black smoke and ash clouds that blind/suffocate enemies.
- **Custom Item**: **Withered Wrap** (Phantom Membranes infused with Wither Skeleton Skulls and Black Dye).

---

### Holy Runes (Blessings Altar Crafting / Prismarine Shard base)

#### 22. Hermes' Tread (Boots)
- **Item ID**: `explorer.runes.hermes_tread`
- **Effect**: Grants permanent Speed II and allows the wearer to step up full 1-block elevations automatically without needing to jump (auto-jump bypass).
- **Custom Item**: **Winged Insignia** (Feathers, Gold Nuggets, and Phantom Membranes).

#### 23. Warding Halo (Chestplate)
- **Item ID**: `arcane.runes.warding_halo`
- **Effect**: Grants protective rings that nullify the damage of the next 3 incoming attacks. Halo takes 30s out of combat to regenerate.
- **Custom Item**: **Blazing Chakram** (Blaze Rods circular pattern around Iron Block in Arcana Table).

#### 24. Radial Blind (Shields)
- **Item ID**: `explorer.runes.radial_blind`
- **Effect**: Blocking a heavy attack emits a blinding flash, applying Blindness and severe Slowness to all hostile mobs within 8 blocks.
- **Custom Item**: **Radiant Core** (Sunflower, Glowstone Dust, and a Ghast Tear).

#### 25. Apollo's Ray (Bows)
- **Item ID**: `arcane.runes.apollos_ray`
- **Effect**: Fully drawn arrows transform into hitscan beams of pure light, instantly hitting targets and igniting them with unextinguishable holy fire.
- **Custom Item**: **Sun-Kissed Feather** (Feather infused in Heavy Forge with Magma Cream, Blaze Powder, and Gold Ingots).

#### 26. Trinity's Well (Bows)
- **Item ID**: `arcane.runes.trinitys_well`
- **Effect**: Shooting allies heals them. Shooting enemies marks them with light, allowing allies attacking the mark to leech health.
- **Custom Item**: **Radiant Geode** (Vitality Geode infused with Gold Blocks and a Golden Apple).

#### 27. Valkyrie's Grace (Armor)
- **Item ID**: `arcane.runes.valkyries_grace`
- **Effect**: Fatal damage triggers a blinding knockback flash and grants you and nearby allies Regeneration III and Resistance II (5m cooldown).
- **Custom Item**: **Sun-Kissed Feather** (Feather infused with Magma Cream, Blaze Powder, and Synthetic Emerald).

#### 28. Hallowed Ground (Boots)
- **Item ID**: `explorer.runes.hallowed_ground`
- **Effect**: Walking creates holy fire trail that damages Undead mobs and cures poison/wither for players.
- **Custom Item**: **Purified Core** (Sunflower, Glowstone Dust, and Amethyst Cluster).

#### 29. Smite of Jupiter (Swords)
- **Item ID**: `arcane.runes.smite_of_jupiter`
- **Effect**: Fully charged sweep attack calls down localized silent lightning. Vaporizes Undead targets into extra XP.
- **Custom Item**: **Stormcaller Medallion** (consumes previous trinket).

---

## 🎒 New Custom Tools & Items (Non-Rune)

### 30. Abyssal Sponge
- **Item ID**: `explorer.materials.abyssal_sponge`
- **Description**: Sponge that never gets wet. Surrounds Sponge with Prismarine Crystals and Hardened Coal Max.

### 31. Singularity Orb
- **Item ID**: `explorer.gadgets.singularity_orb`
- **Description**: Throwable pearl (10 uses) creating mini-black hole pulling mobs in. Recipe: Ender Essence, Obsidian, Echo Shards, Ghast Tears.

### 32. Void Walker Boots
- **Item ID**: `explorer.armor.void_walker_boots`
- **Description**: Saves you from the void by breaking and teleporting you up with Levitation. Recipe: Diamond Boots, Chorus Fruit, Ender Essence, Phantom Membranes.

### 33. Mirror Shield
- **Item ID**: `explorer.tools.mirror_shield`
- **Description**: Reflects blocked projectiles back as high-damage light beams. Recipe: Shield, Amethyst Clusters, Glass Blocks, Synthetic Emerald.

### 34. Decoy Mirage
- **Item ID**: `explorer.tools.decoy_mirage`
- **Description**: Spawns illusory clone drawing aggro while wearer goes invisible. Recipe: Armor Stand, Phantom Membranes, Ender Essence, Zombie/Skeleton Skull.

### 35. Midas Gauntlet
- **Item ID**: `explorer.tools.midas_gauntlet`
- **Description**: Off-hand item with chance to duplicate ores or turn trash (rotten flesh, seeds, flowers, bones) to gold. Recipe: Gold Blocks, Nether Star, Synthetic Diamond, Leather.

### 36. Nebula Glider
- **Item ID**: `explorer.armor.nebula_glider`
- **Description**: Elytra upgrade with infinite propelled flight (no rockets). Recipe: Elytra, Phantom Membranes, Ender Essence, Dragon's Breath.

### 37. Tectonic Hammer
- **Item ID**: `explorer.tools.tectonic_hammer`
- **Description**: Mines 3x3 tunnel instantly (heavy durability cost). Recipe: Synthetic Diamond pickaxe head, Iron Blocks, Obsidian, Hardened Coal.

### 38. Launch Pad Placer
- **Item ID**: `explorer.tools.launch_pad_placer`
- **Description**: Places temporary bounce pad. Recipe: Slime Blocks, Heavy Weighted Pressure Plate, Pistons, Redstone Dust.

### 39. Acoustic Radar
- **Item ID**: `explorer.tools.acoustic_radar`
- **Description**: Pings when near unopened loot chests/buried treasure. Recipe: Compass, Note Blocks, Echo Shards, Gold Ingots.

### 40. Glacial Pathmaker
- **Item ID**: `explorer.tools.glacial_pathmaker`
- **Description**: Staff that creates temporary 3x3 ice bridges while sprinting. Recipe: Blue Ice, Blaze Rod, Snowballs, Ender Essence.

### 41. Pocket Anvil / Repair Kit
- **Item ID**: `explorer.tools.pocket_anvil`
- **Description**: Consumable restoring 25% durability to held item. Recipe: Iron Ingots, Grindstone, String, Hardened Coal.

### 42. Bio-Scanner Lens
- **Item ID**: `explorer.tools.bio_scanner_lens`
- **Description**: Spyglass showing health bars and status effects. Recipe: Spyglass, Spider Eye, Glass Panes, Redstone.

### 43. Hydro-Pack Leggings
- **Item ID**: `explorer.armor.hydro_pack_leggings`
- **Description**: Underwater leg armour that boosts dash sprint. Recipe: Diamond Leggings, Heart of the Sea, Prismarine Shards, Kelp.

### 44. Nanite Constructor
- **Item ID**: `explorer.tools.nanite_constructor`
- **Description**: Chassis block constructor filling blocks in 5x5 grid instantly. Recipe: Dispenser, Ender Essence, Redstone Blocks, Iron Trapdoors.
