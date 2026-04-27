# Внутренняя единица: открытие БД и миграция схемы

**Родительский модуль:** `storage`  
**Файл кода:** `src/database/StorageService.ts`

## `getDatabase`

- Ленивое открытие: первый вызов создаёт `openPromise`.
- Параллельные вызовы делят один `openAndMigrate`.
- При ошибке сбрасывает `openPromise` и `db` для повторной попытки.

## `openAndMigrate`

- `openDatabaseAsync('chitalka.db')`.
- `execAsync` с `CREATE TABLE IF NOT EXISTS` для `reading_progress` и `library_books` + индексы.
- Перед `ALTER TABLE … ADD COLUMN` для `library_books`: **`PRAGMA table_info(library_books)`** → `Set` имён колонок; `addLibraryColumnIfMissing` пропускает уже существующие поля и обновляет кэш после успешного `ALTER` (меньше гонок и шума в логах, чем опора только на ошибку SQLite).

## Связи

- [`storage-errors-and-logging.md`](./storage-errors-and-logging.md).

## Риски для агентов

Изменение схемы: добавлять версионирование миграций, если понадобятся ALTER — сейчас только IF NOT EXISTS.
