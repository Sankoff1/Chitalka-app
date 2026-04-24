# Внутренняя единица: `onMessage` и debounce RN

**Родительский модуль:** `ui-reader-view`  
**Файл кода:** `src/components/ReaderView.tsx`

## `handleMessage`

Парсит JSON:

- `t === 'scroll'` и конечное `y` → debounce → `onScrollOffsetChange`.
- `t === 'page'`, `dir === 'prev' | 'next'` → `onRequestPageChange?.(dir)`.
- `t === 'ready'` → `onContentReady?.()`.

## Debounce 350 ms (scroll)

Таймер на стороне RN перед вызовом `onScrollOffsetChange` — снижает частоту автосохранения.

## Cleanup

`useEffect` unmount: `clearTimeout` для debounce.

## Связи

- [`screen-reader-progress-autosave.md`](./screen-reader-progress-autosave.md).

## Риски для агентов

Несоответствие интервала 200 (web) vs 350 (native) — осознанная двухступенчатая фильтрация.
