# Внутренняя единица: жизненный цикл WebView в `ReaderView`

**Родительский модуль:** `ui-reader-view`  
**Файл кода:** `src/components/ReaderView.tsx`

## `key={chapterKey}`

Полный remount WebView при смене главы — сброс DOM и скриптов.

## `source={{ html, baseUrl }}`

Фрагмент HTML + база для относительных путей (критично для картинок EPUB).

## Android

`androidLayerType="hardware"` — стабильнее композитинг при анимациях родителя (`ReaderScreen`).

## Безопасность / доступ к файлам

`originWhitelist={['*']}`, `allowFileAccess`, `allowFileAccessFromFileURLs`, `allowUniversalAccessFromFileURLs`, `mixedContentMode`, `setSupportMultipleWindows={false}` — осознанный компромисс для локального чтения.

## Связи

- HTML из [`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md).

## Риски для агентов

Ослабление `originWhitelist` для удалённого контента нежелательно.
