# Внутренняя единица: `I18nProvider` и `useI18n`

**Родительский модуль:** `i18n-context`  
**Файл кода:** `src/i18n/I18nContext.tsx`

## Состояние

`locale` по умолчанию `'ru'`; при монтировании чтение AsyncStorage по [`i18n-types.md`](./i18n-types.md) ключу.

## API контекста

- `setLocale` — обновление state + запись в AsyncStorage.
- `t(path, vars?)` — обёртка над `tSync` с текущей локалью.

## `useI18n`

Бросает вне провайдера.

## Связи

- [`i18n-catalog.md`](./i18n-catalog.md).

## Риски для агентов

`t` не для тяжёлой логики в hot path без мемоизации колбэков.
