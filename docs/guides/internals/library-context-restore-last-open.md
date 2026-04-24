# Внутренняя единица: эффект автооткрытия последней книги

**Родительский модуль:** `library-context`
**Файл кода:** `src/context/LibraryContext.tsx`

## Условия запуска

`storageReady`, `lastOpenRestoreAttempted.current === false`. Эффект не зависит от дебаг-автозагрузки: [`debug-autoload-epub.md`](./debug-autoload-epub.md) только импортирует демо-книгу в библиотеку и сам читалку не открывает, поэтому автооткрытие проходит одним путём и в production, и в __DEV__.

## Действия

1. `getLastOpenBookId()` из [`library-last-open-book.md`](./library-last-open-book.md). Если нет — выход.
2. `storage.getLibraryBook(bookId)` — если записи нет или `deletedAt != null`, ключ очищается через `clearLastOpenBookId()`.
3. Иначе — `openReader(record.fileUri, record.bookId)` → [`navigation-ref-navigate-to-reader.md`](./navigation-ref-navigate-to-reader.md).

## Инварианты

- Эффект срабатывает **один раз** за жизнь провайдера (`useRef` guard), даже при повторных рендерах `storageReady`.
- Источник истины для `fileUri` — `StorageService`, не персисный ключ: это защищает от устаревших путей после переимпорта.

## Связи

- [`library-last-open-book.md`](./library-last-open-book.md) — чтение/очистка ключа.
- [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md) — кто ключ выставляет/очищает.
- [`storage-api-library.md`](./storage-api-library.md) — `getLibraryBook`.
- [`debug-autoload-epub.md`](./debug-autoload-epub.md) — исключение, когда эффект пропускается.

## Риски для агентов

- Не возвращать `openReader` в `runDebugAutoLoadEpubIfNeeded`: иначе в __DEV__ каждый запуск будет открывать книгу, перекрывая правило «в меню → остаёмся в меню».
- Не дублировать логику в `RootStack`/`App.tsx`: навигация должна идти строго через `navigateToReader`, который корректно ждёт готовности контейнера.
