# Внутренняя единица: `injectReaderViewportAndReflowCss`

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## Назначение

Встраивает в HTML главы meta viewport и CSS для переноса/читаемости в мобильном WebView (без изменения семантики контента книги сверх необходимого).

## Связи

- Вызывается из цепочки [`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md).

## Риски для агентов

Чрезмерный CSS может конфликтовать с встроенными стилями издательства EPUB — тестировать на нескольких книгах.
