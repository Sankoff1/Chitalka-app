# Внутренняя единица: API каталога библиотеки

**Родительский модуль:** `storage`  
**Файл кода:** `src/database/StorageService.ts`

## `addBook(row)`

Алиас на `upsertLibraryBook` (в коде явный JSDoc).

## `upsertLibraryBook(row)`

`INSERT … ON CONFLICT(book_id) DO UPDATE` — обновляет URI, метаданные, размер, обложку, `added_at` при том же `bookId`.

## `listLibraryBooks()`

Сортировка по `added_at DESC`.

## `getLibraryBook(bookId)`

Одна запись или `null`.

## Дополнительные выборки и операции (тот же `StorageService`)

В `src/database/StorageService.ts` также объявлены (для экранов библиотеки):

- `listRecentlyReadBooks`, `listFavoriteBooks`, `listTrashedBooks` — JOIN с прогрессом где нужно, фильтр по `deleted_at`.
- `setBookFavorite`, `setBookTotalChapters`, `moveBookToTrash`, `restoreBookFromTrash`, `purgeBook`.

## Связи

- [`core-type-library-book-record.md`](./core-type-library-book-record.md).
- [`import-library-orchestration.md`](./import-library-orchestration.md), [`screen-books-and-docs.md`](./screen-books-and-docs.md).

## Риски для агентов

Согласованность `bookId` между прогрессом и библиотекой обязательна.
