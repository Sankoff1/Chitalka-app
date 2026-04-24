---
moduli_section: "§3.3"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#33-навигация-типы-маршрутов-drawer-жизненный-цикл-читалки"
tags: [navigation, DrawerScreen, RootStackRoutes, ReaderRouteParams, ReaderRouteLifecycle]
---

# §3.3. Модуль `library-kotlin` — навигация

Оглавление: [MODULI §3.3](../MODULI-I-KOMPONENTY.md#33-навигация-типы-маршрутов-drawer-жизненный-цикл-читалки)

| Часть | Путь | Связи |
|-------|------|--------|
| Маршруты и drawer | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/NavTypes.kt` | `DrawerScreen`, `RootStackRoutes`, `ReaderRouteParams` — используются в `app` ([§2.3](sec-02-3-app-navigatsiya.md)) и в `ReaderNavCoordinator` ([§4.3](sec-04-3-library-android-picker-chitalka.md)). |
| Корневой стек | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/RootStackDestination.kt` | Sealed `Main` / `Reader`; документация соответствия RN в KDoc. |
| Drawer spec | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/DrawerNavigationSpec.kt` | Порядок пунктов, i18n-пути меток → `ChitalkaMainShell`. |
| Lifecycle читалки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/ReaderRouteLifecycle.kt` | Вызывается из `ReaderRouteScreen` ([§2.4](sec-02-4-app-chitalka.md)). |

Тесты: `navigation/*Test.kt` в `src/test/kotlin/com/chitalka/navigation/`.
