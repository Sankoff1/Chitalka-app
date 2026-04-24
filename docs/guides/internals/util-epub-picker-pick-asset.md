# Внутренняя единица: `pickEpubAsset` и тип результата

**Родительский модуль:** `util-epub-picker`  
**Файл кода:** `src/utils/epubPicker.ts`

## Тип `EpubPickResult`

Дискриминированный union:

- `{ kind: 'ok'; uri: string; bookId: string }`
- `{ kind: 'canceled' }`
- `{ kind: 'error'; messageKey: string }` — ключ для `t(messageKey)` в UI, не готовая строка.

## `pickEpubAsset`

- Вызывает `expo-document-picker`.
- Проверяет тип/имя через внутренние хелперы (см. [`util-epub-picker-ids-and-filter.md`](./util-epub-picker-ids-and-filter.md)).
- Возвращает один из вариантов union.

## Связи

- [`library-context-pick-toolbar.md`](./library-context-pick-toolbar.md), [`library-context-pick-welcome.md`](./library-context-pick-welcome.md), [`screen-library-legacy.md`](./screen-library-legacy.md).

## Риски для агентов

При `error` не показывать `messageKey` пользователю без перевода через i18n.
