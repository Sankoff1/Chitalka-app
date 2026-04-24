# Внутренняя единица: начальная прокрутка после загрузки

**Родительский модуль:** `ui-reader-view`  
**Файл кода:** `src/components/ReaderView.tsx`

## `applyInitialScroll`

`injectJavaScript('window.scrollTo(0, y)')` где `y = floor(max(0, initialScrollY))` при конечном числе.

## Триггер

`onLoadEnd` WebView — после загрузки документа.

## Связи

- [`screen-reader-open-lifecycle.md`](./screen-reader-open-lifecycle.md) задаёт `initialScrollY` из прогресса.

## Риски для агентов

Слишком ранний scroll до `onLoadEnd` может не примениться.
