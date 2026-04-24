# Внутренняя единица: навигация по главам и перелистывание

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## Состояние слоёв

Два буфера `ReaderLayerState` — `layerA` / `layerB` с полями `chapterIndex`, `html`, `initialScrollY`, уникальный `token` (идёт в `ReaderView` как `chapterKey`). Активный слой: `activeLayerId` (`'a' | 'b'`). При переходе целевой неактивный слой получает новый HTML **до** анимации.

## `goChapter(nextIndex)`

- Guard: `epub`, длина spine, `phase === 'ready'`, не `flippingRef`, есть `activeLayer`.
- `clampChapterIndex`; без изменения индекса — выход.
- `persistProgress` для **текущей** главы и последнего скролла.
- `prepareChapter` для нового индекса; запись в **неактивный** слой (`inactiveLayerId`), выставление `transitionTargetLayerId`, `transitionDirection` по знаку `(next - current)`.
- `waitForPendingLayer(targetLayerId)`: ожидание `onContentReady` от соответствующего `ReaderView` (таймаут **400 ms** — форс-резолв).
- `runTransitionAnim(500)`: `Animated.timing(transitionAnim, { toValue: 1, useNativeDriver: true })` с интерполяциями opacity/translateX для уходящего и входящего слоя; полупрозрачные оверлеи «шейда» во время перехода.
- После анимации: `setActiveLayerId`, сброс `transitionTargetLayerId`, `resetTransitionAfterCommit` (rAF обнуляет значение анимации).
- Ошибка → `phase='error'`, сброс перехода; в `finally` снимается `flippingRef`, чистятся таймеры ожидания `ready`.

## Жесты и кнопки

`onRequestPageChange` передаётся в `ReaderView` **только если** слой совпадает с `activeLayerId` и нет незавершённого перехода — жесты не уходят на фоновый WebView.

## Связи

- [`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md).
- [`ui-reader-view-message-and-debounce.md`](./ui-reader-view-message-and-debounce.md) (`ready` / `page`).

## Риски для агентов

Пропуск `persistProgress` перед сменой главы теряет позицию. Несогласованность `token` и `onContentReady(layerId)` ломает ожидание и может зависнуть визуально на полупрозрачном слое.
