# Внутренняя единица: метаданные, обложка и `destroy`

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## `getMetadata()`

Возвращает `{ title, author }` из OPF (после открытия/распаковки).

## `resolveCoverFileUri()`

Разрешает путь к файлу обложки для копирования на диск библиотеки при импорте.

## `destroy()`

Сбрасывает поля экземпляра: `spineItems = []`, `opfDirFileUrl = ''`, `unpackedRootUri = null` — не удаляет файлы на диске, только состояние объекта.

## Связи

- Импорт: [`import-library-orchestration.md`](./import-library-orchestration.md) вызывает `destroy()` в `finally` после распаковки для метаданных.

## Риски для агентов

После `destroy` не вызывать методы экземпляра без нового `new EpubService`.
