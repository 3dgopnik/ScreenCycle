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

### Changed
- Removed `package` attribute from manifest and marked `MainActivity` as exported.
- Set Java and Kotlin compilation targets to version 17.
- Switched app theme to inherit from `Theme.MaterialComponents.DayNight.NoActionBar`.

### Fixed
- Resolved AAPT build error by externalizing accessibility service description.
- Replaced theme `colorBackground` with `android:colorBackground` to ensure correct resource resolution.
- Overlay now consumes touch events to block input during rest phase.

### Docs
- Added user manual skeleton with build and run instructions and linked docs from README.
- Documented AndroidX and Jetifier build requirements.
- Added troubleshooting note about `android:exported` requirement on Android 12+.
- Noted automatic light/dark theme switching in the UI guide.
- Added FAQ entry explaining touch blocking during rest.
