---
id: lib-kotlin-core-types
tags: [domain, LibraryBookRecord, ReadingProgress]
module: library-kotlin
package: com.chitalka.core.types
---

# `library-kotlin` — подмодуль `com.chitalka.core.types`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/`

## Назначение

- **`LibraryBookRecord`** — запись книги в библиотеке (id, пути, флаги удаления и т.д.).
- **`ReadingProgress`** — позиция/прогресс чтения.
- **`LibraryBookWithProgress`** — агрегат для списков UI.

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [lib-android-storage.md](lib-android-storage.md) | SQLite и маппинг строк БД → типы |
| [lib-android-library.md](lib-android-library.md) | Импорт EPUB создаёт записи |
| [app-ui-library.md](app-ui-library.md), [app-ui-reader.md](app-ui-reader.md) | Отображение и сохранение прогресса |

Без Android; тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`CoreTypesTest`).
