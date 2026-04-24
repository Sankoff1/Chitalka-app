# Внутренняя единица: автозагрузка демо EPUB

**Родительский модуль:** `debug-autoload`  
**Файл кода:** `src/debug/debugAutoLoadEpub.ts`

## Флаги

`DEBUG_AUTO_LOAD_EPUB_ENABLED`, `DEBUG_DEMO_BOOK_ID` — правка точки включения без изменения `LibraryContext`.

## `isDebugAutoLoadEpubActive`

`__DEV__` + флаг + платформа android/ios.

## `runDebugAutoLoadEpubIfNeeded`

Идемпотентно гарантирует наличие демо-EPUB в библиотеке: если записи нет — загрузка ассета через `expo-asset`, импорт через [`import-library-orchestration.md`](./import-library-orchestration.md) с `suppressSuccessAlert`. **Читалку не открывает**: переход в читалку управляется автооткрытием последней книги (см. [`library-last-open-book.md`](./library-last-open-book.md) и [`library-context-restore-last-open.md`](./library-context-restore-last-open.md)). Зависит только от `storage`, `locale`, `onImported`.

## Связи

- [`debug-bundled-epub-asset.md`](./debug-bundled-epub-asset.md), [`library-context-debug-autoload-effect.md`](./library-context-debug-autoload-effect.md).

## Риски для агентов

Не включать автозагрузку в production-сборках.
