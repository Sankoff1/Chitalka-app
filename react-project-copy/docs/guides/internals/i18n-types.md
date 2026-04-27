# Внутренняя единица: типы локали

**Родительский модуль:** `i18n-types`  
**Файл кода:** `src/i18n/types.ts`

## `AppLocale`

Обычно `'ru' | 'en'` — см. фактический union в файле.

## `APP_LOCALES`

Массив поддерживаемых кодов для UI выбора.

## `LOCALE_STORAGE_KEY`

Ключ AsyncStorage для сохранения выбранной локали.

## Связи

- [`i18n-context-provider.md`](./i18n-context-provider.md).

## Риски для агентов

Добавление языка → обновить типы, JSON, `APP_LOCALES`, проверку при чтении из storage.
