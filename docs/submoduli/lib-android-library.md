---
id: lib-android-library
tags: [import-epub, refreshBookCount]
module: library-android
package: com.chitalka.library
---

# `library-android` — подмодуль `com.chitalka.library` (Android-часть пакета)

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/library/`

| Файл | Роль |
|------|------|
| `ImportEpubToLibrary.kt` | Копирование выбранного EPUB во внутреннее хранилище, разбор метаданных, запись в БД через [lib-android-storage.md](lib-android-storage.md); i18n [lib-kotlin-i18n.md](lib-kotlin-i18n.md). |
| `LibrarySessionRefresh.kt` | Расширение `LibrarySessionState.refreshBookCount(StorageService)` — обновление счётчика из SQLite. |

## Связи

| Направление | Кто |
|-------------|-----|
| Доменная часть пакета | [lib-kotlin-library.md](lib-kotlin-library.md) — те же имена пакета `com.chitalka.library` на classpath `app` |
| EPUB | [lib-android-epub.md](lib-android-epub.md) |
| App | [app-ui-yadro.md](app-ui-yadro.md) после picker и в читалке |
