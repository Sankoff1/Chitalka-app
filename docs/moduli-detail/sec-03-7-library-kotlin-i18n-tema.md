---
moduli_section: "§3.7"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#37-локализация-и-тема-предпочтения"
tags: [i18n, ThemeColors, ThemeMode, I18nUiState, localization]
---

# §3.7. Модуль `library-kotlin` — локализация и тема

Оглавление: [MODULI §3.7](../MODULI-I-KOMPONENTY.md#37-локализация-и-тема-предпочтения)

| Часть | Путь | Связи |
|-------|------|--------|
| Каталог строк | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nCatalog.kt` | Ключи для UI и спеков экранов ([§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md)). |
| Типы локали | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nTypes.kt` | `AppLocale` и др. |
| Язык UI + персист | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nPreferences.kt` | `I18nUiState`, `loadPersistedLocale` / `persistLocale` — реализация хранения в [§4.4](sec-04-4-library-android-prefs-sessiya.md). |
| Палитра | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/theme/ThemeColors.kt` | `ChitalkaTheme`, читалка, CompositionLocal ([§2.2](sec-02-2-app-compose-koren.md)). |
| Режим темы | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/theme/ThemePreferences.kt` | `ThemeUiState`, `loadPersistedThemeMode` / `persistThemeMode`. |

Тесты: `I18nCatalogTest`, `I18nPreferencesTest`, `ThemeColorsTest`, `ThemePreferencesTest`.
