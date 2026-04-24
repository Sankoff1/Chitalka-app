# Внутренняя единица: `AppTopBar`

**Родительский модуль:** `ui-top-bar` (файл в `components/`, используется навигацией)  
**Файл кода:** `src/components/AppTopBar.tsx`

## Структура

- Кнопка меню → `navigation.openDrawer()`.
- Заголовок из `options.title`.
- Кнопка поиска показывается если `bookCount > 0` ([`library-context-contract.md`](./library-context-contract.md)); вызывает `openBooksForSearch()` → переход на `Main` / `BooksAndDocs`.

## Связи

- [`navigation-app-drawer-shell.md`](./navigation-app-drawer-shell.md) как `header`.

## Риски для агентов

Иконка поиска скрыта при пустой библиотеке — ожидаемое UX.
