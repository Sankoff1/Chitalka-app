---
id: app-ui-yadro
tags: [compose, navigation, drawer, ChitalkaApp, NavHost]
module: app
path: app/src/main/java/.../app/ui/
---

# Модуль `app/` — подмодуль `ui/` (ядро навигации и оболочка)

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/` (файлы в корне пакета `ui`, без подпапок `theme`, `reader`, … — те вынесены в отдельные документы).

## Назначение файлов

| Файл | Что делает |
|------|------------|
| `ChitalkaApp.kt` | Корневой composable: `StorageService`, персистентность, `LibrarySessionState`, `NavController`, импорт EPUB, тема, обёртка `ChitalkaNavHost` + `ChitalkaMainShell`. |
| `ChitalkaAppController.kt` | Обёртка над `ReaderNavCoordinator`: открыть читалку, сигнал обновить списки. |
| `ChitalkaCompositionLocals.kt` | `CompositionLocal` для локали, режима темы, палитры (типы из `library-kotlin`). |
| `ChitalkaMainShell.kt` | `ModalNavigationDrawer`, выбор раздела drawer, поиск, top bar, диалоги первого запуска. |
| `ChitalkaDrawerRouter.kt` | `when (DrawerScreen)` → панели library / settings / debug. |
| `ChitalkaNavHost.kt` | `NavHost`: `Main` и `Reader/{bookId}/{bookPath}`. |
| `ChitalkaNavigationSetup.kt` | `rememberReaderNavCoordinator`, side-effects для сброса pending при lifecycle. |
| `AppNavRoutes.kt` | Строки маршрутов и `navigateToReader` с кодированием URI. |
| `ReaderRouteScreen.kt` | Экран стека «читалка»: lifecycle из Kotlin-модуля, refresh счётчика книг. |
| `ReaderRouteUiModel.kt` | Параметры маршрута + ссылки на `persistence`, `librarySession`, `storage`. |

## Связи

| Направление | С кем |
|-------------|--------|
| → JVM | [lib-kotlin-navigation.md](lib-kotlin-navigation.md), [lib-kotlin-library.md](lib-kotlin-library.md), [lib-kotlin-i18n.md](lib-kotlin-i18n.md), [lib-kotlin-theme.md](lib-kotlin-theme.md), [lib-kotlin-picker.md](lib-kotlin-picker.md) |
| → Android | [lib-android-storage.md](lib-android-storage.md), [lib-android-picker.md](lib-android-picker.md), [lib-android-navigation.md](lib-android-navigation.md), [lib-android-library.md](lib-android-library.md) (import, refresh), [lib-android-prefs.md](lib-android-prefs.md), [lib-android-debug.md](lib-android-debug.md) |
| → UI подпакеты | [app-ui-theme.md](app-ui-theme.md), [app-ui-reader.md](app-ui-reader.md), [app-ui-library.md](app-ui-library.md), [app-ui-settings.md](app-ui-settings.md), [app-ui-debug.md](app-ui-debug.md) |
| ← Вход | [app-vhod.md](app-vhod.md) |
