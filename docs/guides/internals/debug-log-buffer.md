# Внутренняя единица: буфер `DebugLog`

**Родительский модуль:** `debug-log-buffer`  
**Файл кода:** `src/debug/DebugLog.ts`

## Модель записи

`DebugLogEntry`: `ts`, `level`, `message`.

## Лимит

Кольцевой буфер: константа **`MAX_ENTRIES = 4000`** в `DebugLog.ts` — при переполнении остаётся хвост массива.

## API

`debugLogAppend`, `debugLogSubscribe`, `debugLogGetSnapshot`, `debugLogClear`, экспорт формата для экрана логов.

## Связи

- [`debug-console-capture.md`](./debug-console-capture.md), [`screen-debug-logs.md`](./screen-debug-logs.md).

## Риски для агентов

Слушатели не должны бросать — обёртки try/catch внутри notify.
