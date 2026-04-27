---
moduli_section: "§4.4"
module: library-android
source: "../MODULI-I-KOMPONENTY.md#44-настройки-и-обновление-сессии"
tags: [SharedPreferences, LastOpenBookPersistence, refreshBookCount]
---

# §4.4. Модуль `library-android` — настройки и обновление сессии

Оглавление: [MODULI §4.4](../MODULI-I-KOMPONENTY.md#44-настройки-и-обновление-сессии)

| Часть | Путь | Связи |
|-------|------|--------|
| Key-value | `chitalka-kotlin/library-android/src/main/java/com/chitalka/prefs/SharedPreferencesKeyValueStore.kt` | Реализует контракты персистентности из `library-kotlin` (`LastOpenBookPersistence`, настройки темы/локали — [§3.2](sec-03-2-library-kotlin-sessiya.md), [§3.7](sec-03-7-library-kotlin-i18n-tema.md)); создаётся в `ChitalkaApp`. |
| Обновление сессии | `chitalka-kotlin/library-android/src/main/java/com/chitalka/library/LibrarySessionRefresh.kt` | `suspend fun LibrarySessionState.refreshBookCount(StorageService)` — после импорта/читалки ([§2.2](sec-02-2-app-compose-koren.md), [§2.4](sec-02-4-app-chitalka.md)). |
