# Устранение неполадок

## Проект не собирается
- Убедитесь, что в `gradle.properties` включены `android.useAndroidX=true` и `android.enableJetifier=true`.
- На устройствах с Android 12 и выше проверьте, что в `AndroidManifest.xml` у `MainActivity` указано `android:exported="true"`.

## Экран выбора приложений пустой
- Приложению требуется разрешение `android.permission.QUERY_ALL_PACKAGES`, чтобы видеть установленные пакеты и категории, доступные для блокировки. Убедитесь, что разрешение объявлено в `AndroidManifest.xml` рядом с другими `uses-permission`.
- Для публикации в Play Store можно ограничить видимость через `<queries>` и оставить только интенты `MAIN` + `LAUNCHER`, если глобальный доступ ко всем пакетам не нужен.
- Проверить установку разрешения можно командой `adb shell dumpsys package com.example.screencycle | grep QUERY_ALL_PACKAGES` — в выводе должен появиться соответствующий флаг.
- Дополнительно выполните инструментальный тест `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.screencycle.core.PackageVisibilityTest`, чтобы убедиться, что `getInstalledApplications(0)` возвращает больше одного пакета.
