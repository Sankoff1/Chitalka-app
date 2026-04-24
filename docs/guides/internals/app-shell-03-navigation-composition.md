# Внутренняя единица: NavigationContainer и вложение LibraryProvider

**Родительский модуль:** `app-shell` — `App.tsx`  
**Файл кода:** `App.tsx`, функция `RootNavigator`

## Состав

1. `View` с фоном `colors.background`.
2. `AndroidNavigationBar`, `StatusBar` (см. [`app-shell-02-android-system-ui.md`](./app-shell-02-android-system-ui.md)).
3. **`NavigationContainer`** с `ref={navigationRef}` и **`onReady={flushReaderNavigationIfPending}`** — сброс очереди перехода на Reader (см. [`navigation-ref-container-and-flush.md`](./navigation-ref-container-and-flush.md)).
4. **`LibraryProvider`** — **внутри** контейнера, чтобы `openBooksForSearch` и др. видели готовый `navigationRef`.

5. **`RootStack`** — см. [`navigation-root-stack.md`](./navigation-root-stack.md).

## Инвариант

`onReady` обязателен, если используется `navigateToReader` до полной готовности навигации.

## Риски для агентов

Перенос `LibraryProvider` выше `NavigationContainer` сломает навигационные вызовы из контекста, если не переписать привязку ref.
