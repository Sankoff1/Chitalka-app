# Внутренняя единица: `withTimeout`

**Родительский модуль:** `util-timeout`  
**Файл кода:** `src/utils/withTimeout.ts`

## Назначение

Оборачивает `Promise<T>`: при превышении `ms` отклоняет с `Error`, сообщение которого равно переданной строке `timeoutMessage` (используется для сопоставления с константами EPUB).

## Сигнатура

Асинхронная функция принимает промис, миллисекунды и строку ошибки.

## Связи

- [`epub-service-class-unpack.md`](./epub-service-class-unpack.md), [`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md).

## Риски для агентов

Строка таймаута должна совпадать с экспортами `EPUB_ERR_*` для маппинга ошибок в UI.
