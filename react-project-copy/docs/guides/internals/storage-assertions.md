# Внутренняя единица: проверки аргументов Storage

**Родительский модуль:** `storage`  
**Файл кода:** `src/database/StorageService.ts`

## `assertNonEmptyBookId`

`bookId` — непустая строка после trim.

## `assertValidProgress`

- Вызывает `assertNonEmptyBookId`.
- `lastChapterIndex`, `scrollOffset`, `lastReadTimestamp` — конечные числа (`Number.isFinite`).

## Связи

- Перед [`storage-api-progress.md`](./storage-api-progress.md) (`saveProgress`).

## Риски для агентов

Новые поля в `ReadingProgress` потребуют расширения assert и DDL.
