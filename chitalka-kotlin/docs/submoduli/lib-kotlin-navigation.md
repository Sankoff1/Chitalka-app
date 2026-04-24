---
id: lib-kotlin-navigation
tags: [NavTypes, DrawerScreen, ReaderRouteLifecycle]
module: library-kotlin
package: com.chitalka.navigation
---

# `library-kotlin` — подмодуль `com.chitalka.navigation`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/`

## Назначение

| Файл | Роль |
|------|------|
| `NavTypes.kt` | `DrawerScreen`, `RootStackRoutes`, `ReaderRouteParams` — соответствие RN-типам. |
| `RootStackDestination.kt` | Sealed корневого стека: `Main` vs `Reader`. |
| `DrawerNavigationSpec.kt` | Порядок и i18n-ключи пунктов drawer. |
| `ReaderRouteLifecycle.kt` | События жизненного цикла маршрута читалки (обновление списков через колбэки). |

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-yadro.md](app-ui-yadro.md) | NavHost, drawer, `AppNavRoutes` |
| [lib-android-navigation.md](lib-android-navigation.md) | `ReaderNavCoordinator` использует те же параметры маршрута |

Тесты: см. [lib-kotlin-testy.md](lib-kotlin-testy.md) (`navigation/*Test`).
