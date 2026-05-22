# PlayClock Design

## Overview

PlayClock is a client-side Fabric mod for Minecraft that tracks and displays player time spent in singleplayer worlds and multiplayer targets without mutating server MOTDs, server names, or world titles. The mod stores data locally in JSON, renders compact right-side badges in world and server lists, and optionally shows a contextual in-game HUD.

## Product Goals

- Track total playtime for each supported target.
- Track current session duration.
- Track per-day playtime using the player's local calendar day.
- Track last played timestamp.
- Render compact UI overlays in multiplayer and singleplayer selection screens.
- Support automatic Minecraft language selection with English, Russian, and Ukrainian localizations.
- Provide a minimal in-game config screen reachable from Mod Menu.
- Ship separate artifacts for Minecraft `1.21.11` and `26.1.x`.

## Supported Targets

### Singleplayer

Singleplayer worlds are tracked separately from multiplayer targets.

- Primary identity: normalized world folder name.
- Stored metadata:
  - `worldIdentityKey`
  - `folderName`
  - `rawDisplayName`
  - `lastKnownMetadata`

The data model must allow a future migration to stronger world identity if a stable internal identifier becomes available.

### Multiplayer

All network targets are represented under one multiplayer model.

- `sourceType`:
  - `saved`
  - `direct`
  - `lan`
- `localhost` is not its own source type; it is represented as `isLocalAddress`.
- Stored metadata:
  - `rawAddress`
  - `normalizedAddress`
  - `canonicalKey`
  - `sourceType`
  - `isLocalAddress`

Normalization rules for `canonicalKey`:

- trim whitespace
- lower-case host
- strip default port `25565`
- preserve explicit non-default ports
- support IPv4, domain names, and IPv6 safely
- do not merge by DNS, MOTD, or inferred aliases

## UI

### Server and World Lists

- Render a compact right-side badge inside the row area reserved by the design mockup.
- Default badge content: total playtime only.
- On hover or focus, show a tooltip containing:
  - total playtime
  - today playtime
  - current session playtime
  - last played

The overlay must not modify the original row title, subtitle, or MOTD text.

### In-Game HUD

- Contextual HUD, configurable on/off.
- Minimal by default.
- Intended for current session and target-specific quick reference.

### Config UI

- Integrate with Mod Menu.
- Provide a minimal custom config screen in `v1`.

## Storage

JSON storage is local-only and versioned from day one.

Required persisted concepts:

- schema version
- config
- target registry / metadata index
- stats per target
- timestamps for last updates

The storage layer must support forward migration logic.

## Metrics Model

Each tracked target stores at minimum:

- `totalPlaytimeSeconds`
- `todayPlaytimeSeconds`
- `currentSessionSeconds`
- `lastPlayedAt`

Session tracking requirements:

- start tracking when the client enters a supported target
- stop tracking on clean disconnect / world leave
- periodically flush to disk for crash resilience
- perform day rollover using the local calendar day

## Architecture

The repository is one codebase with a shared core and two Minecraft-specific adapters.

- `playclock-core`
  - pure Java logic
  - target identity model
  - stats aggregation
  - storage
  - formatting
  - config model
- `playclock-mc12111`
  - Fabric adapter for Minecraft `1.21.11`
- `playclock-mc261x`
  - Fabric adapter for Minecraft `26.1`, `26.1.1`, `26.1.2`

Each adapter is responsible for:

- client lifecycle integration
- screen hooks / mixins
- HUD rendering
- bridging Minecraft events into the core service layer

## Build and Release

- Java 21
- Fabric Loader + Fabric API
- Fabric Loom
- separate JAR per supported Minecraft line
- GPL-3.0-or-later license
- open-source repository structure suitable for CI and future publishing

## Out of Scope for v1

- Realms
- cloud sync
- cross-device sync
- historical charts / analytics dashboards
- aggressive host alias merging
- non-JSON persistence backends

## Risk Areas

- GUI integration differences between `1.21.11` and `26.1.x`
- correct target identity without false merges
- crash-safe persistence without excessive disk churn
- preserving clean list layouts across GUI scale and language variations

## Recommended Delivery Order

1. Build foundation and shared contracts.
2. Implement and test identity normalization and stats tracking.
3. Implement JSON persistence and schema versioning.
4. Wire both Minecraft adapters to lifecycle events.
5. Add list overlays and tooltip rendering.
6. Add HUD and config screen.
