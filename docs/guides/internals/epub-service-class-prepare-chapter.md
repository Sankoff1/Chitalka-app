# Внутренняя единица: подготовка HTML главы

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## `prepareChapter(htmlPath)`

Обёртка с [`util-timeout-with-timeout.md`](./util-timeout-with-timeout.md) (`EPUB_ERR_TIMEOUT_PREPARE_CHAPTER`) вокруг `prepareChapterBody`.

## `prepareChapterBody` (private)

- Читает файл как UTF-8.
- Подставляет абсолютные URI ресурсов ([`epub-service-resolve-chapter-assets.md`](./epub-service-resolve-chapter-assets.md)).
- Инъекция viewport/CSS ([`epub-service-inject-viewport-css.md`](./epub-service-inject-viewport-css.md)).

## Связи

- [`ui-reader-view-webview-lifecycle.md`](./ui-reader-view-webview-lifecycle.md) получает готовый HTML и `baseUrl`.

## Риски для агентов

Большие главы + сотни картинок — не использовать base64 для изображений (сознательное решение в коде).
