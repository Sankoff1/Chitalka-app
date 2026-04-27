# Внутренняя единица: счётчики и полная очистка

**Родительский модуль:** `storage`  
**Файл кода:** `src/database/StorageService.ts`

## `countLibraryBooks()`

Количество строк в `library_books` — для welcome и top bar.

## `countBooksWithProgress()`

Количество книг с сохранённым прогреком (отдельный запрос).

## `clearAllData()`

Удаляет все строки из **обеих** таблиц — библиотека и прогресс.

## Связи

- [`library-context-storage-epoch.md`](./library-context-storage-epoch.md) (`refreshBookCount`).

## Риски для агентов

`clearAllData` деструктивна; вызывать только из явного UX «сброс данных».
