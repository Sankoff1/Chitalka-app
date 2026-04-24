# Внутренняя единица: `BookActionsSheet`

**Родительский модуль:** `ui-book-actions-sheet`  
**Файл кода:** `src/components/BookActionsSheet.tsx`

## Назначение

Нижний модальный лист (`Modal` + `Animated`): обложка/заголовок/автор и список действий `BookActionItem` (иконка `MaterialIcons`, подпись, опционально `destructive`).

## Публичный API

Пропсы: `visible`, `title`, `author`, `coverUri`, `actions`, `onClose`. Экраны передают `actions` через `useMemo` от выбранной книги (`activeBook`).

## Связи

- [`theme-colors.md`](./theme-colors.md), [`i18n-context-provider.md`](./i18n-context-provider.md).
- Использование: [`screen-books-and-docs.md`](./screen-books-and-docs.md), списки «Сейчас читаю» и избранного (см. [`MODULES.md`](../../MODULES.md)).

## Риски для агентов

Закрывать лист (`onClose`) после действия, если нужно сбросить `activeBook`, иначе повторный тап может показать устаревшие `actions`.
