# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]
### Added
- Added `gradle.properties` enabling AndroidX and Jetifier.

### Changed
- Removed `package` attribute from manifest and marked `MainActivity` as exported.

### Docs
- Documented AndroidX and Jetifier build requirements.
- Added troubleshooting note about `android:exported` requirement on Android 12+.
