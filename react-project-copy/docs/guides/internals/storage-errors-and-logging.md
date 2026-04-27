# Внутренняя единица: ошибки и логирование Storage

**Родительский модуль:** `storage` — `StorageService.ts`  
**Файл кода:** `src/database/StorageService.ts` (верх файла)

## `StorageServiceError`

Именованная ошибка для пользовательски осмысленных сбоев открытия БД и операций.

## `logError`

В dev — полный объект; в prod — укороченное сообщение.

## `wrapOpenFailure` / `wrapOperationFailure`

Преобразуют неизвестные ошибки в `StorageServiceError` с русским текстом для показа/логов.

## Связи

- Вызывается из [`storage-open-migrate.md`](./storage-open-migrate.md) и методов API при сбоях SQL.

## Риски для агентов

Не раскрывать сырой SQL пользователю; при добавлении операций использовать те же обёртки.
