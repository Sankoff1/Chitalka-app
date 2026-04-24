# Внутренняя единица: `EpubServiceError` и экранирование HTML

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`

## `EpubServiceError`

Расширяет `Error`, поле `cause` для цепочки оригинальной ошибки.

## `escapeHtmlAttrValue(raw, quote)`

Экранирует `&`, `<`, кавычки для безопасной подстановки в HTML-атрибуты при сборке разметки главы.

## Связи

- Используется в цепочке подготовки HTML ([`epub-service-class-prepare-chapter.md`](./epub-service-class-prepare-chapter.md)).

## Риски для агентов

Не смешивать с React-экранированием — это чистый HTML для WebView.
