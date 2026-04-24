# Внутренняя единица: тип `ReadingProgress`

**Родительский модуль:** `core-types`  
**Файл кода:** `src/core/types.ts`

## Поля

| Поле | Смысл |
|------|--------|
| `bookId` | PK в SQLite, строка книги в приложении. |
| `lastChapterIndex` | Индекс элемента **spine** (глава). |
| `scrollOffset` | Вертикальный скролл WebView. |
| `lastReadTimestamp` | Unix ms последнего сохранения. |

## Связи

- Колонки `reading_progress` в [`storage-api-progress.md`](./storage-api-progress.md).
- Заполняется в [`screen-reader-progress-autosave.md`](./screen-reader-progress-autosave.md).

## Риски для агентов

Индекс главы должен соответствовать длине spine после `open()`; см. `clampChapterIndex` в ReaderScreen.
