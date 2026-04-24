# Внутренняя единица: тип `LibraryBookRecord`

**Родительский модуль:** `core-types`  
**Файл кода:** `src/core/types.ts`

## Поля

| Поле | Смысл |
|------|--------|
| `bookId` | Уникальный id в каталоге. |
| `fileUri` | Стабильный `file://` к EPUB в `library_epubs/` (не временный URI пикера). |
| `title`, `author` | Метаданные для UI. |
| `fileSizeBytes` | Размер файла. |
| `coverUri` | `file://` обложки или `null`. |
| `addedAt` | Unix ms. |

## Связи

- Таблица `library_books`: [`storage-api-library.md`](./storage-api-library.md).
- Создаётся в [`import-library-orchestration.md`](./import-library-orchestration.md).
- Список: [`screen-books-and-docs.md`](./screen-books-and-docs.md).

## Риски для агентов

`fileUri` должен оставаться доступным после импорта; не сохранять `content://` как долгосрочный путь.
