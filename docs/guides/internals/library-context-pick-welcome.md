# Внутренняя единица: `pickEpubFromWelcome`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Отличия от тулбара

1. Сброс `welcomePickerHint`, `suppressWelcomeForPicker = true`.
2. **`await new Promise(r => setTimeout(r, 320))`** — даёт RN закрыть/скрыть модалку перед системным пикером.
3. Тот же пикер и импорт, что в [`library-context-pick-toolbar.md`](./library-context-pick-toolbar.md).
4. При успехе: `welcomeDismissedSession`, epoch, count, reader.
5. Ошибки: `welcomePickerHint` + иногда `Alert`.
6. `finally`: `suppressWelcomeForPicker = false`.

## Связи

- [`library-context-welcome-modal.md`](./library-context-welcome-modal.md).

## Риски для агентов

Уменьшать 320 ms может вернуть баг невидимого пикера на Android.
