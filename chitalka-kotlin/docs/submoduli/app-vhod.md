---
id: app-vhod
tags: [Application, MainActivity, process-entry]
module: app
---

# Модуль `app/` — вход процесса и Activity

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/`

| Файл | Роль |
|------|------|
| `ChitalkaApplication.kt` | Класс `Application`: `installConsoleCapture` (stdout/stderr → буфер отладки, `library-kotlin`). |
| `MainActivity.kt` | Единственная точка входа UI: `setContent { ChitalkaApp(this) }`. |

## Назначение

- Зарегистрировать глобальные **побочные эффекты процесса** до показа UI.
- Поднять **Compose** и передать `ComponentActivity` в корневой composable.

## Связи

- **→** [app-ui-yadro.md](app-ui-yadro.md) — `ChitalkaApp` строит граф навигации и состояние.
- **→** [lib-kotlin-debug.md](lib-kotlin-debug.md) — `InstallConsoleCapture`; зеркало `Log` — [lib-android-debug.md](lib-android-debug.md).
- **→** [app-resursy.md](app-resursy.md) — `AndroidManifest.xml` ссылается на `Application` и `MainActivity`.
- **→** [app-testy.md](app-testy.md) — инструментальные тесты стартуют с Activity.
