# Внутренняя единица: URI и пути файлов EPUB

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## `ensureFileUri`

Нормализует путь: исправляет префикс `null/` (баг opaque origin + путей на Android/Hermes).

## `ensureDirectoryRootFileUrl` / `fileUriToNativePath`

Подготовка каталогов и преобразование `file://` в путь для нативного `unzip`.

## `joinUnderUnpackedRoot`

Склеивает базу OPF-каталога и относительный `href` spine.

## Прочие мелкие утилиты в блоке

`stripXmlFragment`, `decodeBasicXmlEntities`, `escapeRegExp` — разбор и безопасная обработка строк XML/путей.

## Связи

- [`epub-service-class-unpack.md`](./epub-service-class-unpack.md), [`epub-service-class-open.md`](./epub-service-class-open.md).

## Риски для агентов

Любая смена формата URI ломает WebView `baseUrl` и `react-native-zip-archive`.
