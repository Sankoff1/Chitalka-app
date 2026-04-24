# Внутренняя единица: `installConsoleCapture`

**Родительский модуль:** `debug-console-capture`  
**Файл кода:** `src/debug/installConsoleCapture.ts`

## Поведение

Подменяет методы `console.*` на обёртки, вызывающие оригинал + [`debug-log-buffer.md`](./debug-log-buffer.md).

## Идемпотентность

Флаг на `globalThis` с уникальным ключом — повторный вызов безопасен.

## Точка подключения

Импорт из [`entry-02-console-capture.md`](./entry-02-console-capture.md).

## Риски для агентов

Рекурсия при логировании внутри `debugLogAppend` — избегать `console.log` внутри низкоуровневого append без guard.
