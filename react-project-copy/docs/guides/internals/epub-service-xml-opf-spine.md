# Внутренняя единица: разбор OPF и построение spine

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## Цепочка

1. `readOpfFromUnpackedRoot` — `META-INF/container.xml` → путь к OPF → чтение OPF XML и каталога OPF как base URL.
2. `extractManifestIdToHrefMap`, `extractSpineItemrefsFromOpf`, `extractItemHrefById`, `buildSpineFromOpfXml`.
3. Вспомогательные: `pickDcText` для title/creator из DC metadata.

## Результат

Массив `EpubSpineItem[]` с индексом, `href`, `idref`, `linear`.

## Связи

- [`epub-service-class-open.md`](./epub-service-class-open.md).
- Пустой spine → [`epub-service-constants-and-log.md`](./epub-service-constants-and-log.md) `EPUB_EMPTY_SPINE`.

## Риски для агентов

Нестандартные EPUB (несколько spine, некорректный OPF) требуют ручных тестов.
