# Внутренняя единица: `readFilesystemLibraryMetadata`

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts` (экспортируемая async-функция)

## Назначение

По уже распакованному корню книги (`unpackedRootUri`) читает OPF и возвращает **title**, **author**, **coverFileUri** без поднятия полного `EpubService.open()` для UI импорта.

## Связи

- [`import-library-orchestration.md`](./import-library-orchestration.md) после `unpackThroughStep5`.
- Использует те же XML/OPF хелперы, что и [`epub-service-xml-opf-spine.md`](./epub-service-xml-opf-spine.md).

## Риски для агентов

Должен вызываться только при валидной распаковке; иначе исключения из чтения файлов.
