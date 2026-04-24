# Внутренняя единица: автозагрузка демо EPUB

**Родительский модуль:** `debug-autoload`  
**Файл кода:** `src/debug/debugAutoLoadEpub.ts`

## Флаги

`DEBUG_AUTO_LOAD_EPUB_ENABLED`, `DEBUG_DEMO_BOOK_ID` — правка точки включения без изменения `LibraryContext`.

## `isDebugAutoLoadEpubActive`

`__DEV__` + флаг + платформа android/ios.

## `runDebugAutoLoadEpubIfNeeded`

Загрузка ассета через `expo-asset`, импорт через [`import-library-orchestration.md`](./import-library-orchestration.md) с `suppressSuccessAlert`, открытие reader.

## Связи

- [`debug-bundled-epub-asset.md`](./debug-bundled-epub-asset.md), [`library-context-debug-autoload-effect.md`](./library-context-debug-autoload-effect.md).

## Риски для агентов

Не включать автозагрузку в production-сборках.
