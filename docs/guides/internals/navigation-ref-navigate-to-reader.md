# Внутренняя единица: `navigateToReader`

**Родительский модуль:** `nav-ref`  
**Файл кода:** `src/navigation/navigationRef.ts`

## Поведение

1. Записывает параметры в `pendingReader`.
2. Сразу вызывает `flushReaderNavigationIfPending()`.
3. Если очередь не пуста — планирует `setTimeout` тики по 50 ms до 50 попыток (~2.5 s), каждый раз вызывая flush.
4. По таймауту — `console.warn` и сброс `pendingReader`.

## Зачем

После импорта `navigationRef.isReady()` может быть ещё ложь — без повторов переход молча не срабатывает.

## Связи

- [`import-library-orchestration.md`](./import-library-orchestration.md) косвенно через вызовы из контекста; [`screen-books-and-docs.md`](./screen-books-and-docs.md).

## Риски для агентов

Не дублировать логику ожидания в других местах — централизовать здесь.
