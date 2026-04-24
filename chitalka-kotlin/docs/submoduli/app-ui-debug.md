---
id: app-ui-debug
tags: [debug, logs-ui]
module: app
path: app/.../ui/debug/
---

# Модуль `app/` — подмодуль `ui/debug/`

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/debug/ChitalkaDebugLogsPane.kt`

## Назначение

- Отобразить **отладочный лог** в UI: подписка на снимок/поток из `library-kotlin` (`debugLog*` API вокруг `DebugLog`).
- Панель действий: **очистить** буфер, **скопировать** в системный буфер обмена тот же текст, что и при экспорте (`debugLogFormatExport()`), **экспорт в файл** через `FileProvider` и `ACTION_SEND` (строки и MIME — `DebugLogsScreenSpec` + `ru.json` / `en.json`, в т.ч. ключ `debugLogs.copy`).
- При узком экране ряд кнопок прокручивается по горизонтали (`horizontalScroll`).

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-kotlin-debug.md](lib-kotlin-debug.md), [lib-kotlin-screens.md](lib-kotlin-screens.md) (`DebugLogsScreenSpec`), строки `debugLogs.*` в [library-kotlin — i18n JSON](../modules/library-kotlin.md) |
| ← | [app-ui-yadro.md](app-ui-yadro.md) — drawer `DebugLogs` |
