# PlayClock Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-minded client-side Fabric mod with shared core logic and two Minecraft-specific adapters that track and render playtime for singleplayer and multiplayer targets.

**Architecture:** The project is a Gradle multi-module repository with one pure-Java core module and two Fabric Loom adapter modules for `1.21.11` and `26.1.x`. The adapters translate Minecraft client lifecycle and screen rendering events into core services responsible for identity, persistence, formatting, and playtime accounting.

**Tech Stack:** Java 21, Gradle, Fabric Loom, Fabric Loader, Fabric API, JUnit 5, Gson, Mod Menu

---

### Task 1: Repository Scaffold

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `playclock-core/build.gradle`
- Create: `playclock-mc12111/build.gradle`
- Create: `playclock-mc261x/build.gradle`
- Create: `README.md`
- Create: `LICENSE`

- [ ] **Step 1: Add Gradle multi-module settings and common build conventions**
- [ ] **Step 2: Add pure-Java core module**
- [ ] **Step 3: Add two Fabric Loom adapter modules with distinct Minecraft coordinates**
- [ ] **Step 4: Add repository docs and license metadata**

### Task 2: Core Identity Model

**Files:**
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/model/TargetKind.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/model/SourceType.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/model/TrackedTarget.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/identity/ServerIdentityNormalizer.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/identity/WorldIdentityResolver.java`
- Create: `playclock-core/src/test/java/dev/maksg/playclock/core/identity/ServerIdentityNormalizerTest.java`
- Create: `playclock-core/src/test/java/dev/maksg/playclock/core/identity/WorldIdentityResolverTest.java`

- [ ] **Step 1: Write failing tests for multiplayer normalization**
- [ ] **Step 2: Run tests to verify expected failures**
- [ ] **Step 3: Implement normalization and world identity resolution**
- [ ] **Step 4: Run tests and refactor names/contracts**

### Task 3: Core Stats and Session Tracking

**Files:**
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/time/Clock.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/time/SystemClock.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/stats/PlaytimeStats.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/stats/SessionTracker.java`
- Create: `playclock-core/src/test/java/dev/maksg/playclock/core/stats/SessionTrackerTest.java`

- [ ] **Step 1: Write failing tests for session start, tick, stop, and day rollover**
- [ ] **Step 2: Verify red state**
- [ ] **Step 3: Implement minimal session accounting**
- [ ] **Step 4: Re-run tests and refactor carefully**

### Task 4: JSON Persistence and Config

**Files:**
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/config/PlayClockConfig.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/storage/PlayClockStore.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/storage/JsonPlayClockStore.java`
- Create: `playclock-core/src/test/java/dev/maksg/playclock/core/storage/JsonPlayClockStoreTest.java`

- [ ] **Step 1: Write failing persistence tests for save/load and schema version**
- [ ] **Step 2: Verify failures**
- [ ] **Step 3: Implement JSON storage**
- [ ] **Step 4: Re-run tests and refine file layout**

### Task 5: Adapter Bootstrap

**Files:**
- Create: `playclock-mc12111/src/main/java/dev/maksg/playclock/mc12111/PlayClock12111.java`
- Create: `playclock-mc12111/src/main/java/dev/maksg/playclock/mc12111/client/PlayClock12111Client.java`
- Create: `playclock-mc261x/src/main/java/dev/maksg/playclock/mc261x/PlayClock261x.java`
- Create: `playclock-mc261x/src/main/java/dev/maksg/playclock/mc261x/client/PlayClock261xClient.java`
- Create: `playclock-mc12111/src/main/resources/fabric.mod.json`
- Create: `playclock-mc261x/src/main/resources/fabric.mod.json`

- [ ] **Step 1: Add Fabric entrypoints and shared bootstrap wiring**
- [ ] **Step 2: Configure resources and mod metadata for both artifacts**
- [ ] **Step 3: Verify each adapter builds**

### Task 6: UI Overlay Foundation

**Files:**
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/ui/BadgeViewModel.java`
- Create: `playclock-core/src/main/java/dev/maksg/playclock/core/ui/TooltipViewModel.java`
- Create: adapter-specific mixins and render helpers under each adapter module

- [ ] **Step 1: Define badge and tooltip view-model contracts**
- [ ] **Step 2: Add adapter render hooks for list rows**
- [ ] **Step 3: Validate layout assumptions in both target lines**

### Task 7: HUD and Mod Menu Config

**Files:**
- Create: core config presenter classes and adapter-specific config screen classes
- Create: Mod Menu integration metadata / API implementation files

- [ ] **Step 1: Add configurable HUD foundation**
- [ ] **Step 2: Add minimal config screen and Mod Menu entry**
- [ ] **Step 3: Verify user-facing settings flow**

### Task 8: Verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Run core tests**
- [ ] **Step 2: Run adapter builds**
- [ ] **Step 3: Document supported versions, build commands, and current limitations**
