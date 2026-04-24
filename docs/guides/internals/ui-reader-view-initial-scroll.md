# Внутренняя единица: начальная прокрутка после загрузки

**Родительский модуль:** `ui-reader-view`  
**Файл кода:** `src/components/ReaderView.tsx`

## `handleLoadEnd` (`onLoadEnd` WebView)

Один инжект IIFE: `window.scrollTo(0, y)` с `y = floor(max(0, initialScrollY))` (или 0 при нечисле); затем **два `requestAnimationFrame`** и `postMessage(JSON.stringify({ t: 'ready' }))` в RN (`onContentReady`). Если `requestAnimationFrame` нет — fallback `setTimeout(..., 32)`.

## Связи

- [`screen-reader-open-lifecycle.md`](./screen-reader-open-lifecycle.md) задаёт `initialScrollY` из прогресса.

## Риски для агентов

Слишком ранний scroll до `onLoadEnd` может не примениться.
