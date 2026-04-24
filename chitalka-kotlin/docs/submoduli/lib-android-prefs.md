---
id: lib-android-prefs
tags: [SharedPreferences, persistence]
module: library-android
package: com.chitalka.prefs
---

# `library-android` — подмодуль `com.chitalka.prefs`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/prefs/SharedPreferencesKeyValueStore.kt`

## Назначение

Реализация **ключ-значение** на `SharedPreferences` для контрактов Kotlin-модуля: last-open book id, настройки темы/локали (через функции `persist*` / `loadPersisted*` из [lib-kotlin-i18n.md](lib-kotlin-i18n.md) и [lib-kotlin-theme.md](lib-kotlin-theme.md)).

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-yadro.md](app-ui-yadro.md) | Создаёт `SharedPreferencesKeyValueStore(context)` как единое хранилище настроек и last-open |
| [lib-kotlin-library.md](lib-kotlin-library.md) | Интерфейс `LastOpenBookPersistence` |
