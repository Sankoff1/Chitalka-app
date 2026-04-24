# Внутренняя единица: `LibraryScreen` (наследие)

**Родительский модуль:** `screen-library-legacy`  
**Файл кода:** `src/screens/LibraryScreen.tsx`

## Назначение

Самодостоятельный экран: выбор EPUB через пикер, вызов внешнего `onBookSelected(uri, bookId)`, затем `bumpLibraryEpoch` и `refreshBookCount` из контекста.

## Статус в приложении

**Не подключён** к `AppDrawer` / `RootStack` в текущей навигации — оставлен для встраивания или будущего маршрута.

## Связи

- [`util-epub-picker-pick-asset.md`](./util-epub-picker-pick-asset.md), [`library-context-contract.md`](./library-context-contract.md).

## Риски для агентов

Не считать его основным потоком импорта; основной — контекст + BooksAndDocs FAB.
