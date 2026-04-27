# Внутренняя единица: вспомогательные функции импорта

**Родительский модуль:** `import-library`  
**Файл кода:** `src/library/importEpubToLibrary.ts`

## `sanitizeFileStem(bookId)`

Очистка символов файловой системы, обрезка длины, fallback `book`.

## `shortFileSuffix(bookId)`

Детерминированный короткий суффикс из хеша строки — уникальность имён файлов при коллизии имён.

## `coverExtensionFromUri`

Выбор расширения обложки по URI.

## `logImportStage`

Единый префикс `[Chitalka][Импорт]` для стадий.

## Связи

- Используются только внутри [`import-library-orchestration.md`](./import-library-orchestration.md).

## Риски для агентов

Менять правила sanitize — проверять длину путей на Windows/Android.
