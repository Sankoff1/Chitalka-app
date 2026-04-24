# Chitalka-app

Офлайн-читалка EPUB: Expo 54, React Native, TypeScript. Карта кода — [docs/MODULES.md](docs/MODULES.md).

## Запуск

```bash
npm install
npx expo start
```

Нативный запуск с dev client (нужны папки `android` / `ios`, см. ниже): `npx expo run:android` или `npx expo run:ios`.

## Android: EAS

Сборка на серверах Expo, на машине достаточно Node и `eas-cli`. Логин и первичная настройка: `eas login`, при необходимости `eas build:configure`.

```bash
npm install
eas build --profile preview -p android      # APK, см. eas.json
eas build --profile production -p android   # релиз (обычно AAB под магазин)
```

Ссылка на артефакт — в выводе команды и в кабинете expo.dev. Про настройку билдов: [docs.expo.dev/build](https://docs.expo.dev/build/introduction/).

## Android: локальный APK (Windows)

Нужны Android SDK и JDK 17+. SDK часто лежит в `%LOCALAPPDATA%\Android\Sdk`; JDK — рядом (`jdk-17`) или JBR из Android Studio (`…\Android Studio\jbr`).

Сгенерировать нативный проект, если папки `android` ещё нет:

```powershell
$env:CI = "1"
npx expo prebuild --platform android
```

В `android/local.properties` одна строка (слэши прямые, путь свой):

```properties
sdk.dir=C:/Users/<username>/AppData/Local/Android/Sdk
```

Перед Gradle в PowerShell:

```powershell
$env:JAVA_HOME = "C:\Users\<username>\AppData\Local\Android\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Сборка из корня репозитория:

```powershell
cd android
.\gradlew.bat --stop
.\gradlew.bat assembleRelease
```

APK: `android/app/build/outputs/apk/release/app-release.apk`. Debug: `.\gradlew.bat assembleDebug` → `android/app/build/outputs/apk/debug/app-debug.apk`.

Если Gradle ругается на блокировку файлов в `node_modules\react-native-reanimated\android\build`, остановите демоны (`.\gradlew.bat --stop`), снесите эту папку `build` и повторите `assembleRelease`. Странный NDK без `source.properties` — удалить битую папку в `%LOCALAPPDATA%\Android\Sdk\ndk\…` или доустановить NDK из SDK Manager.

## Версии в релизе

В `eas.json` стоит `appVersionSource: "remote"`: билд- и версионные номера для стора ведутся через EAS/консоли, а не только правкой `app.json` вручную.

### Повторная сборка APK вручную (пример для Windows PowerShell)

Если нужна повторная сборка релизного APK, используйте следующий порядок команд (пути указывайте под себя):

```powershell
cd D:\project\Chitalka-app\android
$env:JAVA_HOME = "C:\Users\Alex\AppData\Local\Android\jdk-17"
$env:ANDROID_HOME = "C:\Users\Alex\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat --stop
.\gradlew.bat assembleRelease
```
### Повторная сбора дебаг версии
```powershell
cd D:\project\Chitalka-app\android
$env:JAVA_HOME = "C:\Users\Alex\AppData\Local\Android\jdk-17"
$env:ANDROID_HOME = "C:\Users\Alex\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat --stop
.\gradlew.bat assembleDebug
```
Готовый APK будет лежать в `android/app/build/outputs/apk/release/app-release.apk`.