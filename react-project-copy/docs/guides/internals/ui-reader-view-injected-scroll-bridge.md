# Внутренняя единица: инжект скролл-моста

**Родительский модуль:** `ui-reader-view`  
**Файл кода:** `src/components/ReaderView.tsx`

## Строка `injectedScrollBridge`

IIFE в странице: слушатель `scroll` (passive), внутренний debounce 200 ms, затем `postMessage(JSON.stringify({ t: 'scroll', y }))` в React Native.

## Инвариант сообщения

Поле `t` должно быть `'scroll'`, `y` — число.

## Связи

- [`ui-reader-view-message-and-debounce.md`](./ui-reader-view-message-and-debounce.md).

## Риски для агентов

Любой синтаксический сбой в строке ломает весь мост скролла.
