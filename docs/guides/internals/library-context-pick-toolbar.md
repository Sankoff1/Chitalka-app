# Внутренняя единица: `pickEpubFromToolbar`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Поток

1. [`util-epub-picker-pick-asset.md`](./util-epub-picker-pick-asset.md).
2. Отмена — выход; ошибка — `Alert` с `t(messageKey)`.
3. [`import-library-orchestration.md`](./import-library-orchestration.md) с locale из i18n.
4. `setLibraryEpoch`, `refreshBookCount`, [`navigation-ref-navigate-to-reader.md`](./navigation-ref-navigate-to-reader.md).

## Ошибки

`Alert` с текстом ошибки или `library.importFailed`.

## Связи

FAB на [`screen-books-and-docs.md`](./screen-books-and-docs.md).

## Риски для агентов

Дублировать импорт в экране без обновления epoch сломает список книг.
