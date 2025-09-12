# ScreenCycle — Concept (Co‑Pilot for Family Link)

## Idea
Split Family Link’s allowed windows into child‑friendly cycles: PLAY for N minutes → REST for M minutes, repeating until the daily budget is consumed or the sleep window starts.

## Architecture (MVP)
- `CycleService` — foreground service that toggles PLAY/REST and broadcasts state.
- `AppAccessibilityService` — watches current foreground app.
- `BlockOverlayService` — draws full-screen overlay during REST for selected packages.
- `Prefs` — stores durations, selected packages, running state.
- `MainActivity`, `AppSelectionActivity` — UI.

## Permissions
- Overlay (draw over other apps)
- Accessibility Service
- Usage Stats (optional telemetry for robustness)
- Notifications (foreground service)

## Limitations
No public API for Family Link → Co‑Pilot mode infers availability via device state; it never expands access, only tightens it within Family Link windows.
