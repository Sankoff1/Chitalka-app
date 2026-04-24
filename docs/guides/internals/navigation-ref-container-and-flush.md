# Внутренняя единица: ref контейнера и сброс очереди Reader

**Родительский модуль:** `nav-ref`  
**Файл кода:** `src/navigation/navigationRef.ts`

## `navigationRef`

`createNavigationContainerRef<RootStackParamList>()` — привязка в `App.tsx` к `NavigationContainer`.

## `pendingReader`

Пара `{ bookPath, bookId }` или `null`, если переход выполнен.

## `flushReaderNavigationIfPending`

Если есть `pendingReader` и `navigationRef.isReady()` — `navigate('Reader', p)` и очистка очереди.

## Связи

- [`app-shell-03-navigation-composition.md`](./app-shell-03-navigation-composition.md) `onReady`.

## Риски для агентов

Без `onReady` переходы, инициированные до готовности, теряются.
