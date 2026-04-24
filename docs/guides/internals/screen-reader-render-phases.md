# Внутренняя единица: фазы UI `ReaderScreen`

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## Состояние `phase`

- `loading` — индикатор, WebView может быть пустым.
- `ready` — [`ui-reader-view-webview-lifecycle.md`](./ui-reader-view-webview-lifecycle.md) с html/baseUrl/chapterKey/initialScrollY, кнопки глав, назад в библиотеку.
- `error` — текст [`screen-reader-error-mapping.md`](./screen-reader-error-mapping.md).

## Навигация назад в библиотеку

Вызывает `onBackToLibrary` из props (обёртка делает `goBack` + refresh count).

## Связи

- [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md).

## Риски для агентов

Не рендерить WebView с удалённым `baseUrl` при смене книги без сброса `phase`.
