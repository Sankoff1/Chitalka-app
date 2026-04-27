---
id: app-ui-theme
tags: [compose, Material3, theme]
module: app
path: app/.../ui/theme/
---

# Модуль `app/` — подмодуль `ui/theme/`

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/theme/ChitalkaTheme.kt`

## Назначение

- Собрать **Material 3** тему приложения (`ChitalkaMaterialTheme`).
- Связать режим светлая/тёмная и **палитру** с типами из `library-kotlin` (`ThemeColors`, `ThemeMode`).

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-kotlin-theme.md](lib-kotlin-theme.md) — определения цветов и режима |
| ← | [app-ui-yadro.md](app-ui-yadro.md) — `ChitalkaApp` оборачивает контент в тему |
| ← | Все composable в `app` читают палитру через [CompositionLocal в app-ui-yadro](app-ui-yadro.md) |
