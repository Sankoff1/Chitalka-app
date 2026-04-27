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
| `totalChapters` | Длина spine (0, пока книгу не открывали в читалке). |
| `isFavorite` | Флаг избранного. |
| `deletedAt` | Время soft-delete в корзину (`null` — книга активна). |

Тип **`LibraryBookWithProgress`** расширяет запись полями `lastChapterIndex` и `progressFraction` (0..1) для списков.

## Связи

- Таблица `library_books`: [`storage-api-library.md`](./storage-api-library.md).
- Создаётся в [`import-library-orchestration.md`](./import-library-orchestration.md).
- Списки: [`screen-books-and-docs.md`](./screen-books-and-docs.md) и экраны «Сейчас читаю» / избранное / корзина (см. [`MODULES.md`](../../MODULES.md)).

## Риски для агентов

`fileUri` должен оставаться доступным после импорта; не сохранять `content://` как долгосрочный путь.
