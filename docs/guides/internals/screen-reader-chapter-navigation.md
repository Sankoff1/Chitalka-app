# Внутренняя единица: навигация по главам

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## `goChapter(nextIndex)`

- Guard: есть `epub`, spine, `phase === 'ready'`.
- `clampChapterIndex`; если индекс не изменился — return.
- Сначала `persistProgress` для **текущей** главы с последним скроллом.
- `phase='loading'`, `prepareChapter` для новой главы, сброс скролла в 0, `persistProgress(clamped, 0)`.
- Ошибка → `error` + текст.

## Кнопки назад/вперёд

`onBack` / `onForward` вызывают `goChapter(chapterIndex ± 1)`.

## Связи

- [`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md).

## Риски для агентов

Пропуск `persistProgress` перед сменой главы теряет позицию в предыдущей главе.
