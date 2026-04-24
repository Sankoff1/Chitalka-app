---
id: lib-kotlin-library
tags: [LibrarySessionState, LastOpenBook, LibraryBookLookup]
module: library-kotlin
package: com.chitalka.library
---

# `library-kotlin` — подмодуль `com.chitalka.library`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/`

## Назначение

| Файл | Роль |
|------|------|
| `LibrarySessionState.kt` | Сессионное состояние «как в LibraryContext»: счётчик книг, поиск, флаги welcome/picker. |
| `LibraryBookLookup.kt` | Интерфейс «найти книгу по id» — реализует `StorageService` в Android-модуле. |
| `LastOpenBook.kt` | Ключи и операции с id последней открытой книги (без знания SharedPreferences). |
| `LastOpenReaderRestore.kt` | `restoreLastOpenReaderIfNeeded` — при старте открыть читалку, если в персистентности есть валидная книга. |

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-kotlin-core-types.md](lib-kotlin-core-types.md) через lookup/записи |
| ← Android | [lib-android-storage.md](lib-android-storage.md) реализует lookup; [lib-android-library.md](lib-android-library.md) — расширение `refreshBookCount`; [lib-android-prefs.md](lib-android-prefs.md) — `LastOpenBookPersistence` |
| ← App | [app-ui-yadro.md](app-ui-yadro.md) держит `LibrarySessionState` и вызывает restore/import |

**Внимание:** классы в пакете `com.chitalka.library` также дополняются файлами из `library-android` (`ImportEpubToLibrary`, `LibrarySessionRefresh`) — один пакет на classpath у `app`.
