# Внутренняя единица: разметка `BookCard`

**Родительский модуль:** `ui-book-card`  
**Файл кода:** `src/components/BookCard.tsx`

## Пропсы

| Проп | Смысл |
|------|--------|
| `title`, `author` | Текст под обложкой. |
| `coverUri` | Обложка или плейсхолдер-эмодзи. |
| `progress` | Доля 0..1; если не число — полоса прогресса скрыта. |
| `isFavorite` | Бейдж «♥» на обложке. |
| `onPress` | Открытие книги / переход. |
| `onLongPress` | Опционально (задержка long press по умолчанию RN). |

## UI

Прессable-карточка: при `coverUri` — `Image`; иначе fallback-блок с укороченным заголовком/автором и акцентной полосой (`colors.topBar`). При `progress` — трек + процент через `t('books.readPercent', { percent })`, `accessibilityRole="progressbar"` при наличии прогресса. Рамка обложки: `hairline` + полупрозрачный `borderColor`.

## Связи

- [`theme-colors.md`](./theme-colors.md), [`i18n-context-provider.md`](./i18n-context-provider.md).
- Использование: [`screen-books-and-docs.md`](./screen-books-and-docs.md), экраны «Сейчас читаю» и избранное (см. [`MODULES.md`](../../MODULES.md)).

## Риски для агентов

`progress` передаётся уже нормализованным смыслом 0..1 со стороны списка (`LibraryBookWithProgress.progressFraction`). Размер файла на карточке **не** отображается — при необходимости считать на уровне экрана отдельно.
