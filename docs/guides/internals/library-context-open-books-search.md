# Внутренняя единица: состояние поиска в `LibraryContext`

**Родительский модуль:** `library-context`
**Файл кода:** `src/context/LibraryContext.tsx`

## Контракт

| Поле / метод | Поведение |
|--------------|-----------|
| `isSearchOpen: boolean` | Открыта ли строка поиска в `AppTopBar`. |
| `searchQuery: string` | Текущий запрос (хранится как есть; нормализация — на стороне потребителей). |
| `openSearch()` | `setIsSearchOpen(true)`. |
| `closeSearch()` | `setIsSearchOpen(false)` + очистка `searchQuery`. |
| `setSearchQuery(q)` | Замена запроса. Вызывается из `TextInput` в `AppTopBar` на каждом символе. |

## Назначение

Единое состояние поиска для всех экранов со списками книг: открытие инициирует `AppTopBar`, чтение (`searchQuery`) — `ReadingNowScreen`, `BooksAndDocsScreen`, `FavoritesScreen`, `TrashScreen`. Каждый экран фильтрует свою коллекцию через `toLocaleLowerCase().includes(...)` по `title` и `author` и отдаёт пустое состояние c `t('search.noResults')`, когда запрос не совпал.

## Связи

- [`navigation-app-top-bar.md`](./navigation-app-top-bar.md) — единственный писатель.
- Экраны drawer со списками книг — читатели.

## Риски для агентов

- `closeSearch` сбрасывает запрос; если когда-то понадобится «скрыть, но запомнить» — нужен отдельный флаг.
- На экранах `Settings` / `DebugLogs` лупа не рендерится; `AppTopBar` сам вызывает `closeSearch()` при переходе на них, чтобы состояние не «прилипало».
- Раньше здесь был `openBooksForSearch()` — императивная навигация на `BooksAndDocs`. Заменено на in-header поиск: прямых вызовов нет, поэтому в истории не ищите навигацию из шапки.
