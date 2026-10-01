<div align="center">

<img src="readme%20media/banner.svg" alt="Arcanum" width="100%">

<br>

[![Latest release](https://img.shields.io/github/v/release/Skullmc1/Arcanum?style=for-the-badge&color=8b5cf6&label=release)](https://github.com/Skullmc1/Arcanum/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/Skullmc1/Arcanum/release.yml?style=for-the-badge&color=2dd4bf&label=build)](https://github.com/Skullmc1/Arcanum/actions/workflows/release.yml)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20→%2026.3-f5d98b?style=for-the-badge)
![Paper](https://img.shields.io/badge/Paper-required-6366f1?style=for-the-badge)

**A navigation HUD, waypoint system and an arcane item Codex, in one Paper plugin.**

</div>

<img src="readme%20media/divider.svg" alt="" width="100%">

## Features

<img src="readme%20media/features.svg" alt="Arcanum features" width="100%">

- **Live HUD**: an action bar showing coordinates, biome and Nether/Overworld conversion. Every part can be toggled per player.
- **Waypoints and navigation**: create, list, share and teleport to waypoints. Set a destination or track a player and follow a particle trail to them.
- **The Codex**: a catalogue of custom items you can craft in-game, in two schools:
  **Arcane** (runes, armor, weapons, trinkets) and **Explorer** (gadgets, tools, navigation).
- **Multiblock machinery**: Arcana Table, Heavy Forge, Upgrade Table, Block Duplicator, Blood Altar and more.
- **Death Chest**: your items are kept in a physical chest with a Wither Skeleton Skull where you died.
- **Quality of life**: linked chest, hats, ping, shared position, MOTD day counter, and halved duration for repeated harmful potion effects.
- **Skills**: XP-based skills (Running and Mining available now, more coming) that level up to 150 and unlock perks along the way, such as Speed while sprinting, virtual Efficiency and Fortune while mining, and instant ore smelting at max level. Open `/skills` to see your progress.
- **Auto-updater**: pulls the newest build from GitHub Releases.

<img src="readme%20media/divider.svg" alt="" width="100%">

## Compatibility

<img src="readme%20media/compatibility.svg" alt="Supported versions 1.21.1 to 26.3" width="100%">

Arcanum ships as a **single jar** that runs on every Paper version from **1.21.1** up to the latest release (**26.3**), including the new `26.x` numbering.
It is compiled against the oldest supported API and looks up version-specific constants at runtime, so you never need to pick a jar per version.

| Requirement | Version |
|---|---|
| Server | [Paper](https://papermc.io/downloads/paper) (or a Paper fork). Spigot and CraftBukkit are not supported. |
| Minecraft | 1.21.1 or newer |
| Java | 21 for 1.21.x, 25 or newer for 26.x (whatever your server version already requires) |

Tested on 1.21.1, 1.21.11, 26.1.2 and 26.3.

## Installation

1. Download `Arcanum-<version>.jar` from the [latest release](https://github.com/Skullmc1/Arcanum/releases/latest).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.

> [!NOTE]
> **Coming from Dashboard?** Arcanum is the new name of the Dashboard plugin. Remove the old jar and install Arcanum. Your data in `plugins/Dashboard/` is moved to `plugins/Arcanum/` on first start, and items you already own keep working. `/dashboard` and `/db` remain as aliases.

<img src="readme%20media/divider.svg" alt="" width="100%">

## Commands

| Command | Aliases | Description |
|---|---|---|
| `/arcanum toggle \| xyz \| biome \| nether` | `/dashboard`, `/db` | Toggle the HUD or individual parts of it |
| `/arcanum version` | | Show the installed version |
| `/arcanum update` | | Check GitHub Releases and download the latest version |
| `/waypoint create \| list \| tp \| delete \| navigate \| share` | `/wp` | Manage your waypoints |
| `/destination <x y z>` / `clear` | `/dest` | Set a coordinate target and follow the trail |
| `/track <player>` / `clear` | | Point the HUD at another player |
| `/skills` | | Open your skill levels and next perks |
| `/skills set \| addxp \| reset <player> ...` | | Admin: change a player's skill progress (OP or `arcanum.admin`) |
| `/pos` | `/where` | Broadcast your coordinates with a clickable track button |
| `/linkchest` and `/chest` | | Link the chest you stand on, then open it from anywhere |
| `/hat` | | Wear the item you are holding |
| `/ping` | | Check your latency |
| `/suicide` | | Get out of sticky situations |
| `/codex` | | Open the Codex of custom items and recipes |

Admin commands (`/codexitem`, `/codexenchant`, `/enchantment`, `/codexdummy`) need OP or the `arcanum.admin` permission. The old `dashboard.admin` permission is still honored.

<img src="readme%20media/divider.svg" alt="" width="100%">

## The Codex

<img src="readme%20media/codex-overview.svg" alt="Codex structure" width="100%">

Open it with `/codex`. Every item lists its recipe and the machine it is crafted at.
The full item reference lives in [docs/README.md](docs/README.md); the items that are implemented are listed in [ItemsInCodex.md](ItemsInCodex.md).

## Updates

Arcanum checks the [GitHub Releases](https://github.com/Skullmc1/Arcanum/releases) page periodically. When a newer version is published it downloads the jar and applies it on the next server restart. You can also trigger a check yourself with `/arcanum update`.

Every push to `main` is built by GitHub Actions and published as a release named `v<base>.<build>`.

## Building from source

You need JDK 26 (the Gradle toolchain targets it; the output is Java 21 bytecode).

```bash
./gradlew build          # produces build/libs/Arcanum-<version>.jar
./gradlew runServer      # starts a Paper test server (default 26.3)
./gradlew runServer -PmcVersion=1.21.1   # test on another version
```

The project compiles against the Paper 1.21.1 API. When a Minecraft update renames a constant, add a lookup to `compat/Compat.java` instead of referencing it directly, so the single jar keeps working everywhere.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for what is planned next.

<div align="center">
<sub>Made by Qclid</sub>
</div>
