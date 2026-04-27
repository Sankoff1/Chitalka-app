---
moduli_section: "§2.2"
module: app
source: "../MODULI-I-KOMPONENTY.md#22-корневой-compose-слой-и-состояние-приложения"
tags: [compose, theme, CompositionLocal, ChitalkaApp, controller]
---

# §2.2. Модуль `app` — корневой Compose-слой и состояние

Оглавление: [MODULI §2.2](../MODULI-I-KOMPONENTY.md#22-корневой-compose-слой-и-состояние-приложения)

## Файлы и связи

| Часть | Путь | Связи |
|-------|------|--------|
| Корневой composable | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaApp.kt` | Создаёт `StorageService`, `SharedPreferencesKeyValueStore`, `LibrarySessionState`, `NavController`, импорт EPUB, тему; оборачивает [§2.3](sec-02-3-app-navigatsiya.md) `ChitalkaNavHost`. Использует `library-android` + `library-kotlin`. |
| Контроллер | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaAppController.kt` | `ReaderNavCoordinator` + `bumpLists`; вызывается из shell и списков. |
| CompositionLocal | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaCompositionLocals.kt` | `LocalChitalkaLocale`, `LocalChitalkaThemeMode`, `LocalChitalkaThemeColors` — типы из `library-kotlin` (`ThemeColors`, `ThemeMode`, локаль). |
| Тема | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/theme/ChitalkaTheme.kt` | `ChitalkaMaterialTheme` + Material 3, палитра из Kotlin-модуля. |

## Связанные пункты MODULI

- Навигация из корня: [§2.3](sec-02-3-app-navigatsiya.md)
- i18n/тема в JVM: [§3.7](sec-03-7-library-kotlin-i18n-tema.md)
