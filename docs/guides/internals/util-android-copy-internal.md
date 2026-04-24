# Внутренняя единица: `copyFileToInternalStorage`

**Родительский модуль:** `util-android-copy`  
**Файл кода:** `src/utils/epubPipelineAndroid.ts`

## Назначение

Копирует произвольный `sourceUri` (в т.ч. `content://`) в **`cacheDirectory/temp.epub`** — стабильный `file://` без отдельных разрешений к внешнему хранилищу.

## Инварианты

- Требует непустой `FileSystem.cacheDirectory`.
- После `copyAsync` проверяет `getInfoAsync`: файл существует и не каталог.

## Связи

- [`epub-service-class-unpack.md`](./epub-service-class-unpack.md) (шаг копирования перед unzip).
- [`import-library-orchestration.md`](./import-library-orchestration.md) (промежуточная копия перед переносом в library).

## Риски для агентов

Имя `temp.epub` фиксировано — параллельные импорты могут конфликтовать; сейчас вызовы сериализуются сценарием UI.
