# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]
### Added
- Added `gradle.properties` enabling AndroidX and Jetifier.
- Added placeholder adaptive launcher icons.

### Changed
- Removed `package` attribute from manifest and marked `MainActivity` as exported.
- Set Java and Kotlin compilation targets to version 21.

### Fixed
- Resolved AAPT build error by externalizing accessibility service description.

### Docs
- Added user manual skeleton with build and run instructions and linked docs from README.
- Documented AndroidX and Jetifier build requirements.
- Added troubleshooting note about `android:exported` requirement on Android 12+.
