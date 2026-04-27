# Внутренняя единица: оркестрация `importEpubToLibrary`

**Родительский модуль:** `import-library`  
**Файл кода:** `src/library/importEpubToLibrary.ts`

## Поток

1. Проверка `documentDirectory`.
2. Каталоги `library_epubs/`, `library_covers/` — `makeDirectoryAsync`.
3. Имя файла: stem + suffix + `.epub` в постоянный путь `stableUri`.
4. [`util-android-copy-internal.md`](./util-android-copy-internal.md) → `copyAsync` в `stableUri`, удаление `temp.epub`.
5. `new EpubService(stableUri)` → `unpackThroughStep5` → [`epub-service-read-filesystem-metadata.md`](./epub-service-read-filesystem-metadata.md) → копия обложки в covers.
6. Сборка `LibraryBookRecord` с [`i18n-catalog.md`](./i18n-catalog.md) `bookFallbackLabels(locale)`.
7. [`storage-api-library.md`](./storage-api-library.md) `addBook`.
8. Опционально `Alert` успеха; `suppressSuccessAlert` в options.
9. `finally`: `svc.destroy()`.

## Возврат

`{ stableUri, bookId }` для навигации в Reader.

## Риски для агентов

Исключения пробрасываются наверх; вызывающий показывает Alert (см. LibraryContext).
