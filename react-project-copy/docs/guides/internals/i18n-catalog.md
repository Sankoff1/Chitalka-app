# Внутренняя единица: каталог строк `catalog.ts`

**Родительский модуль:** `i18n-catalog`  
**Файл кода:** `src/i18n/catalog.ts`

## Загрузка JSON

Импорт `ru.json`, `en.json` в объект `catalogs`.

## `getNested` / `tSync(locale, path, vars?)`

Разбор пути `a.b.c`, подстановка `{{name}}` из `vars`.

## `bookFallbackLabels(locale)`

Синхронные строки «без названия» / «неизвестный автор» для импорта.

## Связи

- [`import-library-orchestration.md`](./import-library-orchestration.md).

## Риски для агентов

Несуществующий ключ возвращает сам путь — легко заметить в UI при тесте.
