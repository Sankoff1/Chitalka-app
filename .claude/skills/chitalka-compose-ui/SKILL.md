---
name: chitalka-compose-ui
description: >-
  Jetpack Compose, Material 3, navigation, and Web reader shell in chitalka-kotlin
  app module. Use when building or changing UI, NavHost, theme, or WebView reader.
---

# Chitalka — Compose UI и читалка

Код UI: `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/`.

## Точки входа

- `ChitalkaApplication.kt` — `Application`.
- `MainActivity.kt` — точка входа Activity.
- Корневой Compose: `ui/ChitalkaApp.kt`; контроллер состояния: `ChitalkaAppController.kt`.
- Локалы композиции: `ChitalkaCompositionLocals.kt`.
- Тема: `ui/theme/ChitalkaTheme.kt`; строки/цвета также в `app/src/main/res/values/`.

## Навигация

- Настройка графа: `ChitalkaNavigationSetup.kt`.
- Реализация `NavHost`: `ChitalkaNavHost.kt`.
- Оболочка (drawer + контент): `ChitalkaMainShell.kt`; роутинг drawer: `ChitalkaDrawerRouter.kt`.
- Маршруты приложения: `AppNavRoutes.kt`.

Предпочитай существующие типы маршрутов из **library-kotlin** (`com.chitalka.navigation`) вместо дублирования строк маршрутов в `app`.

## Экран читалки (натив + Web)

- Маршрут: `ReaderRouteScreen.kt`, состояние: `ReaderRouteUiModel.kt`.
- Compose-обвязка: `ui/reader/ChitalkaReaderScreen.kt`.
- `WebView` и загрузка: `ChitalkaReaderWebView.kt`.
- Полифиллы под RN/Web-читалку: `ReactNativeWebPolyfill.kt`.

При изменении взаимодействия с веб-частью проверь пары **скриптов/сообщений** в `library-kotlin/.../readerview/ReaderBridge*.kt` (см. скилл домена или `docs/MODULI-I-KOMPONENTY.md` §3.6).

## Стиль реализации

- **Material 3** (`compose.material3`), BOM из каталога версий Gradle.
- Состояние: `ViewModel` + `lifecycle-runtime-compose` / `viewmodel-compose` по паттернам уже используемым в `app`.
- Новые экраны библиотеки: по возможности описывай контракт в **library-kotlin** (`*ScreenSpec`), реализацию в **app**.

## Чего избегать

- Не подключать и не развивать основной UI в **library-compose** (не в составе приложения).
