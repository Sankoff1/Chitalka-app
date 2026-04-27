# Внутренняя единица: публичный баррель `src/i18n/index.ts`

**Родительский модуль:** `i18n-barrel`  
**Файл кода:** `src/i18n/index.ts`

## Экспорты

- Из `catalog`: `bookFallbackLabels`, `tSync`.
- Из `I18nContext`: `I18nProvider`, `useI18n`.
- Из `types`: `APP_LOCALES`, `LOCALE_STORAGE_KEY`, тип `AppLocale`.

## Связи

Потребители импортируют из `../i18n` или `@/i18n` в зависимости от стиля проекта (здесь относительные пути).

## Риски для агентов

Прямой импорт внутренних файлов минуя barrel допустим для tree-shaking, но стиль репозитория — через `index.ts` где уместно.
