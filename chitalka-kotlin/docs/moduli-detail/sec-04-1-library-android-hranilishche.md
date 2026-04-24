---
moduli_section: "§4.1"
module: library-android
source: "../MODULI-I-KOMPONENTY.md#41-хранилище-и-база"
tags: [sqlite, StorageService, CRUD, LibraryBookLookup]
---

# §4.1. Модуль `library-android` — хранилище и база

Оглавление: [MODULI §4.1](../MODULI-I-KOMPONENTY.md#41-хранилище-и-база)

| Часть | Путь | Связи |
|-------|------|--------|
| Сервис хранилища | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/StorageService.kt` | Реализует `LibraryBookLookup` ([§3.2](sec-03-2-library-kotlin-sessiya.md)); типы [§3.1](sec-03-1-library-kotlin-tipy.md); вызывается из `app` и импорта [§4.2](sec-04-2-library-android-epub.md). |
| Ошибки | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/StorageServiceError.kt` | Классификация сбоев БД/файлов. |
| SQLite helper | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/ChitalkaSqliteOpenHelper.kt` | Схема таблиц для `StorageService`. |
| Инструментальный тест | `chitalka-kotlin/library-android/src/androidTest/java/com/chitalka/storage/StorageServiceInstrumentedTest.kt` | Проверка на устройстве/эмуляторе. |

Зависимость Gradle: `library-kotlin` ([§1](sec-01-gradle-sostav.md)).
