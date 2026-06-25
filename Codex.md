# Codex System Design & Specification

This document details the architectural layout, categories, items, and custom crafting mechanics of the **Codex System**.

---

## 📂 Category & Sub-category Hierarchy

The Codex is split into three main top-level categories, each containing sub-categories to group items logically:

### 1. General Machinery
Machines used to craft custom items. These are built as physical block structures in the world.
* **Structures / Machines**:
  * **Arcana Table**: Used for infusing items and crafting runes.
  * **Heavy Forge**: Used for forging heavy explorer gear and advanced alloys.
  * **Upgrade Table**: Used to upgrade and combine items (such as runes).
  * **Block Duplicator**: Duplicates building blocks using lava fuel.
  * **Blood Altar**: Performs dark sacrifice rituals to craft Demonium runes.
  * **Auto Sifter**: Automatically sifts sand and gravel.
  * **Auto Smelter**: Automatically smelts ore inputs using fuel.

### 2. Arcane Category
Mystical tools, armors, weapons, and custom enchantment runes.
* **Sub-categories**:
  * **Enchantment Runes**: Custom enchantments applied via hand-swapping.
  * **Armor**: Infused armor pieces with passive effects.
  * **Melee Weapons**: Custom swords with special on-hit attributes.
  * **Ranged Weapons**: Wands and magic staves.
  * **Arcana Boosts**: One-time use consumable power-ups.
  * **Ingredients**: Synthetic reagents used as crafting materials.

### 3. Exploration Category
Navigation tools, structures, and utility gadgets.
* **Sub-categories**:
  * **Navigation**: Compass utilities, teleportation devices, and base-building aids.
  * **Exploration**: Items that help locate hidden treasures or custom structures.
  * **Gadgets**: Interactive movement utilities (e.g., Grappling Hooks).

---

## 🏗️ Custom Machinery & Block Structures

Instead of using a standard Crafting Table interface alone, advanced Codex items require specific physical structures to be placed in the world. When a player right-clicks the crafting table component of these structures, it allows them to craft the corresponding recipes.

### 1. Arcana Table
* **Structure**: A Crafting Table with at least 2 Bookshelves horizontally adjacent to it.
* **Usage**: Required for Arcane category recipes and Enchantment Runes.

### 2. Heavy Forge
* **Structure**: A Crafting Table placed directly on top of a Blast Furnace.
* **Usage**: Required for advanced exploration gadgets and forged gear.

---

## ⛓️ Grappling Hook Mechanic (Lead-based)

The Grappling Hook uses a **Lead** (`Material.LEAD`) as its physical item.

* **Usage**: Right-clicking launches an invisible projectile entity that is visually leashed to the player.
* **Hit Detection**: If the projectile collides with a block, it pulls the player towards the hit location with a smooth velocity curve.
* **Restrictions**:
  * Cannot hook onto animals or fence posts.
* **Levels & Range**:
  * **Iron Grappling Hook**: 10 blocks range.
  * **Diamond Grappling Hook**: 25 blocks range.
  * **Netherite Grappling Hook**: 50 blocks range.

---

## 🔮 Enchantment Runes

Enchantment Runes are custom enchantments that players can apply to their gear.

* **Texture**: Firework Stars (`Material.FIREWORK_STAR`). They are colored and cannot be used in vanilla firework recipes.
* **Rune Types**: Each rune defines a target type (e.g., `ARMOR_BOOTS`, `WEAPON_SWORD`).
* **Application**:
  1. Hold the **Enchantment Rune** in your main hand.
  2. Hold the target item in your off-hand.
  3. Press the swap key (`F` by default).
  4. The rune is consumed, and the custom enchantment is permanently applied to the target item (adding persistent tags and custom lore).
* **Effects**:
  * **Lifesteal Rune (Weapon-Sword)**: Restores a percentage of damage dealt as health on hit.
  * **Speed Rune (Armor-Boots)**: Grants passive Speed I when boots are worn.
