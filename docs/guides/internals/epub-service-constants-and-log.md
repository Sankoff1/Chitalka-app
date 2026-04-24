# Внутренняя единица: константы, таймауты и лог EPUB

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts` (начало файла)

## Константы

- `EPUB_OPEN_LOG` — префикс строки лога.
- `BOOK_CACHE_SEGMENT` — подкаталог `book_cache/` под `documentDirectory`.
- `TIMEOUT_COPY_MS`, `TIMEOUT_UNZIP_MS`, `TIMEOUT_PREPARE_CHAPTER_MS` — пределы ожидания нативных операций.

## Экспортируемые коды ошибок

`EPUB_EMPTY_SPINE`, `EPUB_ERR_TIMEOUT_*` — строки сообщений для сравнения в UI ([`screen-reader-error-mapping.md`](./screen-reader-error-mapping.md)).

## `logEpubOpen(step, detail?)`

Dev-ориентированный лог шагов открытия; длинные `detail` обрезаются.

## Риски для агентов

Менять строки таймаут-ошибок только синхронно с Reader и импортом.
