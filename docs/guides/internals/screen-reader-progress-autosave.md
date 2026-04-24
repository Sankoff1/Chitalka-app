# Внутренняя единица: автосохранение прогресса в `ReaderScreen`

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## `persistProgress(index, scrollY)`

`storage.saveProgress` с `Date.now()`; ошибки глотаются (чтение не блокируется).

## `scheduleScrollSave`

Debounce **500 ms** на запись после последнего скролла; обновляет `latestScrollRef`.

## `onScrollOffsetChange`

Обновляет `latestScrollRef` из [`ui-reader-view-message-and-debounce.md`](./ui-reader-view-message-and-debounce.md).

## Cleanup таймера скролла

Отдельный `useEffect` с пустыми deps — очистка при unmount.

## Связи

- Перед сменой главы `goChapter` вызывает немедленный `persistProgress` для текущей главы.

## Риски для агентов

Слишком маленький debounce увеличит нагрузку на SQLite.
