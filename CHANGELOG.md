# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]
### Added
- Added PIN screen requiring verification before accessing settings.
- Added `gradle.properties` enabling AndroidX and Jetifier.
- Introduced day/night theme and color palette.
- Added base `.editorconfig` and `.gitattributes` for consistent formatting.
- Added instrumented test to verify `MainActivity` launches without exceptions.
- Added instrumented tests for permission dialog and service start/stop.
- Added `SettingsRepository` using DataStore for durations and blocked packages.
- Added main screen counter showing number of selected apps.
- Cycle service now broadcasts remaining time every second and updates notification and UI.
- Introduced `BlockActivity` to display a full-screen rest screen.
- Dialog prompting to enable accessibility service when disabled.
- Localization unit test ensuring Russian strings match the default locale.

### Changed
- Removed `package` attribute from manifest and marked `MainActivity` as exported.
- Set Java and Kotlin compilation targets to version 17.
- Switched app theme to inherit from `Theme.MaterialComponents.DayNight.NoActionBar`.
- Replaced `Prefs` usage in activities and services with `SettingsRepository`.
- Replaced global broadcasts with `LocalBroadcastManager`.
- `AppAccessibilityService` now returns to the home screen and launches `BlockActivity` during rest for selected packages.
- Cycle starts only when required permissions are granted.
- Start button toggles between "Working..." and "Stop" while the cycle runs.
- Permissions dialog now handles battery optimization exemption; duplicate logic removed from `MainActivity`.
- Refined instrumented tests to assert `PermissionsDialogFragment` visibility and verify service start/stop state via `ActivityScenario`.
- Finalized adaptive launcher icons and moved them to mipmap resources.
- Updated Russian translations for accessibility description and battery optimization dialog.

### Removed
- Removed legacy `Prefs` helper based on `SharedPreferences`.
- Removed `BlockOverlayService` and related overlay permission.

### Fixed
- Resolved AAPT build error by externalizing accessibility service description.
- Replaced theme `colorBackground` with `android:colorBackground` to ensure correct resource resolution.
- Overlay now consumes touch events to block input during rest phase.
- Text input fields on the main screen now stretch to full width.
- Added required `FOREGROUND_SERVICE_DATA_SYNC` permission.
- Removed deprecated `TYPE_PHONE` overlay window type.
- Start button now detects running cycle on app return and shows **Stop**.
- Use `getEnabledAccessibilityServiceList` to query enabled accessibility services.
- Declared `SYSTEM_ALERT_WINDOW` permission so the app appears in overlay settings.

### Docs
- Added user manual skeleton with build and run instructions and linked docs from README.
- Documented AndroidX and Jetifier build requirements.
- Added troubleshooting note about `android:exported` requirement on Android 12+.
- Noted automatic light/dark theme switching in the UI guide.
- Added FAQ entry explaining touch blocking during rest.
- Noted full-width input fields on the main screen in the UI guide.
- Documented project concept in root `CONCEPT.md` and linked from README and docs index.
- Updated UI guide and README to mention selected app counter.
 - Updated roadmap statuses for MVP features.
- Documented cycle timer on the main screen.
- Added UI guide section for start/stop button and noted permission dialog in quickstart.
- Documented permissions dialog and battery optimization request in quickstart and UI guide.
- Clarified battery optimization handling in quickstart and UI guide.
- Introduced permissions dialog guiding overlay, usage access, accessibility and battery optimization exemption.
- Added references to UI screenshots and a play/rest flow diagram.
