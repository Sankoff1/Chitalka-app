---
id: lib-android-picker
tags: [open-document, ActivityResult]
module: library-android
package: com.chitalka.picker
---

# `library-android` — подмодуль `com.chitalka.picker`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/picker/EpubPickerAndroid.kt`

## Назначение

- Описать **MIME-типы** и `ActivityResultContracts` для выбора `.epub`.
- Смаппировать `Uri` из `ContentResolver` в [lib-kotlin-picker.md](lib-kotlin-picker.md) `EpubPickResult`.

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-yadro.md](app-ui-yadro.md) | `rememberLauncherForActivityResult` в `ChitalkaApp` |
