# Внутренняя единица: API прогресса чтения

**Родительский модуль:** `storage`  
**Файл кода:** `src/database/StorageService.ts`

## `saveProgress(progress)`

- `assertValidProgress`.
- Prepared statement: `INSERT … ON CONFLICT(book_id) DO UPDATE`.
- Числа: `Math.trunc` для индекса главы и timestamp.

## `getProgress(bookId)`

- Возвращает `ReadingProgress | null`.
- Маппинг колонок snake_case → camelCase.

## Связи

- [`core-type-reading-progress.md`](./core-type-reading-progress.md).
- [`screen-reader-progress-autosave.md`](./screen-reader-progress-autosave.md), [`screen-reader-open-lifecycle.md`](./screen-reader-open-lifecycle.md).

## Риски для агентов

Не блокировать UI длительными транзакциями; вызывается часто при автосохранении.
