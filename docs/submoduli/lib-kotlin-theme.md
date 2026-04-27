---
id: lib-kotlin-theme
tags: [ThemeMode, ThemeColors, Material]
module: library-kotlin
package: com.chitalka.theme
---

# `library-kotlin` — подмодуль `com.chitalka.theme`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/theme/`

| Файл | Роль |
|------|------|
| `ThemeColors.kt` | Палитры для светлой/тёмной темы (данные для Compose и WebView). |
| `ThemePreferences.kt` | `ThemeUiState`, чтение/запись режима темы (персистентность через Android prefs из `app`). |

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-theme.md](app-ui-theme.md) | Material 3 оболочка |
| [app-ui-reader.md](app-ui-reader.md) | Тёмная страница читалки совместно с [lib-kotlin-ui.md](lib-kotlin-ui.md) |
| [app-ui-yadro.md](app-ui-yadro.md) | `CompositionLocal` для цветов |

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`theme/*Test`).
