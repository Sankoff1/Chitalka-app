# Внутренняя единица: контракт `LibraryContext`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Значение контекста

- `bookCount`, `storageReady`
- `libraryEpoch`, `bumpLibraryEpoch`
- `refreshBookCount`, `pickEpubFromToolbar`, `openBooksForSearch`

## `useLibrary()`

Бросает, если провайдер отсутствует — ожидается использование только под деревом из [`app-shell-03-navigation-composition.md`](./app-shell-03-navigation-composition.md).

## Связи

Потребители: [`navigation-app-top-bar.md`](./navigation-app-top-bar.md), [`screen-books-and-docs.md`](./screen-books-and-docs.md), экраны корзины/избранного/«сейчас читаю» (эпоха и счётчик), [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md), [`screen-library-legacy.md`](./screen-library-legacy.md).

## Риски для агентов

Не использовать `useLibrary` вне `LibraryProvider`.
