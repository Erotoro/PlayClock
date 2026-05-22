[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%20%7C%2026.1%2B-62B047?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21%2B%20%2F%2025%2B-orange?style=flat-square)](https://adoptium.net/)
[![Tests](https://img.shields.io/badge/tests-passing-success?style=flat-square)](#download)
[![Release](https://img.shields.io/badge/Download-Releases-blue?style=flat-square)](https://github.com/maksg/PlayClock/releases)

# PlayClock

**PlayClock** is a **client-side Fabric Minecraft mod** that tracks your **server playtime** and **singleplayer world playtime** directly inside Minecraft.

If you want a clean **Minecraft playtime tracker mod** with **HUD**, **menu markers**, **hover tooltips**, **color customization**, and **Mod Menu settings**, PlayClock is built for that exact use case.

## Why Use PlayClock?

PlayClock helps you answer the questions Minecraft does not answer well by default:

- How long have I played on this server?
- How much time did I spend here today?
- How long is my current session?
- When did I last play this world?

Everything is shown directly in the UI you already use:

- multiplayer server list
- singleplayer world list
- hover tooltips
- in-game HUD
- controls menu
- Mod Menu settings screen

## Features

- Tracks playtime for:
  - saved multiplayer servers
  - direct connect targets
  - LAN / localhost servers
  - singleplayer worlds
- Shows:
  - **today**
  - **total**
  - **session**
  - **last played**
- Lightweight menu markers in server and world lists
- Mouse-follow hover tooltip with detailed stats
- Text-only HUD with **3 layout variants**
- Header summary in list screens
- Hotkeys for quick toggling
- Color presets and custom colors
- RGB / HEX color editing in Mod Menu
- Supports:
  - `English`
  - `Русский`
  - `Українська`
- Stores data locally in JSON
- Client-side only

## Supported Versions

PlayClock currently supports:

- **Minecraft 1.21.11**
- **Minecraft 26.1 / 26.1.1 / 26.1.2**

There are separate release files for each Minecraft branch.

## Download

Download the latest release here:

**[GitHub Releases](https://github.com/maksg/PlayClock/releases)**

Recommended files:

- `PlayClock-1.0-1.21.11.jar`
- `PlayClock-1.0-26.1+.jar`

You should also have:

- **Fabric Loader**
- **Fabric API**
- **Mod Menu** (optional, but recommended)

## Installation

1. Download the correct PlayClock jar for your Minecraft version.
2. Make sure you are using **Fabric Loader**.
3. Install **Fabric API**.
4. Put the PlayClock jar into your Minecraft `mods` folder.
5. Launch the game.

## How To Use

### In Menus

- Open the **multiplayer server list** or **singleplayer world list**
- Look for the playtime text shown next to entries
- Hover an entry to see:
  - today
  - total
  - session
  - last played

### In Game

PlayClock can show a simple text HUD with your current playtime stats.

Available HUD styles:

- `Minimal`
- `Compact`
- `Stacked`

### Hotkeys

Default controls:

- `H` — toggle HUD
- `B` — toggle menu markers

The hotkeys appear in the standard Minecraft **Controls** screen under the **PlayClock** category.

## Configuration

If you have **Mod Menu** installed, PlayClock includes a custom settings screen where you can change:

- HUD enabled / disabled
- menu markers enabled / disabled
- tooltip enabled / disabled
- HUD variant
- HUD position
- time format
- show or hide `today` in HUD
- show or hide `session` in HUD
- header summary enabled / disabled
- language override
- preset colors
- custom colors with RGB / HEX editing

## Notes

- PlayClock is **client-side only**
- Your playtime data is stored locally
- `Realms` is **not supported** in the current release

## Data Storage

PlayClock stores its data in:

`config/playclock/playclock-state.json`

## Need The Latest Version?

Check the releases page:

**[Download PlayClock Releases](https://github.com/maksg/PlayClock/releases)**

If you want the correct file quickly:

- use `PlayClock-1.0-1.21.11.jar` for **Minecraft 1.21.11**
- use `PlayClock-1.0-26.1+.jar` for **Minecraft 26.1+**

## License

`GPL-3.0-or-later`
