---
moduli_section: "§2.3"
module: app
source: "../MODULI-I-KOMPONENTY.md#23-навигация-корневой-граф-drawer-маршрут-читалки"
tags: [navigation-compose, NavHost, drawer, ReaderNavCoordinator, AppNavRoutes]
---

# §2.3. Модуль `app` — навигация

Оглавление: [MODULI §2.3](../MODULI-I-KOMPONENTY.md#23-навигация-корневой-граф-drawer-маршрут-читалки)

## Файлы и связи

| Часть | Путь | Связи |
|-------|------|--------|
| NavHost + координатор | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavigationSetup.kt` | `rememberReaderNavCoordinator`, `ReaderNavCoordinatorSideEffects` → класс из `library-android` ([§4.3](sec-04-3-library-android-picker-chitalka.md)). |
| NavHost | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavHost.kt` | Маршруты `Main` / `Reader`; `ReaderRouteScreen` → [§2.4](sec-02-4-app-chitalka.md). Аргументы согласованы с `RootStackRoutes` ([§3.3](sec-03-3-library-kotlin-navigatsiya.md)). |
| Shell + drawer | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaMainShell.kt` | `DrawerScreen`, `DrawerNavigationSpec` из `library-kotlin`; открытие импорта, поиск, top bar. |
| Роутер drawer | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaDrawerRouter.kt` | Переключает панели: списки, корзина, настройки, логи ([§2.4](sec-02-4-app-chitalka.md) не включает — только библиотека/настройки). |
| Маршруты | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/AppNavRoutes.kt` | `READER` pattern + `navigateToReader`; использует `RootStackRoutes` из [§3.3](sec-03-3-library-kotlin-navigatsiya.md). |

## Поток открытия читалки

`ChitalkaAppController.openReader` → `ReaderNavCoordinator.navigateToReader` → при готовности графа → `AppNavRoutes.navigateToReader(nav, bookId, bookPath)`.
