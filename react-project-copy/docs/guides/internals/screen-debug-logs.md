# Внутренняя единица: `DebugLogsScreen`

**Родительский модуль:** `screen-debug-logs`  
**Файл кода:** `src/screens/DebugLogsScreen.tsx`

## Подписка

`debugLogSubscribe` → обновление списка из [`debug-log-buffer.md`](./debug-log-buffer.md).

## Экспорт

Форматирование снимка, запись во временный файл, `expo-sharing` при доступности.

## Очистка

Вызов `debugLogClear` с подтверждением `Alert`.

## Связи

- [`debug-console-capture.md`](./debug-console-capture.md) наполняет буфер.

## Риски для агентов

Большие логи — следить за лимитом буфера и производительностью списка.
