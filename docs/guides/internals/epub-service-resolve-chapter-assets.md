# Внутренняя единица: `resolveChapterAssetUri`

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts` (приватная/вспомогательная логика вокруг ресурсов главы)

## Назначение

Разрешает относительные пути картинок и ссылок в HTML главы в абсолютные `file://` URI относительно каталога главы/OPF, чтобы WebView мог загрузить ресурсы без base64.

## Связи

- Используется внутри `prepareChapterBody` ([`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md)).

## Риски для агентов

Неверная база пути даёт пустые картинки; следить за согласованностью с `getSpineChapterUri`.
