# Внутренняя единица: разметка `BookCard`

**Родительский модуль:** `ui-book-card`  
**Файл кода:** `src/components/BookCard.tsx`

## Пропсы

`title`, `author`, `fileSizeMb` (уже в МБ), `coverUri?`, `onPress`.

## UI

Прессable-карточка, строка обложки (или плейсхолдер), текстовый блок, размер файла через `t` с подстановкой.

## Связи

- [`theme-colors.md`](./theme-colors.md), [`i18n-context-provider.md`](./i18n-context-provider.md).
- Использование: [`screen-books-and-docs.md`](./screen-books-and-docs.md).

## Риски для агентов

`fileSizeMb` должен быть уже вычислен вызывающим (контракт пропса).
