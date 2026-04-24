# Внутренняя единица: `AppTopBar`

**Родительский модуль:** `ui-top-bar` (файл в `components/`, используется навигацией)
**Файл кода:** `src/components/AppTopBar.tsx`

## Структура

- **Обычный режим** (`isSearchOpen === false`):
  - Слева — кнопка меню → `navigation.openDrawer()`.
  - В центре — заголовок из `options.title`.
  - Справа — кнопка поиска (лупа), если **экран поддерживает поиск** и `bookCount > 0`; по нажатию вызывает `openSearch()` из `LibraryContext`.
- **Режим поиска** (`isSearchOpen === true`):
  - Слева — `arrow-back` → `closeSearch()` (сбрасывает и состояние, и `searchQuery`).
  - В центре — `TextInput` с `placeholder = t('search.placeholder')`; меняет `searchQuery` через `setSearchQuery`. Автофокус через `setTimeout(50 ms)` после рендера, чтобы клавиатура гарантированно поднялась.
  - Справа — `close` очищает `searchQuery` без закрытия строки (если запрос непустой).

## Где поиск спрятан

Множество `NON_SEARCHABLE_ROUTES = new Set(['Settings', 'DebugLogs'])` — экраны без списков книг. Решение принимается по `route.name` из `DrawerHeaderProps`. Для этих экранов:

- Лупа не рендерится (и в открытом режиме `TextInput` не показывается).
- Если пользователь перешёл на такой экран при открытом поиске — `useEffect` автоматически вызовет `closeSearch()`, чтобы состояние не «протекло» обратно.

## Связи

- [`navigation-app-drawer-shell.md`](./navigation-app-drawer-shell.md) — монтирует компонент как `header`.
- [`library-context-contract.md`](./library-context-contract.md) — источник состояния поиска (`isSearchOpen`, `searchQuery`, `openSearch`, `closeSearch`, `setSearchQuery`).

## Риски для агентов

- Лупа скрыта при `bookCount === 0` даже на «книжных» экранах — ожидаемое UX.
- Список `NON_SEARCHABLE_ROUTES` жёстко совпадает с именами экранов drawer (`DrawerParamList`). При добавлении нового экрана без списка книг не забыть дописать его туда.
- Экраны со списками книг (`ReadingNow`, `BooksAndDocs`, `Favorites`, `Cart`) сами фильтруют `books` по `searchQuery` через `toLocaleLowerCase().includes(...)` по `title` и `author`; `AppTopBar` не знает про содержимое списков.
