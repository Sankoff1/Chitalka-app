# Внутренняя единица: эффект открытия книги

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## `useEffect` на `[bookPath, bookId, storage, t]`

1. Сброс фаз: `loading`, очистка spine/html, `unpackedRootUri`.
2. `epubRef.current?.destroy()`; новый `EpubService(bookPath)`.
3. `getProgress(bookId)`; `epub.open()`; проверка `cancelled`.
4. Восстановление индекса главы и скролла из прогресса.
5. `getSpineChapterUri` + `prepareChapter` → `setChapterHtml`, `phase='ready'`.
6. Сохранение прогресса + вызов `onOpenedRef.current()`.
7. Ошибка → `destroy`, `phase='error'`, текст через [`screen-reader-error-mapping.md`](./screen-reader-error-mapping.md).

## Cleanup

`cancelled = true`, `destroy` сервиса.

## Связи

- [`epub-service-class-open.md`](./epub-service-class-open.md), [`storage-api-progress.md`](./storage-api-progress.md).

## Риски для агентов

Гонки при смене `bookId` до завершения — обрабатываются флагом `cancelled`.
