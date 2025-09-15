# Android contribution guide

## Supported tools
- Android Studio Giraffe or newer
- Gradle 8.x
- JDK/Kotlin 17

## Tests
Run instrumented tests on an emulator or device:
```bash
./gradlew connectedAndroidTest
```

## CI
No dedicated CI. Run tests locally before pushing.

## Secrets
None required for local development.

## CLI help
List available Gradle tasks:
```bash
./gradlew tasks
```
