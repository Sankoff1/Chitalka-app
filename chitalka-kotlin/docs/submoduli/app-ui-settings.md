---
id: app-ui-settings
tags: [settings, locale, theme-persist]
module: app
path: app/.../ui/settings/
---

# Модуль `app/` — подмодуль `ui/settings/`

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/settings/ChitalkaSettingsPane.kt`

## Назначение

- UI **настроек**: язык интерфейса, тема.
- Вызов функций персистентности из `library-kotlin` (`persistLocale`, `persistThemeMode`) с реализацией хранилища из `library-android` (`SharedPreferencesKeyValueStore`), передаваемым сверху.

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-kotlin-i18n.md](lib-kotlin-i18n.md), [lib-kotlin-theme.md](lib-kotlin-theme.md), [lib-android-prefs.md](lib-android-prefs.md) |
| ← | [app-ui-yadro.md](app-ui-yadro.md) — drawer `Settings` |
