# Внутренняя единица: логика `BooksAndDocsScreen`

**Родительский модуль:** `screen-books`  
**Файл кода:** `src/screens/BooksAndDocsScreen.tsx`

## `loadBooks`

`storage.listLibraryBooks()` → state; ошибки → пустой список.

## `useFocusEffect`

Перезагрузка при фокусе экрана.

## `useEffect` на `libraryEpoch`

Если `libraryEpoch > 0` — повторная загрузка (импорт извне).

## Список

`FlatList` + [`ui-book-card.md`](./ui-book-card.md); `openReader` → [`navigation-ref-navigate-to-reader.md`](./navigation-ref-navigate-to-reader.md).

## FAB

Вызывает `pickEpubFromToolbar` из [`library-context-contract.md`](./library-context-contract.md).

## Связи

- Локальный `useMemo(() => new StorageService())` — отдельный экземпляр от контекста (осознанно).

## Риски для агентов

Не забывать `libraryEpoch` при новых путях импорта в обход контекста.
