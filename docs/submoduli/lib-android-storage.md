---
id: lib-android-storage
tags: [sqlite, StorageService, CRUD]
module: library-android
package: com.chitalka.storage
---

# `library-android` — подмодуль `com.chitalka.storage`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/`

| Файл | Роль |
|------|------|
| `ChitalkaSqliteOpenHelper.kt` | Создание/миграция схемы БД. |
| `StorageService.kt` | CRUD книг, прогресс, списки; реализует `LibraryBookLookup` из Kotlin-модуля. |
| `StorageServiceError.kt` | Типизированные ошибки слоя хранилища. |

## Связи

| Направление | Кто |
|-------------|-----|
| Типы записей | [lib-kotlin-core-types.md](lib-kotlin-core-types.md) |
| Вызовы из UI | [app-ui-yadro.md](app-ui-yadro.md), [app-ui-library.md](app-ui-library.md), [app-ui-reader.md](app-ui-reader.md) |
| Импорт | [lib-android-library.md](lib-android-library.md) пишет файлы и строки БД |

Тесты: [lib-android-testy.md](lib-android-testy.md).
