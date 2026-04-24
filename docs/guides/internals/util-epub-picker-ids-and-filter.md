# Внутренняя единица: идентификация EPUB при выборе файла

**Родительский модуль:** `util-epub-picker`  
**Файл кода:** `src/utils/epubPicker.ts`

## `deriveBookId(fileName)`

- Берёт basename пути, убирает `.epub`, trim.
- Пустой результат → `book_${Date.now()}`.

## `isEpubFileName(name)`

Проверка расширения `.epub` без учёта регистра.

## Внутренняя логика `isLikelyEpubAsset`

Комбинация имени файла и `mimeType` (подстрока `epub`) для фильтрации результата пикера.

## Связи

- Результат передаётся в [`import-library-orchestration.md`](./import-library-orchestration.md) и в [`library-context-pick-toolbar.md`](./library-context-pick-toolbar.md).

## Риски для агентов

`bookId` не гарантирует уникальность в БД при разных файлах с одним именем — см. дедупликацию/стратегию в импорте при необходимости.
