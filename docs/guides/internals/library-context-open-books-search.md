# Внутренняя единица: `openBooksForSearch`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Поведение

Если `navigationRef.isReady()` — `navigationRef.navigate('Main', { screen: 'BooksAndDocs' })`.

## Назначение

Переход из шапки к списку книг при непустой библиотеке.

## Связи

- [`navigation-app-top-bar.md`](./navigation-app-top-bar.md).

## Риски для агентов

Без `isReady()` — no-op; не дублировать навигацию в TopBar напрямую.
