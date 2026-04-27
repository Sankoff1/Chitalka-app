# Внутренняя единица: `open` и `getSpineChapterUri`

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## `open(): Promise<EpubStructure>`

1. `await unpackThroughStep5()`.
2. Чтение OPF, построение spine ([`epub-service-xml-opf-spine.md`](./epub-service-xml-opf-spine.md)).
3. Пустой spine → `EPUB_EMPTY_SPINE`.
4. Возвращает `{ spine, toc: [], unpackedRootUri }` — TOC пока пустой массив.

## `getSpineChapterUri(spineIndex)`

Абсолютный `file://` к файлу главы; требует предварительного `open()`.

## Связи

- [`screen-reader-open-lifecycle.md`](./screen-reader-open-lifecycle.md).

## Риски для агентов

Вызывать `open` перед любыми URI глав; индекс в пределах `spine.length`.
