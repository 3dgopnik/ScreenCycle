# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]
### Added
- Added `gradle.properties` enabling AndroidX and Jetifier.
- Added placeholder adaptive launcher icons.
- Introduced day/night theme and color palette.
- Added base `.editorconfig` and `.gitattributes` for consistent formatting.
- Added instrumented test to verify `MainActivity` launches without exceptions.
- Added `SettingsRepository` using DataStore for durations and blocked packages.
- Added main screen counter showing number of selected apps.
- Cycle service now broadcasts remaining time every second and updates notification and UI.

### Changed
- Removed `package` attribute from manifest and marked `MainActivity` as exported.
- Set Java and Kotlin compilation targets to version 17.
- Switched app theme to inherit from `Theme.MaterialComponents.DayNight.NoActionBar`.
- Replaced `Prefs` usage in activities and services with `SettingsRepository`.
- Replaced global broadcasts with `LocalBroadcastManager`.

### Removed
- Removed legacy `Prefs` helper based on `SharedPreferences`.

### Fixed
- Resolved AAPT build error by externalizing accessibility service description.
- Replaced theme `colorBackground` with `android:colorBackground` to ensure correct resource resolution.
- Overlay now consumes touch events to block input during rest phase.
- Text input fields on the main screen now stretch to full width.
- Added required `FOREGROUND_SERVICE_DATA_SYNC` permission.
- Removed deprecated `TYPE_PHONE` overlay window type.

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
