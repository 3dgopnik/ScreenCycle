# Устранение неполадок

## Проект не собирается
- Убедитесь, что в `gradle.properties` включены `android.useAndroidX=true` и `android.enableJetifier=true`.
- На устройствах с Android 12 и выше проверьте, что в `AndroidManifest.xml` у `MainActivity` указано `android:exported="true"`.
