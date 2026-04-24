# Внутренняя единица: фазы UI `ReaderScreen`

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## Состояние `phase`

- `loading` — индикатор, пока нет готового `unpackedRootUri` / слоёв.
- `ready` — хост `pageHost` с **до двух** наложенных [`ui-reader-view-webview-lifecycle.md`](./ui-reader-view-webview-lifecycle.md): активный слой с жестами, второй может грузиться для следующей главы; индикатор «текущая/всего» берёт `activeLayer.chapterIndex`.
- `error` — текст [`screen-reader-error-mapping.md`](./screen-reader-error-mapping.md).

## Навигация назад в библиотеку

Вызывает `onBackToLibrary` из props (обёртка делает `goBack` + refresh count).

## Связи

- [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md).

## Риски для агентов

Не рендерить WebView с удалённым `baseUrl` при смене книги без сброса `phase`.
