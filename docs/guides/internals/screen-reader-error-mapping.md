# Внутренняя единица: маппинг ошибок в `ReaderScreen`

**Родительский модуль:** `screen-reader`  
**Файл кода:** `src/screens/ReaderScreen.tsx`

## `clampChapterIndex(index, spineLength)`

Ограничивает индекс диапазоном spine; при нулевой длине возвращает 0.

## `errorMessage(error, t)`

- `EpubServiceError` + известные коды → ключи `reader.errors.*`.
- Иначе текст `Error` или `reader.errors.unknown`.

## Связи

- [`epub-service-constants-and-log.md`](./epub-service-constants-and-log.md) коды ошибок.

## Риски для агентов

Новый код ошибки EPUB → добавить ветку и ключ в `ru.json`/`en.json`.
