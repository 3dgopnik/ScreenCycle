# ScreenCycle — Android (30/30 cycles)

**Goal:** A companion app (“Co‑Pilot”) for Google Family Link that splits allowed screen‑time windows into configurable play/rest cycles (e.g., 30 min play → 30 min rest).

## MVP (v0.1)
- Set play/rest durations (defaults 30/30).
- Select “game” apps to control.
- Foreground service runs a PLAY/REST loop.
- During REST, selected apps are blocked with a full-screen overlay.
- Accessibility Service detects active app.
- PIN-gated settings.

## Co-Pilot to Family Link (concept)
- Respect Family Link windows (study/sleep). Work **only** inside allowed windows.
- Parent defines “daily budget” and cycle lengths (e.g., 2h total, 30/30).
- Two placement modes inside evening window: **continuous** or **spread across window**.
- If Family Link locks the device, pause; resume when it unlocks.

## Build
Open in **Android Studio** (Arctic Fox or newer).
- minSdk 26, targetSdk 34
- Kotlin
- AndroidX with Jetifier (enabled in `gradle.properties`)
- Requires JDK 17 (source/target compatibility and Kotlin `jvmTarget`)

## Documentation
- [User manual](docs/index.md)
- [Quickstart](docs/quickstart.md)

> This is an MVP scaffold. Hardening, PIN lock, and full Co-Pilot logic will be added in subsequent commits.
