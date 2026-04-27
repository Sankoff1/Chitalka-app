# Внутренняя единица: Storage в контексте и `libraryEpoch`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## `StorageService`

Один экземпляр на жизнь провайдера через `useMemo(() => new StorageService(), [])`.

## `refreshBookCount`

`countLibraryBooks()` → `setBookCount`; при ошибке → 0.

## Начальная загрузка

`useEffect` при монтировании: `refreshBookCount`, затем `setStorageReady(true)`.

## `libraryEpoch` / `bumpLibraryEpoch`

Монотонный счётчик для принудительного обновления списков (импорт, внешние bump). Подписчики сравнивают `libraryEpoch > 0` в эффектах.

## Связи

- [`storage-api-counts-clear.md`](./storage-api-counts-clear.md).

## Риски для агентов

`storageReady` гейтит welcome-модалку — не показывать импорт до готовности.
